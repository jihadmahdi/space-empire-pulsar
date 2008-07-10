/**
 * @author Escallier Pierre
 * @file SEPServerClientSessionListenerSpec.java
 * @date 10 juil. 2008
 */
package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.Socket;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import net.orfjackal.darkstar.integration.DarkstarServer;
import net.orfjackal.darkstar.integration.util.StreamWaiter;
import net.orfjackal.darkstar.integration.util.TempDirectory;

import org.junit.runner.RunWith;

import client.pretests.SEPConsoleClient;

import com.sun.sgs.app.AppListener;

/**
 * 
 */
@RunWith(JDaveRunner.class)
public class SEPServerClientSessionListenerSpec extends Specification<Object>
{
	// private static final byte[] KERNEL_READY_MSG = "Kernel is ready".getBytes();
	private static final byte[]				APPLICATION_READY_MSG		= "application is ready".getBytes();

	private static final byte[]				APPLESS_CONTEXT_READY_MSG	= "non-application context is ready".getBytes();

	private static final int				TIMEOUT						= 10000;

	private TempDirectory					tempDirectory;

	private Properties						props;

	private String							appName;

	private int								port;

	private Class<? extends AppListener>	appListener;

	private String							appRoot;

	public void create()
	{
		tempDirectory = new TempDirectory();
		tempDirectory.create();

		props = new Properties();
		try
		{
			props.load(new FileInputStream("sep-server.properties"));

			appName = props.getProperty("com.sun.sgs.app.name");
			appListener = (Class<? extends AppListener>) Class.forName(props.getProperty("com.sun.sgs.app.listener"));
			port = Integer.valueOf(props.getProperty("com.sun.sgs.app.port"));
			appRoot = props.getProperty("com.sun.sgs.app.root");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void destroy()
	{
		tempDirectory.dispose();
	}

	public class WhenAClientTryToConnect
	{
		private DarkstarServer		server;

		private StreamWaiter		waiter;

		private SEPConsoleClient	consoleClient;

		private Thread				consoleClientThread;

		private PipedWriter			write;

		private PipedReader			read;

		private File				consoleClientOut;

		public Object create() throws InterruptedException
		{
			server = new DarkstarServer(tempDirectory.getDirectory());

			server.setAppName(appName);
			server.setAppListener(appListener);
			server.setPort(port);

			Iterator<Object> it = props.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next().toString();
				server.setProperty(key, props.getProperty(key));
			}
			server.setProperty("my.custom.key", "MyValue");

			server.start();
			waiter = new StreamWaiter(server.getSystemErr());

			consoleClient = new SEPConsoleClient();
			consoleClientThread = new Thread(consoleClient);

			write = new PipedWriter();
			PrintStream display = null;
			try
			{
				read = new PipedReader(write);
				consoleClientOut = new File(tempDirectory.getDirectory(), "clientConsoleOut");
				if ( !consoleClientOut.exists()) consoleClientOut.createNewFile();
				display = new PrintStream(consoleClientOut);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("Cannot hook the keyboard.");
			}

			BufferedReader keyboardHook = new BufferedReader(read);
			SEPConsoleClient.setKeyboard(keyboardHook);
			SEPConsoleClient.setDisplay(display);

			specify(server.getAppName(), should.equal(appName));
			specify(server.getAppListener(), should.equal(appListener));
			specify(server.getPort(), should.equal(port));
			specify(server.getProperty("my.custom.key"), should.equal("MyValue"));
			return null;
		}

		public void destroy()
		{
			waiter.dispose();
			server.shutdown();

			try
			{
				write.close();
				read.close();
			}
			catch (IOException e)
			{
			}

			consoleClientThread.interrupt();
		}

		public void itIsRunning()
		{
			specify(server.isRunning());
		}

		public void itCanBeShutDown()
		{
			server.shutdown();
			specify( !server.isRunning());
		}

		public void itPrintsSomeLogMessages() throws InterruptedException, TimeoutException
		{
			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

			String out = server.getSystemOut().toString();
			String err = server.getSystemErr().toString();
			specify(err, err.contains(appName + ": application is ready"));
			specify(out, out.contains("SEPServer initializing."));
		}

		public void itListensToTheSpecifiedPort() throws IOException
		{
			long firstTime = System.currentTimeMillis();
			Socket clientSocket = null;

			do
			{
				try
				{
					clientSocket = new Socket("localhost", port);
				}
				catch (Exception e)
				{
				}
			} while ((System.currentTimeMillis() - firstTime) < 5000);

			specify(clientSocket, isNotNull());
			specify(clientSocket.isConnected());
			clientSocket.close();
		}

		public void clientIsConnected() throws InterruptedException, TimeoutException, IOException
		{
			long firstTime = System.currentTimeMillis();
			Socket clientSocket = null;

			do
			{
				try
				{
					Thread.sleep(200);
					clientSocket = new Socket("localhost", port);
				}
				catch (Exception e)
				{
				}
			} while ((System.currentTimeMillis() - firstTime) < 5000);

			specify(clientSocket, isNotNull());
			specify(clientSocket.isConnected());
			clientSocket.close();

			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

			consoleClientThread.start();

			BufferedWriter bw = new BufferedWriter(write);
			bw.write("1");
			bw.newLine();
			bw.newLine();
			bw.newLine();
			bw.newLine();
			bw.newLine();
			bw.flush();

			Thread.sleep(5000);
			
			String out = server.getSystemOut().toString();
			String err = server.getSystemErr().toString();

			writeToFile(new File("/tmp/SEPServer.out"), out);
			writeToFile(new File("/tmp/SEPServer.err"), err);

			specify(err, err.contains(appName + ": application is ready"));
			specify(out, out.contains("SEPServer initializing."));
			specify(err, err.contains("loggedIn"));
			specify(err, err.contains("Identity: guest is not admin"));
			specify(consoleClient.getStatus().contains("Connected"));
			
			//TODO: mettre en place des traces propres dans tous les composants, avec facilités de redirection entrées/sorties pour s'en servir dans les tests.
		}
	}

	private static void writeToFile(File file, String string)
	{
		try
		{
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(string);
			fw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
