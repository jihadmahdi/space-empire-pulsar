package client.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.SwingUtilities;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;

import server.SEPServer;

import client.SEPClient;
import client.gui.lib.JImagePanel;
import com.cloudgarden.layout.AnchorConstraint;
import com.cloudgarden.layout.AnchorLayout;
import com.jgoodies.forms.layout.FormLayout;

import common.GameConfig;
import common.Player;
import common.PlayerConfig;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class SpaceEmpirePulsarGUI extends javax.swing.JFrame implements SEPClient.IUserInterface
{
	private JMenuBar	jMenuBar;

	private JMenu		jFileMenu;

	private JTextField	jHostGameTimeoutTextField;

	private JTextField	jHostGamePortTextField;

	private JLabel		gameCreationConfigLabel1;
	private JTextField gameCreationConfigEditionNeutralCelestialBodiesTextField;
	private JLabel gameCreationConfigEditionNbNeutralCelestialBodiesLabel;
	private JTextField gameCreationConfigEditionUniverseZSizeTextField;
	private JTextField gameCreationConfigEditionUniverseYSizeTextField;
	private JTextField gameCreationConfigEditionUniverseXSizeTextField;
	private JLabel gameCreationConfigUniverseSizeLabel;
	private JPanel gameCreationConfigEditionPanel;
	private JLabel gameCreationPlayerConfigEditionColoredNameLabel;
	private JPanel gameCreationPlayerConfigEditionPanel;

	private JTextField	gameCreationChatMessageTextField;

	private JEditorPane	gameCreationChatEditorPane;

	private JLabel		gameCreationChatLabel;

	private JPanel		gameCreationChatPanel;

	private JPanel		gameCreationWestPanel;

	private JLabel		gameCreationPlayerConfigLabel;

	private JPanel		gameCreationPlayerConfigPanel;

	private JLabel		gameCreationPlayerListLabel;

	private JPanel		gameCreationPlayerListPanel;

	private JPanel		gameCreationBtnsPanel;

	private JPanel		gameCreationWestSouthPanel;

	private JPanel		gameCreationEastPanel;

	private JLabel		jHostGameTimeoutLabel;

	private JLabel		jHostGamePortLabel;

	private JButton		jHostGameOKButton;

	private JImagePanel	jMainPanel;

	private JMenuItem	jQuitMenuItem;

	private JSeparator	jFileMenuSeparator;

	private JPanel		gameCreationPanel;

	private JTextField	jNameTextField;

	private JLabel		jLoginLabel;

	private JButton		jHostGameCANCELButton;

	private JMenuItem	jJoinMenuItem;

	private JMenuItem	jHostMenuItem;

	private SEPServer	server;

	private SEPClient	client;

	private Box			jGameCreationPlayerListPanel;

	private JPanel		gameCreationConfigPanel;

	private JScrollPane	gameCreationPlayerListScrollPane;

	private JScrollPane	gameCreationPlayerConfigScrollPane;

	private JScrollPane	gameCreationChatScrollPane;

	private JScrollPane	gameCreationConfigScrollPane;

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
			this.setPreferredSize(new java.awt.Dimension(800, 600));
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
				jMainPanel.setImage("resources/client/gui/img/".replace('/', File.separatorChar) + "logo.png");
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
			this.setSize(800, 600);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

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
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null, "Please input correct values.\nPort and Timeout must be numbers, Name can't be empty.", "Format error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					client = new SEPClient(SpaceEmpirePulsarGUI.this, login, null, host, port, timeOut);
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
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null, "Please input correct values.\nPort and Timeout must be numbers, Name can't be empty.", "Format error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					server = new SEPServer(port, timeOut);
					server.start();
					client = new SEPClient(SpaceEmpirePulsarGUI.this, login, server.getServerAdminKey(), server.getAddress().getHostAddress(), port, timeOut);
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
					System.out.println("GUI: displayGameCreationPanel");
					setContentPane(getGameCreationPanel());
					getGameCreationPanel().setVisible(false);
					getGameCreationPanel().setVisible(true);

					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							refreshPlayerList();
						}
					});
				}
			});
		}
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
			Set<Player> players = client.getGameCreationInterface().getPlayerList();
			refreshPlayerList(players);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
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
		// TODO Auto-generated method stub
		System.out.println("GUI: onGamePaused");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#onGameRan()
	 */
	@Override
	public void onGameRan()
	{
		// TODO Auto-generated method stub
		System.out.println("GUI: onGameRan");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#onGameResumed()
	 */
	@Override
	public void onGameResumed()
	{
		// TODO Auto-generated method stub
		System.out.println("GUI: onGameResumed");
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
			gameCreationConfigScrollPane.setPreferredSize(new java.awt.Dimension(585, 200));
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
		}
		return gameCreationConfigPanel;
	}

	private Box getJGameCreationPlayerListPanel()
	{
		if (jGameCreationPlayerListPanel == null)
		{
			jGameCreationPlayerListPanel = Box.createVerticalBox();
			jGameCreationPlayerListPanel.setSize( -1, -1);
			jGameCreationPlayerListPanel.setPreferredSize(new java.awt.Dimension( -1, -1));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.SEPClient.IUserInterface#refreshPlayerList(java.util.Set)
	 */
	@Override
	public void refreshPlayerList(final Set<Player> playerList)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
		
			@Override
			public void run()
			{
				getJGameCreationPlayerListPanel().removeAll();
				for (Player player : playerList)
				{
					if (player.getName().compareTo(client.getLogin()) == 0)
					{
						getGameCreationPlayerConfigEditionColoredNameLabel().setBackground(null);
						getGameCreationPlayerConfigEditionColoredNameLabel().setForeground(player.getConfig().getColor());
					}
					
					getJGameCreationPlayerListPanel().add(createGameCreationPlayerListPlayerPanel(player));
				}

				getJGameCreationPlayerListPanel().setVisible(false);
				getJGameCreationPlayerListPanel().setVisible(true);
			}
		});				
	}

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
			BorderLayout gameCreationBtnsPanelLayout = new BorderLayout();
			gameCreationBtnsPanel.setLayout(gameCreationBtnsPanelLayout);
			gameCreationBtnsPanel.setPreferredSize(new java.awt.Dimension(10, 30));
		}
		return gameCreationBtnsPanel;
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
									
				String htmlText = "<br><font color='#" + getHTMLColor(fromPlayer.getConfig().getColor()) + "'>" + fromPlayer.getName() + "</font> : " + msg + "</br>";
				HTMLDocument doc = ((HTMLDocument) getGameCreationChatEditorPane().getDocument());
				
				try
				{
					doc.insertBeforeEnd(doc.getDefaultRootElement(), htmlText);
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});		
	}
	
	private static String getHTMLColor(Color c)
	{
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
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
						client.getGameCreationInterface().sendMessage(msg);
						gameCreationChatMessageTextField.setText("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		return gameCreationChatMessageTextField;
	}
	
	private JPanel getGameCreationPlayerConfigEditionPanel() {
		if(gameCreationPlayerConfigEditionPanel == null) {
			gameCreationPlayerConfigEditionPanel = new JPanel();
			BorderLayout gameCreationPlayerConfigEditionPanelLayout = new BorderLayout();
			gameCreationPlayerConfigEditionPanel.setLayout(gameCreationPlayerConfigEditionPanelLayout);
			gameCreationPlayerConfigEditionPanel.add(getGameCreationPlayerConfigEditionColoredNameLabel(), BorderLayout.NORTH);
		}
		return gameCreationPlayerConfigEditionPanel;
	}
	
	private JLabel getGameCreationPlayerConfigEditionColoredNameLabel() {
		if(gameCreationPlayerConfigEditionColoredNameLabel == null) {
			gameCreationPlayerConfigEditionColoredNameLabel = new JLabel();
			gameCreationPlayerConfigEditionColoredNameLabel.setText(client.getLogin());
			gameCreationPlayerConfigEditionColoredNameLabel.setFont(new java.awt.Font("AlArabiya",1,18));
			gameCreationPlayerConfigEditionColoredNameLabel.setSize(198, 20);
			gameCreationPlayerConfigEditionColoredNameLabel.setPreferredSize(new java.awt.Dimension(0, 20));
			gameCreationPlayerConfigEditionColoredNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			gameCreationPlayerConfigEditionColoredNameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			gameCreationPlayerConfigEditionColoredNameLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
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
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}

	/* (non-Javadoc)
	 * @see client.SEPClient.IUserInterface#refreshGameConfig(common.GameConfig)
	 */
	@Override
	public void refreshGameConfig(GameConfig gameCfg)
	{
		// TODO : afficher
	}
	
	private JPanel getGameCreationConfigEditionPanel() {
		if(gameCreationConfigEditionPanel == null) {
			gameCreationConfigEditionPanel = new JPanel();
			gameCreationConfigEditionPanel.setLayout(null);
			gameCreationConfigEditionPanel.add(getGameCreationConfigUniverseSizeLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseXSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseYSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionUniverseZSizeTextField());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNbNeutralCelestialBodiesLabel());
			gameCreationConfigEditionPanel.add(getGameCreationConfigEditionNeutralCelestialBodiesTextField());
		}
		return gameCreationConfigEditionPanel;
	}
	
	private JLabel getGameCreationConfigUniverseSizeLabel() {
		if(gameCreationConfigUniverseSizeLabel == null) {
			gameCreationConfigUniverseSizeLabel = new JLabel();
			gameCreationConfigUniverseSizeLabel.setText("Universe size :         x        x");
			gameCreationConfigUniverseSizeLabel.setBounds(0, 0, 279, 20);
		}
		return gameCreationConfigUniverseSizeLabel;
	}
	
	private JTextField getGameCreationConfigEditionUniverseXSizeTextField() {
		if(gameCreationConfigEditionUniverseXSizeTextField == null) {
			gameCreationConfigEditionUniverseXSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseXSizeTextField.setText("20");
			gameCreationConfigEditionUniverseXSizeTextField.setBounds(100, 0, 26, 20);
		}
		return gameCreationConfigEditionUniverseXSizeTextField;
	}
	
	private JTextField getGameCreationConfigEditionUniverseYSizeTextField() {
		if(gameCreationConfigEditionUniverseYSizeTextField == null) {
			gameCreationConfigEditionUniverseYSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseYSizeTextField.setText("20");
			gameCreationConfigEditionUniverseYSizeTextField.setBounds(140, 0, 26, 20);
		}
		return gameCreationConfigEditionUniverseYSizeTextField;
	}
	
	private JTextField getGameCreationConfigEditionUniverseZSizeTextField() {
		if(gameCreationConfigEditionUniverseZSizeTextField == null) {
			gameCreationConfigEditionUniverseZSizeTextField = new JTextField();
			gameCreationConfigEditionUniverseZSizeTextField.setText("20");
			gameCreationConfigEditionUniverseZSizeTextField.setBounds(178, 0, 26, 20);
		}
		return gameCreationConfigEditionUniverseZSizeTextField;
	}
	
	private JLabel getGameCreationConfigEditionNbNeutralCelestialBodiesLabel() {
		if(gameCreationConfigEditionNbNeutralCelestialBodiesLabel == null) {
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel = new JLabel();
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel.setText("Neutral celestial bodies :");
			gameCreationConfigEditionNbNeutralCelestialBodiesLabel.setBounds(279, 0, 157, 20);
		}
		return gameCreationConfigEditionNbNeutralCelestialBodiesLabel;
	}
	
	private JTextField getGameCreationConfigEditionNeutralCelestialBodiesTextField() {
		if(gameCreationConfigEditionNeutralCelestialBodiesTextField == null) {
			gameCreationConfigEditionNeutralCelestialBodiesTextField = new JTextField();
			gameCreationConfigEditionNeutralCelestialBodiesTextField.setText("3");
			gameCreationConfigEditionNeutralCelestialBodiesTextField.setBounds(440, 0, 26, 20);
		}
		return gameCreationConfigEditionNeutralCelestialBodiesTextField;
	}

}
