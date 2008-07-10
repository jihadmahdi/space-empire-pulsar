/**
 * @author Escallier Pierre
 * @file SEPServerSpec.java
 * @date 10 juil. 2008
 */
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import net.orfjackal.darkstar.integration.DarkstarServer;
import net.orfjackal.darkstar.integration.DarkstarServerSpec.HelloWorld;
import net.orfjackal.darkstar.integration.util.StreamWaiter;
import net.orfjackal.darkstar.integration.util.TempDirectory;

import org.junit.runner.RunWith;

import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;

/**
 * 
 */
@RunWith(JDaveRunner.class)
public class SEPServerSpec extends Specification<Object>
{

	// private static final byte[] KERNEL_READY_MSG = "Kernel is ready".getBytes();
	private static final byte[]	APPLICATION_READY_MSG		= "application is ready".getBytes();

	private static final byte[]	APPLESS_CONTEXT_READY_MSG	= "non-application context is ready".getBytes();

	private static final int	TIMEOUT						= 10000;

	private TempDirectory		tempDirectory;
	
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

	public class WhenTheServerHasNotBeenStarted
	{

		private DarkstarServer	server;

		public Object create()
		{
			server = new DarkstarServer(tempDirectory.getDirectory());
			return null;
		}

		public void destroy()
		{
		}

		public void itIsNotRunning()
		{
			specify( !server.isRunning());
		}

		public void itCanNotBeShutDown()
		{
			specify(new Block()
			{
				public void run() throws Throwable
				{
					server.shutdown();
				}
			}, should.raise(IllegalStateException.class));
		}

		public void itIsNotListeningToTheSpecifiedPort() throws IOException
		{
			specify(new Block()
			{
				public void run() throws Throwable
				{
					new Socket("localhost", server.getPort());
				}
			}, should.raise(ConnectException.class));
		}

		public void appNameMustBeSetBeforeStarting()
		{
			specify(new Block()
			{
				public void run() throws Throwable
				{
					server.start();
				}
			}, should.raise(IllegalArgumentException.class, "appName is not set"));
		}
	}

	public class WhenAnEmptyServerIsStarted
	{

		private DarkstarServer	server;

		private StreamWaiter	waiter;

		public Object create()
		{
			server = new DarkstarServer(tempDirectory.getDirectory());
			server.setAppName("NoApp");
			server.start();
			waiter = new StreamWaiter(server.getSystemErr());
			return null;
		}

		public void destroy()
		{
			waiter.dispose();
			server.shutdown();
		}

		public void theServerStartsWithoutAppListener() throws InterruptedException, TimeoutException
		{
			specify(server.getAppListener(), should.equal(null));
			waiter.waitForBytes(APPLESS_CONTEXT_READY_MSG, TIMEOUT);
			String err = server.getSystemErr().toString();
			specify(err, err.contains("NoApp"));
		}
	}

	public class WhenTheServerIsStartedWithAnApplication
	{

		private DarkstarServer					server;

		private StreamWaiter					waiter;

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
			} while ((System.currentTimeMillis() - firstTime) < 2000);

			specify(clientSocket, isNotNull());
			specify(clientSocket.isConnected());
			clientSocket.close();
		}

		public void customPropertyKeysCanBeSet() throws IOException
		{
			File configFile = new File(tempDirectory.getDirectory(), appName+".properties");
			Properties appProps = new Properties();
			FileInputStream in = new FileInputStream(configFile);
			appProps.load(in);
			in.close();
			specify(appProps.getProperty("my.custom.key"), should.equal("MyValue"));
		}

		public void allFilesAreWrittenInTheWorkingDirectory() throws InterruptedException, TimeoutException
		{
			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

			File dir = tempDirectory.getDirectory();
			File appProps = new File(dir, appName+".properties");
			File dataDir = new File(dir, "data" + File.separator + appName + File.separator + "dsdb");
			specify(appProps.isFile());
			specify(dataDir.isDirectory());

			final long MB = 1024 * 1024;
			long totalSize = 0;
			for (File file : dataDir.listFiles())
			{
				totalSize += file.length();
			}
			specify((Object) totalSize, totalSize > 10 * MB);
		}

		public void itCanNotBeStartedWithoutFirstShuttingItDown()
		{
			specify(new Block()
			{
				public void run() throws Throwable
				{
					server.start();
				}
			}, should.raise(IllegalStateException.class));
			specify(new Block()
			{
				public void run() throws Throwable
				{
					server.start(new File(tempDirectory.getDirectory(), "HelloWorld.properties"));
				}
			}, should.raise(IllegalStateException.class));
		}

		public void itCanBeRestarted() throws InterruptedException, TimeoutException
		{
			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
			server.shutdown();
			specify( !server.isRunning());
			String log1 = server.getSystemErr().toString();
			specify(log1, log1.contains("The Kernel is ready"));
			specify(log1, log1.contains(appName+": application is ready"));
			specify(log1, !log1.contains("recovering for node"));

			server.start();
			waiter.setStream(server.getSystemErr());
			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
			specify(server.isRunning());
			String log2 = server.getSystemErr().toString();
			specify(log2, log2.contains("The Kernel is ready"));
			specify(log2, log2.contains(appName+": application is ready"));
			specify(log2, log2.contains("recovering for node"));
		}
	}

	public class WhenAnExistingAppPropertiesFileIsUsed
	{

		private DarkstarServer	server;

		private StreamWaiter	waiter;

		private File			dsdb;

		public Object create() throws InterruptedException, IOException
		{
			File appRoot = new File(tempDirectory.getDirectory(), SEPServerSpec.this.appRoot);
			appRoot.mkdir();
			dsdb = new File(appRoot, "dsdb");
			dsdb.mkdir();

			Properties p = new Properties();
			p.setProperty(DarkstarServer.APP_NAME, appName);
			p.setProperty(DarkstarServer.APP_LISTENER, appListener.getCanonicalName());
			p.setProperty(DarkstarServer.APP_PORT, String.valueOf(port));
			p.setProperty(DarkstarServer.APP_ROOT, appRoot.getAbsolutePath());
			File configFile = new File(appRoot, appName+".properties");
			FileOutputStream out = new FileOutputStream(configFile);
			p.store(out, null);
			out.close();

			server = new DarkstarServer(tempDirectory.getDirectory());
			server.start(configFile);
			waiter = new StreamWaiter(server.getSystemErr());
			return null;
		}

		public void destroy()
		{
			waiter.dispose();
			server.shutdown();
		}

		public void theServerIsStartedUnderTheSpecifiedAppRoot() throws TimeoutException
		{
			waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
			String err = server.getSystemErr().toString();
			specify(err, err.contains(appName));
			specify(dsdb.listFiles().length > 5);
		}
	}
}
