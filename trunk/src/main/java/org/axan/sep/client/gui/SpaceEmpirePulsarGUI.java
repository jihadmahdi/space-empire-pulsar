package org.axan.sep.client.gui;

import java.awt.Container;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.axan.eplib.utils.Reflect;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.SEPClient.IUserInterface;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;
import org.axan.sep.server.SEPServer;
import org.javabuilders.BuildResult;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.event.CancelStatus;
import org.javabuilders.swing.SwingJavaBuilder;

public class SpaceEmpirePulsarGUI extends JFrame implements IUserInterface
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	public static Logger getLogger()
	{
		return log;
	}
	
	public static final URL fallbackPortrait = Reflect.getResource(SpaceEmpirePulsarGUI.class.getPackage().getName()+".img", "portrait_todo.png");
	public static final URL fallbackSymbol = Reflect.getResource(SpaceEmpirePulsarGUI.class.getPackage().getName()+".img", "symbol_todo.png");

	private static SpaceEmpirePulsarGUI singleton;
	
	////////// static methods
	
	public static SpaceEmpirePulsarGUI getInstance()
	{
		return singleton;
	}
	
	private static void setInstance(SpaceEmpirePulsarGUI singleton)
	{
		if (SpaceEmpirePulsarGUI.singleton != null) throw new RuntimeException("Cannot instantiate more than one SpaceEmpirePulsarGUI in a single JVM.");
		SpaceEmpirePulsarGUI.singleton = singleton;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{				
				SwingJavaBuilder.getConfig().addResourceBundle(SpaceEmpirePulsarGUI.class.getName());
				try
				{
					SpaceEmpirePulsarGUI app = new SpaceEmpirePulsarGUI();					
					app.setVisible(true);					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	////////// static classes
	public static class HostGamePanel extends JPanel implements IModalComponent
	{
		////////// private attributes
		private final BuildResult result;
		private boolean canceled;
		
		////////// ui controls
		private JSpinner spnPort;
		private JSpinner spnTimeout;
		
		////////// bean fields
		private String login;
		private int port;
		private int timeout;

		////////// no arguments constructor
		public HostGamePanel()
		{
			result = SwingJavaBuilder.build(this);
			spnPort.setModel(new SpinnerNumberModel(8082, 1, 65000, 1));
			spnTimeout.setModel(new SpinnerNumberModel(5, 1, 10, 1));
		}
		
		////////// IModalComponent implementation		
		
		@Override
		public boolean validateForm()
		{
			return result.validate();
		}
		
		@Override
		public boolean isCanceled()
		{
			return canceled;
		}
		
		////////// bean getters/setters
				
		public String getLogin()
		{
			return login;
		}
		
		public void setLogin(String login)
		{
			String old = this.login;
			this.login = login;
			firePropertyChange("login", old, login);
		}
		
		public int getPort()
		{
			return port;
		}
		
		public void setPort(int port)
		{
			int old = this.port;
			this.port = port;
			firePropertyChange("port", old, port);
		}
		
		public int getTimeout()
		{
			return timeout;
		}
		
		public void setTimeout(int timeout)
		{
			int old = this.timeout;
			this.timeout = timeout;
			firePropertyChange("timeout", old, timeout);
		}
		
		////////// ui events
		
		/** Ok button click */
		protected void ok()
		{
			canceled = false;
			setVisible(false);
		}
		
		/** Cancel button click */
		protected void cancel()
		{
			canceled = true;
			setVisible(false);
		}				
	}
	
	public static class JoinGamePanel extends HostGamePanel
	{
		////////// bean fields
		private String host;

		////////// no args constructor
		public JoinGamePanel() {}

		////////// bean getters/setters
		public String getHost()
		{
			return host;
		}
		
		public void setHost(String host)
		{
			String old = this.host;
			this.host = host;
			firePropertyChange("host", old, host);
		}
	}
	
	////////// private attributes	
	private final BuildResult build;
	private boolean isAdmin;
	private Set<BackgroundEvent> mainPanelBackgroundTasks = new HashSet<BackgroundEvent>();
		
	////////// ui controls
	private HostGamePanel hostGamePanel;
	private JoinGamePanel joinGamePanel;
	private GameCreationPanel gameCreationPanel;
	private RunningGamePanel runningGamePanel;
	
	////////// bean fields
	private SEPServer sepServer;
	private SEPClient sepClient;
	
	////////// no arguments constructor
	public SpaceEmpirePulsarGUI()
	{
		setInstance(this);
		
		SwingJavaBuilderMyUtils.addType(JImagePanel.class, HostGamePanel.class, JoinGamePanel.class, SpinnerNumberModel.class, RunningGamePanel.class);
		
		build = SwingJavaBuilder.build(this);		
		refresh();
	}
	
	////////// bean getters/setters
	
	public SEPClient getSepClient()
	{
		return sepClient;
	}
	
	public void setSepClient(SEPClient sepClient)
	{
		SEPClient old = this.sepClient;
		this.sepClient = sepClient;
		firePropertyChange("sepClient", old, sepClient);
	}
	
	public SEPServer getSepServer()
	{
		return sepServer;
	}
	
	public void setSepServer(SEPServer sepServer)
	{
		this.sepServer = sepServer;
	}
	
	@Override
	public boolean isAdmin()
	{
		return isAdmin;
	}
	
	////////// ui events
	
	/**
	 * Ask for confirmation, close and exit.
	 */
	private void close()
	{		
		quitGame();
		dispose();
	}
	
	/**
	 * Display host new game panel.
	 */
	@DoInBackground(blocking=false, cancelable=true, indeterminateProgress=true)
	private void showHostGamePanel(BackgroundEvent evt)
	{
		if (showBlokingPanel(hostGamePanel, evt)) return;
		if (hostGamePanel.isCanceled())
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			showLogoPanel();
			return;
		}
		
		hostNewGame(hostGamePanel.login, hostGamePanel.port, hostGamePanel.timeout*1000);	
	}
	
	/**
	 * Display join game panel.
	 */
	@DoInBackground(blocking=false, cancelable=true, indeterminateProgress=true)
	private void showJoinGamePanel(BackgroundEvent evt)
	{
		if (showBlokingPanel(joinGamePanel, evt)) return;
		if (joinGamePanel.isCanceled())
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			showLogoPanel();
			return;
		}
		
		joinNewGame(joinGamePanel.getLogin(), joinGamePanel.getHost(), joinGamePanel.getPort(), joinGamePanel.getTimeout()*1000);
	}
	
	/**
	 * Quit current game if any (not the program).
	 */
	private void quitGame()
	{
		if (getSepClient() != null && getSepClient().isConnected())
		{
			if (JOptionPane.showConfirmDialog(null, build.getResource("msg.quit.game"), build.getResource("msg.quit.game.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			{
				return;
			}
			
			getSepClient().disconnect();
			setSepClient(null);			
		}
		
		if (getSepServer() != null)
		{
			getSepServer().terminate();
			setSepServer(null);
		}
		
		showLogoPanel();
	}
	
	private void showLogoPanel()
	{
		setMainPanelBackgroundTask(null);
		setContentPane((JPanel) build.get("logoPanel"));
		refresh();
	}
	
	@Override
	public void displayGameCreationPanel()
	{
		log.log(Level.INFO, "displayGameCreationPanel");
		SwingJavaBuilderMyUtils.callBackgroundMethod(build, "showGameCreationPanel", this);
	}
	
	@Override
	public void onGamePaused()
	{
		log.log(Level.INFO, "onGamePaused");
	}

	@Override
	public void onGameRan()
	{
		log.log(Level.INFO, "onGameRan");
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				if (getSepClient().getGameBoard() == null)
				{
					try
					{
						getSepClient().setGameBoard(getSepClient().getRunningGameInterface().getPlayerGameBoard());
					}
					catch(Exception e)
					{
						log.log(Level.SEVERE, "Cannot retreive initial player gameboard", e);
					}
				}
				
				gameCreationPanel.setVisible(false);
				SwingJavaBuilderMyUtils.callBackgroundMethod(build, "showRunningGamePanel", SpaceEmpirePulsarGUI.this);
			}
		}).start();
	}

	@Override
	public void onGameResumed()
	{
		log.log(Level.INFO, "onGameResumed");
	}

	@Override
	public void refreshPlayerList(Map<Player, PlayerConfig> playerList)
	{
		log.log(Level.INFO, "refreshPlayerList");
		gameCreationPanel.getPlayersListPanel().refreshPlayers(playerList.keySet());
	}
	
	@Override
	public void receiveGameCreationMessage(Player fromPlayer, String msg)
	{
		log.log(Level.INFO, "receiveGameCreationMessage");
		gameCreationPanel.getChatPanel().receivedMessages(fromPlayer, msg);
	}

	@Override
	public void refreshGameConfig(GameConfig gameCfg)
	{
		log.log(Level.INFO, "refreshGameConfig");
	}

	@Override
	public void receiveRunningGameMessage(Player fromPlayer, String msg)
	{
		log.log(Level.INFO, "receiveRunningGameMessage");
	}

	@Override
	public void receiveNewTurnGameBoard(PlayerGameBoard gameBoard)
	{
		log.log(Level.INFO, "receiveNewTurnGameBoard");
	}

	@Override
	public void receivePausedGameMessage(Player fromPlayer, String msg)
	{
		log.log(Level.INFO, "receivePausedGameMessage");
	}
	
	////////// private methods
	
	public boolean isGameRunning()
	{
		return sepClient != null;
	}
	
	private void refresh()
	{
		validate();
		doLayout();
		invalidate();
		repaint();		
	}
	
	/**
	 * Cancel all background events and note the new one.
	 * @param evt
	 */
	private void setMainPanelBackgroundTask(BackgroundEvent evt)
	{
		for(BackgroundEvent e : mainPanelBackgroundTasks)
		{
			e.setCancelStatus(CancelStatus.REQUESTED);
		}
		
		mainPanelBackgroundTasks.clear();
		if (evt != null) mainPanelBackgroundTasks.add(evt);
	}
	
	/**
	 * Display given panel as blocking, out of EDT.
	 * @return true if task is canceled, it that case calling method just has to return asap.
	 */
	private boolean showBlokingPanel(final Container contentPane, BackgroundEvent evt)
	{
		setMainPanelBackgroundTask(evt);
		setContentPane(contentPane);
		refresh();
		contentPane.setVisible(true);
		
		while(contentPane.isVisible() && evt.getCancelStatus() != CancelStatus.REQUESTED)
		{
			try
			{
				Thread.sleep(200);
			}
			catch(InterruptedException ie) {}
		}
		
		if (evt.getCancelStatus() == CancelStatus.REQUESTED)
		{
			evt.setCancelStatus(CancelStatus.PROCESSING);
			contentPane.setVisible(false);
			evt.setCancelStatus(CancelStatus.COMPLETED);
			return true;
		}	
		
		return false;
	}	
	
	/**
	 * Run new game server.
	 */
	private void hostNewGame(String login, int port, int timeout)
	{
		sepServer = new SEPServer(port, timeout);
		sepServer.start();
		
		try
		{
			Thread.sleep(3000);
		}
		catch(InterruptedException ie) {}
		
		isAdmin = true;
		setSepClient(new SEPClient(this, login, sepServer.getServerAdminKey(), sepServer.getAddress().getHostAddress(), port, timeout));
		getSepClient().connect();		
	}	
	
	private void joinNewGame(String login, String host, int port, int timeout)
	{
		isAdmin = false;
		setSepClient(new SEPClient(this, login, host, port, timeout));
		getSepClient().connect();
	}
	
	@DoInBackground(blocking=false, cancelable=true, indeterminateProgress=true)
	public void showGameCreationPanel(BackgroundEvent evt)
	{		
		if (showBlokingPanel(gameCreationPanel, evt)) return;
		
		if (gameCreationPanel.isCanceled())
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			showLogoPanel();
			return;
		}
	}
	
	@DoInBackground(blocking=false, cancelable=true, indeterminateProgress=true)
	public void showRunningGamePanel(BackgroundEvent evt)
	{
		if (showBlokingPanel(runningGamePanel, evt)) return;
		
		if (runningGamePanel.isCanceled())
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			showLogoPanel();
			return;
		}
	}

}
