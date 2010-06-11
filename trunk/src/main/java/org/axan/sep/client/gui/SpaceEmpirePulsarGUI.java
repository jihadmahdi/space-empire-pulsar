package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.AsteroidField;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.ICelestialBody;
import org.axan.sep.common.Nebula;
import org.axan.sep.common.Planet;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.server.SEPServer;




/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class SpaceEmpirePulsarGUI extends javax.swing.JFrame implements SEPClient.IUserInterface
{
	static Logger				log			= SEPClient.log;

	public static final String	IMG_PATH	= ("resources/"+SpaceEmpirePulsarGUI.class.getPackage().getName().replace('.', '/')+"/img/").replace('/', File.separatorChar);

	private boolean				isAdmin		= false;

	private JMenuBar			jMenuBar;

	private JMenu				jFileMenu;

	private JTextField			jHostGameTimeoutTextField;

	private JTextField			jHostGamePortTextField;

	private JLabel				gameCreationConfigLabel1;

	private JLabel				gameCreationConfigEditionNebulaLabel;

	private JTextField			gameCreationConfigEditionNebulaCarbonMinTextLabel;

	private JLabel				gameCreationConfigEditionNebulaSlashesLabel;

	private JTextField			gameCreationConfigEditionNebulaCarbonMaxTextField;

	private JTextField			gameCreationConfigEditionNebulaSlotsMinTextField;

	private JCheckBox			gameCreationConfigEditionPanelVictoryTeamCheckBox;

	private JLabel				gameCreationConfigEditionPanelVictoryRulesLabel;

	private JTextField			gameCreationConfigEditionNebulaSlotsMaxTextField;

	private JLabel				gameCreationConfigEditionAsteroidFieldLabel;

	private JTextField			gameCreationConfigEditionAsteroidFieldSlotsMinTextField;

	private JTextField			gameCreationConfigEditionAsteroidFieldSlotsMaxTextField;

	private JTextField			gameCreationConfigEditionAsteroidFieldCarbonMaxTextField;

	private JTextField			gameCreationConfigEditionAsteroidFieldCarbonMinTextField;

	private JLabel				gameCreationConfigEditionSlashLabel2;

	private JLabel				gameCreationConfigEditionSlashLabel;

	private JTextField			gameCreationConfigEditionPlanetCarbonMinTextField;

	private JTextField			gameCreationConfigEditionPlanetCarbonMaxTextField;

	private JTextField			gameCreationConfigEditionPlanetSlotsMaxTextField;

	private JTextField			gameCreationConfigEditionPlanetSlotsMinTextField;

	private JLabel				gameCreationConfigEditionPlanetLabel;

	private JLabel				gameCreationConfigEditionSlotMinLabel;

	private JLabel				gameCreationConfigEditionCarbonMinLabel;

	private JLabel				gameCreationConfigEditionCelestialBodyTypeLabel;

	private JTextField			gameCreationConfigEditionNeutralCelestialBodiesTextField;

	private JLabel				gameCreationConfigEditionNbNeutralCelestialBodiesLabel;

	private JTextField			gameCreationConfigEditionUniverseZSizeTextField;

	private JTextField			gameCreationConfigEditionUniverseYSizeTextField;

	private JTextField			gameCreationConfigEditionUniverseXSizeTextField;

	private JLabel				gameCreationConfigUniverseSizeLabel;

	private JPanel				gameCreationConfigEditionPanel;

	private JLabel				gameCreationPlayerConfigEditionColoredNameLabel;

	private JPanel				gameCreationPlayerConfigEditionPanel;

	private JTextField			gameCreationChatMessageTextField;

	private JEditorPane			gameCreationChatEditorPane;

	private JLabel				gameCreationChatLabel;

	private JPanel				gameCreationChatPanel;

	private JPanel				gameCreationWestPanel;

	private JLabel				gameCreationPlayerConfigLabel;

	private JPanel				gameCreationPlayerConfigPanel;

	private JLabel				gameCreationPlayerListLabel;

	private JPanel				gameCreationPlayerListPanel;

	private JPanel				gameCreationBtnsPanel;

	private JPanel				gameCreationWestSouthPanel;

	private JPanel				gameCreationEastPanel;

	private JLabel				jHostGameTimeoutLabel;

	private JLabel				jHostGamePortLabel;

	private JButton				jHostGameOKButton;

	private JImagePanel			jMainPanel;

	private JMenuItem			jQuitMenuItem;

	private JSeparator			jFileMenuSeparator;

	private JPanel				gameCreationPanel;

	private JTextField			jNameTextField;

	private JLabel				jLoginLabel;

	private JButton				jHostGameCANCELButton;

	private JMenuItem			jJoinMenuItem;

	private JMenuItem			jHostMenuItem;

	private SEPServer			server;

	private SEPClient			client;

	private Box					jGameCreationPlayerListPanel;

	private JPanel				gameCreationConfigPanel;

	private JScrollPane			gameCreationPlayerListScrollPane;

	private JScrollPane			gameCreationPlayerConfigScrollPane;

	private JScrollPane			gameCreationChatScrollPane;

	private JScrollPane			gameCreationConfigScrollPane;

	private UniverseRenderer	universeRenderer;

	public enum GameState
	{
		Creation, Running, Paused
	}

	private GameState	gameState	= GameState.Creation;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				SpaceEmpirePulsarGUI inst = new SpaceEmpirePulsarGUI();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public SpaceEmpirePulsarGUI(UniverseRenderer universeRenderer)
	{
		super();
		this.universeRenderer = universeRenderer;
		initGUI();
	}

	public SpaceEmpirePulsarGUI()
	{
		super();
		initGUI();
	}

	private void initGUI()
	{
		try
		{
			setTitle("SpaceEmpirePulsar");
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.setPreferredSize(new java.awt.Dimension(1024, 768));
			this.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent evt)
				{
					closing();
				}
			});
			{
				// "resources" + File.separator + NeutronGUI.class.getPackage().getName().replace('.', File.separatorChar) + File.separator + "img" + File.separator;
				jMainPanel = new JImagePanel();
				jMainPanel.setImage(IMG_PATH + "logo.png");
				jMainPanel.setAutoSize(true);
				BorderLayout jMainPanelLayout = new BorderLayout();
				setContentPane(jMainPanel);
				// getContentPane().add(jMainPanel, BorderLayout.CENTER);
				jMainPanel.setLayout(jMainPanelLayout);
			}
			{
				jMenuBar = new JMenuBar();
				setJMenuBar(jMenuBar);
				{
					jFileMenu = new JMenu();
					jMenuBar.add(jFileMenu);
					jFileMenu.setText("File");
					{
						jHostMenuItem = new JMenuItem();
						jFileMenu.add(jHostMenuItem);
						jHostMenuItem.setText("Host a new game");
						jHostMenuItem.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent evt)
							{
								JPanel contentPane = new JPanel();
								contentPane.add(getJHostGamePanel(), BorderLayout.WEST);
								setContentPane(contentPane);
								contentPane.setVisible(false);
								contentPane.setVisible(true);
							}
						});
					}
					{
						jJoinMenuItem = new JMenuItem();
						jFileMenu.add(jJoinMenuItem);
						jJoinMenuItem.setText("Join a game");
						jJoinMenuItem.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e)
							{
								JPanel contentPane = new JPanel();
								contentPane.add(getJJoinGamePanel(), BorderLayout.WEST);
								setContentPane(contentPane);
								contentPane.setVisible(false);
								contentPane.setVisible(true);
							}
						});
					}
					{
						jFileMenuSeparator = new JSeparator();
						jFileMenu.add(jFileMenuSeparator);
					}
					{
						jQuitMenuItem = new JMenuItem();
						jFileMenu.add(jQuitMenuItem);
						jQuitMenuItem.setText("Exit");
						jQuitMenuItem.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent evt)
							{
								closing();
							}
						});
					}
				}
			}
			pack();
			this.setSize(1024, 768);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static final Random random = new Random();
	
	private JPanel	joinGameJPanel	= null;

	private JPanel getJJoinGamePanel()
	{
		if (joinGameJPanel == null)
		{
			joinGameJPanel = new JPanel();
			GridLayout jJoinGamePanelLayout = new GridLayout(5, 2);
			joinGameJPanel.setLayout(jJoinGamePanelLayout);
			jJoinGamePanelLayout.setHgap(5);
			jJoinGamePanelLayout.setVgap(5);
			jJoinGamePanelLayout.setColumns(2);
			joinGameJPanel.add(getHostAddressJLabel());
			joinGameJPanel.add(getHostAddressJTextField());
			joinGameJPanel.add(getLoginJLabel());
			joinGameJPanel.add(getNameJTextField());
			joinGameJPanel.add(getGamePortJLabel());
			joinGameJPanel.add(getGamePortJTextField());
			joinGameJPanel.add(getGameTimeoutJLabel());
			joinGameJPanel.add(getGameTimeoutJTextField());

			joinGameJPanel.add(getJoinGameOkJButton());
			joinGameJPanel.add(getJoinGameCancelJButton());
			
			getNameJTextField().setText("Guest"+random.nextInt());
		}
		return joinGameJPanel;
	}

	private JPanel	hostGameJPanel	= null;

	private JPanel getJHostGamePanel()
	{
		if (hostGameJPanel == null)
		{
			hostGameJPanel = new JPanel();
			GridLayout jHostGamePanelLayout = new GridLayout(4, 2);
			hostGameJPanel.setLayout(jHostGamePanelLayout);
			jHostGamePanelLayout.setHgap(5);
			jHostGamePanelLayout.setVgap(5);
			jHostGamePanelLayout.setColumns(2);
			hostGameJPanel.add(getLoginJLabel());
			hostGameJPanel.add(getNameJTextField());
			hostGameJPanel.add(getGamePortJLabel());
			hostGameJPanel.add(getGamePortJTextField());
			hostGameJPanel.add(getGameTimeoutJLabel());
			hostGameJPanel.add(getGameTimeoutJTextField());

			hostGameJPanel.add(getHostGameOkJButton());
			hostGameJPanel.add(getHostGameCancelJButton());
			
			getNameJTextField().setText("Host");
		}
		return hostGameJPanel;
	}

	private JButton	joinGameOkJButton	= null;

	private JButton getJoinGameOkJButton()
	{
		if (joinGameOkJButton == null)
		{
			joinGameOkJButton = new JButton();
			joinGameOkJButton.setText("Join");
			joinGameOkJButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent evt)
				{
					int port;
					long timeOut;
					String login;
					String host;

					try
					{
						port = Integer.parseInt(getGamePortJTextField().getText());
						timeOut = Long.parseLong(getGameTimeoutJTextField().getText());
						login = getNameJTextField().getText();
						host = getHostAddressJTextField().getText();
						if (host == null || host.isEmpty()) throw new Exception("Host is null or empty");
						if (login == null || login.isEmpty()) throw new Exception("Login is null or empty");
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null, "Please input correct values.\nPort and Timeout must be numbers, Name can't be empty.", "Format error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					client = new SEPClient(SpaceEmpirePulsarGUI.this, login, null, host, port, timeOut);
					isAdmin = false;
					client.connect();
				}
			});
		}
		return joinGameOkJButton;
	}

	private JButton getHostGameOkJButton()
	{
		if (jHostGameOKButton == null)
		{
			jHostGameOKButton = new JButton();
			jHostGameOKButton.setText("Create");
			jHostGameOKButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					int port;
					long timeOut;
					String login;
					try
					{
						port = Integer.parseInt(getGamePortJTextField().getText());
						timeOut = Long.parseLong(getGameTimeoutJTextField().getText());
						login = getNameJTextField().getText();
						if (login == null || login.isEmpty()) throw new Exception("Login is null or empty");
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null, "Please input correct values.\nPort and Timeout must be numbers, Name can't be empty.", "Format error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					server = new SEPServer(port, timeOut);
					server.start();
					client = new SEPClient(SpaceEmpirePulsarGUI.this, login, server.getServerAdminKey(), server.getAddress().getHostAddress(), port, timeOut);
					isAdmin = true;
					client.connect();
				}
			});
		}
		return jHostGameOKButton;
	}

	private JLabel getGamePortJLabel()
	{
		if (jHostGamePortLabel == null)
		{
			jHostGamePortLabel = new JLabel();
			jHostGamePortLabel.setText("Port");
		}
		return jHostGamePortLabel;
	}

	private JLabel getGameTimeoutJLabel()
	{
		if (jHostGameTimeoutLabel == null)
		{
			jHostGameTimeoutLabel = new JLabel();
			jHostGameTimeoutLabel.setText("Timeout");
		}
		return jHostGameTimeoutLabel;
	}

	private JTextField getGamePortJTextField()
	{
		if (jHostGamePortTextField == null)
		{
			jHostGamePortTextField = new JTextField();
			jHostGamePortTextField.setText("3131");
		}
		return jHostGamePortTextField;
	}

	private JTextField getGameTimeoutJTextField()
	{
		if (jHostGameTimeoutTextField == null)
		{
			jHostGameTimeoutTextField = new JTextField();
			jHostGameTimeoutTextField.setText("10000");
		}
		return jHostGameTimeoutTextField;
	}

	private JButton getJoinGameCancelJButton()
	{
		return getHostGameCancelJButton();
	}

	private JButton getHostGameCancelJButton()
	{
		if (jHostGameCANCELButton == null)
		{
			jHostGameCANCELButton = new JButton();
			jHostGameCANCELButton.setText("Cancel");
			jHostGameCANCELButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					SpaceEmpirePulsarGUI.this.setContentPane(jMainPanel);
					jMainPanel.setVisible(false);
					jMainPanel.setVisible(true);
				}
			});
		}
		return jHostGameCANCELButton;
	}

	private JLabel	hostAddressJLabel	= null;

	private JLabel getHostAddressJLabel()
	{
		if (hostAddressJLabel == null)
		{
			hostAddressJLabel = new JLabel();
			hostAddressJLabel.setText("Host");
		}
		return hostAddressJLabel;
	}

	private JTextField	hostAddressJTextField	= null;

	private JCheckBox	gameCreationConfigEditionPanelVictoryRegimicideCheckBox;

	private JCheckBox	gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox;

	private JCheckBox	gameCreationConfigEditionPanelVictoryTotalConquestCheckBox;

	private JCheckBox	gameCreationConfigEditionPanelVictoryEconomicCheckBox;

	private JCheckBox	gameCreationConfigEditionPanelVictoryTimeLimitCheckBox;

	private JTextField	gameCreationConfigEditionPanelVictoryEconomicCarbonTextField;

	private JTextField	gameCreationConfigEditionPanelVictoryTimeLimitTextField;

	private JTextField	gameCreationConfigEditionPanelVictoryEconomicPopulationTextField;

	private JLabel		gameCreationConfigEditionPanelVictoryEconomicLabel;

	private JButton		gameCreationBtnsStartBtn;

	private JLabel		gameCreationConfigEditionPanelVictoryTimeLimitLabel;

	private JTextField getHostAddressJTextField()
	{
		if (hostAddressJTextField == null)
		{
			hostAddressJTextField = new JTextField();
			hostAddressJTextField.setText("localhost");
		}
		return hostAddressJTextField;
	}

	private JLabel getLoginJLabel()
	{
		if (jLoginLabel == null)
		{
			jLoginLabel = new JLabel();
			jLoginLabel.setText("Name");
		}
		return jLoginLabel;
	}

	private JTextField getNameJTextField()
	{
		if (jNameTextField == null)
		{
			jNameTextField = new JTextField();
			jNameTextField.setText("Guest");
		}
		return jNameTextField;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#displayGameCreationPanel()
	 */
	@Override
	public void displayGameCreationPanel()
	{		
		if (getContentPane() != getGameCreationPanel())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					if (gameState == GameState.Creation) System.out.println("GUI: displayGameCreationPanel");
					setContentPane(getGameCreationPanel());
					getGameCreationPanel().setVisible(false);
					getGameCreationPanel().setVisible(true);

					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							try
							{
								Thread.sleep(1000);
							}
							catch(InterruptedException e)
							{
								e.printStackTrace();
							}
							
							refreshPlayerList();
							refreshGameConfig();
						}
					});
				}
			});
		}
	}

	private PlayerGameBoard	currentGameBoard;

	private void refreshGameBoard()
	{
		try
		{
			refreshGameBoard(client.getRunningGameInterface().getPlayerGameBoard());
		}
		catch(Throwable t)
		{
			showError("Refresh game board", t);
		}
	}
	
	private Integer stackedErrors = 0;
	private void showError(final String title, final String msg, final Throwable t)
	{
		synchronized(stackedErrors)
		{
			++stackedErrors;
			
			if (stackedErrors > 3) return;
			
			t.printStackTrace();
			
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					JOptionPane.showMessageDialog(null, "Error"+(msg!=null ? ", "+msg : "")+" : ("+t.getClass().getSimpleName()+") "+t.getMessage(), title, JOptionPane.ERROR_MESSAGE);
					
					synchronized(stackedErrors)
					{
						--stackedErrors;
					}
				}
			});
		}
	}
	
	private void showError(String title, Throwable t) {showError(title, null, t);}

	private void refreshGameConfig()
	{
		GameConfig gameCfg;
		try
		{
			switch(gameState)
			{
				case Creation:
				{
					gameCfg = client.getGameCreationInterface().getGameConfig();
					break;
				}
				case Running:
				{
					gameCfg = client.getRunningGameInterface().getGameConfig();
					break;
				}
				default:
				{
					return;
				}
			}
		}
		catch(Throwable t)
		{
			showError("Refresh game config", t);
			return;
		}

		refreshGameConfig(gameCfg);
	}

	private JPanel createPausedGamePlayerListPlayerPanel(Player player, boolean isConnected)
	{
		JPanel playerPanel = new JPanel(new BorderLayout());
		if (player.getConfig().getPortrait() != null)
		{
			JImagePanel portrait = new JImagePanel(player.getConfig().getPortrait(), 50, 60);
			portrait.setAutoSize(false);
			playerPanel.add(portrait, BorderLayout.WEST);
		}
		if (player.getConfig().getSymbol() != null)
		{
			JImagePanel symbol = new JImagePanel(player.getConfig().getSymbol(), 50, 60);
			symbol.setAutoSize(false);
			playerPanel.add(symbol, BorderLayout.EAST);
		}

		JLabel name = new JLabel(player.getName()+(isConnected?"":" (waiting)"), JLabel.CENTER);
		name.setForeground(player.getConfig().getColor());
		playerPanel.add(name, BorderLayout.CENTER);
		
		playerPanel.setBorder(isConnected?null:BorderFactory.createLineBorder(Color.red));

		return playerPanel;
	}
	
	private JPanel createGameCreationPlayerListPlayerPanel(Player player)
	{
		JPanel playerPanel = new JPanel(new BorderLayout());
		if (player.getConfig().getPortrait() != null)
		{
			JImagePanel portrait = new JImagePanel(player.getConfig().getPortrait(), 50, 60);
			portrait.setAutoSize(false);
			playerPanel.add(portrait, BorderLayout.WEST);
		}
		if (player.getConfig().getSymbol() != null)
		{
			JImagePanel symbol = new JImagePanel(player.getConfig().getSymbol(), 50, 60);
			symbol.setAutoSize(false);
			playerPanel.add(symbol, BorderLayout.EAST);
		}

		JLabel name = new JLabel(player.getName(), JLabel.CENTER);
		name.setForeground(player.getConfig().getColor());
		playerPanel.add(name, BorderLayout.CENTER);

		return playerPanel;
	}

	private void refreshPlayerList()
	{
		try
		{
			if (gameState == GameState.Creation)
			{
				this.playerList = client.getGameCreationInterface().getPlayerList();
				refreshPlayerList(this.playerList);
			}
			else if (gameState == GameState.Paused)
			{
				Map<Player, Boolean> players = client.getPausedGameInterface().getPlayerStateList();
				this.playerList = players.keySet();
				refreshPausedGamePlayerList(players);
			}
		}
		catch(Throwable t)
		{
			showError("Refresh player list", t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#onGamePaused()
	 */
	@Override
	public void onGamePaused()
	{
		gameState = GameState.Paused;
		System.out.println("GUI: onGamePaused");
		displayPausedGamePanel();
	}

	private void displayPausedGamePanel()
	{
		displayGameCreationPanel();
	}
	
	private void displayRunningGamePanel()
	{
		if (getContentPane() == getRunningGamePanel()) return;
		
		try
		{
			setContentPane(getRunningGamePanel());
			getRunningGamePanel().setVisible(false);
			getRunningGamePanel().setVisible(true);
			
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					refreshGameBoard();
				}
			});
		}
		catch(Throwable t)
		{
			showError("Display running game panel", t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#onGameRan()
	 */
	@Override
	public void onGameRan()
	{
		gameState = GameState.Running;
		if (getContentPane() != getRunningGamePanel())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					System.out.println("GUI: onGameRan");
					displayRunningGamePanel();
/*
					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							try
							{
								refreshGameBoard();
							}
							catch(Throwable t)
							{
								t.printStackTrace();
							}
						}
					});
*/
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#onGameResumed()
	 */
	@Override
	public void onGameResumed()
	{
		gameState = GameState.Running;
		System.out.println("GUI: onGameResumed");
		displayRunningGamePanel();
	}

	private JPanel getGameCreationPanel()
	{
		if (gameCreationPanel == null)
		{
			gameCreationPanel = new JPanel();
			BorderLayout jGameCreationPanelLayout = new BorderLayout();
			gameCreationPanel.setLayout(jGameCreationPanelLayout);
			gameCreationPanel.add(getGameCreationEastPanel(), BorderLayout.EAST);
			gameCreationPanel.add(getGameCreationWestPanel(), BorderLayout.CENTER);
		}
		return gameCreationPanel;
	}

	private JScrollPane getGameCreationConfigScrollPane()
	{
		if (gameCreationConfigScrollPane == null)
		{
			gameCreationConfigScrollPane = new JScrollPane();
			gameCreationConfigScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			gameCreationConfigScrollPane.setViewportView(getGameCreationConfigPanel());
		}
		return gameCreationConfigScrollPane;
	}

	private JScrollPane getGameCreationChatScrollPane()
	{
		if (gameCreationChatScrollPane == null)
		{
			gameCreationChatScrollPane = new JScrollPane();
			gameCreationChatScrollPane.setPreferredSize(new java.awt.Dimension(585, 315));
			gameCreationChatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gameCreationChatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			gameCreationChatScrollPane.setViewportView(getGameCreationChatEditorPane());

			gameCreationChatScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
			{

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					if (!e.getValueIsAdjusting())
					{
						JScrollBar vBar = gameCreationChatScrollPane.getVerticalScrollBar();
						int newVal = (vBar.getMinimum() + (vBar.getMaximum() - vBar.getMinimum()) * 1);

						if (vBar.getValue() >= (vBar.getMaximum() - vBar.getVisibleAmount() - 30))
						{
							vBar.setValue(newVal);
							getGameCreationPanel().updateUI();
						}
					}
				}
			});
		}
		return gameCreationChatScrollPane;
	}

	private JScrollPane getGameCreationPlayerConfigScrollPane()
	{
		if (gameCreationPlayerConfigScrollPane == null)
		{
			gameCreationPlayerConfigScrollPane = new JScrollPane();
			gameCreationPlayerConfigScrollPane.setPreferredSize(new java.awt.Dimension(200, 150));
			gameCreationPlayerConfigScrollPane.setViewportView(getGameCreationPlayerConfigPanel());
		}
		return gameCreationPlayerConfigScrollPane;
	}

	private JScrollPane getGameCreationPlayerListScrollPane()
	{
		if (gameCreationPlayerListScrollPane == null)
		{
			gameCreationPlayerListScrollPane = new JScrollPane();
			gameCreationPlayerListScrollPane.setViewportView(getGameCreationPlayerListPanel());
			gameCreationPlayerListScrollPane.setAutoscrolls(true);
			gameCreationPlayerListScrollPane.setPreferredSize(new java.awt.Dimension(200, 100));
			gameCreationPlayerListScrollPane.setSize(200, 330);
		}
		return gameCreationPlayerListScrollPane;
	}

	private JPanel getGameCreationConfigPanel()
	{
		if (gameCreationConfigPanel == null)
		{
			gameCreationConfigPanel = new JPanel();
			BorderLayout jGameCreationConfigPanelLayout = new BorderLayout();
			gameCreationConfigPanel.setLayout(jGameCreationConfigPanelLayout);
			gameCreationConfigPanel.add(getGameCreationConfigLabel1(), BorderLayout.NORTH);
			gameCreationConfigPanel.add(getGameCreationConfigEditionPanel(), BorderLayout.CENTER);
			gameCreationConfigPanel.setMinimumSize(new java.awt.Dimension(500, getGameCreationConfigEditionPanelVictoryTimeLimitTextField().getY()
					+ getGameCreationConfigEditionPanelVictoryTimeLimitTextField().getHeight() + 30));
			gameCreationConfigPanel.setPreferredSize(gameCreationConfigPanel.getMinimumSize());
		}
		return gameCreationConfigPanel;
	}

	private Box getJGameCreationPlayerListPanel()
	{
		if (jGameCreationPlayerListPanel == null)
		{
			jGameCreationPlayerListPanel = Box.createVerticalBox();
			jGameCreationPlayerListPanel.setSize(-1, -1);
			jGameCreationPlayerListPanel.setPreferredSize(new java.awt.Dimension(-1, -1));
		}
		return jGameCreationPlayerListPanel;
	}

	private void closing()
	{
		if (client != null && client.isConnected())
		{
			if (JOptionPane.showConfirmDialog(null, "Seems you are currently connected, are you sure you want to exit the game ?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			{
				return;
			}
		}

		SpaceEmpirePulsarGUI.this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose()
	{
		if (client != null)
		{
			System.out.println("Disconnecting client");
			client.disconnect();
		}

		if (server != null)
		{
			System.out.println("Terminating server");
			server.terminate();
		}

		super.dispose();
	}

	private Set<Player> playerList;	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#refreshPlayerList(java.util.Set)
	 */
	@Override
	public void refreshPlayerList(final Set<Player> playerList)
	{
		this.playerList = playerList;
		
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				switch(gameState)
				{
					case Creation:
					{
						getJGameCreationPlayerListPanel().removeAll();
						for(Player player : playerList)
						{
							if (player.getName().compareTo(client.getLogin()) == 0)
							{
								currentPlayer = player;
								getGameCreationPlayerConfigEditionColoredNameLabel().setBackground(null);
								getGameCreationPlayerConfigEditionColoredNameLabel().setForeground(player.getConfig().getColor());
							}

							getJGameCreationPlayerListPanel().add(createGameCreationPlayerListPlayerPanel(player));
						}

						getJGameCreationPlayerListPanel().setVisible(false);
						getJGameCreationPlayerListPanel().setVisible(true);
						break;
					}
					case Running:
					{
						getRunningGamePanel().refreshPlayerList(playerList);
						break;
					}
					default:
					{
						break;
					}
				}
			}
		});
	}
	
	public void refreshPausedGamePlayerList(final Map<Player, Boolean> playerStateList)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				switch(gameState)
				{
					case Paused:
					{
						getJGameCreationPlayerListPanel().removeAll();
						for(Player player : playerStateList.keySet())
						{
							if (player.getName().compareTo(client.getLogin()) == 0)
							{
								currentPlayer = player;
								getGameCreationPlayerConfigEditionColoredNameLabel().setBackground(null);
								getGameCreationPlayerConfigEditionColoredNameLabel().setForeground(player.getConfig().getColor());
							}

							getJGameCreationPlayerListPanel().add(createPausedGamePlayerListPlayerPanel(player, playerStateList.get(player)));
						}

						getJGameCreationPlayerListPanel().setVisible(false);
						getJGameCreationPlayerListPanel().setVisible(true);
						break;
					}
					default:
					{
						break;
					}
				}
			}
		});
	}

	private Player	currentPlayer;

	private JPanel getGameCreationEastPanel()
	{
		if (gameCreationEastPanel == null)
		{
			gameCreationEastPanel = new JPanel();
			BorderLayout gameCreationEastPanelLayout = new BorderLayout();
			gameCreationEastPanel.setLayout(gameCreationEastPanelLayout);
			gameCreationEastPanel.setPreferredSize(new java.awt.Dimension(200, 205));
			gameCreationEastPanel.add(getGameCreationPlayerListScrollPane(), BorderLayout.CENTER);
			gameCreationEastPanel.add(getGameCreationPlayerConfigScrollPane(), BorderLayout.NORTH);
		}
		return gameCreationEastPanel;
	}

	private JPanel getGameCreationWestSouthPanel()
	{
		if (gameCreationWestSouthPanel == null)
		{
			gameCreationWestSouthPanel = new JPanel();
			BorderLayout gameCreationSouthPanelLayout = new BorderLayout();
			gameCreationWestSouthPanel.setLayout(gameCreationSouthPanelLayout);
			gameCreationWestSouthPanel.add(getGameCreationChatPanel(), BorderLayout.CENTER);
			gameCreationWestSouthPanel.add(getGameCreationBtnsPanel(), BorderLayout.SOUTH);
		}
		return gameCreationWestSouthPanel;
	}

	private JPanel getGameCreationBtnsPanel()
	{
		if (gameCreationBtnsPanel == null)
		{
			gameCreationBtnsPanel = new JPanel();
			FlowLayout gameCreationBtnsPanelLayout = new FlowLayout();
			gameCreationBtnsPanel.setLayout(gameCreationBtnsPanelLayout);
			gameCreationBtnsPanel.setPreferredSize(new java.awt.Dimension(10, 30));
			if (isAdmin)
			{
				gameCreationBtnsPanel.add(getGameCreationBtnsStartBtn());
				gameCreationBtnsPanel.add(getGameCreationBtnsLoadGameComboBox());
				gameCreationBtnsPanel.add(getGameCreationBtnsLoadGameBtn());
			}
		}
		return gameCreationBtnsPanel;
	}
	
	private JComboBox gameCreationBtnsLoadGameComboBox;
	private JComboBox getGameCreationBtnsLoadGameComboBox()
	{
		if (gameCreationBtnsLoadGameComboBox == null)
		{
			File workingDir = new File(SEPUtils.SAVE_SUBDIR);
			gameCreationBtnsLoadGameComboBox = new JComboBox();
			
			for(String fileName : workingDir.list(new FilenameFilter()
			{
				
				@Override
				public boolean accept(File dir, String name)
				{
					return name.matches("^.+\\.sav$");
				}
			}))
			{
				gameCreationBtnsLoadGameComboBox.addItem(SEPUtils.getSaveID(fileName));
			}
		}
		
		return gameCreationBtnsLoadGameComboBox;
	}
	
	private JButton gameCreationBtnsLoadGameBtn;
	private JButton getGameCreationBtnsLoadGameBtn()
	{
		if (gameCreationBtnsLoadGameBtn == null)
		{
			gameCreationBtnsLoadGameBtn = new JButton("Load game");
			
			gameCreationBtnsLoadGameBtn.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try
					{
						client.loadGame(getGameCreationBtnsLoadGameComboBox().getSelectedItem().toString());
					}
					catch(Throwable t)
					{
						showError("Loading game", t);						
					}
				}
			});
		}
		
		return gameCreationBtnsLoadGameBtn;
	}

	private JLabel getGameCreationConfigLabel1()
	{
		if (gameCreationConfigLabel1 == null)
		{
			gameCreationConfigLabel1 = new JLabel();
			gameCreationConfigLabel1.setText("Server config");
			gameCreationConfigLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return gameCreationConfigLabel1;
	}

	private JPanel getGameCreationPlayerListPanel()
	{
		if (gameCreationPlayerListPanel == null)
		{
			gameCreationPlayerListPanel = new JPanel();
			BorderLayout gameCreationPlayerListPanelLayout = new BorderLayout();
			gameCreationPlayerListPanel.setLayout(gameCreationPlayerListPanelLayout);
			gameCreationPlayerListPanel.add(getGameCreationPlayerListLabel(), BorderLayout.NORTH);
			gameCreationPlayerListPanel.add(getJGameCreationPlayerListPanel(), BorderLayout.CENTER);
		}
		return gameCreationPlayerListPanel;
	}

	private JLabel getGameCreationPlayerListLabel()
	{
		if (gameCreationPlayerListLabel == null)
		{
			gameCreationPlayerListLabel = new JLabel();
			gameCreationPlayerListLabel.setText("Player list");
			gameCreationPlayerListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return gameCreationPlayerListLabel;
	}

	private JPanel getGameCreationPlayerConfigPanel()
	{
		if (gameCreationPlayerConfigPanel == null)
		{
			gameCreationPlayerConfigPanel = new JPanel();
			BorderLayout gameCreationPlayerConfigPanelLayout = new BorderLayout();
			gameCreationPlayerConfigPanel.setLayout(gameCreationPlayerConfigPanelLayout);
			gameCreationPlayerConfigPanel.add(getGameCreationPlayerConfigLabel(), BorderLayout.NORTH);
			gameCreationPlayerConfigPanel.add(getGameCreationPlayerConfigEditionPanel(), BorderLayout.CENTER);
		}
		return gameCreationPlayerConfigPanel;
	}

	private JLabel getGameCreationPlayerConfigLabel()
	{
		if (gameCreationPlayerConfigLabel == null)
		{
			gameCreationPlayerConfigLabel = new JLabel();
			gameCreationPlayerConfigLabel.setText("Player config");
			gameCreationPlayerConfigLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return gameCreationPlayerConfigLabel;
	}

	private JPanel getGameCreationWestPanel()
	{
		if (gameCreationWestPanel == null)
		{
			gameCreationWestPanel = new JPanel();
			BorderLayout gameCreationWestPanelLayout = new BorderLayout();
			gameCreationWestPanel.setLayout(gameCreationWestPanelLayout);
			gameCreationWestPanel.add(getGameCreationConfigScrollPane(), BorderLayout.CENTER);
			gameCreationWestPanel.add(getGameCreationWestSouthPanel(), BorderLayout.SOUTH);
		}
		return gameCreationWestPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#receiveGameCreationMessage(common.Player, java.lang.String)
	 */
	@Override
	public void receiveGameCreationMessage(final Player fromPlayer, final String msg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				displayGameCreationPanel();

				String htmlText = "<br><font color='#" + GUIUtils.getHTMLColor(fromPlayer.getConfig().getColor()) + "'>" + fromPlayer.getName() + "</font> : "
						+ msg + "</br>";
				HTMLDocument doc = ((HTMLDocument) getGameCreationChatEditorPane().getDocument());

				try
				{
					doc.insertBeforeEnd(doc.getDefaultRootElement(), htmlText);
				}
				catch(BadLocationException e)
				{
					e.printStackTrace();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel getGameCreationChatPanel()
	{
		if (gameCreationChatPanel == null)
		{
			gameCreationChatPanel = new JPanel();
			BorderLayout gameCreationChatPanelLayout = new BorderLayout();
			gameCreationChatPanel.setLayout(gameCreationChatPanelLayout);
			gameCreationChatPanel.add(getGameCreationChatScrollPane(), BorderLayout.CENTER);
			gameCreationChatPanel.add(getGameCreationChatLabel(), BorderLayout.NORTH);
			gameCreationChatPanel.add(getGameCreationChatMessageTextField(), BorderLayout.SOUTH);
		}
		return gameCreationChatPanel;
	}

	private JLabel getGameCreationChatLabel()
	{
		if (gameCreationChatLabel == null)
		{
			gameCreationChatLabel = new JLabel();
			gameCreationChatLabel.setText("Game creation chat");
		}
		return gameCreationChatLabel;
	}

	private JEditorPane getGameCreationChatEditorPane()
	{
		if (gameCreationChatEditorPane == null)
		{
			gameCreationChatEditorPane = new JEditorPane("text/html", "<i>Chat</i> <b>editor</b> <u>pane</u>");
			gameCreationChatEditorPane.setEditable(false);
		}
		return gameCreationChatEditorPane;
	}

	private JTextField getGameCreationChatMessageTextField()
	{
		if (gameCreationChatMessageTextField == null)
		{
			gameCreationChatMessageTextField = new JTextField();
			gameCreationChatMessageTextField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					String msg = gameCreationChatMessageTextField.getText();
					if (msg.isEmpty()) return;
					try
					{
						if (gameState == GameState.Creation)
						{
							client.getGameCreationInterface().sendMessage(msg);
							gameCreationChatMessageTextField.setText("");
						}
						else if (gameState == GameState.Paused)
						{
							client.getPausedGameInterface().sendMessage(msg);
							gameCreationChatMessageTextField.setText("");
						}							
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		return gameCreationChatMessageTextField;
	}

	private JPanel getGameCreationPlayerConfigEditionPanel()
	{
		if (gameCreationPlayerConfigEditionPanel == null)
		{
			gameCreationPlayerConfigEditionPanel = new JPanel();
			BorderLayout gameCreationPlayerConfigEditionPanelLayout = new BorderLayout();
			gameCreationPlayerConfigEditionPanel.setLayout(gameCreationPlayerConfigEditionPanelLayout);
			gameCreationPlayerConfigEditionPanel.add(getGameCreationPlayerConfigEditionColoredNameLabel(), BorderLayout.NORTH);
		}
		return gameCreationPlayerConfigEditionPanel;
	}

	private JLabel getGameCreationPlayerConfigEditionColoredNameLabel()
	{
		if (gameCreationPlayerConfigEditionColoredNameLabel == null)
		{
			gameCreationPlayerConfigEditionColoredNameLabel = new JLabel();
			gameCreationPlayerConfigEditionColoredNameLabel.setText(client.getLogin());
			gameCreationPlayerConfigEditionColoredNameLabel.setFont(new java.awt.Font("AlArabiya", 1, 18));
			gameCreationPlayerConfigEditionColoredNameLabel.setSize(198, 20);
			gameCreationPlayerConfigEditionColoredNameLabel.setPreferredSize(new java.awt.Dimension(0, 20));
			gameCreationPlayerConfigEditionColoredNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			gameCreationPlayerConfigEditionColoredNameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			gameCreationPlayerConfigEditionColoredNameLabel.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent evt)
				{
					if (gameState != GameState.Creation) return;
					
					Color newColor = JColorChooser.showDialog(null, "Choose your color", gameCreationPlayerConfigEditionColoredNameLabel.getForeground());
					if (newColor != null)
					{
						gameCreationPlayerConfigEditionColoredNameLabel.setBackground(newColor);
						updatePlayerConfig();
					}
				}
			});
		}
		return gameCreationPlayerConfigEditionColoredNameLabel;
	}

	private void updatePlayerConfig()
	{
		PlayerConfig config = new PlayerConfig(getGameCreationPlayerConfigEditionColoredNameLabel().getBackground(), null, null);
		try
		{
			client.getGameCreationInterface().updatePlayerConfig(config);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private Boolean	isGameConfigCurrentlyRefreshed	= false;

	private void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		if (currentGameBoard == null)
		{
			saveGame(gameBoard.getDate());
		}
		
		currentGameBoard = gameBoard;
		log.log(Level.INFO, "GUI : refreshGameBoard : " + ((gameBoard == null) ? "not implemented" : gameBoard.toString()));
		getRunningGamePanel().refreshGameBoard(gameBoard);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#refreshGameConfig(common.GameConfig)
	 */
	@Override
	public void refreshGameConfig(final GameConfig gameCfg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized(isGameConfigCurrentlyRefreshed)
				{
					log.log(Level.INFO, "Update GameConfig: " + gameCfg);

					isGameConfigCurrentlyRefreshed = true;

					boolean isEditable = isAdmin && (gameState == GameState.Creation);
					
					// /

					getGameCreationConfigEditionUniverseXSizeTextField().setEditable(isEditable);
					getGameCreationConfigEditionUniverseXSizeTextField().setText(String.valueOf(gameCfg.getDimX()));

					getGameCreationConfigEditionUniverseYSizeTextField().setEditable(isEditable);
					getGameCreationConfigEditionUniverseYSizeTextField().setText(String.valueOf(gameCfg.getDimY()));

					getGameCreationConfigEditionUniverseZSizeTextField().setEditable(isEditable);
					getGameCreationConfigEditionUniverseZSizeTextField().setText(String.valueOf(gameCfg.getDimZ()));

					getGameCreationConfigEditionSunRadiusTextField().setEditable(isEditable);
					getGameCreationConfigEditionSunRadiusTextField().setText(String.valueOf(gameCfg.getSunRadius()));

					getGameCreationConfigEditionPlayersStartingCarbonTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlayersStartingCarbonTextField().setText(String.valueOf(gameCfg.getPlayersPlanetsStartingCarbonResources()));

					getGameCreationConfigEditionPlayersStartingPopulationTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlayersStartingPopulationTextField().setText(String.valueOf(gameCfg.getPlayersPlanetsStartingPopulation()));					
					
					// /

					getGameCreationConfigEditionNeutralCelestialBodiesTextField().setEditable(isEditable);
					getGameCreationConfigEditionNeutralCelestialBodiesTextField().setText(String.valueOf(gameCfg.getNeutralCelestialBodiesCount()));

					getGameCreationConfigEditionPlanetPopulationPerTurnMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlanetPopulationPerTurnMaxTextField().setEditable(isEditable);
					int[] populationPerTurn = gameCfg.getPopulationPerTurn();
					if (populationPerTurn.length == 2)
					{
						getGameCreationConfigEditionPlanetPopulationPerTurnMinTextField().setText(String.valueOf(populationPerTurn[0]));
						getGameCreationConfigEditionPlanetPopulationPerTurnMaxTextField().setText(String.valueOf(populationPerTurn[1]));
					}

					getGameCreationConfigEditionPlanetPopulationLimitMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlanetPopulationLimitMaxTextField().setEditable(isEditable);
					int[] populationLimit = gameCfg.getPopulationLimit();
					if (populationLimit.length == 2)
					{
						getGameCreationConfigEditionPlanetPopulationLimitMinTextField().setText(String.valueOf(populationLimit[0]));
						getGameCreationConfigEditionPlanetPopulationLimitMaxTextField().setText(String.valueOf(populationLimit[1]));
					}

					// /

					getGameCreationConfigEditionPlanetSlotsMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlanetSlotsMaxTextField().setEditable(isEditable);
					Integer[] planetSlotsRange = gameCfg.getCelestialBodiesSlotsAmount().get(Planet.class);
					if (planetSlotsRange != null && planetSlotsRange.length == 2)
					{
						getGameCreationConfigEditionPlanetSlotsMinTextField().setText(String.valueOf(planetSlotsRange[0]));
						getGameCreationConfigEditionPlanetSlotsMaxTextField().setText(String.valueOf(planetSlotsRange[1]));
					}

					getGameCreationConfigEditionAsteroidFieldSlotsMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionAsteroidFieldSlotsMaxTextField().setEditable(isEditable);
					Integer[] asteroidFieldSlotsRange = gameCfg.getCelestialBodiesSlotsAmount().get(AsteroidField.class);
					if (asteroidFieldSlotsRange != null && asteroidFieldSlotsRange.length == 2)
					{
						getGameCreationConfigEditionAsteroidFieldSlotsMinTextField().setText(String.valueOf(asteroidFieldSlotsRange[0]));
						getGameCreationConfigEditionAsteroidFieldSlotsMaxTextField().setText(String.valueOf(asteroidFieldSlotsRange[1]));
					}

					getGameCreationConfigEditionNebulaSlotsMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionNebulaSlotsMaxTextField().setEditable(isEditable);
					Integer[] nebulaSlotsRange = gameCfg.getCelestialBodiesSlotsAmount().get(Nebula.class);
					if (nebulaSlotsRange != null && nebulaSlotsRange.length == 2)
					{
						getGameCreationConfigEditionNebulaSlotsMinTextField().setText(String.valueOf(nebulaSlotsRange[0]));
						getGameCreationConfigEditionNebulaSlotsMaxTextField().setText(String.valueOf(nebulaSlotsRange[1]));
					}

					// /

					getGameCreationConfigEditionPlanetCarbonMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionPlanetCarbonMaxTextField().setEditable(isEditable);
					Integer[] planetCarbonRange = gameCfg.getCelestialBodiesStartingCarbonAmount().get(Planet.class);
					if (planetCarbonRange != null && planetCarbonRange.length == 2)
					{
						getGameCreationConfigEditionPlanetCarbonMinTextField().setText(String.valueOf(planetCarbonRange[0]));
						getGameCreationConfigEditionPlanetCarbonMaxTextField().setText(String.valueOf(planetCarbonRange[1]));
					}

					getGameCreationConfigEditionAsteroidFieldCarbonMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionAsteroidFieldCarbonMaxTextField().setEditable(isEditable);
					Integer[] asteroidFieldCarbonRange = gameCfg.getCelestialBodiesStartingCarbonAmount().get(AsteroidField.class);
					if (asteroidFieldCarbonRange != null && asteroidFieldCarbonRange.length == 2)
					{
						getGameCreationConfigEditionAsteroidFieldCarbonMinTextField().setText(String.valueOf(asteroidFieldCarbonRange[0]));
						getGameCreationConfigEditionAsteroidFieldCarbonMaxTextField().setText(String.valueOf(asteroidFieldCarbonRange[1]));
					}

					getGameCreationConfigEditionNebulaCarbonMinTextField().setEditable(isEditable);
					getGameCreationConfigEditionNebulaCarbonMaxTextField().setEditable(isEditable);
					Integer[] nebulaCarbonRange = gameCfg.getCelestialBodiesStartingCarbonAmount().get(Nebula.class);
					if (nebulaCarbonRange != null && nebulaCarbonRange.length == 2)
					{
						getGameCreationConfigEditionNebulaCarbonMinTextField().setText(String.valueOf(nebulaCarbonRange[0]));
						getGameCreationConfigEditionNebulaCarbonMaxTextField().setText(String.valueOf(nebulaCarbonRange[1]));
					}

					// /

					getGameCreationConfigEditionPlanetNeutralGenTextField().setEditable(isEditable);
					Float planetNeutralGenRate = gameCfg.getNeutralCelestialBodiesGenerationTable().get(Planet.class);
					if (planetNeutralGenRate != null)
					{
						getGameCreationConfigEditionPlanetNeutralGenTextField().setText(String.valueOf(planetNeutralGenRate));
					}

					getGameCreationConfigEditionAsteroidFieldNeutralGenTextField().setEditable(isEditable);
					Float asteroidNeutralGenRate = gameCfg.getNeutralCelestialBodiesGenerationTable().get(AsteroidField.class);
					if (asteroidNeutralGenRate != null)
					{
						getGameCreationConfigEditionAsteroidFieldNeutralGenTextField().setText(String.valueOf(asteroidNeutralGenRate));
					}

					getGameCreationConfigEditionNebulaNeutralGenTextField().setEditable(isEditable);
					Float nebulaNeutralGenRate = gameCfg.getNeutralCelestialBodiesGenerationTable().get(Nebula.class);
					if (nebulaNeutralGenRate != null)
					{
						getGameCreationConfigEditionNebulaNeutralGenTextField().setText(String.valueOf(nebulaNeutralGenRate));
					}

					// /

					getGameCreationConfigEditionUnitsProbeScopeTextField().setEnabled(isEditable);
					getGameCreationConfigEditionUnitsProbeScopeTextField().setText(String.valueOf(gameCfg.getProbeScope()));

					// /

					getGameCreationConfigEditionPanelVictoryTeamCheckBox().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryTeamCheckBox().setSelected(gameCfg.isAllianceVictory());

					getGameCreationConfigEditionPanelVictoryRegimicideCheckBox().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryRegimicideCheckBox().setSelected(gameCfg.isRegimicide());

					getGameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox().setSelected(gameCfg.isAssimilateNeutralisedPeoples());

					getGameCreationConfigEditionPanelVictoryTotalConquestCheckBox().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryTotalConquestCheckBox().setSelected(gameCfg.isTotalConquest());

					getGameCreationConfigEditionPanelVictoryEconomicCheckBox().setEnabled(false);
					getGameCreationConfigEditionPanelVictoryEconomicCarbonTextField().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryEconomicPopulationTextField().setEnabled(isEditable);

					boolean economicVictoryEnabled = false;

					if (gameCfg.getEconomicVictory() != null && gameCfg.getEconomicVictory().length > 0)
					{
						getGameCreationConfigEditionPanelVictoryEconomicPopulationTextField().setText(String.valueOf(gameCfg.getEconomicVictory()[0]));
						if (gameCfg.getEconomicVictory()[0] > 0)
						{
							economicVictoryEnabled = true;
						}

						getGameCreationConfigEditionPanelVictoryEconomicCarbonTextField().setText(String.valueOf(gameCfg.getEconomicVictory()[1]));
						if (gameCfg.getEconomicVictory()[1] > 0)
						{
							economicVictoryEnabled = true;
						}
					}
					getGameCreationConfigEditionPanelVictoryEconomicCheckBox().setSelected(economicVictoryEnabled);

					getGameCreationConfigEditionPanelVictoryTimeLimitCheckBox().setEnabled(false);
					getGameCreationConfigEditionPanelVictoryTimeLimitTextField().setEnabled(isEditable);
					getGameCreationConfigEditionPanelVictoryTimeLimitCheckBox().setSelected(false);

					if (gameCfg.getTimeLimitVictory() != null && gameCfg.getTimeLimitVictory() > 0)
					{
						getGameCreationConfigEditionPanelVictoryTimeLimitCheckBox().setSelected(true);
						getGameCreationConfigEditionPanelVictoryTimeLimitTextField().setText(String.valueOf(gameCfg.getTimeLimitVictory()));
					}
					isGameConfigCurrentlyRefreshed = false;
				}
			}
		});

	}

	private JPanel getGameCreationConfigEditionPanel()
	{
		if (gameCreationConfigEditionPanel == null)
		{
			gameCreationConfigEditionPanel = new JPanel();
			gameCreationConfigEditionPanel.setLayout(null);
			gameCreationConfigEditionPanel.add(getGameCreationConfigUniverseSizeLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseXSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseYSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseZSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNbNeutralCelestialBodiesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNeutralCelestialBodiesTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionSunRadiusLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionSunRadiusTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlayersStartingCarbonLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlayersStartingCarbonTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlayersStartingPopulationLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlayersStartingPopulationTextField());
			
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetStatsLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetStatsMinMaxLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationPerTurnLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationPerTurnMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationPerTurnMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationLimitLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationLimitSlashesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationLimitMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetPopulationLimitMaxTextField());

			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionCelestialBodyTypeLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionCarbonMinLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionSlotMinLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNeutralGenLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetSlotsMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetSlotsMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetCarbonMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetCarbonMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPlanetNeutralGenTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionSlashLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionSlashLabel2());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldCarbonMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldCarbonMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldSlotsMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldSlotsMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldNeutralGenTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionAsteroidFieldLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaSlotsMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaSlotsMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaCarbonMaxTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaSlashesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaCarbonMinTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaNeutralGenTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNebulaLabel());

			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUnitsConfigLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUnitsProbeScopeLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUnitsProbeScopeTextField());

			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryRulesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryTeamCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryRegimicideCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryTotalConquestCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryEconomicCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryEconomicLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryEconomicCarbonTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryEconomicPopulationTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryTimeLimitCheckBox());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryTimeLimitLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionPanelVictoryTimeLimitTextField());
		}
		return gameCreationConfigEditionPanel;
	}

	private FocusListener		gameCreationConfigEditionFocusListener;

	private JLabel				gameCreationConfigEditionPlanetStatsLabel;

	private JLabel				gameCreationConfigEditionPlanetStatsMinMaxLabel;

	private JLabel				gameCreationConfigEditionPlanetPopulationPerTurnLabel;

	private JTextField			gameCreationConfigEditionPlanetPopulationPerTurnMinTextField;

	private JTextField			gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField;

	private JLabel				gameCreationConfigEditionPlanetPopulationLimitLabel;

	private JTextField			gameCreationConfigEditionPlanetPopulationLimitMinTextField;

	private JTextField			gameCreationConfigEditionPlanetPopulationLimitMaxTextField;

	private JLabel				gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel;

	private JLabel				gameCreationConfigEditionPlanetPopulationLimitSlashesLabel;

	private RunningGamePanel	runningGamePanel;

	private JTextField			gameCreationConfigEditionNebulaNeutralGenTextField;

	private JTextField			gameCreationConfigEditionAsteroidFieldNeutralGenTextField;

	private JTextField			gameCreationConfigEditionPlanetNeutralGenTextField;

	private JLabel				gameCreationConfigEditionNeutralGenLabel;

	private JLabel				gameCreationConfigEditionSunRadiusLabel;

	private JTextField			gameCreationConfigEditionSunRadiusTextField;

	private JLabel				gameCreationConfigEditionUnitsConfigLabel;

	private JLabel				gameCreationConfigEditionUnitsProbeScopeLabel;

	private JTextField			gameCreationConfigEditionUnitsProbeScopeTextField;

	private JTextField			gameCreationConfigEditionPlayersStartingPopulationTextField;

	private JLabel				gameCreationConfigEditionPlayersStartingPopulationLabel;

	private JTextField			gameCreationConfigEditionPlayersStartingCarbonTextField;

	private JLabel				gameCreationConfigEditionPlayersStartingCarbonLabel;

	private FocusListener getGameCreationConfigEditionFocusListener()
	{
		if (gameCreationConfigEditionFocusListener == null)
		{
			gameCreationConfigEditionFocusListener = new FocusListener()
			{

				@Override
				public void focusLost(FocusEvent e)
				{
					if (getContentPane() != getGameCreationPanel()) return;
					if (isGameConfigCurrentlyRefreshed) return;
					if (gameState != GameState.Creation) return;
					if (!isAdmin) return;

					int dimX, dimY, dimZ, neutralCelestialBodiesCount, economicVictoryCarbon, economicVictoryPopulation, timeLimitVictory;
					int planetSlotsMin, planetSlotsMax, asteroidSlotsMin, asteroidSlotsMax, nebulaSlotsMin, nebulaSlotsMax;
					int planetCarbonMin, planetCarbonMax, asteroidCarbonMin, asteroidCarbonMax, nebulaCarbonMin, nebulaCarbonMax;
					int sunRadius, probeScope;

					try
					{
						dimX = Integer.valueOf(getGameCreationConfigEditionUniverseXSizeTextField().getText());
						dimY = Integer.valueOf(getGameCreationConfigEditionUniverseYSizeTextField().getText());
						dimZ = Integer.valueOf(getGameCreationConfigEditionUniverseZSizeTextField().getText());

						neutralCelestialBodiesCount = Integer.valueOf(getGameCreationConfigEditionNeutralCelestialBodiesTextField().getText());

						sunRadius = Integer.valueOf(getGameCreationConfigEditionSunRadiusTextField().getText());

						// /

						Map<Class<? extends ICelestialBody>, Integer[]> slotsAmount = new Hashtable<Class<? extends ICelestialBody>, Integer[]>();

						planetSlotsMin = Integer.valueOf(getGameCreationConfigEditionPlanetSlotsMinTextField().getText());
						planetSlotsMax = Integer.valueOf(getGameCreationConfigEditionPlanetSlotsMaxTextField().getText());
						slotsAmount.put(Planet.class, new Integer[] { planetSlotsMin, planetSlotsMax });

						asteroidSlotsMin = Integer.valueOf(getGameCreationConfigEditionAsteroidFieldSlotsMinTextField().getText());
						asteroidSlotsMax = Integer.valueOf(getGameCreationConfigEditionAsteroidFieldSlotsMaxTextField().getText());
						slotsAmount.put(AsteroidField.class, new Integer[] { asteroidSlotsMin, asteroidSlotsMax });

						nebulaSlotsMin = Integer.valueOf(getGameCreationConfigEditionNebulaSlotsMinTextField().getText());
						nebulaSlotsMax = Integer.valueOf(getGameCreationConfigEditionNebulaSlotsMaxTextField().getText());
						slotsAmount.put(Nebula.class, new Integer[] { nebulaSlotsMin, nebulaSlotsMax });

						// /

						Map<Class<? extends ICelestialBody>, Integer[]> carbonAmount = new Hashtable<Class<? extends ICelestialBody>, Integer[]>();

						planetCarbonMin = Integer.valueOf(getGameCreationConfigEditionPlanetCarbonMinTextField().getText());
						planetCarbonMax = Integer.valueOf(getGameCreationConfigEditionPlanetCarbonMaxTextField().getText());
						carbonAmount.put(Planet.class, new Integer[] { planetCarbonMin, planetCarbonMax });

						asteroidCarbonMin = Integer.valueOf(getGameCreationConfigEditionAsteroidFieldCarbonMinTextField().getText());
						asteroidCarbonMax = Integer.valueOf(getGameCreationConfigEditionAsteroidFieldCarbonMaxTextField().getText());
						carbonAmount.put(AsteroidField.class, new Integer[] { asteroidCarbonMin, asteroidCarbonMax });

						nebulaCarbonMin = Integer.valueOf(getGameCreationConfigEditionNebulaCarbonMinTextField().getText());
						nebulaCarbonMax = Integer.valueOf(getGameCreationConfigEditionNebulaCarbonMaxTextField().getText());
						carbonAmount.put(Nebula.class, new Integer[] { nebulaCarbonMin, nebulaCarbonMax });

						// /

						float planetNeutralGenerationRate, asteroidNeutralGenerationRate, nebulaNeutralGenerationRate;

						Map<Class<? extends ICelestialBody>, Float> neutralGenerationRates = new Hashtable<Class<? extends ICelestialBody>, Float>();

						planetNeutralGenerationRate = Float.valueOf(getGameCreationConfigEditionPlanetNeutralGenTextField().getText());
						neutralGenerationRates.put(Planet.class, planetNeutralGenerationRate);

						asteroidNeutralGenerationRate = Float.valueOf(getGameCreationConfigEditionAsteroidFieldNeutralGenTextField().getText());
						neutralGenerationRates.put(AsteroidField.class, asteroidNeutralGenerationRate);

						nebulaNeutralGenerationRate = Float.valueOf(getGameCreationConfigEditionNebulaNeutralGenTextField().getText());
						neutralGenerationRates.put(Nebula.class, nebulaNeutralGenerationRate);

						// /

						economicVictoryCarbon = Integer.valueOf(getGameCreationConfigEditionPanelVictoryEconomicCarbonTextField().getText());
						economicVictoryPopulation = Integer.valueOf(getGameCreationConfigEditionPanelVictoryEconomicPopulationTextField().getText());

						timeLimitVictory = Integer.valueOf(getGameCreationConfigEditionPanelVictoryTimeLimitTextField().getText());

						// /

						probeScope = Integer.valueOf(getGameCreationConfigEditionUnitsProbeScopeTextField().getText());

						// /

						int populationPerTurnMin, populationPerTurnMax, populationLimitMin, populationLimitMax;

						populationPerTurnMin = Integer.valueOf(getGameCreationConfigEditionPlanetPopulationPerTurnMinTextField().getText());
						populationPerTurnMax = Integer.valueOf(getGameCreationConfigEditionPlanetPopulationPerTurnMaxTextField().getText());

						populationLimitMin = Integer.valueOf(getGameCreationConfigEditionPlanetPopulationLimitMinTextField().getText());
						populationLimitMax = Integer.valueOf(getGameCreationConfigEditionPlanetPopulationLimitMaxTextField().getText());

						int playersPlanetsStartingCarbonResources, playersPlanetsStartingPopulation;

						playersPlanetsStartingCarbonResources = Integer.valueOf(getGameCreationConfigEditionPlayersStartingCarbonTextField().getText());
						playersPlanetsStartingPopulation = Integer.valueOf(getGameCreationConfigEditionPlayersStartingPopulationTextField().getText());

						GameConfig gameCfg = new GameConfig(dimX, dimY, dimZ, neutralCelestialBodiesCount, populationPerTurnMin, populationPerTurnMax, populationLimitMin, populationLimitMax, carbonAmount, slotsAmount, neutralGenerationRates, getGameCreationConfigEditionPanelVictoryTeamCheckBox().isSelected(), getGameCreationConfigEditionPanelVictoryRegimicideCheckBox().isSelected(), getGameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox().isSelected(), getGameCreationConfigEditionPanelVictoryTotalConquestCheckBox().isSelected(), economicVictoryCarbon, economicVictoryPopulation, timeLimitVictory, probeScope, sunRadius, playersPlanetsStartingCarbonResources, playersPlanetsStartingPopulation);

						log.log(Level.INFO, "Try to update gameConfig : " + gameCfg);

						client.getGameCreationInterface().updateGameConfig(gameCfg);
					}
					catch(ServerPrivilegeException spe)
					{
						JOptionPane.showMessageDialog(null, spe.getMessage(), "Server Privilege Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					catch(IllegalArgumentException iae)
					{
						JOptionPane.showMessageDialog(null, iae.getMessage(), "GameConfig error", JOptionPane.ERROR_MESSAGE);
						refreshGameConfig();
						return;
					}
					catch(Exception ex)
					{
						refreshGameConfig();
						return;
					}
				}

				@Override
				public void focusGained(FocusEvent e)
				{

				}
			};
		}
		return gameCreationConfigEditionFocusListener;
	}

	// Universe config area start
	private JLabel getGameCreationConfigUniverseSizeLabel()
	{
		if (gameCreationConfigUniverseSizeLabel == null)
		{
			gameCreationConfigUniverseSizeLabel = new JLabel();
			gameCreationConfigUniverseSizeLabel.setText("Universe size :         x        x");
			gameCreationConfigUniverseSizeLabel.setBounds(0, 0, 279, 20);
		}
		return gameCreationConfigUniverseSizeLabel;
	}

	private JTextField getGameCreationConfigEditionUniverseXSizeTextField()
	{
		if (gameCreationConfigEditionUniverseXSizeTextField == null)
		{
			gameCreationConfigEditionUniverseXSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseXSizeTextField.setText("0");
			gameCreationConfigEditionUniverseXSizeTextField.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 100, getGameCreationConfigUniverseSizeLabel().getY(), 26, 20);
			gameCreationConfigEditionUniverseXSizeTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionUniverseXSizeTextField;
	}

	private JTextField getGameCreationConfigEditionUniverseYSizeTextField()
	{
		if (gameCreationConfigEditionUniverseYSizeTextField == null)
		{
			gameCreationConfigEditionUniverseYSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseYSizeTextField.setText("0");
			gameCreationConfigEditionUniverseYSizeTextField.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 140, getGameCreationConfigUniverseSizeLabel().getY(), 26, 20);
			gameCreationConfigEditionUniverseYSizeTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionUniverseYSizeTextField;
	}

	private JTextField getGameCreationConfigEditionUniverseZSizeTextField()
	{
		if (gameCreationConfigEditionUniverseZSizeTextField == null)
		{
			gameCreationConfigEditionUniverseZSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseZSizeTextField.setText("0");
			gameCreationConfigEditionUniverseZSizeTextField.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 178, getGameCreationConfigUniverseSizeLabel().getY(), 26, 20);
			gameCreationConfigEditionUniverseZSizeTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionUniverseZSizeTextField;
	}

	private JLabel getGameCreationConfigEditionNbNeutralCelestialBodiesLabel()
	{
		if (gameCreationConfigEditionNbNeutralCelestialBodiesLabel == null)
		{
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel = new JLabel();
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel.setText("Neutral celestial bodies :");
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 279, getGameCreationConfigUniverseSizeLabel().getY(), 157, 20);
		}
		return gameCreationConfigEditionNbNeutralCelestialBodiesLabel;
	}

	private JTextField getGameCreationConfigEditionNeutralCelestialBodiesTextField()
	{
		if (gameCreationConfigEditionNeutralCelestialBodiesTextField == null)
		{
			gameCreationConfigEditionNeutralCelestialBodiesTextField = new JTextField();
			gameCreationConfigEditionNeutralCelestialBodiesTextField.setText("0");
			gameCreationConfigEditionNeutralCelestialBodiesTextField.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 440, getGameCreationConfigUniverseSizeLabel().getY(), 26, 20);
			gameCreationConfigEditionNeutralCelestialBodiesTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNeutralCelestialBodiesTextField;
	}

	private JLabel getGameCreationConfigEditionSunRadiusLabel()
	{
		if (gameCreationConfigEditionSunRadiusLabel == null)
		{
			gameCreationConfigEditionSunRadiusLabel = new JLabel();
			gameCreationConfigEditionSunRadiusLabel.setText("Sun radius :");
			gameCreationConfigEditionSunRadiusLabel.setBounds(getGameCreationConfigUniverseSizeLabel().getX(), getGameCreationConfigUniverseSizeLabel().getY() + 26, 100, 20);
			gameCreationConfigEditionSunRadiusLabel.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionSunRadiusLabel;
	}

	private JTextField getGameCreationConfigEditionSunRadiusTextField()
	{
		if (gameCreationConfigEditionSunRadiusTextField == null)
		{
			gameCreationConfigEditionSunRadiusTextField = new JTextField();
			gameCreationConfigEditionSunRadiusTextField.setText("1");
			gameCreationConfigEditionSunRadiusTextField.setBounds(getGameCreationConfigUniverseSizeLabel().getX() + 100, getGameCreationConfigUniverseSizeLabel().getY() + 26, 26, 20);
			gameCreationConfigEditionSunRadiusTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionSunRadiusTextField;
	}

	private JLabel getGameCreationConfigEditionPlayersStartingCarbonLabel()
	{
		if (gameCreationConfigEditionPlayersStartingCarbonLabel == null)
		{
			gameCreationConfigEditionPlayersStartingCarbonLabel = new JLabel();
			gameCreationConfigEditionPlayersStartingCarbonLabel.setText("Starting carbon :");
			gameCreationConfigEditionPlayersStartingCarbonLabel.setBounds(getGameCreationConfigUniverseSizeLabel().getX(), getGameCreationConfigUniverseSizeLabel().getY() + 2 * 26, 150, 20);
			gameCreationConfigEditionPlayersStartingCarbonLabel.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlayersStartingCarbonLabel;
	}

	private JTextField getGameCreationConfigEditionPlayersStartingCarbonTextField()
	{
		if (gameCreationConfigEditionPlayersStartingCarbonTextField == null)
		{
			gameCreationConfigEditionPlayersStartingCarbonTextField = new JTextField();
			gameCreationConfigEditionPlayersStartingCarbonTextField.setText("1");
			gameCreationConfigEditionPlayersStartingCarbonTextField.setBounds(getGameCreationConfigEditionPlayersStartingCarbonLabel().getX() + 150, getGameCreationConfigUniverseSizeLabel().getY() + 2 * 26, 70, 20);
			gameCreationConfigEditionPlayersStartingCarbonTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlayersStartingCarbonTextField;
	}

	private JLabel getGameCreationConfigEditionPlayersStartingPopulationLabel()
	{
		if (gameCreationConfigEditionPlayersStartingPopulationLabel == null)
		{
			gameCreationConfigEditionPlayersStartingPopulationLabel = new JLabel();
			gameCreationConfigEditionPlayersStartingPopulationLabel.setText("Starting Population :");
			gameCreationConfigEditionPlayersStartingPopulationLabel.setBounds(getGameCreationConfigEditionPlayersStartingCarbonTextField().getX() + 100, getGameCreationConfigUniverseSizeLabel().getY() + 2 * 26, 150, 20);
			gameCreationConfigEditionPlayersStartingPopulationLabel.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlayersStartingPopulationLabel;
	}

	private JTextField getGameCreationConfigEditionPlayersStartingPopulationTextField()
	{
		if (gameCreationConfigEditionPlayersStartingPopulationTextField == null)
		{
			gameCreationConfigEditionPlayersStartingPopulationTextField = new JTextField();
			gameCreationConfigEditionPlayersStartingPopulationTextField.setText("1");
			gameCreationConfigEditionPlayersStartingPopulationTextField.setBounds(getGameCreationConfigEditionPlayersStartingCarbonTextField().getX() + 100 + 150, getGameCreationConfigUniverseSizeLabel().getY() + 2 * 26, 70, 20);
			gameCreationConfigEditionPlayersStartingPopulationTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlayersStartingPopulationTextField;
	}

	// Planet config area start (linked to Universe config area end)
	private JLabel getGameCreationConfigEditionPlanetStatsLabel()
	{
		if (gameCreationConfigEditionPlanetStatsLabel == null)
		{
			gameCreationConfigEditionPlanetStatsLabel = new JLabel();
			gameCreationConfigEditionPlanetStatsLabel.setText("Planet stats");
			gameCreationConfigEditionPlanetStatsLabel.setBounds(0, getGameCreationConfigEditionPlayersStartingPopulationLabel().getY()
					+ getGameCreationConfigEditionPlayersStartingPopulationLabel().getHeight() + 5, 100, 20);
		}
		return gameCreationConfigEditionPlanetStatsLabel;
	}

	private JLabel getGameCreationConfigEditionPlanetStatsMinMaxLabel()
	{
		if (gameCreationConfigEditionPlanetStatsMinMaxLabel == null)
		{
			gameCreationConfigEditionPlanetStatsMinMaxLabel = new JLabel();
			gameCreationConfigEditionPlanetStatsMinMaxLabel.setText("min / max");
			gameCreationConfigEditionPlanetStatsMinMaxLabel.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 248, getGameCreationConfigEditionPlanetStatsLabel().getY(), 100, 20);
		}
		return gameCreationConfigEditionPlanetStatsMinMaxLabel;
	}

	private JLabel getGameCreationConfigEditionPlanetPopulationPerTurnLabel()
	{
		if (gameCreationConfigEditionPlanetPopulationPerTurnLabel == null)
		{
			gameCreationConfigEditionPlanetPopulationPerTurnLabel = new JLabel();
			gameCreationConfigEditionPlanetPopulationPerTurnLabel.setText("Population per turn");
			gameCreationConfigEditionPlanetPopulationPerTurnLabel.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 50, getGameCreationConfigEditionPlanetStatsLabel().getY() + 26, 200, 20);
		}
		return gameCreationConfigEditionPlanetPopulationPerTurnLabel;
	}

	private JLabel getGameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel()
	{
		if (gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel == null)
		{
			gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel = new JLabel();
			gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel.setText("/");
			gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 274, getGameCreationConfigEditionPlanetStatsLabel().getY() + 26, 20, 20);
		}
		return gameCreationConfigEditionPlanetPopulationPerTurnSlashesLabel;
	}

	private JTextField getGameCreationConfigEditionPlanetPopulationPerTurnMinTextField()
	{
		if (gameCreationConfigEditionPlanetPopulationPerTurnMinTextField == null)
		{
			gameCreationConfigEditionPlanetPopulationPerTurnMinTextField = new JTextField();
			gameCreationConfigEditionPlanetPopulationPerTurnMinTextField.setText("0");
			gameCreationConfigEditionPlanetPopulationPerTurnMinTextField.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 200, getGameCreationConfigEditionPlanetStatsLabel().getY() + 26, 70, 20);
			gameCreationConfigEditionPlanetPopulationPerTurnMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetPopulationPerTurnMinTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetPopulationPerTurnMaxTextField()
	{
		if (gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField == null)
		{
			gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField = new JTextField();
			gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField.setText("0");
			gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 282, getGameCreationConfigEditionPlanetStatsLabel().getY() + 26, 70, 20);
			gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetPopulationPerTurnMaxTextField;
	}

	private JLabel getGameCreationConfigEditionPlanetPopulationLimitLabel()
	{
		if (gameCreationConfigEditionPlanetPopulationLimitLabel == null)
		{
			gameCreationConfigEditionPlanetPopulationLimitLabel = new JLabel();
			gameCreationConfigEditionPlanetPopulationLimitLabel.setText("Population limit");
			gameCreationConfigEditionPlanetPopulationLimitLabel.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 50, getGameCreationConfigEditionPlanetStatsLabel().getY() + 52, 150, 20);
		}
		return gameCreationConfigEditionPlanetPopulationLimitLabel;
	}

	private JLabel getGameCreationConfigEditionPlanetPopulationLimitSlashesLabel()
	{
		if (gameCreationConfigEditionPlanetPopulationLimitSlashesLabel == null)
		{
			gameCreationConfigEditionPlanetPopulationLimitSlashesLabel = new JLabel();
			gameCreationConfigEditionPlanetPopulationLimitSlashesLabel.setText("/");
			gameCreationConfigEditionPlanetPopulationLimitSlashesLabel.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 274, getGameCreationConfigEditionPlanetStatsLabel().getY() + 52, 20, 20);
		}
		return gameCreationConfigEditionPlanetPopulationLimitSlashesLabel;
	}

	private JTextField getGameCreationConfigEditionPlanetPopulationLimitMinTextField()
	{
		if (gameCreationConfigEditionPlanetPopulationLimitMinTextField == null)
		{
			gameCreationConfigEditionPlanetPopulationLimitMinTextField = new JTextField();
			gameCreationConfigEditionPlanetPopulationLimitMinTextField.setText("0");
			gameCreationConfigEditionPlanetPopulationLimitMinTextField.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 200, getGameCreationConfigEditionPlanetStatsLabel().getY() + 52, 70, 20);
			gameCreationConfigEditionPlanetPopulationLimitMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetPopulationLimitMinTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetPopulationLimitMaxTextField()
	{
		if (gameCreationConfigEditionPlanetPopulationLimitMaxTextField == null)
		{
			gameCreationConfigEditionPlanetPopulationLimitMaxTextField = new JTextField();
			gameCreationConfigEditionPlanetPopulationLimitMaxTextField.setText("0");
			gameCreationConfigEditionPlanetPopulationLimitMaxTextField.setBounds(getGameCreationConfigEditionPlanetStatsLabel().getX() + 282, getGameCreationConfigEditionPlanetStatsLabel().getY() + 52, 70, 20);
			gameCreationConfigEditionPlanetPopulationLimitMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetPopulationLimitMaxTextField;
	}

	// Celestial bodies config area start (linked to Planet config area end)
	private JLabel getGameCreationConfigEditionCelestialBodyTypeLabel()
	{
		if (gameCreationConfigEditionCelestialBodyTypeLabel == null)
		{
			gameCreationConfigEditionCelestialBodyTypeLabel = new JLabel();
			gameCreationConfigEditionCelestialBodyTypeLabel.setText("Celestial body");
			gameCreationConfigEditionCelestialBodyTypeLabel.setBounds(0, getGameCreationConfigEditionPlanetPopulationLimitLabel().getY()
					+ getGameCreationConfigEditionPlanetPopulationLimitLabel().getHeight() + 5, 89, 20);
		}
		return gameCreationConfigEditionCelestialBodyTypeLabel;
	}

	private JLabel getGameCreationConfigEditionCarbonMinLabel()
	{
		if (gameCreationConfigEditionCarbonMinLabel == null)
		{
			gameCreationConfigEditionCarbonMinLabel = new JLabel();
			gameCreationConfigEditionCarbonMinLabel.setText("Carbon min / max");
			gameCreationConfigEditionCarbonMinLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 210, getGameCreationConfigEditionCelestialBodyTypeLabel().getY(), 130, 20);
		}
		return gameCreationConfigEditionCarbonMinLabel;
	}

	private JLabel getGameCreationConfigEditionSlotMinLabel()
	{
		if (gameCreationConfigEditionSlotMinLabel == null)
		{
			gameCreationConfigEditionSlotMinLabel = new JLabel();
			gameCreationConfigEditionSlotMinLabel.setText("Slots min / max");
			gameCreationConfigEditionSlotMinLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 350, getGameCreationConfigEditionCelestialBodyTypeLabel().getY(), 100, 20);
		}
		return gameCreationConfigEditionSlotMinLabel;
	}

	private JLabel getGameCreationConfigEditionNeutralGenLabel()
	{
		if (gameCreationConfigEditionNeutralGenLabel == null)
		{
			gameCreationConfigEditionNeutralGenLabel = new JLabel();
			gameCreationConfigEditionNeutralGenLabel.setText("Neutral %");
			gameCreationConfigEditionNeutralGenLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 455, getGameCreationConfigEditionCelestialBodyTypeLabel().getY(), 100, 20);
		}
		return gameCreationConfigEditionNeutralGenLabel;
	}

	private JLabel getGameCreationConfigEditionPlanetLabel()
	{
		if (gameCreationConfigEditionPlanetLabel == null)
		{
			gameCreationConfigEditionPlanetLabel = new JLabel();
			gameCreationConfigEditionPlanetLabel.setText("planet");
			gameCreationConfigEditionPlanetLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 50, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 204, 20);
		}
		return gameCreationConfigEditionPlanetLabel;
	}

	private JTextField getGameCreationConfigEditionPlanetSlotsMinTextField()
	{
		if (gameCreationConfigEditionPlanetSlotsMinTextField == null)
		{
			gameCreationConfigEditionPlanetSlotsMinTextField = new JTextField();
			gameCreationConfigEditionPlanetSlotsMinTextField.setText("0");
			gameCreationConfigEditionPlanetSlotsMinTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 392, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 18, 20);
			gameCreationConfigEditionPlanetSlotsMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetSlotsMinTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetSlotsMaxTextField()
	{
		if (gameCreationConfigEditionPlanetSlotsMaxTextField == null)
		{
			gameCreationConfigEditionPlanetSlotsMaxTextField = new JTextField();
			gameCreationConfigEditionPlanetSlotsMaxTextField.setText("0");
			gameCreationConfigEditionPlanetSlotsMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 422, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 18, 20);
			gameCreationConfigEditionPlanetSlotsMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetSlotsMaxTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetCarbonMaxTextField()
	{
		if (gameCreationConfigEditionPlanetCarbonMaxTextField == null)
		{
			gameCreationConfigEditionPlanetCarbonMaxTextField = new JTextField();
			gameCreationConfigEditionPlanetCarbonMaxTextField.setText("0");
			gameCreationConfigEditionPlanetCarbonMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 295, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 70, 20);
			gameCreationConfigEditionPlanetCarbonMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetCarbonMaxTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetCarbonMinTextField()
	{
		if (gameCreationConfigEditionPlanetCarbonMinTextField == null)
		{
			gameCreationConfigEditionPlanetCarbonMinTextField = new JTextField();
			gameCreationConfigEditionPlanetCarbonMinTextField.setText("0");
			gameCreationConfigEditionPlanetCarbonMinTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 213, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 70, 20);
			gameCreationConfigEditionPlanetCarbonMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetCarbonMinTextField;
	}

	private JTextField getGameCreationConfigEditionPlanetNeutralGenTextField()
	{
		if (gameCreationConfigEditionPlanetNeutralGenTextField == null)
		{
			gameCreationConfigEditionPlanetNeutralGenTextField = new JTextField();
			gameCreationConfigEditionPlanetNeutralGenTextField.setText("0");
			gameCreationConfigEditionPlanetNeutralGenTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 462, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 26, 20);
			gameCreationConfigEditionPlanetNeutralGenTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPlanetNeutralGenTextField;
	}

	private JLabel getGameCreationConfigEditionSlashLabel()
	{
		if (gameCreationConfigEditionSlashLabel == null)
		{
			gameCreationConfigEditionSlashLabel = new JLabel();
			gameCreationConfigEditionSlashLabel.setText("/                              /");
			gameCreationConfigEditionSlashLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 287, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 26, 161, 17);
		}
		return gameCreationConfigEditionSlashLabel;
	}

	private JLabel getGameCreationConfigEditionSlashLabel2()
	{
		if (gameCreationConfigEditionSlashLabel2 == null)
		{
			gameCreationConfigEditionSlashLabel2 = new JLabel();
			gameCreationConfigEditionSlashLabel2.setText("/                              /");
			gameCreationConfigEditionSlashLabel2.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 287, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 166, 17);
		}
		return gameCreationConfigEditionSlashLabel2;
	}

	private JTextField getGameCreationConfigEditionAsteroidFieldCarbonMinTextField()
	{
		if (gameCreationConfigEditionAsteroidFieldCarbonMinTextField == null)
		{
			gameCreationConfigEditionAsteroidFieldCarbonMinTextField = new JTextField();
			gameCreationConfigEditionAsteroidFieldCarbonMinTextField.setText("0");
			gameCreationConfigEditionAsteroidFieldCarbonMinTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 213, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 70, 20);
			gameCreationConfigEditionAsteroidFieldCarbonMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionAsteroidFieldCarbonMinTextField;
	}

	private JTextField getGameCreationConfigEditionAsteroidFieldCarbonMaxTextField()
	{
		if (gameCreationConfigEditionAsteroidFieldCarbonMaxTextField == null)
		{
			gameCreationConfigEditionAsteroidFieldCarbonMaxTextField = new JTextField();
			gameCreationConfigEditionAsteroidFieldCarbonMaxTextField.setText("0");
			gameCreationConfigEditionAsteroidFieldCarbonMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 295, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 70, 20);
			gameCreationConfigEditionAsteroidFieldCarbonMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionAsteroidFieldCarbonMaxTextField;
	}

	private JTextField getGameCreationConfigEditionAsteroidFieldSlotsMaxTextField()
	{
		if (gameCreationConfigEditionAsteroidFieldSlotsMaxTextField == null)
		{
			gameCreationConfigEditionAsteroidFieldSlotsMaxTextField = new JTextField();
			gameCreationConfigEditionAsteroidFieldSlotsMaxTextField.setText("0");
			gameCreationConfigEditionAsteroidFieldSlotsMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 422, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 18, 20);
			gameCreationConfigEditionAsteroidFieldSlotsMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionAsteroidFieldSlotsMaxTextField;
	}

	private JTextField getGameCreationConfigEditionAsteroidFieldSlotsMinTextField()
	{
		if (gameCreationConfigEditionAsteroidFieldSlotsMinTextField == null)
		{
			gameCreationConfigEditionAsteroidFieldSlotsMinTextField = new JTextField();
			gameCreationConfigEditionAsteroidFieldSlotsMinTextField.setText("0");
			gameCreationConfigEditionAsteroidFieldSlotsMinTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 392, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 18, 20);
			gameCreationConfigEditionAsteroidFieldSlotsMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionAsteroidFieldSlotsMinTextField;
	}

	private JTextField getGameCreationConfigEditionAsteroidFieldNeutralGenTextField()
	{
		if (gameCreationConfigEditionAsteroidFieldNeutralGenTextField == null)
		{
			gameCreationConfigEditionAsteroidFieldNeutralGenTextField = new JTextField();
			gameCreationConfigEditionAsteroidFieldNeutralGenTextField.setText("0");
			gameCreationConfigEditionAsteroidFieldNeutralGenTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 462, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 26, 20);
			gameCreationConfigEditionAsteroidFieldNeutralGenTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionAsteroidFieldNeutralGenTextField;
	}

	private JLabel getGameCreationConfigEditionAsteroidFieldLabel()
	{
		if (gameCreationConfigEditionAsteroidFieldLabel == null)
		{
			gameCreationConfigEditionAsteroidFieldLabel = new JLabel();
			gameCreationConfigEditionAsteroidFieldLabel.setText("asteroid field");
			gameCreationConfigEditionAsteroidFieldLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 50, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 48, 204, 20);
		}
		return gameCreationConfigEditionAsteroidFieldLabel;
	}

	private JTextField getGameCreationConfigEditionNebulaSlotsMaxTextField()
	{
		if (gameCreationConfigEditionNebulaSlotsMaxTextField == null)
		{
			gameCreationConfigEditionNebulaSlotsMaxTextField = new JTextField();
			gameCreationConfigEditionNebulaSlotsMaxTextField.setText("0");
			gameCreationConfigEditionNebulaSlotsMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 422, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 18, 20);
			gameCreationConfigEditionNebulaSlotsMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNebulaSlotsMaxTextField;
	}

	private JTextField getGameCreationConfigEditionNebulaSlotsMinTextField()
	{
		if (gameCreationConfigEditionNebulaSlotsMinTextField == null)
		{
			gameCreationConfigEditionNebulaSlotsMinTextField = new JTextField();
			gameCreationConfigEditionNebulaSlotsMinTextField.setText("0");
			gameCreationConfigEditionNebulaSlotsMinTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 392, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 18, 20);
			gameCreationConfigEditionNebulaSlotsMinTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNebulaSlotsMinTextField;
	}

	private JTextField getGameCreationConfigEditionNebulaCarbonMaxTextField()
	{
		if (gameCreationConfigEditionNebulaCarbonMaxTextField == null)
		{
			gameCreationConfigEditionNebulaCarbonMaxTextField = new JTextField();
			gameCreationConfigEditionNebulaCarbonMaxTextField.setText("0");
			gameCreationConfigEditionNebulaCarbonMaxTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 295, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 70, 20);
			gameCreationConfigEditionNebulaCarbonMaxTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNebulaCarbonMaxTextField;
	}

	private JTextField getGameCreationConfigEditionNebulaNeutralGenTextField()
	{
		if (gameCreationConfigEditionNebulaNeutralGenTextField == null)
		{
			gameCreationConfigEditionNebulaNeutralGenTextField = new JTextField();
			gameCreationConfigEditionNebulaNeutralGenTextField.setText("0");
			gameCreationConfigEditionNebulaNeutralGenTextField.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 462, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 26, 20);
			gameCreationConfigEditionNebulaNeutralGenTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNebulaNeutralGenTextField;
	}

	private JLabel getGameCreationConfigEditionNebulaSlashesLabel()
	{
		if (gameCreationConfigEditionNebulaSlashesLabel == null)
		{
			gameCreationConfigEditionNebulaSlashesLabel = new JLabel();
			gameCreationConfigEditionNebulaSlashesLabel.setText("/                              /");
			gameCreationConfigEditionNebulaSlashesLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 287, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 161, 17);
		}
		return gameCreationConfigEditionNebulaSlashesLabel;
	}

	private JTextField getGameCreationConfigEditionNebulaCarbonMinTextField()
	{
		if (gameCreationConfigEditionNebulaCarbonMinTextLabel == null)
		{
			gameCreationConfigEditionNebulaCarbonMinTextLabel = new JTextField();
			gameCreationConfigEditionNebulaCarbonMinTextLabel.setText("0");
			gameCreationConfigEditionNebulaCarbonMinTextLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 213, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 70, 20);
			gameCreationConfigEditionNebulaCarbonMinTextLabel.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionNebulaCarbonMinTextLabel;
	}

	private JLabel getGameCreationConfigEditionNebulaLabel()
	{
		if (gameCreationConfigEditionNebulaLabel == null)
		{
			gameCreationConfigEditionNebulaLabel = new JLabel();
			gameCreationConfigEditionNebulaLabel.setText("nebula");
			gameCreationConfigEditionNebulaLabel.setBounds(getGameCreationConfigEditionCelestialBodyTypeLabel().getX() + 50, getGameCreationConfigEditionCelestialBodyTypeLabel().getY() + 70, 200, 20);
		}
		return gameCreationConfigEditionNebulaLabel;
	}

	// Units config area start (linked to Celestial bodies config area end)
	private JLabel getGameCreationConfigEditionUnitsConfigLabel()
	{
		if (gameCreationConfigEditionUnitsConfigLabel == null)
		{
			gameCreationConfigEditionUnitsConfigLabel = new JLabel();
			gameCreationConfigEditionUnitsConfigLabel.setText("Units specific configuration :");
			gameCreationConfigEditionUnitsConfigLabel.setBounds(0, getGameCreationConfigEditionNebulaLabel().getY()
					+ getGameCreationConfigEditionNebulaLabel().getHeight() + 5, 200, 20);
		}
		return gameCreationConfigEditionUnitsConfigLabel;
	}

	private JLabel getGameCreationConfigEditionUnitsProbeScopeLabel()
	{
		if (gameCreationConfigEditionUnitsProbeScopeLabel == null)
		{
			gameCreationConfigEditionUnitsProbeScopeLabel = new JLabel();
			gameCreationConfigEditionUnitsProbeScopeLabel.setText("Probe scope :");
			gameCreationConfigEditionUnitsProbeScopeLabel.setBounds(getGameCreationConfigEditionUnitsConfigLabel().getX() + 50, getGameCreationConfigEditionUnitsConfigLabel().getY() + 26, 100, 20);
		}
		return gameCreationConfigEditionUnitsProbeScopeLabel;
	}

	private JTextField getGameCreationConfigEditionUnitsProbeScopeTextField()
	{
		if (gameCreationConfigEditionUnitsProbeScopeTextField == null)
		{
			gameCreationConfigEditionUnitsProbeScopeTextField = new JTextField();
			gameCreationConfigEditionUnitsProbeScopeTextField.setText("0");
			gameCreationConfigEditionUnitsProbeScopeTextField.setBounds(getGameCreationConfigEditionUnitsProbeScopeLabel().getX() + 100, getGameCreationConfigEditionUnitsProbeScopeLabel().getY(), 50, 20);
		}
		return gameCreationConfigEditionUnitsProbeScopeTextField;
	}

	// Victory rules config area start (linked to Units bodies config area end)
	private JLabel getGameCreationConfigEditionPanelVictoryRulesLabel()
	{
		if (gameCreationConfigEditionPanelVictoryRulesLabel == null)
		{
			gameCreationConfigEditionPanelVictoryRulesLabel = new JLabel();
			gameCreationConfigEditionPanelVictoryRulesLabel.setText("Victory Rules");
			gameCreationConfigEditionPanelVictoryRulesLabel.setBounds(0, getGameCreationConfigEditionUnitsProbeScopeLabel().getY()
					+ getGameCreationConfigEditionUnitsProbeScopeLabel().getHeight() + 5, 200, 20);
		}
		return gameCreationConfigEditionPanelVictoryRulesLabel;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryTeamCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryTeamCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryTeamCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryTeamCheckBox.setText("Allied victory");
			gameCreationConfigEditionPanelVictoryTeamCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 50, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 20, 200, 20);

			gameCreationConfigEditionPanelVictoryTeamCheckBox.addItemListener(new ItemListener()
			{

				@Override
				public void itemStateChanged(ItemEvent e)
				{
					getGameCreationConfigEditionFocusListener().focusLost(new FocusEvent(gameCreationConfigEditionPanelVictoryTeamCheckBox, e.getID()));
				}
			});
		}
		return gameCreationConfigEditionPanelVictoryTeamCheckBox;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryRegimicideCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryRegimicideCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryRegimicideCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryRegimicideCheckBox.setText("Regimicide");
			gameCreationConfigEditionPanelVictoryRegimicideCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 50, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 40, 200, 20);
			gameCreationConfigEditionPanelVictoryRegimicideCheckBox.addItemListener(new ItemListener()
			{

				@Override
				public void itemStateChanged(ItemEvent e)
				{
					getGameCreationConfigEditionFocusListener().focusLost(new FocusEvent(gameCreationConfigEditionPanelVictoryRegimicideCheckBox, e.getID()));
				}
			});
		}
		return gameCreationConfigEditionPanelVictoryRegimicideCheckBox;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox.setText("Assimilate peoples");
			gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 100, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 60, 200, 20);
			gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox.addItemListener(new ItemListener()
			{

				@Override
				public void itemStateChanged(ItemEvent e)
				{
					getGameCreationConfigEditionFocusListener().focusLost(new FocusEvent(gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox, e.getID()));
				}
			});
		}
		return gameCreationConfigEditionPanelVictoryRegimicideAssimilatePeoplesCheckBox;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryTotalConquestCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryTotalConquestCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryTotalConquestCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryTotalConquestCheckBox.setText("Total conquest");
			gameCreationConfigEditionPanelVictoryTotalConquestCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 50, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 80, 200, 20);
			gameCreationConfigEditionPanelVictoryTotalConquestCheckBox.addItemListener(new ItemListener()
			{

				@Override
				public void itemStateChanged(ItemEvent e)
				{
					getGameCreationConfigEditionFocusListener().focusLost(new FocusEvent(gameCreationConfigEditionPanelVictoryTotalConquestCheckBox, e.getID()));
				}
			});
		}
		return gameCreationConfigEditionPanelVictoryTotalConquestCheckBox;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryEconomicCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryEconomicCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryEconomicCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryEconomicCheckBox.setText("Economic");
			gameCreationConfigEditionPanelVictoryEconomicCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 50, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 100, 100, 20);
		}
		return gameCreationConfigEditionPanelVictoryEconomicCheckBox;
	}

	private JLabel getGameCreationConfigEditionPanelVictoryEconomicLabel()
	{
		if (gameCreationConfigEditionPanelVictoryEconomicLabel == null)
		{
			gameCreationConfigEditionPanelVictoryEconomicLabel = new JLabel();
			gameCreationConfigEditionPanelVictoryEconomicLabel.setText("Carbon                          Population");
			gameCreationConfigEditionPanelVictoryEconomicLabel.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 150, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 100, 300, 20);
		}

		return gameCreationConfigEditionPanelVictoryEconomicLabel;
	}

	private JTextField getGameCreationConfigEditionPanelVictoryEconomicCarbonTextField()
	{
		if (gameCreationConfigEditionPanelVictoryEconomicCarbonTextField == null)
		{
			gameCreationConfigEditionPanelVictoryEconomicCarbonTextField = new JTextField();
			gameCreationConfigEditionPanelVictoryEconomicCarbonTextField.setText("0");
			gameCreationConfigEditionPanelVictoryEconomicCarbonTextField.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 200, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 100, 70, 20);
			gameCreationConfigEditionPanelVictoryEconomicCarbonTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPanelVictoryEconomicCarbonTextField;
	}

	private JTextField getGameCreationConfigEditionPanelVictoryEconomicPopulationTextField()
	{
		if (gameCreationConfigEditionPanelVictoryEconomicPopulationTextField == null)
		{
			gameCreationConfigEditionPanelVictoryEconomicPopulationTextField = new JTextField();
			gameCreationConfigEditionPanelVictoryEconomicPopulationTextField.setText("0");
			gameCreationConfigEditionPanelVictoryEconomicPopulationTextField.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 380, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 100, 70, 20);
			gameCreationConfigEditionPanelVictoryEconomicPopulationTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPanelVictoryEconomicPopulationTextField;
	}

	private JCheckBox getGameCreationConfigEditionPanelVictoryTimeLimitCheckBox()
	{
		if (gameCreationConfigEditionPanelVictoryTimeLimitCheckBox == null)
		{
			gameCreationConfigEditionPanelVictoryTimeLimitCheckBox = new JCheckBox();
			gameCreationConfigEditionPanelVictoryTimeLimitCheckBox.setText("Time limit");
			gameCreationConfigEditionPanelVictoryTimeLimitCheckBox.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 50, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 120, 100, 20);
		}
		return gameCreationConfigEditionPanelVictoryTimeLimitCheckBox;
	}

	private JLabel getGameCreationConfigEditionPanelVictoryTimeLimitLabel()
	{
		if (gameCreationConfigEditionPanelVictoryTimeLimitLabel == null)
		{
			gameCreationConfigEditionPanelVictoryTimeLimitLabel = new JLabel();
			gameCreationConfigEditionPanelVictoryTimeLimitLabel.setText("turns");
			gameCreationConfigEditionPanelVictoryTimeLimitLabel.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 180, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 120, 100, 20);
		}
		return gameCreationConfigEditionPanelVictoryTimeLimitLabel;
	}

	private JTextField getGameCreationConfigEditionPanelVictoryTimeLimitTextField()
	{
		if (gameCreationConfigEditionPanelVictoryTimeLimitTextField == null)
		{
			gameCreationConfigEditionPanelVictoryTimeLimitTextField = new JTextField();
			gameCreationConfigEditionPanelVictoryTimeLimitTextField.setText("0");
			gameCreationConfigEditionPanelVictoryTimeLimitTextField.setBounds(getGameCreationConfigEditionPanelVictoryRulesLabel().getX() + 150, getGameCreationConfigEditionPanelVictoryRulesLabel().getY() + 120, 26, 20);
			gameCreationConfigEditionPanelVictoryTimeLimitTextField.addFocusListener(getGameCreationConfigEditionFocusListener());
		}
		return gameCreationConfigEditionPanelVictoryTimeLimitTextField;
	}

	private JButton getGameCreationBtnsStartBtn()
	{
		if (gameCreationBtnsStartBtn == null)
		{
			gameCreationBtnsStartBtn = new JButton();
			gameCreationBtnsStartBtn.setText("Start");
			gameCreationBtnsStartBtn.setPreferredSize(new java.awt.Dimension(100, 25));
			gameCreationBtnsStartBtn.setHorizontalTextPosition(SwingConstants.CENTER);
			gameCreationBtnsStartBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						client.runGame();
					}
					catch(ServerPrivilegeException e1)
					{
						JOptionPane.showConfirmDialog(null, "You are not authorised to run the game.", "Server Privilege Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
						return;
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
						return;
					}
				}
			});
		}
		return gameCreationBtnsStartBtn;
	}

	private RunningGamePanel getRunningGamePanel()
	{
		if (runningGamePanel == null)
		{
			runningGamePanel = new RunningGamePanel(currentPlayer, client, universeRenderer);
		}
		return runningGamePanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#receiveRunningGameMessage(common.Player, java.lang.String)
	 */
	@Override
	public void receiveRunningGameMessage(final Player fromPlayer, final String msg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				displayRunningGamePanel();
				getRunningGamePanel().receiveRunningGameMessage(fromPlayer, msg);
			}
		});
	}
	
	private String saveFileBaseName;
	private String getSaveFileBaseName()
	{
		if (saveFileBaseName == null)
		{
			if (playerList == null) return "Error";
			
			StringBuffer sb = new StringBuffer();
			sb.append(new Date().toString()+" - ");			
			for(Player p : playerList) sb.append(p.getName()+", ");
			if (playerList.size() > 0) sb.setLength(sb.length() - 2);
			sb.append(" - T");
						
			saveFileBaseName = sb.toString();
		}
		return saveFileBaseName;
	}

	private void saveGame(final int date)
	{
		if (isAdmin)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String saveId = getSaveFileBaseName()+date;
						log.log(Level.INFO, "saveGame('"+saveId+"')");
						client.saveGame(saveId);
					}
					catch(Throwable t)
					{
						showError("Saving game", t);
					}
				}
			});
		}
	}
	
	@Override
	public void receiveNewTurnGameBoard(final PlayerGameBoard gameBoard)
	{
		saveGame(gameBoard.getDate());
		
		SwingUtilities.invokeLater(new Runnable()
		{
		
			@Override
			public void run()
			{
				displayRunningGamePanel();
				getRunningGamePanel().receiveNewTurnGameBoard(gameBoard);
			}
		});
	}

	@Override
	public void receivePausedGameMessage(Player fromPlayer, String msg)
	{
		receiveGameCreationMessage(fromPlayer, msg);
	}
}
