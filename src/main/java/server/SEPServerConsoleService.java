/**
 * @author Escallier Pierre
 * @file SEPServerConsoleService.java
 * @date 26 juin 08
 */
package server;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.SEPUtils.SerializableTask;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.kernel.TaskScheduler;
import com.sun.sgs.kernel.TransactionScheduler;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;

/**
 * 
 */
public class SEPServerConsoleService implements Service
{
	/** the logger. */
	private final static Logger			logger				= Logger.getLogger(SEPServerConsoleService.class.getName());

	// a proxy providing access to the transaction state
	static TransactionProxy				transactionProxy	= null;

	static ComponentRegistry			componentRegistry	= null;

	static TaskScheduler				taskScheduler		= null;
	
	static TransactionScheduler			transactionScheduler= null;

	// the data service used in the same context
	static DataService					dataService			= null;

	static Identity						owner				= null;

	// An SGS-instance that we need to spin off our own thread.
	private boolean						worker_still_running;

	private Thread						worker_thread;

	private boolean						enabled				= true;

	private eServerCmd					lastCmd				= null;

	private static final Scanner		in					= new Scanner(System.in);

	private static final PrintStream	out					= System.out;

	private static enum eServerCmd
	{
		help, listBounds, nettoyerPartieEnCreationVides, test
	}

	/**
	 * The constructor as it is called from SGS.
	 * 
	 * @param properties
	 * @param componentRegistry
	 * @param transProxy
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public SEPServerConsoleService(Properties properties, ComponentRegistry componentRegistry, TransactionProxy transProxy) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		System.out.println("SEPServerConsoleService CTOR");

		// just for searching.
		for (Entry<Object, Object> e : properties.entrySet())
		{
			System.out.println("Prop: " + e.getKey() + ":" + e.getValue());
		}

		// Get the ResourceCoordinator
		transactionScheduler = componentRegistry.getComponent(TransactionScheduler.class);
		taskScheduler = componentRegistry.getComponent(TaskScheduler.class);
		transactionProxy = transProxy;
		dataService = transProxy.getService(DataService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.service.Service#getName()
	 */
	@Override
	public String getName()
	{
		System.out.println("SEPServerConsoleService.getName()");
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.service.Service#ready()
	 */
	@Override
	public void ready() throws Exception
	{
		// ok, we are on
		owner = transactionProxy.getCurrentOwner();
		System.out.println("SEPServerConsoleService.ready()");
		run();
	}

	/*
	 * public static class Test extends AbstractKernelRunnable implements Serializable {
	 * 
	 * private static final long serialVersionUID = 1L;
	 * 
	 * @Override public void run() throws Exception { commandProcessingTaskFactory(eServerCmd.test).run(); } }
	 */

	private void run()
	{
		if ( !worker_still_running)
		{
			logger.log(Level.INFO, "Run ConsoleService Thread");

			worker_thread = new Thread()
			{

				@Override
				public void run()
				{
					String cmd = "";

					out.println("SEPServer Console command line is ready");
					while (enabled)
					{
						out.print("> ");

						try
						{
							cmd = in.nextLine();
						}
						catch (NoSuchElementException e)
						{
							continue;
						}

						synchronized (worker_thread)
						{
							for(eServerCmd cmdPossible : eServerCmd.values())
							{
								if (cmd.compareToIgnoreCase(cmdPossible.toString()) == 0)
								{
									lastCmd = cmdPossible;
									
									transactionScheduler.scheduleTask(new KernelRunnable()
									{
									
										@Override
										public void run() throws Exception
										{
											logger.log(Level.INFO, "transactionSchedeled !");
											commandProcessingTaskFactory(lastCmd).run();
										}
									
										@Override
										public String getBaseTaskType()
										{
											return Task.class.getName();
										}
									}, owner);
									
									break;
								}
							}
							
							if (lastCmd == null)
							{
								out.println("CL> Commande inconnue, tapez \"help\" pour afficher la liste des commandes.");
							}
						}
					}
					out.println("SEPServer Console command line is down");
					worker_still_running = false;
				}
			};
			worker_still_running = true;
			worker_thread.start();
		}
	}
	
	private static interface SerializableKernelRunnable extends KernelRunnable, Serializable {};
	
	private static SerializableKernelRunnable commandProcessingTaskFactory(final eServerCmd cmd)
	{
		SerializableKernelRunnable result = new SerializableKernelRunnable()
		{
			@Override
			public void run() throws Exception
			{
				switch (cmd)
				{
				case help:
				{
					help();
					break;
				}
				case listBounds:
				{
					listBounds();
					break;
				}
				case nettoyerPartieEnCreationVides:
				{
					SEPServer.getServer().nettoyerPartieEnCreationVides();
					break;
				}
				case test:
				{
					test();
					break;
				}
				default:
				{
					out.println("Not yet implemented or unknown command \"" + cmd.toString() + "\"");
				}
				}
				
			}

			@Override
			public String getBaseTaskType()
			{
				return Task.class.getName();
			}

		};

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.service.Service#shutdown()
	 */
	@Override
	public boolean shutdown()
	{
		System.out.println("SEPServerConsoleService.shutdown()");
		if (enabled)
		{
			enabled = false;
			synchronized (this)
			{
				this.notify(); // Wake up our worker (TODO: block here untill he is gone)
				while (worker_still_running)
				{
					logger.info("Waiting for SEPServerConsole thread to complete");
					try
					{
						this.wait(25);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return true; // ok, we are down
	}

	static void help()
	{
		out.println("Liste des commandes :");
		out.println("Help\tAffiche cette aide.");
		out.println("ListBounds\tAffiche les noms liés dans le DataManager.");
		out.println("NettoyerPartieEnCreationVides\tNéttoie les parties en créations vides.");
	}

	static void listBounds()
	{
		DataManager dm = AppContext.getDataManager();

		String boundName = dm.nextBoundName(null);
		StringBuilder sb = new StringBuilder();
		while (boundName != null)
		{
			sb.append(boundName);
			boundName = dm.nextBoundName(boundName);
			if (boundName != null)
			{
				sb.append(", ");
			}
		}

		out.println("SEPServer bounds list: " + sb.toString());
	}
	
	static void test()
	{
		ChannelManager cm = AppContext.getChannelManager();
		out.println("CL> Test OK");
	}
}
