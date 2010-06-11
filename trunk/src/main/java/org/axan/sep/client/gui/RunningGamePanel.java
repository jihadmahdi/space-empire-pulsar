package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.UniverseRenderer.UniverseRendererListener;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.client.gui.lib.SingleRowFlowLayout;
import org.axan.sep.client.gui.lib.TristateCheckBox;
import org.axan.sep.client.gui.lib.TypedListWrapper;
import org.axan.sep.client.gui.lib.WrappedJLabel;
import org.axan.sep.common.ABuilding;
import org.axan.sep.common.ALogEntry;
import org.axan.sep.common.AbstractGameCommandCheck;
import org.axan.sep.common.AntiProbeMissile;
import org.axan.sep.common.Area;
import org.axan.sep.common.CarbonCarrier;
import org.axan.sep.common.CarbonOrder;
import org.axan.sep.common.DefenseModule;
import org.axan.sep.common.Diplomacy;
import org.axan.sep.common.ExtractionModule;
import org.axan.sep.common.Fleet;
import org.axan.sep.common.GovernmentModule;
import org.axan.sep.common.ICelestialBody;
import org.axan.sep.common.IGame;
import org.axan.sep.common.IGameCommand;
import org.axan.sep.common.ISpecialUnit;
import org.axan.sep.common.LaunchedPulsarMissile;
import org.axan.sep.common.LocalGame;
import org.axan.sep.common.Planet;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Probe;
import org.axan.sep.common.ProductiveCelestialBody;
import org.axan.sep.common.PulsarLauchingPad;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SpaceCounter;
import org.axan.sep.common.SpaceRoad;
import org.axan.sep.common.SpaceRoadDeliverer;
import org.axan.sep.common.StarshipPlant;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.Unit;
import org.axan.sep.common.UnitMarker;
import org.axan.sep.common.UnitSeenLogEntry;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;
import org.axan.sep.common.Fleet.Move;
import org.axan.sep.common.IGame.AttackEnemiesFleetCheck;
import org.axan.sep.common.IGame.Build;
import org.axan.sep.common.IGame.BuildCheck;
import org.axan.sep.common.IGame.BuildSpaceRoad;
import org.axan.sep.common.IGame.ChangeDiplomacy;
import org.axan.sep.common.IGame.DemolishCheck;
import org.axan.sep.common.IGame.DemolishSpaceRoad;
import org.axan.sep.common.IGame.DismantleFleet;
import org.axan.sep.common.IGame.DismantleFleetCheck;
import org.axan.sep.common.IGame.EmbarkGovernment;
import org.axan.sep.common.IGame.EmbarkGovernmentCheck;
import org.axan.sep.common.IGame.FireAntiProbeMissile;
import org.axan.sep.common.IGame.FireAntiProbeMissileCheck;
import org.axan.sep.common.IGame.FormFleet;
import org.axan.sep.common.IGame.FormFleetCheck;
import org.axan.sep.common.IGame.LaunchProbe;
import org.axan.sep.common.IGame.LaunchProbeCheck;
import org.axan.sep.common.IGame.MakeAntiProbeMissiles;
import org.axan.sep.common.IGame.MakeAntiProbeMissilesCheck;
import org.axan.sep.common.IGame.MakeProbes;
import org.axan.sep.common.IGame.MakeProbesCheck;
import org.axan.sep.common.IGame.MakeStarships;
import org.axan.sep.common.IGame.MakeStarshipsCheck;
import org.axan.sep.common.IGame.ModifyCarbonOrder;
import org.axan.sep.common.IGame.MoveFleet;
import org.axan.sep.common.IGame.SettleGovernment;
import org.axan.sep.common.IGame.SettleGovernmentCheck;
import org.axan.sep.common.IGameCommand.GameCommandException;
import org.axan.sep.common.PlayerGameBoard.PlayerGameBoardQueryException;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer.SEPImplementationException;



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
public class RunningGamePanel extends javax.swing.JPanel implements UniverseRendererListener, LocalGame.Client
{
	private static final int	EAST_AREA_WIDTH						= 200;
	private static final int	EAST_AREA_COMPONENTS_MAX_WIDTH		= EAST_AREA_WIDTH - 25;
	private static final int	CELESTIALBODY_DETAILS_AREA_HEIGHT	= 530;
	private static final int	TEXTAREA_MAX_HEIGHT					= 100;

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(new RunningGamePanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private RunningGamePanel()
	{
		this(null, null, null);
	}

	private final Player	player;
	private final SEPClient	client;

	public RunningGamePanel(Player player, SEPClient client, UniverseRenderer universeRenderer)
	{
		super();
		this.player = player;
		this.client = client;		
		this.universeRenderer = universeRenderer;
		initGUI();
	}
	
	public void endTurn(List<IGameCommand> commands) throws StateMachineNotExpectedEventException, RpcException, SEPImplementationException, RunningGameCommandException
	{
		client.getRunningGameInterface().endTurn(commands);		
	}

	private static void checkCommandBtn(JComponent component, AbstractGameCommandCheck result)
	{
		if (result == null)
		{
			component.setEnabled(false);
			component.setToolTipText("TODO");
		}
		else if (result.isPossible())
		{
			component.setEnabled(true);
			//component.setToolTipText(result.isPriceDefined() ? (result.getCarbonPrice() > 0 ? " " + result.getCarbonPrice() + "c." : "") + (result.getPopulationPrice() > 0 ? " " + result.getPopulationPrice() + "pop." : "") : " free");
			component.setToolTipText(result.toString());
		}
		else
		{
			component.setEnabled(false);
			//component.setToolTipText(result.getReason());
			component.setToolTipText(result.toString());
		}
	}

	private void initGUI()
	{
		try
		{
			BorderLayout jPanel1Layout = new BorderLayout();
			setLayout(jPanel1Layout);
			setPreferredSize(new java.awt.Dimension(800, 575));

			add(getRunningGameEastPanel(), BorderLayout.EAST);
			add(getRunningGameSouthPanel(), BorderLayout.SOUTH);
			add(getRunningGameCenterPanel(), BorderLayout.CENTER);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private JPanel	runningGameEastPanel;

	private JPanel getRunningGameEastPanel()
	{
		if (runningGameEastPanel == null)
		{
			runningGameEastPanel = new JPanel();
			BorderLayout runningGameEastPanelLayout = new BorderLayout();
			runningGameEastPanel.setLayout(runningGameEastPanelLayout);
			runningGameEastPanel.setPreferredSize(new Dimension(EAST_AREA_WIDTH, Integer.MAX_VALUE));

			runningGameEastPanel.add(getRunningGameCelestialBodyDetailsScrollPane(), BorderLayout.CENTER);
			runningGameEastPanel.add(getRunningGameFleetDetails(), BorderLayout.SOUTH);
		}
		return runningGameEastPanel;
	}

	private JPanel	runningGameSouthPanel;

	private JPanel getRunningGameSouthPanel()
	{
		if (runningGameSouthPanel == null)
		{
			runningGameSouthPanel = new JPanel();
			FlowLayout runningGameSouthPanelLayout = new FlowLayout();
			runningGameSouthPanelLayout.setAlignment(FlowLayout.LEFT);
			runningGameSouthPanelLayout.setVgap(1);
			runningGameSouthPanel.setLayout(runningGameSouthPanelLayout);
			runningGameSouthPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));

			runningGameSouthPanel.add(getRunningGameShortcutBarLabel());
			runningGameSouthPanel.add(getDiplomacyBtn());
			runningGameSouthPanel.add(getLogsBtn());
			runningGameSouthPanel.add(getRunningGameRedoBtn());
			runningGameSouthPanel.add(getRunningGameUndoBtn());
			runningGameSouthPanel.add(getRunningGameCancelTurnBtn());
			runningGameSouthPanel.add(getRunningGameEndTurnBtn());
		}
		return runningGameSouthPanel;
	}

	private static void showRunningGameCommandExceptionMsg(Exception e)
	{
		e.printStackTrace();
		JOptionPane.showMessageDialog(null, e.getMessage(), "Game command exception", JOptionPane.ERROR_MESSAGE);
	}

	private JButton logsBtn;
	private JButton getLogsBtn()
	{
		if (logsBtn == null)
		{
			logsBtn = new JButton("Logs");
			logsBtn.setPreferredSize(new Dimension(logsBtn.getPreferredSize().width, 20));
			logsBtn.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					getRunningGameTabbedPanel().setSelectedComponent(getRunningGameLogPanel());
				}
			});
		}
		return logsBtn;
	}
	
	private JButton	diplomacyBtn;
	private JButton getDiplomacyBtn()
	{
		if (diplomacyBtn == null)
		{
			diplomacyBtn = new JButton("Diplomacy");
			diplomacyBtn.setPreferredSize(new Dimension(diplomacyBtn.getPreferredSize().width, 20));
			diplomacyBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					displayDiplomacyActionPanel();
					getRunningGameTabbedPanel().setSelectedComponent(getActionPanel());
				}
			});
			diplomacyBtn.setEnabled(true);
		}
		return diplomacyBtn;
	}

	private JButton runningGameUndoBtn;
	
	private JButton getRunningGameUndoBtn()
	{
		if (runningGameUndoBtn == null)
		{
			runningGameUndoBtn = new JButton("Undo last command");
			runningGameUndoBtn.setPreferredSize(new Dimension(runningGameUndoBtn.getPreferredSize().width, 20));
			runningGameUndoBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						currentLocalGame.undo();
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}					
				}
			});
		}
		
		return runningGameUndoBtn;
	}
	
	private JButton runningGameRedoBtn;
	
	private JButton getRunningGameRedoBtn()
	{
		if (runningGameRedoBtn == null)
		{
			runningGameRedoBtn = new JButton("Redo last command");
			runningGameRedoBtn.setPreferredSize(new Dimension(runningGameRedoBtn.getPreferredSize().width, 20));
			runningGameRedoBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						currentLocalGame.redo();
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}					
				}
			});
		}
		
		return runningGameRedoBtn;
	}
	
	private JButton	runningGameCancelTurnBtn;

	private JButton getRunningGameCancelTurnBtn()
	{
		if (runningGameCancelTurnBtn == null)
		{
			runningGameCancelTurnBtn = new JButton("Reset turn");
			runningGameCancelTurnBtn.setPreferredSize(new Dimension(runningGameCancelTurnBtn.getPreferredSize().width, 20));
			runningGameCancelTurnBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						currentLocalGame.resetTurn();
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}					
				}
			});
		}

		return runningGameCancelTurnBtn;
	}

	private JButton	runningGameEndTurnBtn;

	private JButton getRunningGameEndTurnBtn()
	{
		if (runningGameEndTurnBtn == null)
		{
			runningGameEndTurnBtn = new JButton("End turn");
			runningGameEndTurnBtn.setPreferredSize(new Dimension(runningGameEndTurnBtn.getPreferredSize().width, 20));
			runningGameEndTurnBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						currentLocalGame.endTurn();
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}

		return runningGameEndTurnBtn;
	}

	private JPanel	runningGameCenterPanel;

	private JPanel getRunningGameCenterPanel()
	{
		if (runningGameCenterPanel == null)
		{
			runningGameCenterPanel = new JPanel();
			BorderLayout runningGameCenterPanelLayout = new BorderLayout();
			runningGameCenterPanel.setLayout(runningGameCenterPanelLayout);
			runningGameCenterPanel.add(getRunningGameTabbedPanel(), BorderLayout.SOUTH);
			runningGameCenterPanel.add(getRunningGameUniverseRenderingPanel(), BorderLayout.CENTER);
		}
		return runningGameCenterPanel;
	}

	private JPanel	runningGameFleetDetails;

	private JPanel getRunningGameFleetDetails()
	{
		if (runningGameFleetDetails == null)
		{
			runningGameFleetDetails = new JPanel();
			BorderLayout runningGameFleetDetailsLayout = new BorderLayout();
			runningGameFleetDetails.setLayout(runningGameFleetDetailsLayout);
			runningGameFleetDetails.setPreferredSize(new Dimension(Integer.MAX_VALUE, 400));

			runningGameFleetDetails.add(getRunningGameFleetDetailsContent(), BorderLayout.NORTH);
			runningGameFleetDetails.add(getRunningGameFleetDetailsSpecificDetailsPanel(), BorderLayout.CENTER);
		}
		return runningGameFleetDetails;
	}

	private JLabel	runningGameShortcutBarLabel;

	private JLabel getRunningGameShortcutBarLabel()
	{
		if (runningGameShortcutBarLabel == null)
		{
			runningGameShortcutBarLabel = new JLabel();
			runningGameShortcutBarLabel.setText("Shortcut Bar");
		}
		return runningGameShortcutBarLabel;
	}

	private JTabbedPane	runningGameTabbedPanel;

	private JTabbedPane getRunningGameTabbedPanel()
	{
		if (runningGameTabbedPanel == null)
		{
			runningGameTabbedPanel = new JTabbedPane();
			runningGameTabbedPanel.setPreferredSize(new Dimension(4, 200));
			runningGameTabbedPanel.addTab("Chat panel", getRunningGameChatPanel());
			runningGameTabbedPanel.addTab("Log", getRunningGameLogPanel());
			runningGameTabbedPanel.addTab("Action", getActionPanel());
		}
		return runningGameTabbedPanel;
	}

	private JPanel	actionPanel;

	private JPanel getActionPanel()
	{
		if (actionPanel == null)
		{
			actionPanel = new JPanel(new BorderLayout());
		}
		return actionPanel;
	}

	private JPanel	runningGameChatPanel;

	private JPanel getRunningGameChatPanel()
	{
		if (runningGameChatPanel == null)
		{
			runningGameChatPanel = new JPanel();
			BorderLayout runningGameChatPanelLayout = new BorderLayout();
			runningGameChatPanel.setLayout(runningGameChatPanelLayout);
			runningGameChatPanel.add(getRunningGameChatTextField(), BorderLayout.SOUTH);
			runningGameChatPanel.add(getRunningGameChatScrollPane(), BorderLayout.CENTER);
			runningGameChatPanel.add(getRunningGameChatPlayerListScrollPane(), BorderLayout.EAST);
		}
		return runningGameChatPanel;
	}
	
	private JPanel runningGameLogPanel;
	
	private JPanel getRunningGameLogPanel()
	{
		if (runningGameLogPanel == null)
		{
			runningGameLogPanel = new JPanel(new BorderLayout());
			runningGameLogPanel.add(getRunningGameLogScrollPane(), BorderLayout.CENTER);
		}
		return runningGameLogPanel;		
	}

	private JPanel	runningGameUniverseRenderingPanel;

	private JPanel getRunningGameUniverseRenderingPanel()
	{
		if (runningGameUniverseRenderingPanel == null)
		{
			runningGameUniverseRenderingPanel = new JPanel();
			BorderLayout runningGameUniverseRenderingPanelLayout = new BorderLayout();
			runningGameUniverseRenderingPanel.setLayout(runningGameUniverseRenderingPanelLayout);
			runningGameUniverseRenderingPanel.add(getUniversePanel(), BorderLayout.CENTER);
		}
		return runningGameUniverseRenderingPanel;
	}

	private WrappedJLabel	runningGameCelestialBodyDetailsLabel;

	private WrappedJLabel getRunningGameCelestialBodyDetailsLabel()
	{
		if (runningGameCelestialBodyDetailsLabel == null)
		{
			runningGameCelestialBodyDetailsLabel = new WrappedJLabel();
			runningGameCelestialBodyDetailsLabel.setText("Celestial body details");
			runningGameCelestialBodyDetailsLabel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));
		}
		return runningGameCelestialBodyDetailsLabel;
	}

	private WrappedJLabel	runningGameFleetDetailsContent;

	private WrappedJLabel getRunningGameFleetDetailsContent()
	{
		if (runningGameFleetDetailsContent == null)
		{
			runningGameFleetDetailsContent = new WrappedJLabel();
			runningGameFleetDetailsContent.setText("");
		}
		return runningGameFleetDetailsContent;
	}

	private JScrollPane	runningGameCelestialBodyDetailsScrollPanel;

	private JScrollPane getRunningGameCelestialBodyDetailsScrollPane()
	{
		if (runningGameCelestialBodyDetailsScrollPanel == null)
		{
			JPanel runningGameCelestialBodyDetailsPanel = new JPanel();

			/*
			BoxLayout runningGameCelestialBodyDetailsPanelLayout = new BoxLayout(runningGameCelestialBodyDetailsPanel, BoxLayout.Y_AXIS);			
			*/

			FlowLayout runningGameCelestialBodyDetailsPanelLayout = new SingleRowFlowLayout(FlowLayout.LEFT);
			runningGameCelestialBodyDetailsPanel.setLayout(runningGameCelestialBodyDetailsPanelLayout);
			runningGameCelestialBodyDetailsPanel.setPreferredSize(new Dimension(EAST_AREA_WIDTH, CELESTIALBODY_DETAILS_AREA_HEIGHT));

			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsContentLabel());
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingsListScrollPane());

			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel());
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel());

			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsUnitsListScrollPane());

			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsAttackFleetsBtn());

			/*
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsContentPanel());
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsPanel());
			*/

			runningGameCelestialBodyDetailsScrollPanel = new JScrollPane(runningGameCelestialBodyDetailsPanel);
			runningGameCelestialBodyDetailsScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			runningGameCelestialBodyDetailsScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			runningGameCelestialBodyDetailsScrollPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}
		return runningGameCelestialBodyDetailsScrollPanel;
	}

	private JButton	runningGameCelestialBodyDetailsAttackFleetsBtn;

	private JButton getRunningGameCelestialBodyDetailsAttackFleetsBtn()
	{
		if (runningGameCelestialBodyDetailsAttackFleetsBtn == null)
		{
			runningGameCelestialBodyDetailsAttackFleetsBtn = new JButton("Attack ennemy fleets");
			runningGameCelestialBodyDetailsAttackFleetsBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (currentSelectedArea == null) return;
					if (currentSelectedArea.getCelestialBody() == null || !ProductiveCelestialBody.class.isInstance(currentSelectedArea.getCelestialBody())) return;
					ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(currentSelectedArea.getCelestialBody());
					if (currentPlayer.isNamed(productiveCelestialBody.getOwnerName())) return;

					try
					{
						currentLocalGame.executeCommand(new LocalGame.AttackEnemiesFleet(productiveCelestialBody.getName()));
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return runningGameCelestialBodyDetailsAttackFleetsBtn;
	}

	/*
	private JPanel	runningGameCelestialBodyDetailsContentPanel;

	private JPanel getRunningGameCelestialBodyDetailsContentPanel()
	{
		if (runningGameCelestialBodyDetailsContentPanel == null)
		{
			runningGameCelestialBodyDetailsContentPanel = new JPanel();
			FlowLayout runningGameCelestialBodyDetailsContentPanelLayout = new SingleRowFlowLayout(FlowLayout.LEFT);

			runningGameCelestialBodyDetailsContentPanel.setLayout(runningGameCelestialBodyDetailsContentPanelLayout);
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsContentLabel());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsBuildingsListScrollPane());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsUnitsListScrollPane());
		}
		return runningGameCelestialBodyDetailsContentPanel;
	}
	*/

	private WrappedJLabel	runningGameCelestialBodyDetailsContentLabel;

	private WrappedJLabel getRunningGameCelestialBodyDetailsContentLabel()
	{
		if (runningGameCelestialBodyDetailsContentLabel == null)
		{
			runningGameCelestialBodyDetailsContentLabel = new WrappedJLabel();
			runningGameCelestialBodyDetailsContentLabel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));
		}
		return runningGameCelestialBodyDetailsContentLabel;
	}

	private JScrollPane	runningGameCelestialBodyDetailsBuildingsListScrollPane;

	private JScrollPane getRunningGameCelestialBodyDetailsBuildingsListScrollPane()
	{
		if (runningGameCelestialBodyDetailsBuildingsListScrollPane == null)
		{
			runningGameCelestialBodyDetailsBuildingsListScrollPane = new JScrollPane(getRunningGameCelestialBodyDetailsBuildingsList());
			runningGameCelestialBodyDetailsBuildingsListScrollPane.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, getRunningGameCelestialBodyDetailsBuildingsList().getPreferredScrollableViewportSize().height));
			runningGameCelestialBodyDetailsBuildingsListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return runningGameCelestialBodyDetailsBuildingsListScrollPane;
	}

	private JList	runningGameCelestialBodyDetailsBuildingsList;

	private JList getRunningGameCelestialBodyDetailsBuildingsList()
	{
		if (runningGameCelestialBodyDetailsBuildingsList == null)
		{
			runningGameCelestialBodyDetailsBuildingsList = new JList();
			runningGameCelestialBodyDetailsBuildingsList.setVisibleRowCount(5);
			runningGameCelestialBodyDetailsBuildingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			runningGameCelestialBodyDetailsBuildingsList.addListSelectionListener(new ListSelectionListener()
			{

				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					if (runningGameCelestialBodyDetailsBuildingsList.getSelectedValue() == null) return;

					refreshBuildingDetails();

					if (runningGameCelestialBodyDetailsBuildingsList.isFocusOwner())
					{
						getRunningGameTabbedPanel().setSelectedComponent(getActionPanel());
					}
				}
			});

			runningGameCelestialBodyDetailsBuildingsList.addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = runningGameCelestialBodyDetailsBuildingsList.locationToIndex(e.getPoint());
					if (index < 0) return;

					String label = runningGameCelestialBodyDetailsBuildingsList.getModel().getElementAt(index).toString();
					if (label == null) return;

					runningGameCelestialBodyDetailsBuildingsList.setToolTipText(label);

					super.mouseMoved(e);
				}
			});
		}
		return runningGameCelestialBodyDetailsBuildingsList;
	}

	private final TypedListWrapper.TypedListElementSelector<Unit>		UNIT_SELECTOR	= new TypedListWrapper.TypedListElementSelector<Unit>()
																						{
																							@Override
																							public boolean equals(Unit o1, Unit o2)
																							{
																								return o1.getOwnerName().equals(o2.getOwnerName()) && o1.getName().equals(o2.getName());
																							}
																						};

	private final TypedListWrapper.AbstractTypedJListCellRender<Unit>	UNIT_RENDERER	= new TypedListWrapper.AbstractTypedJListCellRender<Unit>()
																						{
																							@Override
																							public Component getListCellRendererComponent(JList list, Unit unit, int index, boolean isSelected, boolean cellHasFocus)
																							{
																								super.getListCellRendererComponent(list, unit, index, isSelected, cellHasFocus);
																								label.setText(unit.getClass().getSimpleName() + " [" + ((unit.getOwnerName() != null) ? unit.getOwnerName() : "unknown") + "] " + unit.getName());
																								return label;
																							}
																						};

	private TypedListWrapper<JList, Unit>								runningGameCelestialBodyDetailsUnitsList;

	private TypedListWrapper<JList, Unit> getRunningGameCelestialBodyDetailsUnitsList()
	{
		if (runningGameCelestialBodyDetailsUnitsList == null)
		{
			runningGameCelestialBodyDetailsUnitsList = new TypedListWrapper<JList, Unit>(Unit.class, new JList(), UNIT_SELECTOR);

			runningGameCelestialBodyDetailsUnitsList.setCellRenderer(UNIT_RENDERER);

			runningGameCelestialBodyDetailsUnitsList.getComponent().setVisibleRowCount(5);
			runningGameCelestialBodyDetailsUnitsList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			runningGameCelestialBodyDetailsUnitsList.getComponent().addListSelectionListener(new ListSelectionListener()
			{

				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					Unit selectedUnit = runningGameCelestialBodyDetailsUnitsList.getSelectedElement();
					if (selectedUnit == null) return;

					refreshUnitDetails();

					if (runningGameCelestialBodyDetailsUnitsList.getComponent().isFocusOwner())
					{
						if (Fleet.class.isInstance(selectedUnit) && Fleet.class.cast(selectedUnit).isUnasignedFleet())
						{
							getRunningGameTabbedPanel().setSelectedComponent(getActionPanel());
						}
						else
						{
							getRunningGameTabbedPanel().setSelectedComponent(getActionPanel());
						}
					}
				}
			});
		}
		return runningGameCelestialBodyDetailsUnitsList;
	}

	private JScrollPane	runningGameCelestialBodyDetailsUnitsListPane;

	private JScrollPane getRunningGameCelestialBodyDetailsUnitsListScrollPane()
	{
		if (runningGameCelestialBodyDetailsUnitsListPane == null)
		{
			runningGameCelestialBodyDetailsUnitsListPane = new JScrollPane(getRunningGameCelestialBodyDetailsUnitsList().getComponent());
			runningGameCelestialBodyDetailsUnitsListPane.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, getRunningGameCelestialBodyDetailsUnitsList().getComponent().getPreferredScrollableViewportSize().height));
			runningGameCelestialBodyDetailsUnitsListPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return runningGameCelestialBodyDetailsUnitsListPane;
	}

	private JScrollPane	runningGameChatPlayerListScrollPane;

	private JEditorPane	runningGameChatContentEditorPane;

	private JScrollPane	runningGameChatScrollPane;

	private JPanel		runningGameChatPlayerListPanel;

	private JTextField	runningGameChatTextField;

	/*
	private JPanel		runningGameCelestialBodyDetailsBuildingDetailsPanel;
	
	private JPanel getRunningGameCelestialBodyDetailsBuildingDetailsPanel()
	{
		if (runningGameCelestialBodyDetailsBuildingDetailsPanel == null)
		{
			runningGameCelestialBodyDetailsBuildingDetailsPanel = new JPanel();
			FlowLayout runningGameCelestialBodyDetailsBuildingDetailsPanelLayout = new SingleRowFlowLayout(FlowLayout.LEFT);

			runningGameCelestialBodyDetailsBuildingDetailsPanel.setLayout(runningGameCelestialBodyDetailsBuildingDetailsPanelLayout);
			runningGameCelestialBodyDetailsBuildingDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel());
			runningGameCelestialBodyDetailsBuildingDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel());
		}
		if (runningGameCelestialBodyDetailsBuildingDetailsPanel.getComponentCount() == 0)
		{
			runningGameCelestialBodyDetailsBuildingDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel());
		}
		return runningGameCelestialBodyDetailsBuildingDetailsPanel;
	}
	*/

	private JPanel		runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel;

	private JPanel getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel()
	{
		if (runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel == null)
		{
			runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel = new JPanel();
			runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));
		}
		return runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel;
	}

	private JPanel	runningGameFleetDetailsSpecificDetailsPanel;

	private JPanel getRunningGameFleetDetailsSpecificDetailsPanel()
	{
		if (runningGameFleetDetailsSpecificDetailsPanel == null)
		{
			runningGameFleetDetailsSpecificDetailsPanel = new JPanel();
			runningGameFleetDetailsSpecificDetailsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));
		}
		return runningGameFleetDetailsSpecificDetailsPanel;
	}

	private UniverseRenderer	universeRenderer;
	private JPanel				universePanel;

	private JPanel getUniversePanel()
	{
		if (universePanel == null)
		{
			if (universeRenderer == null)
			{
				universeRenderer = new UniversePanel();
				;
				universePanel = universeRenderer.getPanel();
				universeRenderer.setListener(this);
			}
			else
			{
				return universeRenderer.getPanel();
			}
		}
		return universePanel;
	}

	private UniverseRenderer getUniverseRenderer()
	{
		if (universeRenderer == null)
		{
			UniversePanel universeRendererPanel = new UniversePanel();
			universeRenderer = universeRendererPanel;
			universePanel = universeRendererPanel;
			universeRenderer.setListener(this);
		}
		return universeRenderer;
	}

	private IGame		currentLocalGame;

	private Area			currentSelectedArea;
	private RealLocation	currentSelectedLocation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.gui.UniverseRenderer.UniverseRendererListener#updateSelectedArea(int, int, int)
	 */
	@Override
	public void updateSelectedArea(RealLocation location)
	{
		if (currentLocalGame == null) return;

		Area newSelection = currentLocalGame.getGameBoard().getArea(location);
		if (currentSelectedArea == newSelection) return;

		currentSelectedArea = newSelection;
		currentSelectedLocation = location;

		String selectedAreaDisplay = currentSelectedArea.toString();
		getRunningGameCelestialBodyDetailsContentLabel().setText(selectedAreaDisplay.substring(0, (selectedAreaDisplay.indexOf("Buildings") < 0) ? selectedAreaDisplay.length() : selectedAreaDisplay.indexOf("Buildings")));

		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();

		if (celestialBody != null && celestialBody.getLastObservation() >= 0 && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			Vector<String> buildingsList = new Vector<String>();

			Set<Class<? extends ABuilding>> buildingsTypes = new HashSet<Class<? extends ABuilding>>(SEPUtils.buildingTypes);

			ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
			if (productiveCelestialBody.getBuildings() != null)
			{
				for(ABuilding b : productiveCelestialBody.getBuildings())
				{
					buildingsList.add(buildingToLabel(b));
					buildingsTypes.remove(b.getClass());
				}
			}

			for(Class<? extends ABuilding> bt : buildingsTypes)
			{
				if (productiveCelestialBody.canBuildType(bt))
				{
					buildingsList.add(buildingTypeToLabel(bt));
				}
			}

			Object lastSelectedValue = getRunningGameCelestialBodyDetailsBuildingsList().getSelectedValue();
			getRunningGameCelestialBodyDetailsBuildingsList().setListData(buildingsList);
			if (lastSelectedValue != null)
			{
				getRunningGameCelestialBodyDetailsBuildingsList().setSelectedValue(lastSelectedValue, true);
			}
			getRunningGameCelestialBodyDetailsBuildingsList().setVisible(true);

			if (!currentPlayer.isNamed(productiveCelestialBody.getOwnerName()))
			{
				AttackEnemiesFleetCheck check = new LocalGame.AttackEnemiesFleet(productiveCelestialBody.getName()).can(currentLocalGame.getGameBoard());
				checkCommandBtn(getRunningGameCelestialBodyDetailsAttackFleetsBtn(), check);
			}
			else
			{
				getRunningGameCelestialBodyDetailsAttackFleetsBtn().setEnabled(false);
			}		
		}
		else
		{
			getRunningGameCelestialBodyDetailsBuildingsList().setVisible(false);
			
			getRunningGameCelestialBodyDetailsAttackFleetsBtn().setEnabled(false);
		}

		// Units list	
		Unit lastSelectedUnit = getRunningGameCelestialBodyDetailsUnitsList().getSelectedElement();
		getRunningGameCelestialBodyDetailsUnitsList().clear();

		Set<Unit> units = currentSelectedArea.getUnits();
		if (units != null)
		{
			getRunningGameCelestialBodyDetailsUnitsList().addAll(units);
		}

		Set<UnitMarker> unitsMarker = currentSelectedArea.getMarkers(UnitMarker.class);
		if (unitsMarker != null)
		{
			for(UnitMarker m : unitsMarker)
			{
				getRunningGameCelestialBodyDetailsUnitsList().add(m.getUnit());
			}
		}

		if (lastSelectedUnit != null)
		{
			getRunningGameCelestialBodyDetailsUnitsList().setSelectedElement(lastSelectedUnit);
		}

		eraseBuildingDetails();

		updateUI();

		// TODO : Markers
	}

	private void addBuildBtns(ProductiveCelestialBody productiveCelestialBody, Class<? extends ABuilding> buildingType, int carbonCost, int nbBuild) throws StateMachineNotExpectedEventException, RpcException
	{
		addBuildBtns(productiveCelestialBody, buildingType, new int[] { carbonCost }, nbBuild);
	}

	private void addBuildBtns(final ProductiveCelestialBody productiveCelestialBody, final Class<? extends ABuilding> buildingType, int[] buildCosts, int nbBuild) throws StateMachineNotExpectedEventException, RpcException
	{
		JPanel buildBtnsPanel = new JPanel(new FlowLayout());
		buildBtnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

		JButton buildBtn = new JButton();
		String label = (nbBuild > 0) ? "Upgrade" : "Build";
		buildBtn.setText(label);
		buildBtn.setToolTipText(label + " " + (nbBuild + 1) + " " + buildingType.getSimpleName() + " for " + ((buildCosts[0] > 0) ? buildCosts[0] + "c" : "") + ((buildCosts.length > 1 && buildCosts[1] > 0) ? buildCosts[1] + "pop." : ""));

		BuildCheck checkBuild = new LocalGame.Build(productiveCelestialBody.getName(), buildingType).can(currentLocalGame.getGameBoard());
		checkCommandBtn(buildBtn, checkBuild);
		buildBtn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					currentLocalGame.executeCommand(new LocalGame.Build(productiveCelestialBody.getName(), buildingType));
				}
				catch(GameCommandException e1)
				{
					showRunningGameCommandExceptionMsg(e1);
				}				
			}
		});
		buildBtnsPanel.add(buildBtn);

		JButton destroyBtn = new JButton();
		destroyBtn.setText("Demolish");
		destroyBtn.setToolTipText("Demolish 1 defense module to free one slot.");
		DemolishCheck checkDemolish = new LocalGame.Demolish(productiveCelestialBody.getName(), buildingType).can(currentLocalGame.getGameBoard());
		checkCommandBtn(destroyBtn, checkDemolish);
		destroyBtn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					currentLocalGame.executeCommand(new LocalGame.Demolish(productiveCelestialBody.getName(), buildingType));
				}
				catch(GameCommandException e1)
				{
					showRunningGameCommandExceptionMsg(e1);
				}				
			}
		});
		buildBtnsPanel.add(destroyBtn);

		getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(buildBtnsPanel);
	}

	private void eraseUnitDetails()
	{
		getRunningGameFleetDetailsContent().setText("");
		getRunningGameFleetDetailsSpecificDetailsPanel().removeAll();

		updateUI();
	}

	private void refreshUnitDetails()
	{
		Unit selectedUnit = getRunningGameCelestialBodyDetailsUnitsList().getSelectedElement();
		if (selectedUnit == null)
		{
			eraseUnitDetails();
			return;
		}

		refreshUnitDetails(selectedUnit);
	}

	private static void showTodoMsg()
	{
		JOptionPane.showMessageDialog(null, "Not implemented yet.", "TODO", JOptionPane.INFORMATION_MESSAGE);
	}

	private void refreshUnitDetails(final Unit unit)
	{
		eraseUnitDetails();

		if (unit == null) return;

		RealLocation unitLocation;
		try
		{
			unitLocation = currentLocalGame.getGameBoard().getUnitLocation(unit.getOwnerName(), unit.getName());
		}
		catch(PlayerGameBoardQueryException e2)
		{
			e2.printStackTrace();
			unitLocation = null;
		}
		if (unitLocation == null) return;

		Area area = currentLocalGame.getGameBoard().getArea(unitLocation);

		if (area == null) return;

		final ICelestialBody celestialBody = area.getCelestialBody();

		ProductiveCelestialBody productiveCelestialBody = null;

		if (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		}

		Planet planet = null;

		if (celestialBody != null && Planet.class.isInstance(celestialBody))
		{
			planet = Planet.class.cast(celestialBody);
		}

		getRunningGameFleetDetailsContent().setText(unit.toString());

		boolean isUnitOwner = (unit.getOwnerName() != null && unit.getOwnerName().compareTo(player.getName()) == 0);

		try
		{
			if (Fleet.class.isInstance(unit))
			{
				Fleet fleet = Fleet.class.cast(unit);

				if (isUnitOwner && fleet.isUnasignedFleet())
				{
					displayStarshipPlantActionPanel();
				}
				else
				{
					if (isUnitOwner)
					{
						// Actions btn	
						JPanel btnsPanel = new JPanel(new FlowLayout());
						btnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

						// Dismantle fleet
						JButton dismantleBtn = new JButton();
						dismantleBtn.setText("Dismantle");
						dismantleBtn.setToolTipText("Dismantle fleet so starships land on plant.");
						DismantleFleetCheck checkDismantleFleet = new DismantleFleet(unit.getName()).can(currentLocalGame.getGameBoard());
						checkCommandBtn(dismantleBtn, checkDismantleFleet);
						dismantleBtn.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e)
							{
								try
								{
									currentLocalGame.executeCommand(new DismantleFleet(unit.getName()));									
								}
								catch(GameCommandException e1)
								{
									showRunningGameCommandExceptionMsg(e1);
								}
							}
						});
						btnsPanel.add(dismantleBtn);

						if (productiveCelestialBody != null)
						{
							// Settle governement fleet
							JButton settleGvnmt = new JButton();
							settleGvnmt.setText("Settle government");
							settleGvnmt.setToolTipText("Settle governement.");
							SettleGovernmentCheck checkSettleGovernment = new SettleGovernment(productiveCelestialBody.getName()).can(currentLocalGame.getGameBoard());
							checkCommandBtn(settleGvnmt, checkSettleGovernment);
							settleGvnmt.addActionListener(new ActionListener()
							{

								@Override
								public void actionPerformed(ActionEvent e)
								{
									try
									{
										currentLocalGame.executeCommand(new SettleGovernment(celestialBody.getName()));
									}
									catch(GameCommandException e1)
									{
										showRunningGameCommandExceptionMsg(e1);
									}
								}
							});
							btnsPanel.add(settleGvnmt);
						}

						getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);

						displayFleetActionPanel();
					}
				}
			}
			else if (AntiProbeMissile.class.isInstance(unit))
			{
				if (isUnitOwner)
				{
					// Actions btn	
					JPanel btnsPanel = new JPanel(new FlowLayout());
					btnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

					// Fire antiprobe missile
					JButton fireBtn = new JButton();
					fireBtn.setText("Fire");
					fireBtn.setToolTipText("Fire anti-probe missile.");
					fireBtn.setEnabled(isUnitOwner);
					fireBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent ae)
						{
							SelectedUnitInfos<AntiProbeMissile> infos = getSelectedUnitInfos(AntiProbeMissile.class);
							if (infos == null || infos.unit == null) return;

							Probe target = getAntiPulsarMissileTargetComboBox().getSelectedElement();
							if (target == null) return;

							try
							{
								FireAntiProbeMissileCheck checkFireAPM = new FireAntiProbeMissile(infos.unit.getName(), target.getOwnerName(), target.getName()).can(currentLocalGame.getGameBoard());								
								if (checkFireAPM.isPossible())
								{
									currentLocalGame.executeCommand(new FireAntiProbeMissile(infos.unit.getName(), target.getOwnerName(), target.getName()));									
								}
								else
								{
									JOptionPane.showMessageDialog(null, "Cannot fire antiprobe missile '" + infos.unit.getName() + "' onto '" + target.toString() + "'\n" + checkFireAPM.getReason(), "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
							catch(GameCommandException e)
							{
								showRunningGameCommandExceptionMsg(e);
							}							
						}
					});

					Set<Probe> probes = currentLocalGame.getGameBoard().getUnits(Probe.class);
					getAntiPulsarMissileTargetComboBox().clear();
					getAntiPulsarMissileTargetComboBox().addAll(probes);

					btnsPanel.add(getAntiPulsarMissileTargetComboBox().getComponent());
					btnsPanel.add(fireBtn);

					getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);
				}
			}
			else if (CarbonCarrier.class.isInstance(unit))
			{
				if (isUnitOwner)
				{

				}
			}
			else if (SpaceRoadDeliverer.class.isInstance(unit))
			{
				if (isUnitOwner)
				{

				}
			}
			else if (LaunchedPulsarMissile.class.isInstance(unit))
			{
				if (isUnitOwner)
				{

				}
			}
			else if (Probe.class.isInstance(unit))
			{
				if (isUnitOwner)
				{
					// Actions btn	
					JPanel btnsPanel = new JPanel(new FlowLayout());
					btnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

					// Launch probe
					btnsPanel.add(getLaunchProbeDestinationXTextField());
					btnsPanel.add(getLaunchProbeDestinationYTextField());
					btnsPanel.add(getLaunchProbeDestinationZTextField());

					JButton launchBtn = new JButton();
					launchBtn.setText("Launch");
					launchBtn.setToolTipText("Launch probe.");
					launchBtn.setEnabled(isUnitOwner);
					launchBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							SelectedUnitInfos<Probe> infos = getSelectedUnitInfos(Probe.class);
							if (infos == null || infos.unit == null) return;

							int x = Basic.intValueOf(getLaunchProbeDestinationXTextField().getText(), -1);
							int y = Basic.intValueOf(getLaunchProbeDestinationYTextField().getText(), -1);
							int z = Basic.intValueOf(getLaunchProbeDestinationZTextField().getText(), -1);

							RealLocation dest = new RealLocation(x + 0.5, y + 0.5, z + 0.5);

							try
							{
								LaunchProbeCheck checkLaunchProbe = new LaunchProbe(infos.unit.getName(), dest).can(currentLocalGame.getGameBoard());
								
								if (!checkLaunchProbe.isPossible())
								{
									JOptionPane.showMessageDialog(null, "Impossible\n" + checkLaunchProbe.getReason(), "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}

								currentLocalGame.executeCommand(new LaunchProbe(infos.unit.getName(), dest));
							}
							catch(GameCommandException e1)
							{
								showRunningGameCommandExceptionMsg(e1);
							}
						}
					});
					btnsPanel.add(launchBtn);

					getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);
				}
			}
			else
			{
				throw new Error(unit.getClass().getSimpleName() + " unit class details display not implemented yet");
			}
		}
		finally
		{
			updateUI();
		}
	}

	private JTextField	launchProbeDestinationXTextField;

	private JTextField getLaunchProbeDestinationXTextField()
	{
		if (launchProbeDestinationXTextField == null)
		{
			launchProbeDestinationXTextField = new JTextField();
			launchProbeDestinationXTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationXTextField;
	}

	private JTextField	launchProbeDestinationYTextField;

	private JTextField getLaunchProbeDestinationYTextField()
	{
		if (launchProbeDestinationYTextField == null)
		{
			launchProbeDestinationYTextField = new JTextField();
			launchProbeDestinationYTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationYTextField;
	}

	private JTextField	launchProbeDestinationZTextField;

	private JTextField getLaunchProbeDestinationZTextField()
	{
		if (launchProbeDestinationZTextField == null)
		{
			launchProbeDestinationZTextField = new JTextField();
			launchProbeDestinationZTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationZTextField;
	}

	private final TypedListWrapper.TypedListElementSelector<Probe>		PROBE_SELECTOR	= new TypedListWrapper.TypedListElementSelector<Probe>()
																						{
																							@Override
																							public boolean equals(Probe o1, Probe o2)
																							{
																								return o1.getOwnerName().equals(o2.getOwnerName()) && o1.getName().equals(o2.getName());
																							}
																						};

	private final TypedListWrapper.AbstractTypedJListCellRender<Probe>	PROBE_RENDERER	= new TypedListWrapper.AbstractTypedJListCellRender<Probe>()
																						{
																							@Override
																							public Component getListCellRendererComponent(JList list, Probe probe, int index, boolean isSelected, boolean cellHasFocus)
																							{
																								super.getListCellRendererComponent(list, probe, index, isSelected, cellHasFocus);
																								label.setText(probe.getClass().getSimpleName() + " [" + ((probe.getOwnerName() != null) ? probe.getOwnerName() : "unknown") + "] " + probe.getName());
																								return label;
																							}
																						};

	private TypedListWrapper<JComboBox, Probe>							antiPulsarMissileTargetComboBox;

	private TypedListWrapper<JComboBox, Probe> getAntiPulsarMissileTargetComboBox()
	{
		if (antiPulsarMissileTargetComboBox == null)
		{
			antiPulsarMissileTargetComboBox = new TypedListWrapper<JComboBox, Probe>(Probe.class, new JComboBox(), PROBE_SELECTOR);

			antiPulsarMissileTargetComboBox.setCellRenderer(PROBE_RENDERER);
		}
		return antiPulsarMissileTargetComboBox;
	}

	private void eraseBuildingDetails()
	{
		getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel().setText("");
		getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().removeAll();

		updateUI();
	}

	private void refreshBuildingDetails()
	{
		Object obj = getRunningGameCelestialBodyDetailsBuildingsList().getSelectedValue();
		if (obj == null)
		{
			eraseBuildingDetails();
			return;
		}

		refreshBuildingDetails(labelToBuildingType(obj.toString()));
	}

	private String buildingTypeToLabel(Class<? extends ABuilding> buildingType)
	{
		return buildingType.getSimpleName() + " (none)";
	}

	private String buildingToLabel(ABuilding building)
	{
		return building.getClass().getSimpleName() + " (" + building.getBuildSlotsCount() + ")";
	}

	private String labelToBuildingType(String label)
	{
		if (label == null) return null;
		return label.substring(0, (label.indexOf(" ") < 0) ? label.length() : label.indexOf(" "));
	}

	private void refreshBuildingDetails(String buildingTypeName)
	{
		eraseBuildingDetails();

		if (currentSelectedArea == null) return;
		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		if (celestialBody == null) return;
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) return;

		final ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		Set<ABuilding> buildings = productiveCelestialBody.getBuildings();
		ABuilding selectedBuildings = null;
		Class<? extends ABuilding> selectedBuildingType = null;

		if (buildings != null) for(ABuilding b : buildings)
		{
			if (b.getClass().getSimpleName().compareTo(buildingTypeName) == 0)
			{
				selectedBuildings = b;
				break;
			}
		}

		if (selectedBuildings == null)
		{
			for(Class<? extends ABuilding> bt : SEPUtils.buildingTypes)
			{
				if (bt.getSimpleName().compareTo(buildingTypeName) == 0)
				{
					selectedBuildingType = bt;
					break;
				}
			}
			if (selectedBuildingType == null) return;

			getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel().setText("No " + selectedBuildingType.getSimpleName() + " build yet.");
		}
		else
		{
			getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel().setText(selectedBuildings.toString());
		}

		try
		{
			// Specific panel
			if (productiveCelestialBody.getOwnerName() != null && productiveCelestialBody.getOwnerName().compareTo(player.getName()) == 0 && (selectedBuildings != null || productiveCelestialBody.canBuildType(selectedBuildingType)))
			{
				if ((selectedBuildings != null && DefenseModule.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && DefenseModule.class.equals(selectedBuildingType)))
				{
					int buildCost;
					int nbBuild;

					if (selectedBuildings != null)
					{
						DefenseModule defenseModule = DefenseModule.class.cast(selectedBuildings);
						buildCost = defenseModule.getUpgradeCarbonCost();
						nbBuild = defenseModule.getBuildSlotsCount();
					}
					else
					{
						buildCost = DefenseModule.FIRST_CARBON_COST;
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, DefenseModule.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && ExtractionModule.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && ExtractionModule.class.equals(selectedBuildingType)))
				{
					int buildCost;
					int nbBuild;

					if (selectedBuildings != null)
					{
						ExtractionModule extractionModule = ExtractionModule.class.cast(selectedBuildings);
						buildCost = extractionModule.getUpgradeCarbonCost();
						nbBuild = extractionModule.getBuildSlotsCount();
					}
					else
					{
						buildCost = ExtractionModule.FIRST_CARBON_COST;
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, ExtractionModule.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && GovernmentModule.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && GovernmentModule.class.equals(selectedBuildingType)))
				{
					Planet planet = Planet.class.cast(productiveCelestialBody);

					if (selectedBuildings != null)
					{
						GovernmentModule governmentModule = GovernmentModule.class.cast(selectedBuildings);

						// Actions btn
						JPanel btnsPanel = new JPanel(new FlowLayout());

						JButton buildBtn = new JButton();
						buildBtn.setText("Embark");
						EmbarkGovernmentCheck checkEmbarkGovernment = new EmbarkGovernment().can(currentLocalGame.getGameBoard());
						checkCommandBtn(buildBtn, checkEmbarkGovernment);
						buildBtn.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e)
							{
								try
								{
									currentLocalGame.executeCommand(new EmbarkGovernment());
								}
								catch(GameCommandException e1)
								{
									showRunningGameCommandExceptionMsg(e1);
								}
							}
						});
						btnsPanel.add(buildBtn);

						getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
					}
					else
					{
						SettleGovernmentCheck checkSettleGovernment = new SettleGovernment(productiveCelestialBody.getName()).can(currentLocalGame.getGameBoard());
						if (checkSettleGovernment.isPossible())
						{
							// Actions btn
							JPanel btnsPanel = new JPanel(new FlowLayout());

							JButton buildBtn = new JButton();
							buildBtn.setText("Settle");
							checkCommandBtn(buildBtn, checkSettleGovernment);
							buildBtn.addActionListener(new ActionListener()
							{

								@Override
								public void actionPerformed(ActionEvent e)
								{
									try
									{
										currentLocalGame.executeCommand(new SettleGovernment(productiveCelestialBody.getName()));										
									}
									catch(GameCommandException e1)
									{
										showRunningGameCommandExceptionMsg(e1);
									}							
								}
							});
							btnsPanel.add(buildBtn);

							getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
						}
					}
				}
				else if ((selectedBuildings != null && PulsarLauchingPad.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && PulsarLauchingPad.class.equals(selectedBuildingType)))
				{
					Planet planet = Planet.class.cast(productiveCelestialBody);

					int unusedCount;

					if (selectedBuildings != null)
					{
						PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(selectedBuildings);
						unusedCount = pulsarLaunchingPad.getUnusedCount();
					}
					else
					{
						unusedCount = 0;
					}

					// Build btn
					JPanel btnsPanel = new JPanel(new FlowLayout());

					JButton buildBtn = new JButton();
					String label = (unusedCount > 0) ? "Upgrade " + unusedCount : "Build";
					buildBtn.setText(label);
					BuildCheck checkBuild = new Build(productiveCelestialBody.getName(), PulsarLauchingPad.class).can(currentLocalGame.getGameBoard());
					checkCommandBtn(buildBtn, checkBuild);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								currentLocalGame.executeCommand(new Build(productiveCelestialBody.getName(), PulsarLauchingPad.class));
							}
							catch(GameCommandException e1)
							{
								showRunningGameCommandExceptionMsg(e1);
							}							
						}
					});
					btnsPanel.add(buildBtn);

					JButton fireBtn = new JButton();
					fireBtn.setText("Fire");					
					checkCommandBtn(fireBtn, null);
					fireBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO (display pulsar launching panel)
							showTodoMsg();
						}
					});
					btnsPanel.add(fireBtn);

					getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
				}
				else if ((selectedBuildings != null && SpaceCounter.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && SpaceCounter.class.equals(selectedBuildingType)))
				{
					int buildCost = SpaceCounter.CARBON_COST;
					int nbBuild;

					if (selectedBuildings != null)
					{
						SpaceCounter spaceCounter = SpaceCounter.class.cast(selectedBuildings);
						nbBuild = spaceCounter.getBuildSlotsCount();
						displaySpaceCounterActionPanel();
					}
					else
					{
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, SpaceCounter.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && StarshipPlant.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && StarshipPlant.class.equals(selectedBuildingType)))
				{
					Planet planet = Planet.class.cast(productiveCelestialBody);

					int nbBuild;
					int[] buildCosts = new int[] { StarshipPlant.CARBON_COST, StarshipPlant.POPULATION_COST };

					if (selectedBuildings != null)
					{
						StarshipPlant starshipPlant = StarshipPlant.class.cast(selectedBuildings);
						nbBuild = starshipPlant.getBuildSlotsCount();
						displayStarshipPlantActionPanel();
					}
					else
					{
						nbBuild = 0;
					}

					// Actions btn
					addBuildBtns(productiveCelestialBody, StarshipPlant.class, buildCosts, nbBuild);
				}
			}
		}
		catch(StateMachineNotExpectedEventException e1)
		{
			e1.printStackTrace();
		}
		catch(RpcException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			updateUI();
		}
	}

	private void refreshStarshipPlantActionPanel()
	{
		SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
		if (infos == null || infos.productiveCelestialBody == null) return;

		getStarshipPlantWorkshopTitleLabel().setText("Available starships on " + infos.productiveCelestialBody.getName());

		int availableQt, toMake, toFleet;
		int makeCarbonPrice = 0, makePopulationPrice = 0;
		int carbonPrice, populationPrice;

		Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();
		Map<StarshipTemplate, Integer> fleetToFormStarships = new HashMap<StarshipTemplate, Integer>();
		Set<String> fleetToFormSpecialUnits = new HashSet<String>();

		Set<StarshipTemplate> starshipTemplates = new TreeSet<StarshipTemplate>();
		starshipTemplates.addAll(SEPUtils.starshipTypes);
		if (infos.productiveCelestialBody.getUnasignedStarships() != null) starshipTemplates.addAll(infos.productiveCelestialBody.getUnasignedStarships().keySet());

		for(StarshipTemplate starshipType : starshipTemplates)
		{
			if (infos.productiveCelestialBody.getUnasignedStarships() == null)
			{
				availableQt = 0;
			}
			else
			{
				availableQt = (infos.productiveCelestialBody.getUnasignedStarships().get(starshipType) == null) ? 0 : infos.productiveCelestialBody.getUnasignedStarships().get(starshipType);
			}

			toMake = Basic.intValueOf(getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).getText(), 0);
			toFleet = Basic.intValueOf(getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).getText(), 0);

			carbonPrice = starshipType.getCarbonPrice();
			populationPrice = starshipType.getPopulationPrice();

			makeCarbonPrice += toMake * carbonPrice;
			makePopulationPrice += toMake * populationPrice;

			starshipsToMake.put(starshipType, toMake);
			fleetToFormStarships.put(starshipType, toFleet);

			// Update display			
			getStarshipPlantWorkshopStarshipQtLabel(starshipType).setText(String.format("%d (%d)", availableQt, availableQt - toFleet));
			getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).setText(String.valueOf(toMake));
			getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).setText(String.valueOf(toFleet));

		}

		if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null) for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
		{
			if (specialUnit == null) continue;

			availableQt = 1;

			toFleet = Basic.intValueOf(getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit).getText(), 0);

			if (toFleet > 0)
			{
				toFleet = 1;
				fleetToFormSpecialUnits.add(specialUnit.getName());
			}

			// Update display			
			getStarshipPlantWorkshopSpecialUnitQtLabel(specialUnit).setText(String.format("%d (%d)", availableQt, availableQt - toFleet));
			getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit).setText(String.valueOf(toFleet));
		}

		int probeToMake = Basic.intValueOf(getProbeMakeQtTextField().getText(), 0);
		int antiProbeMissileToMake = Basic.intValueOf(getAntiProbeMissileMakeQtTextField().getText(), 0);

		String probeName = getProbeNamePrefixTextField().getText();
		if (probeName == null || probeName.isEmpty())
		{
			probeName = Unit.generateName();
			getProbeNamePrefixTextField().setText(probeName);
		}

		String antiProbeMissileName = getAntiProbeMissileNamePrefixTextField().getText();
		if (antiProbeMissileName == null || antiProbeMissileName.isEmpty())
		{
			antiProbeMissileName = Unit.generateName();
			getAntiProbeMissileNamePrefixTextField().setText(antiProbeMissileName);
		}

		String fleetName = getStarshipPlantNewFleetNameTextField().getText();
		if (fleetName == null || fleetName.isEmpty())
		{
			fleetName = Unit.generateName();
			getStarshipPlantNewFleetNameTextField().setText(fleetName);
		}

		getStarshipPlantWorkshopMakeStarshipBtn().setToolTipText("Make starships for " + makeCarbonPrice + "c and " + makePopulationPrice + "pop.");
		try
		{
			MakeStarshipsCheck checkMakeStarships = new MakeStarships(infos.productiveCelestialBody.getName(), starshipsToMake).can(currentLocalGame.getGameBoard());
			checkCommandBtn(getStarshipPlantWorkshopMakeStarshipBtn(), checkMakeStarships);

			FormFleetCheck checkFormFleet = new FormFleet(infos.productiveCelestialBody.getName(), fleetName, fleetToFormStarships, fleetToFormSpecialUnits).can(currentLocalGame.getGameBoard());
			checkCommandBtn(getStarshipPlantFormFleetBtn(), checkFormFleet);

			MakeProbesCheck checkMakeProbe = new MakeProbes(infos.productiveCelestialBody.getName(), probeName, probeToMake).can(currentLocalGame.getGameBoard());			
			checkCommandBtn(getStarshipPlantWorkshopMakeProbeBtn(), checkMakeProbe);

			MakeAntiProbeMissilesCheck checkMakeAntiProbeMissiles = new MakeAntiProbeMissiles(infos.productiveCelestialBody.getName(), antiProbeMissileName, antiProbeMissileToMake).can(currentLocalGame.getGameBoard());
			checkCommandBtn(getStarshipPlantWorkshopMakeAntiProbeMissileBtn(), checkMakeAntiProbeMissiles);
		}		
		finally
		{
			updateUI();
		}
	}

	FocusListener	starshipPlantActionFocusListener;

	FocusListener getStarshipPlantActionFocusListener()
	{
		if (starshipPlantActionFocusListener == null)
		{
			starshipPlantActionFocusListener = new FocusAdapter()
			{
				@Override
				public void focusLost(FocusEvent arg0)
				{
					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							refreshStarshipPlantActionPanel();
						}
					});
				}

			};
		}
		return starshipPlantActionFocusListener;
	}

	private JButton	starshipPlantFormFleetBtn;

	private JButton getStarshipPlantFormFleetBtn()
	{
		if (starshipPlantFormFleetBtn == null)
		{
			starshipPlantFormFleetBtn = new JButton("Form fleet");
			starshipPlantFormFleetBtn.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
					if (infos == null || infos.productiveCelestialBody == null) return;

					Map<StarshipTemplate, Integer> fleetToFormStarships = new HashMap<StarshipTemplate, Integer>();
					Set<String> fleetToFormSpecialUnits = new HashSet<String>();

					Set<StarshipTemplate> starshipTemplates = new TreeSet<StarshipTemplate>();
					starshipTemplates.addAll(SEPUtils.starshipTypes);
					if (infos.productiveCelestialBody.getUnasignedStarships() != null) starshipTemplates.addAll(infos.productiveCelestialBody.getUnasignedStarships().keySet());

					for(StarshipTemplate starshipType : starshipTemplates)
					{
						int fleetToFormQt = Basic.intValueOf(getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).getText(), 0);
						if (fleetToFormQt > 0) fleetToFormStarships.put(starshipType, fleetToFormQt);
					}

					if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null) for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
					{
						int fleetToFormQt = Basic.intValueOf(getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit).getText(), 0);
						if (fleetToFormQt > 0) fleetToFormSpecialUnits.add(specialUnit.getName());
					}

					try
					{
						currentLocalGame.executeCommand(new FormFleet(infos.productiveCelestialBody.getName(), getStarshipPlantNewFleetNameTextField().getText(), fleetToFormStarships, fleetToFormSpecialUnits));
	
						for(StarshipTemplate starshipType : starshipTemplates)
						{
							getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).setText("0");
						}

						if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null) for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
						{
							getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit).setText("0");
						}

						getStarshipPlantNewFleetNameTextField().setText(Unit.generateName());
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return starshipPlantFormFleetBtn;
	}

	private JButton	starshipPlantWorkshipMakeAntiProbeMissileBtn;

	private JButton getStarshipPlantWorkshopMakeAntiProbeMissileBtn()
	{
		if (starshipPlantWorkshipMakeAntiProbeMissileBtn == null)
		{
			starshipPlantWorkshipMakeAntiProbeMissileBtn = new JButton("Make");
			starshipPlantWorkshipMakeAntiProbeMissileBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
					if (infos == null || infos.planet == null) return;

					int antiProbeMissileToMake = Basic.intValueOf(getAntiProbeMissileMakeQtTextField().getText(), 0);

					String antiProbeMissileBaseName = getAntiProbeMissileNamePrefixTextField().getText();

					try
					{
						if (antiProbeMissileToMake > 0 && antiProbeMissileBaseName != null && !antiProbeMissileBaseName.isEmpty())
						{
							currentLocalGame.executeCommand(new MakeAntiProbeMissiles(infos.planet.getName(), antiProbeMissileBaseName, antiProbeMissileToMake));

							getAntiProbeMissileMakeQtTextField().setText("0");
						}
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return starshipPlantWorkshipMakeAntiProbeMissileBtn;
	}

	private JButton	starshipPlantWorkshopMakeProbeBtn;

	private JButton getStarshipPlantWorkshopMakeProbeBtn()
	{
		if (starshipPlantWorkshopMakeProbeBtn == null)
		{
			starshipPlantWorkshopMakeProbeBtn = new JButton("Make");
			starshipPlantWorkshopMakeProbeBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
					if (infos == null || infos.planet == null) return;

					int probeToMake = Basic.intValueOf(getProbeMakeQtTextField().getText(), 0);

					String probeBaseName = getProbeNamePrefixTextField().getText();

					try
					{
						if (probeToMake > 0 && probeBaseName != null && !probeBaseName.isEmpty())
						{
							currentLocalGame.executeCommand(new MakeProbes(infos.planet.getName(), probeBaseName, probeToMake));

							getProbeMakeQtTextField().setText("0");
						}
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return starshipPlantWorkshopMakeProbeBtn;
	}

	private JButton	starshipPlantWorkshipMakeStarshipBtn;

	private JButton getStarshipPlantWorkshopMakeStarshipBtn()
	{
		if (starshipPlantWorkshipMakeStarshipBtn == null)
		{
			starshipPlantWorkshipMakeStarshipBtn = new JButton("Make");
			starshipPlantWorkshipMakeStarshipBtn.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
					if (infos == null || infos.building == null) return;

					Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();

					for(StarshipTemplate starshipType : SEPUtils.starshipTypes)
					{
						int toMake = Basic.intValueOf(getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).getText(), 0);
						if (toMake > 0) starshipsToMake.put(starshipType, toMake);
					}

					try
					{
						currentLocalGame.executeCommand(new MakeStarships(infos.planet.getName(), starshipsToMake));

						for(StarshipTemplate starshipType : SEPUtils.starshipTypes)
						{
							getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).setText("0");
						}
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return starshipPlantWorkshipMakeStarshipBtn;
	}

	private static class SelectedBuildingInfos<B extends ABuilding>
	{
		final Planet					planet;
		final ProductiveCelestialBody	productiveCelestialBody;
		final B							building;

		public SelectedBuildingInfos(ProductiveCelestialBody productiveCelestialBody, B building)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			if (Planet.class.isInstance(productiveCelestialBody))
			{
				this.planet = Planet.class.cast(productiveCelestialBody);
			}
			else
			{
				this.planet = null;
			}

			this.building = building;
		}
	}

	private <B extends ABuilding> SelectedBuildingInfos<B> getSelectedBuildingInfos(Class<B> buildingType)
	{
		if (currentSelectedArea == null) return null;
		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		if (celestialBody == null) return null;
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) return null;
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		B building = null;
		Set<ABuilding> buildings = productiveCelestialBody.getBuildings();
		if (buildings != null) for(ABuilding b : buildings)
		{
			if (buildingType.isInstance(b))
			{
				building = buildingType.cast(b);
				break;
			}
		}
		//if (building == null) return null;

		return new SelectedBuildingInfos<B>(productiveCelestialBody, building);
	}

	private static class SelectedUnitInfos<U extends Unit>
	{
		final Planet					planet;
		final ProductiveCelestialBody	productiveCelestialBody;
		final U							unit;

		public SelectedUnitInfos(ProductiveCelestialBody productiveCelestialBody, U unit)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			if (Planet.class.isInstance(productiveCelestialBody))
			{
				this.planet = Planet.class.cast(productiveCelestialBody);
			}
			else
			{
				this.planet = null;
			}

			this.unit = unit;
		}
	}

	private <U extends Unit> SelectedUnitInfos<U> getSelectedUnitInfos(Class<U> unitTye)
	{
		Unit unit = getRunningGameCelestialBodyDetailsUnitsList().getSelectedElement();
		if (unit == null) return null;
		if (!unitTye.isInstance(unit)) return null;

		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		ProductiveCelestialBody productiveCelestialBody = null;
		if (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		}

		return new SelectedUnitInfos<U>(productiveCelestialBody, unitTye.cast(unit));
	}

	private JScrollPane	starshipPlantActionScrollPane;

	private JScrollPane getStarshipPlantActionScrollPane()
	{
		if (starshipPlantActionScrollPane == null)
		{
			starshipPlantActionScrollPane = new JScrollPane();
		}
		return starshipPlantActionScrollPane;
	}

	private void displaySpaceCounterActionPanel()
	{
		SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
		if (infos == null) return;

		JPanel spaceCounterActionPanel = new JPanel(null);

		spaceCounterActionPanel.add(getSpaceRoadsLabel());
		spaceCounterActionPanel.add(getSpaceRoadsScrollPane());
		spaceCounterActionPanel.add(getSpaceRoadCreateLabel());
		spaceCounterActionPanel.add(getSpaceRoadDestinationComboBox().getComponent());
		spaceCounterActionPanel.add(getSpaceRoadCreateButton());
		spaceCounterActionPanel.add(getSpaceRoadsRemoveButton());

		spaceCounterActionPanel.add(getNextCarbonOrdersLabel());
		spaceCounterActionPanel.add(getNextCarbonOrdersScrollPane());
		spaceCounterActionPanel.add(getCarbonFreightLabel());
		spaceCounterActionPanel.add(getCarbonFreightDestinationComboBox().getComponent());
		spaceCounterActionPanel.add(getCarbonFreightAmountTextField());
		spaceCounterActionPanel.add(getCarbonFreightAutomatedOrderCheckBox());
		spaceCounterActionPanel.add(getCarbonFreightAddButton());
		spaceCounterActionPanel.add(getCarbonFreightDownButton());
		spaceCounterActionPanel.add(getCarbonFreightRemoveButton());
		spaceCounterActionPanel.add(getCarbonFreightUpButton());

		spaceCounterActionPanel.add(getCurrentCarbonOrdersLabel());
		spaceCounterActionPanel.add(getCurrentCarbonOrdersScrollPane());
		spaceCounterActionPanel.add(getCarbonOrdersToReceiveLabel());
		spaceCounterActionPanel.add(getCarbonOrdersToReceiveScrollPane());

		spaceCounterActionPanel.setMinimumSize(new Dimension(getCarbonOrdersToReceiveLabel().getX() + getCarbonOrdersToReceiveLabel().getWidth() + 20, getCarbonOrdersToReceiveScrollPane().getY() + getCarbonOrdersToReceiveScrollPane().getHeight() + 20));
		spaceCounterActionPanel.setPreferredSize(spaceCounterActionPanel.getMinimumSize());

		getSpaceCounterActionScrollPane().setViewportView(spaceCounterActionPanel);

		getActionPanel().removeAll();
		getActionPanel().add(getSpaceCounterActionScrollPane(), BorderLayout.CENTER);

		getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), infos.productiveCelestialBody.getName() + " " + SpaceCounter.class.getSimpleName());

		refreshSpaceCounterActionPanel();

		// TODO

		// client.getRunningGameInterface().buildSpaceRoad(celestialBodyNameA, celestialBodyNameB);
		// client.getRunningGameInterface().demolishSpaceRoad(celestialBodyNameA, celestialBodyNameB);
		// client.getRunningGameInterface().modifyCarbonOrder(originCelestialBodyName, destinationCelestialBodyName, amount)

	}

	private void refreshSpaceCounterActionPanel()
	{
		SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
		if (infos == null || infos.building == null) return;

		Set<ProductiveCelestialBody> possiblesDestinations = currentLocalGame.getGameBoard().getCelestialBodiesWithBuilding(SpaceCounter.class);

		getSpaceRoadsList().clear();
		getSpaceRoadsList().addAll(infos.building.getSpaceRoadsBuilt());
		getSpaceRoadsList().addAll(infos.building.getSpaceRoadsLinked());

		getSpaceRoadDestinationComboBox().clear();
		getSpaceRoadDestinationComboBox().addAll(possiblesDestinations);

		getNextCarbonOrdersList().clear();
		getNextCarbonOrdersList().addAll(infos.building.getNextCarbonOrders());

		getCarbonFreightDestinationComboBox().clear();
		getCarbonFreightDestinationComboBox().addAll(possiblesDestinations);

		getCurrentCarbonOrdersList().clear();
		getCurrentCarbonOrdersList().addAll(infos.building.getCurrentCarbonOrders());

		getCarbonOrdersToReceiveList().clear();
		getCarbonOrdersToReceiveList().addAll(infos.building.getCarbonOrdersToReceive());

		getCarbonFreightLabel().setText("Carbon freight " + infos.building.getCurrentCarbonFreight() + " / " + infos.building.getMaxCarbonFreight());

		updateUI();
	}

	private JScrollPane	spaceCounterActionScrollPane;

	private JScrollPane getSpaceCounterActionScrollPane()
	{
		if (spaceCounterActionScrollPane == null)
		{
			spaceCounterActionScrollPane = new JScrollPane();
		}
		return spaceCounterActionScrollPane;
	}

	private JLabel	spaceRoadsLabel;

	private JLabel getSpaceRoadsLabel()
	{
		if (spaceRoadsLabel == null)
		{
			spaceRoadsLabel = new JLabel("Space roads");
			spaceRoadsLabel.setBounds(0, 0, 200, 20);
		}
		return spaceRoadsLabel;
	}

	private JScrollPane	spaceRoadsScrollPane;

	private JScrollPane getSpaceRoadsScrollPane()
	{
		if (spaceRoadsScrollPane == null)
		{
			spaceRoadsScrollPane = new JScrollPane(getSpaceRoadsList().getComponent());
			spaceRoadsScrollPane.setPreferredSize(new Dimension(200, getSpaceRoadsList().getComponent().getPreferredScrollableViewportSize().height));
			spaceRoadsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			spaceRoadsScrollPane.setBounds(getSpaceRoadsLabel().getX(), getSpaceRoadsLabel().getY() + getSpaceRoadsLabel().getHeight(), getSpaceRoadsLabel().getWidth(), 100);
		}
		return spaceRoadsScrollPane;
	}

	private final TypedListWrapper.TypedListElementSelector<SpaceRoad>	SPACEROAD_SELECTOR	= new TypedListWrapper.TypedListElementSelector<SpaceRoad>()
																							{
																								@Override
																								public boolean equals(SpaceRoad o1, SpaceRoad o2)
																								{
																									return o1.getDestination().equals(o2.getDestination());
																								}
																							};

	private TypedListWrapper<JList, SpaceRoad>							spaceRoadsList;

	private TypedListWrapper<JList, SpaceRoad> getSpaceRoadsList()
	{
		if (spaceRoadsList == null)
		{
			spaceRoadsList = new TypedListWrapper<JList, SpaceRoad>(SpaceRoad.class, new JList(), SPACEROAD_SELECTOR);

			spaceRoadsList.setCellRenderer(new TypedListWrapper.AbstractTypedJListCellRender());
			spaceRoadsList.getComponent().setVisibleRowCount(3);
			spaceRoadsList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return spaceRoadsList;
	}

	private JButton	SpaceRoadsRemoveButton;

	private JButton getSpaceRoadsRemoveButton()
	{
		if (SpaceRoadsRemoveButton == null)
		{
			SpaceRoadsRemoveButton = new JButton("Demolish");
			SpaceRoadsRemoveButton.setBounds(getSpaceRoadsScrollPane().getX(), getSpaceRoadsScrollPane().getY() + getSpaceRoadsScrollPane().getHeight(), getSpaceRoadsScrollPane().getWidth(), getSpaceRoadCreateLabel().getHeight());
			SpaceRoadsRemoveButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.productiveCelestialBody == null || infos.building == null) return;

					SpaceRoad spaceRoad = getSpaceRoadsList().getSelectedElement();
					if (spaceRoad == null) return;

					try
					{
						currentLocalGame.executeCommand(new DemolishSpaceRoad(infos.productiveCelestialBody.getName(), (infos.productiveCelestialBody.getName().compareTo(spaceRoad.getSource()) == 0 ? spaceRoad.getDestination() : spaceRoad.getSource())));
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return SpaceRoadsRemoveButton;
	}

	private JLabel	spaceRoadCreateLabel;

	private JLabel getSpaceRoadCreateLabel()
	{
		if (spaceRoadCreateLabel == null)
		{
			spaceRoadCreateLabel = new JLabel("New space road destination");
			spaceRoadCreateLabel.setBounds(getSpaceRoadsScrollPane().getX() + getSpaceRoadsScrollPane().getWidth() + 10, getSpaceRoadsScrollPane().getY(), 200, 20);
		}
		return spaceRoadCreateLabel;
	}

	private final TypedListWrapper.TypedListElementSelector<ProductiveCelestialBody>		PRODUCTIVE_CELESTIAL_BODY_SELECTOR	= new TypedListWrapper.TypedListElementSelector<ProductiveCelestialBody>()
																																{
																																	public boolean equals(ProductiveCelestialBody o1, ProductiveCelestialBody o2)
																																	{
																																		return o1.getName().equals(o2.getName());
																																	};
																																};

	private final TypedListWrapper.AbstractTypedJListCellRender<ProductiveCelestialBody>	PRODUCTIVE_CELESTIAL_BODY_RENDERER	= new TypedListWrapper.AbstractTypedJListCellRender<ProductiveCelestialBody>()
																																{
																																	@Override
																																	public Component getListCellRendererComponent(JList list, ProductiveCelestialBody value, int index, boolean isSelected, boolean cellHasFocus)
																																	{
																																		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
																																		label.setText("[" + value.getOwnerName() + "] " + value.getName());
																																		return label;
																																	}
																																};

	private TypedListWrapper<JComboBox, ProductiveCelestialBody>							spaceRoadDestinationComboBox;

	private TypedListWrapper<JComboBox, ProductiveCelestialBody> getSpaceRoadDestinationComboBox()
	{
		if (spaceRoadDestinationComboBox == null)
		{
			spaceRoadDestinationComboBox = new TypedListWrapper<JComboBox, ProductiveCelestialBody>(ProductiveCelestialBody.class, new JComboBox(), PRODUCTIVE_CELESTIAL_BODY_SELECTOR);

			spaceRoadDestinationComboBox.setCellRenderer(PRODUCTIVE_CELESTIAL_BODY_RENDERER);

			spaceRoadDestinationComboBox.addAll(currentLocalGame.getGameBoard().getCelestialBodiesWithBuilding(SpaceCounter.class));

			spaceRoadDestinationComboBox.getComponent().setBounds(getSpaceRoadCreateLabel().getX(), getSpaceRoadCreateLabel().getY() + getSpaceRoadCreateLabel().getHeight(), (int) (getSpaceRoadCreateLabel().getWidth() * 0.3), getSpaceRoadCreateLabel().getHeight());
		}
		return spaceRoadDestinationComboBox;
	}

	private JButton	spaceRoadCreateButton;

	private JButton getSpaceRoadCreateButton()
	{
		if (spaceRoadCreateButton == null)
		{
			spaceRoadCreateButton = new JButton("Create");
			spaceRoadCreateButton.setBounds(getSpaceRoadDestinationComboBox().getComponent().getX() + getSpaceRoadDestinationComboBox().getComponent().getWidth() + 2, getSpaceRoadDestinationComboBox().getComponent().getY(), getSpaceRoadCreateLabel().getWidth() - getSpaceRoadDestinationComboBox().getComponent().getWidth() - 4, getSpaceRoadDestinationComboBox().getComponent().getHeight());
			spaceRoadCreateButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;

					ProductiveCelestialBody selectedSpaceRoadDestination = getSpaceRoadDestinationComboBox().getSelectedElement();
					if (selectedSpaceRoadDestination == null) return;

					try
					{
						currentLocalGame.executeCommand(new BuildSpaceRoad(infos.productiveCelestialBody.getName(), selectedSpaceRoadDestination.getName()));
					}					
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return spaceRoadCreateButton;
	}

	private JLabel	nextCarbonOrdersLabel;

	private JLabel getNextCarbonOrdersLabel()
	{
		if (nextCarbonOrdersLabel == null)
		{
			nextCarbonOrdersLabel = new JLabel("Next carbon orders");
			nextCarbonOrdersLabel.setBounds(getSpaceRoadsLabel().getX(), getSpaceRoadsRemoveButton().getY() + getSpaceRoadsRemoveButton().getHeight() + 10, getSpaceRoadsLabel().getWidth(), getSpaceRoadsLabel().getHeight());
		}
		return nextCarbonOrdersLabel;
	}

	private JScrollPane	nextCarbonOrdersScrollPane;

	private JScrollPane getNextCarbonOrdersScrollPane()
	{
		if (nextCarbonOrdersScrollPane == null)
		{
			nextCarbonOrdersScrollPane = new JScrollPane();
			nextCarbonOrdersScrollPane = new JScrollPane(getNextCarbonOrdersList().getComponent());
			nextCarbonOrdersScrollPane.setPreferredSize(new Dimension(200, getNextCarbonOrdersList().getComponent().getPreferredScrollableViewportSize().height));
			nextCarbonOrdersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			nextCarbonOrdersScrollPane.setBounds(getNextCarbonOrdersLabel().getX(), getNextCarbonOrdersLabel().getY() + getNextCarbonOrdersLabel().getHeight(), getNextCarbonOrdersLabel().getWidth(), getSpaceRoadsScrollPane().getHeight());
		}
		return nextCarbonOrdersScrollPane;
	}

	private final TypedListWrapper.TypedListElementSelector<CarbonOrder>		CARBONORDER_SELECTOR	= new TypedListWrapper.TypedListElementSelector<CarbonOrder>()
																										{
																											public boolean equals(CarbonOrder o1, CarbonOrder o2)
																											{
																												return o1.getDestinationName().equals(o2.getDestinationName()) && o1.getAmount() == o2.getAmount();
																											}
																										};

	private final TypedListWrapper.AbstractTypedJListCellRender<CarbonOrder>	CARBONORDER_RENDERER	= new TypedListWrapper.AbstractTypedJListCellRender<CarbonOrder>()
																										{
																											@Override
																											public Component getListCellRendererComponent(JList list, CarbonOrder value, int index, boolean isSelected, boolean cellHasFocus)
																											{
																												super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
																												label.setText(value.getDestinationName() + " : " + value.getAmount() + (value.isAutomated() ? " (auto)" : ""));
																												return label;
																											}
																										};

	private TypedListWrapper<JList, CarbonOrder>								nextCarbonOrdersList;

	private TypedListWrapper<JList, CarbonOrder> getNextCarbonOrdersList()
	{
		if (nextCarbonOrdersList == null)
		{
			nextCarbonOrdersList = new TypedListWrapper<JList, CarbonOrder>(CarbonOrder.class, new JList(), CARBONORDER_SELECTOR);

			nextCarbonOrdersList.getComponent().setVisibleRowCount(3);
			nextCarbonOrdersList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			nextCarbonOrdersList.setCellRenderer(CARBONORDER_RENDERER);
		}
		return nextCarbonOrdersList;
	}

	private JLabel	carbonFreightLabel;

	private JLabel getCarbonFreightLabel()
	{
		if (carbonFreightLabel == null)
		{
			carbonFreightLabel = new JLabel("Carbon freight ?? / ??");
			carbonFreightLabel.setBounds(getNextCarbonOrdersLabel().getX() + getNextCarbonOrdersLabel().getWidth() + 10, getNextCarbonOrdersScrollPane().getY(), 200, 20);
		}
		return carbonFreightLabel;
	}

	private TypedListWrapper<JComboBox, ProductiveCelestialBody>	carbonFreightDestinationComboBox;

	private TypedListWrapper<JComboBox, ProductiveCelestialBody> getCarbonFreightDestinationComboBox()
	{
		if (carbonFreightDestinationComboBox == null)
		{
			carbonFreightDestinationComboBox = new TypedListWrapper<JComboBox, ProductiveCelestialBody>(ProductiveCelestialBody.class, new JComboBox(), PRODUCTIVE_CELESTIAL_BODY_SELECTOR);

			carbonFreightDestinationComboBox.setCellRenderer(PRODUCTIVE_CELESTIAL_BODY_RENDERER);

			carbonFreightDestinationComboBox.addAll(currentLocalGame.getGameBoard().getCelestialBodiesWithBuilding(SpaceCounter.class));
			carbonFreightDestinationComboBox.getComponent().setBounds(getCarbonFreightLabel().getX(), getCarbonFreightLabel().getY() + getCarbonFreightLabel().getHeight(), (int) (getCarbonFreightLabel().getWidth() * 0.6), getCarbonFreightLabel().getHeight());
		}
		return carbonFreightDestinationComboBox;
	}

	private JTextField	carbonFreightAmountTextField;

	private JTextField getCarbonFreightAmountTextField()
	{
		if (carbonFreightAmountTextField == null)
		{
			carbonFreightAmountTextField = new JTextField();
			carbonFreightAmountTextField.setBounds(getCarbonFreightDestinationComboBox().getComponent().getX() + getCarbonFreightDestinationComboBox().getComponent().getWidth() + 2, getCarbonFreightDestinationComboBox().getComponent().getY(), getCarbonFreightLabel().getWidth() - getCarbonFreightDestinationComboBox().getComponent().getWidth() - 4, getCarbonFreightDestinationComboBox().getComponent().getHeight());
		}
		return carbonFreightAmountTextField;
	}

	private JCheckBox	carbonFreightAutomatedOrderCheckBox;

	private JCheckBox getCarbonFreightAutomatedOrderCheckBox()
	{
		if (carbonFreightAutomatedOrderCheckBox == null)
		{
			carbonFreightAutomatedOrderCheckBox = new JCheckBox("Repeat automatically");
			carbonFreightAutomatedOrderCheckBox.setBounds(getCarbonFreightLabel().getX(), getCarbonFreightAmountTextField().getY() + getCarbonFreightAmountTextField().getHeight() + 2, getCarbonFreightLabel().getWidth(), getCarbonFreightLabel().getHeight());
		}
		return carbonFreightAutomatedOrderCheckBox;
	}

	private JButton	carbonFreightAddButton;

	private JButton getCarbonFreightAddButton()
	{
		if (carbonFreightAddButton == null)
		{
			carbonFreightAddButton = new JButton("Add");
			carbonFreightAddButton.setBounds(getCarbonFreightLabel().getX(), getCarbonFreightAutomatedOrderCheckBox().getY() + getCarbonFreightAutomatedOrderCheckBox().getHeight() + 2, getCarbonFreightLabel().getWidth(), getCarbonFreightLabel().getHeight());
			carbonFreightAddButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;

					ProductiveCelestialBody selectedCarbonFreightDestination = getCarbonFreightDestinationComboBox().getSelectedElement();
					if (selectedCarbonFreightDestination == null) return;

					int amount = Basic.intValueOf(getCarbonFreightAmountTextField().getText(), -1);
					boolean automated = getCarbonFreightAutomatedOrderCheckBox().isSelected();

					CarbonOrder newCarbonOrder = new CarbonOrder(infos.productiveCelestialBody.getName(), selectedCarbonFreightDestination.getName(), amount, automated);

					Stack<CarbonOrder> orders = new Stack<CarbonOrder>();
					orders.addAll(getNextCarbonOrdersList());
					orders.add(newCarbonOrder);

					try
					{
						currentLocalGame.executeCommand(new ModifyCarbonOrder(infos.productiveCelestialBody.getName(), orders));
						getNextCarbonOrdersList().add(newCarbonOrder);
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return carbonFreightAddButton;
	}

	private JButton	carbonFreightUpButton;

	private JButton getCarbonFreightUpButton()
	{
		if (carbonFreightUpButton == null)
		{
			carbonFreightUpButton = new JButton("Up");
			carbonFreightUpButton.setBounds(getNextCarbonOrdersScrollPane().getX(), getNextCarbonOrdersScrollPane().getY() + getNextCarbonOrdersScrollPane().getHeight(), 60, getCarbonFreightLabel().getHeight());
			carbonFreightUpButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;

					int selectedIndex = getNextCarbonOrdersList().getSelectedIndex();
					if (selectedIndex <= 0) return;

					CarbonOrder movedCarbonOrder = getNextCarbonOrdersList().getSelectedElement();

					Stack<CarbonOrder> orders = new Stack<CarbonOrder>();
					orders.addAll(getNextCarbonOrdersList());
					orders.remove(selectedIndex);
					orders.add(selectedIndex - 1, movedCarbonOrder);

					try
					{
						currentLocalGame.executeCommand(new ModifyCarbonOrder(infos.productiveCelestialBody.getName(), orders));
						getNextCarbonOrdersList().remove(selectedIndex);
						getNextCarbonOrdersList().add(selectedIndex - 1, movedCarbonOrder);
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return carbonFreightUpButton;
	}

	private JButton	carbonFreightDownButton;

	private JButton getCarbonFreightDownButton()
	{
		if (carbonFreightDownButton == null)
		{
			carbonFreightDownButton = new JButton("Dn");
			carbonFreightDownButton.setBounds(getNextCarbonOrdersScrollPane().getX() + getNextCarbonOrdersScrollPane().getWidth() - getCarbonFreightUpButton().getWidth(), getCarbonFreightUpButton().getY(), getCarbonFreightUpButton().getWidth(), getCarbonFreightUpButton().getHeight());
			carbonFreightDownButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;

					int selectedIndex = getNextCarbonOrdersList().getSelectedIndex();
					if (selectedIndex < 0) return;
					if (selectedIndex + 1 >= getNextCarbonOrdersList().size()) return;

					CarbonOrder movedCarbonOrder = getNextCarbonOrdersList().getSelectedElement();

					Stack<CarbonOrder> orders = new Stack<CarbonOrder>();
					orders.addAll(getNextCarbonOrdersList());
					orders.remove(selectedIndex);
					orders.add(selectedIndex + 1, movedCarbonOrder);

					try
					{
						currentLocalGame.executeCommand(new ModifyCarbonOrder(infos.productiveCelestialBody.getName(), orders));
						getNextCarbonOrdersList().remove(selectedIndex);
						getNextCarbonOrdersList().add(selectedIndex + 1, movedCarbonOrder);
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return carbonFreightDownButton;
	}

	private JButton	carbonFreightRemoveButton;

	private JButton getCarbonFreightRemoveButton()
	{
		if (carbonFreightRemoveButton == null)
		{
			carbonFreightRemoveButton = new JButton("Rem");
			carbonFreightRemoveButton.setBounds(getNextCarbonOrdersScrollPane().getX() + (getNextCarbonOrdersScrollPane().getWidth() / 2) - (getCarbonFreightUpButton().getWidth() / 2), getCarbonFreightUpButton().getY(), getCarbonFreightUpButton().getWidth(), getCarbonFreightUpButton().getHeight());
			carbonFreightRemoveButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;

					int selectedIndex = getNextCarbonOrdersList().getSelectedIndex();
					if (selectedIndex < 0) return;

					Stack<CarbonOrder> orders = new Stack<CarbonOrder>();
					orders.addAll(getNextCarbonOrdersList());
					orders.remove(selectedIndex);

					try
					{
						currentLocalGame.executeCommand(new ModifyCarbonOrder(infos.productiveCelestialBody.getName(), orders));
						getNextCarbonOrdersList().remove(selectedIndex);
					}
					catch(GameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
				}
			});
		}
		return carbonFreightRemoveButton;
	}

	private JLabel	currentCarbonOrdersLabel;

	private JLabel getCurrentCarbonOrdersLabel()
	{
		if (currentCarbonOrdersLabel == null)
		{
			currentCarbonOrdersLabel = new JLabel("Current carbon sent orders");
			currentCarbonOrdersLabel.setBounds(getNextCarbonOrdersLabel().getX(), getCarbonFreightRemoveButton().getY() + getCarbonFreightRemoveButton().getHeight() + 10, getNextCarbonOrdersScrollPane().getWidth(), getNextCarbonOrdersLabel().getHeight());
		}
		return currentCarbonOrdersLabel;
	}

	private JScrollPane	currentCarbonOrdersScrollPane;

	private JScrollPane getCurrentCarbonOrdersScrollPane()
	{
		if (currentCarbonOrdersScrollPane == null)
		{
			currentCarbonOrdersScrollPane = new JScrollPane();
			currentCarbonOrdersScrollPane = new JScrollPane(getCurrentCarbonOrdersList().getComponent());
			currentCarbonOrdersScrollPane.setPreferredSize(new Dimension(200, getCurrentCarbonOrdersList().getComponent().getPreferredScrollableViewportSize().height));
			currentCarbonOrdersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			currentCarbonOrdersScrollPane.setBounds(getCurrentCarbonOrdersLabel().getX(), getCurrentCarbonOrdersLabel().getY() + getCurrentCarbonOrdersLabel().getHeight(), getCurrentCarbonOrdersLabel().getWidth(), getNextCarbonOrdersScrollPane().getHeight());
		}
		return currentCarbonOrdersScrollPane;
	}

	private final TypedListWrapper.TypedListElementSelector<CarbonCarrier>	CARBONCARRIER_SELECTOR	= new TypedListWrapper.TypedListElementSelector<CarbonCarrier>()
																									{
																										public boolean equals(CarbonCarrier o1, CarbonCarrier o2)
																										{
																											return o1.getName().equals(o2.getName()) && o1.getOwnerName().equals(o2.getOwnerName());
																										}
																									};

	private TypedListWrapper<JList, CarbonCarrier>							currentCarbonOrdersList;

	private TypedListWrapper<JList, CarbonCarrier> getCurrentCarbonOrdersList()
	{
		if (currentCarbonOrdersList == null)
		{
			currentCarbonOrdersList = new TypedListWrapper<JList, CarbonCarrier>(CarbonCarrier.class, new JList(), CARBONCARRIER_SELECTOR);
			currentCarbonOrdersList.getComponent().setVisibleRowCount(3);
			currentCarbonOrdersList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return currentCarbonOrdersList;
	}

	private JLabel	carbonOrdersToReceiveLabel;

	private JLabel getCarbonOrdersToReceiveLabel()
	{
		if (carbonOrdersToReceiveLabel == null)
		{
			carbonOrdersToReceiveLabel = new JLabel("Carbon orders to receive");
			carbonOrdersToReceiveLabel.setBounds(getCurrentCarbonOrdersLabel().getX() + getCurrentCarbonOrdersLabel().getWidth() + 20, getCurrentCarbonOrdersLabel().getY(), getCurrentCarbonOrdersLabel().getWidth(), getCurrentCarbonOrdersLabel().getHeight());
		}
		return carbonOrdersToReceiveLabel;
	}

	private JScrollPane	carbonOrdersToReceiveScrollPane;

	private JScrollPane getCarbonOrdersToReceiveScrollPane()
	{
		if (carbonOrdersToReceiveScrollPane == null)
		{
			carbonOrdersToReceiveScrollPane = new JScrollPane();
			carbonOrdersToReceiveScrollPane = new JScrollPane(getCarbonOrdersToReceiveList().getComponent());
			carbonOrdersToReceiveScrollPane.setPreferredSize(new Dimension(200, getCarbonOrdersToReceiveList().getComponent().getPreferredScrollableViewportSize().height));
			carbonOrdersToReceiveScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			carbonOrdersToReceiveScrollPane.setBounds(getCarbonOrdersToReceiveLabel().getX(), getCarbonOrdersToReceiveLabel().getY() + getCarbonOrdersToReceiveLabel().getHeight(), getCurrentCarbonOrdersScrollPane().getWidth(), getCurrentCarbonOrdersScrollPane().getHeight());
		}
		return carbonOrdersToReceiveScrollPane;
	}

	private TypedListWrapper<JList, CarbonCarrier>	carbonOrdersToReceiveList;

	private TypedListWrapper<JList, CarbonCarrier> getCarbonOrdersToReceiveList()
	{
		if (carbonOrdersToReceiveList == null)
		{
			carbonOrdersToReceiveList = new TypedListWrapper<JList, CarbonCarrier>(CarbonCarrier.class, new JList(), CARBONCARRIER_SELECTOR);
			carbonOrdersToReceiveList.getComponent().setVisibleRowCount(3);
			carbonOrdersToReceiveList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return carbonOrdersToReceiveList;
	}

	///////

	private void displayDiplomacyActionPanel()
	{
		Map<String, Diplomacy> policies = currentLocalGame.getGameBoard().getPlayersPolicies();

		JPanel diplomacyActionPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(policies.size() + 1, policies.size() + 1, 1, 1);
		diplomacyActionPanel.setLayout(gridLayout);

		// Top left label
		diplomacyActionPanel.add(getDiplomacyPanelPlayerFieldLabel());

		// First line (players labels, current player first column)
		diplomacyActionPanel.add(getDiplomacyPlayerLabel(currentPlayer));
		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;
			diplomacyActionPanel.add(getDiplomacyPlayerLabel(player));
		}

		// Second line, current player label in first column, empty label in second column, then current player policies.
		diplomacyActionPanel.add(getDiplomacyPlayerLabel(currentPlayer));
		diplomacyActionPanel.add(new JLabel());

		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(getDiplomacyPlayerHomeDiplomacyPanel(player), BorderLayout.NORTH);
			panel.add(getDiplomacyPlayerForeignDiplomacyPanel(player), BorderLayout.SOUTH);

			diplomacyActionPanel.add(panel);
		}

		// Next lines, players labels and policies
		for(Player owner : currentGamePlayers)
		{
			if (currentPlayer.isNamed(owner.getName())) continue;						

			// First column, player label
			diplomacyActionPanel.add(getDiplomacyPlayerLabel(owner));

			// Second column, player policy toward current player.
			diplomacyActionPanel.add(getPlayerPoliciesLabel(owner.getName(), currentPlayer.getName()));
			
			// Next columns, player policies
			for(Player player : currentGamePlayers)
			{
				if (currentPlayer.isNamed(player.getName())) continue;
				if (owner.isNamed(player.getName()))
				{
					diplomacyActionPanel.add(new JLabel());
					continue;
				}
				
				diplomacyActionPanel.add(getPlayerPoliciesLabel(owner.getName(), player.getName()));
			}
		}

		getDiplomacyActionScrollPane().setViewportView(diplomacyActionPanel);

		getActionPanel().removeAll();
		getActionPanel().add(getDiplomacyActionScrollPane(), BorderLayout.CENTER);

		getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), "Diplomacy");

		refreshDiplomacyActionPanel();

		updateUI();
	}
	
	private final HashMap<String, WrappedJLabel> playersPoliciesLabel = new HashMap<String, WrappedJLabel>();
	private static final String mapSeparator = "@ @"; 
	
	private WrappedJLabel getPlayerPoliciesLabel(String ownerName, String targetName)
	{
		String key = ownerName+mapSeparator+targetName;
		if (!playersPoliciesLabel.containsKey(key))
		{
			WrappedJLabel label = (ownerName.equals(targetName) ? null : new WrappedJLabel("Never seen"));			
			playersPoliciesLabel.put(key, label);
		}
		return playersPoliciesLabel.get(key);
	}

	private void refreshDiplomacyActionPanel()
	{
		if (currentLocalGame == null || currentPlayer == null) return;

		Diplomacy currentPlayerDiplomacy = currentLocalGame.getGameBoard().getPlayersPolicies().get(currentPlayer.getName());

		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;

			PlayerPolicies currentPlayerPolicies = currentPlayerDiplomacy.getPolicies(player.getName());

			getDiplomacyPlayerHomeDiplomacyPanel(player).setSelected(currentPlayerPolicies.isAllowedToLandFleetInHomeTerritory());
			getDiplomacyPlayerForeignDiplomacyPanel(player).setState(currentPlayerPolicies.getForeignPolicy() == eForeignPolicy.NEUTRAL ? TristateCheckBox.NOT_SELECTED : currentPlayerPolicies.getForeignPolicy() == eForeignPolicy.HOSTILE_IF_OWNER ? TristateCheckBox.DONT_CARE : TristateCheckBox.SELECTED);			
			
			Diplomacy playerDiplomacy = currentLocalGame.getGameBoard().getPlayersPolicies().get(player.getName());
			
			for(Player target : currentGamePlayers)
			{
				if (player.isNamed(target.getName())) continue;
				
				PlayerPolicies playerPolicies = (playerDiplomacy == null) ? null : playerDiplomacy.getPolicies(target.getName());
				getPlayerPoliciesLabel(player.getName(), target.getName()).setText(playerPolicies == null ? "Unknown" : (playerDiplomacy.isVisible() ? "" : "T"+playerDiplomacy.getLastObservation()+": ") + playerPolicies.toString());
			}
		}
		
		updateUI();
	}

	private JScrollPane	diplomacyActionScrollPane;

	private JScrollPane getDiplomacyActionScrollPane()
	{
		if (diplomacyActionScrollPane == null)
		{
			diplomacyActionScrollPane = new JScrollPane();
		}
		return diplomacyActionScrollPane;
	}

	private JLabel	diplomacyPanelPlayerFieldLabel;

	private JLabel getDiplomacyPanelPlayerFieldLabel()
	{
		if (diplomacyPanelPlayerFieldLabel == null)
		{
			diplomacyPanelPlayerFieldLabel = new JLabel("Diplomacy \\ Players");
		}
		return diplomacyPanelPlayerFieldLabel;
	}

	//private Map<String, JLabel> playersLabels = new Hashtable<String, JLabel>();
	private JLabel getDiplomacyPlayerLabel(Player player)
	{
		/*
		if (!playersLabels.containsKey(player.getName()))		
		{
			JLabel playerLabel = new JLabel(player.getName());
			playerLabel.setForeground(player.getConfig().getColor());
			playersLabels.put(player.getName(), playerLabel);
		}
		
		return playersLabels.get(player.getName());
		*/
		JLabel playerLabel = new JLabel(player.getName());
		playerLabel.setForeground(player.getConfig().getColor());
		return playerLabel;
	}

	private Map<String, JCheckBox>	playersHomeDiplomacy	= new Hashtable<String, JCheckBox>();

	private JCheckBox getDiplomacyPlayerHomeDiplomacyPanel(Player player)
	{
		if (!playersHomeDiplomacy.containsKey(player.getName()))
		{
			JCheckBox checkbox = new JCheckBox("Allow fleet");
			checkbox.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					updateDiplomacy();
				}
			});
			playersHomeDiplomacy.put(player.getName(), checkbox);
		}
		return playersHomeDiplomacy.get(player.getName());
	}

	private Map<String, TristateCheckBox>	playersForeignDiplomacy	= new Hashtable<String, TristateCheckBox>();

	private TristateCheckBox getDiplomacyPlayerForeignDiplomacyPanel(Player player)
	{
		if (!playersForeignDiplomacy.containsKey(player.getName()))
		{
			TristateCheckBox checkbox = new TristateCheckBox("Hostile in foreign conflicts");
			checkbox.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					updateDiplomacy();
				}
			});
			playersForeignDiplomacy.put(player.getName(), checkbox);
		}
		return playersForeignDiplomacy.get(player.getName());
	}

	private void updateDiplomacy()
	{
		Map<String, PlayerPolicies> policies = new Hashtable<String, PlayerPolicies>();
		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;
			;
			;
			;
			boolean allowFleetAtHome = getDiplomacyPlayerHomeDiplomacyPanel(player).isSelected();
			TristateCheckBox.State alwaysEngageFightInStrangerTerritory = getDiplomacyPlayerForeignDiplomacyPanel(player).getState();
			policies.put(player.getName(), new PlayerPolicies(player.getName(), allowFleetAtHome, alwaysEngageFightInStrangerTerritory == TristateCheckBox.NOT_SELECTED ? eForeignPolicy.NEUTRAL : alwaysEngageFightInStrangerTerritory == TristateCheckBox.SELECTED ? eForeignPolicy.HOSTILE : eForeignPolicy.HOSTILE_IF_OWNER));
		}

		try
		{
			currentLocalGame.executeCommand(new ChangeDiplomacy(policies));
		}
		catch(GameCommandException e)
		{
			showRunningGameCommandExceptionMsg(e);
		}
	}

	////////////////

	private void displayFleetActionPanel()
	{
		SelectedUnitInfos<Fleet> infos = getSelectedUnitInfos(Fleet.class);
		if (infos == null) return;

		JPanel fleetActionPanel = new JPanel(null);

		fleetActionPanel.add(getFleetMoveCurrentMoveLabel());

		fleetActionPanel.add(getFleetMoveDestinationLabel());
		fleetActionPanel.add(getFleetMoveDestinationComboBox());
		fleetActionPanel.add(getFleetMoveDelayLabel());
		fleetActionPanel.add(getFleetMoveDelayTextField());
		fleetActionPanel.add(getFleetMoveGoRadioBtn());
		fleetActionPanel.add(getFleetMoveAttackRadioBtn());
		fleetActionPanel.add(getFleetMoveDirectBtn());
		fleetActionPanel.add(getFleetMoveAddBtn());

		fleetActionPanel.add(getFleetMoveCheckPointListScrollPane());
		fleetActionPanel.add(getFleetMoveCheckPointDownBtn());
		fleetActionPanel.add(getFleetMoveCheckPointRemoveBtn());
		fleetActionPanel.add(getFleetMoveCheckPointUpBtn());

		fleetActionPanel.setMinimumSize(new Dimension(getFleetMoveCheckPointListScrollPane().getX() + getFleetMoveCheckPointListScrollPane().getWidth() + 20, getFleetMoveCheckPointRemoveBtn().getY() + getFleetMoveCheckPointRemoveBtn().getHeight() + 20));
		fleetActionPanel.setPreferredSize(fleetActionPanel.getMinimumSize());

		getFleetActionScrollPane().setViewportView(fleetActionPanel);

		getActionPanel().removeAll();
		getActionPanel().add(getFleetActionScrollPane(), BorderLayout.CENTER);

		getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), "Fleet " + infos.unit.getName());

		refreshFleetActionPanel();

		updateUI();
	}

	private void refreshFleetActionPanel()
	{
		SelectedUnitInfos<Fleet> infos = getSelectedUnitInfos(Fleet.class);
		if (infos == null || infos.unit == null) return;

		getFleetMoveCurrentMoveLabel().setText(infos.unit.getCurrentMove() == null ? "Stopped" : "Current move : " + infos.unit.getCurrentMove().toString());

		getFleetMoveCheckPointList().getComponent().setEnabled(false);
		getFleetMoveCheckPointList().clear();
		if (infos.unit.getCheckpoints() != null) getFleetMoveCheckPointList().addAll(infos.unit.getCheckpoints());
		getFleetMoveCheckPointList().getComponent().setEnabled(true);

		updateUI();
	}

	private JScrollPane	fleetActionScrollPane;

	private JScrollPane getFleetActionScrollPane()
	{
		if (fleetActionScrollPane == null)
		{
			fleetActionScrollPane = new JScrollPane();
		}
		return fleetActionScrollPane;
	}

	private JLabel	fleetMoveCurrentMoveLabel;

	private JLabel getFleetMoveCurrentMoveLabel()
	{
		if (fleetMoveCurrentMoveLabel == null)
		{
			fleetMoveCurrentMoveLabel = new JLabel("");
			fleetMoveCurrentMoveLabel.setBounds(10, 10, 400, 20);
		}

		return fleetMoveCurrentMoveLabel;
	}

	private JLabel	fleetMoveDestinationLabel;

	private JLabel getFleetMoveDestinationLabel()
	{
		if (fleetMoveDestinationLabel == null)
		{
			fleetMoveDestinationLabel = new JLabel("Destination");
			fleetMoveDestinationLabel.setBounds(getFleetMoveCurrentMoveLabel().getX(), getFleetMoveCurrentMoveLabel().getY() + getFleetMoveCurrentMoveLabel().getHeight(), 200, getFleetMoveCurrentMoveLabel().getHeight());
		}
		return fleetMoveDestinationLabel;
	}

	private JComboBox	fleetMoveDestinationComboBox;

	private JComboBox getFleetMoveDestinationComboBox()
	{
		if (fleetMoveDestinationComboBox == null)
		{
			Set<ICelestialBody> celestialBodies = currentLocalGame.getGameBoard().getCelestialBodies();
			String[] destinationNames = new String[celestialBodies.size()];
			int i = 0;
			for(ICelestialBody celestialBody : celestialBodies)
			{
				destinationNames[i] = celestialBody.getName();
				++i;
			}

			fleetMoveDestinationComboBox = new JComboBox(destinationNames);
			fleetMoveDestinationComboBox.setBounds(getFleetMoveDestinationLabel().getX(), getFleetMoveDestinationLabel().getY() + getFleetMoveDestinationLabel().getHeight() + 5, getFleetMoveDestinationLabel().getWidth(), getFleetMoveDestinationLabel().getHeight());
		}
		return fleetMoveDestinationComboBox;
	}

	private JLabel	fleetMoveDelayLabel;

	private JLabel getFleetMoveDelayLabel()
	{
		if (fleetMoveDelayLabel == null)
		{
			fleetMoveDelayLabel = new JLabel("Delay");
			fleetMoveDelayLabel.setBounds(getFleetMoveDestinationComboBox().getX(), getFleetMoveDestinationComboBox().getY() + getFleetMoveDestinationComboBox().getHeight() + 5, (int) (getFleetMoveDestinationComboBox().getWidth() * 0.3), getFleetMoveDestinationComboBox().getHeight());
		}
		return fleetMoveDelayLabel;
	}

	private JTextField	fleetMoveDelayTextField;

	private JTextField getFleetMoveDelayTextField()
	{
		if (fleetMoveDelayTextField == null)
		{
			fleetMoveDelayTextField = new JTextField();
			fleetMoveDelayTextField.setBounds(getFleetMoveDelayLabel().getX() + getFleetMoveDelayLabel().getWidth(), getFleetMoveDelayLabel().getY(), getFleetMoveDestinationComboBox().getWidth() - getFleetMoveDelayLabel().getWidth(), getFleetMoveDelayLabel().getHeight());
		}
		return fleetMoveDelayTextField;
	}

	private JRadioButton	fleetMoveGoRadioBtn;

	private JRadioButton getFleetMoveGoRadioBtn()
	{
		if (fleetMoveGoRadioBtn == null)
		{
			fleetMoveGoRadioBtn = new JRadioButton("Go");
			fleetMoveGoRadioBtn.setBounds(getFleetMoveDelayLabel().getX(), getFleetMoveDelayLabel().getY() + getFleetMoveDelayLabel().getHeight() + 5, (getFleetMoveDestinationLabel().getWidth() / 2) - 2, getFleetMoveDestinationLabel().getHeight());
		}
		return fleetMoveGoRadioBtn;
	}

	private JRadioButton	fleetMoveAttackRadioBtn;

	private JRadioButton getFleetMoveAttackRadioBtn()
	{
		if (fleetMoveAttackRadioBtn == null)
		{
			fleetMoveAttackRadioBtn = new JRadioButton("Attack");
			fleetMoveAttackRadioBtn.setBounds(getFleetMoveGoRadioBtn().getX() + getFleetMoveGoRadioBtn().getWidth() + 4, getFleetMoveGoRadioBtn().getY(), getFleetMoveDestinationLabel().getWidth() - getFleetMoveGoRadioBtn().getWidth() - 2, getFleetMoveGoRadioBtn().getHeight());
		}
		return fleetMoveAttackRadioBtn;
	}

	private void updateFleetMove()
	{
		SelectedUnitInfos<Fleet> infos = getSelectedUnitInfos(Fleet.class);
		if (infos == null || infos.unit == null) return;

		Stack<Fleet.Move> checkpoints = new Stack<Fleet.Move>();

		for(Move checkpoint : getFleetMoveCheckPointList())
		{
			checkpoints.push(checkpoint);
		}

		try
		{
			currentLocalGame.executeCommand(new MoveFleet(infos.unit.getName(), checkpoints));
		}
		catch(GameCommandException e)
		{
			showRunningGameCommandExceptionMsg(e);
		}
		finally
		{
			//getFleetMoveCheckPointList().getComponent().setEnabled(false);
		}
	}

	private JButton	fleetMoveDirectBtn;

	private JButton getFleetMoveDirectBtn()
	{
		if (fleetMoveDirectBtn == null)
		{
			fleetMoveDirectBtn = new JButton("Direct");
			fleetMoveDirectBtn.setBounds(getFleetMoveDelayLabel().getX(), getFleetMoveAttackRadioBtn().getY() + getFleetMoveAttackRadioBtn().getHeight() + 5, (getFleetMoveDestinationLabel().getWidth() / 2) - 2, getFleetMoveDestinationLabel().getHeight());
			fleetMoveDirectBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedUnitInfos<Fleet> infos = getSelectedUnitInfos(Fleet.class);
					if (infos == null || infos.unit == null) return;

					if (!getFleetMoveCheckPointList().isEmpty())
					{
						if (JOptionPane.showConfirmDialog(null, infos.unit.getName() + " is already in move, are you sure to clear the unaccomplished checkpoints ?") != JOptionPane.YES_OPTION)
						{
							return;
						}
					}

					String destinationCelestialBodyName = getFleetMoveDestinationComboBox().getSelectedItem().toString();
					int delay = Basic.intValueOf(getFleetMoveDelayTextField().getText(), 0);
					boolean isAnAttack = getFleetMoveAttackRadioBtn().isSelected() && !getFleetMoveGoRadioBtn().isSelected();

					Fleet.Move newCheckpoint = new Fleet.Move(destinationCelestialBodyName, delay, isAnAttack);

					getFleetMoveCheckPointList().clear();
					getFleetMoveCheckPointList().add(newCheckpoint);

					updateUI();

					updateFleetMove();
				}
			});
		}

		return fleetMoveDirectBtn;
	}

	private JButton	fleetMoveAddBtn;

	private JButton getFleetMoveAddBtn()
	{
		if (fleetMoveAddBtn == null)
		{
			fleetMoveAddBtn = new JButton("Add");
			fleetMoveAddBtn.setBounds(getFleetMoveDirectBtn().getX() + getFleetMoveDirectBtn().getWidth() + 4, getFleetMoveDirectBtn().getY(), getFleetMoveDestinationLabel().getWidth() - getFleetMoveDirectBtn().getWidth() - 2, getFleetMoveDirectBtn().getHeight());
			fleetMoveAddBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String destinationCelestialBodyName = getFleetMoveDestinationComboBox().getSelectedItem().toString();
					int delay = Basic.intValueOf(getFleetMoveDelayTextField().getText(), 0);
					boolean isAnAttack = getFleetMoveAttackRadioBtn().isSelected() && !getFleetMoveGoRadioBtn().isSelected();

					Fleet.Move newCheckpoint = new Fleet.Move(destinationCelestialBodyName, delay, isAnAttack);
					getFleetMoveCheckPointList().add(newCheckpoint);

					updateUI();

					updateFleetMove();
				}
			});
		}

		return fleetMoveAddBtn;
	}

	JScrollPane	fleetMoveCheckPointListScrollPane;

	private JScrollPane getFleetMoveCheckPointListScrollPane()
	{
		if (fleetMoveCheckPointListScrollPane == null)
		{
			fleetMoveCheckPointListScrollPane = new JScrollPane();
			fleetMoveCheckPointListScrollPane.setViewportView(getFleetMoveCheckPointList().getComponent());
			fleetMoveCheckPointListScrollPane.setPreferredSize(new Dimension(100, getFleetMoveCheckPointList().getComponent().getPreferredScrollableViewportSize().height));
			fleetMoveCheckPointListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			fleetMoveCheckPointListScrollPane.setBounds(getFleetMoveDestinationLabel().getX() + getFleetMoveDestinationLabel().getWidth() + 30, getFleetMoveDestinationLabel().getY(), getFleetMoveDestinationLabel().getWidth(), getFleetMoveDirectBtn().getY());
		}
		return fleetMoveCheckPointListScrollPane;
	}

	private final TypedListWrapper.TypedListElementSelector<Fleet.Move>	FLEETMOVE_SELECTOR	= new TypedListWrapper.TypedListElementSelector<Fleet.Move>()
																							{
																								@Override
																								public boolean equals(Move o1, Move o2)
																								{
																									return o1.getDestinationName().equals(o2.getDestinationName());
																								}
																							};

	private TypedListWrapper<JList, Fleet.Move>							fleetMoveCheckPointList;

	private TypedListWrapper<JList, Fleet.Move> getFleetMoveCheckPointList()
	{
		if (fleetMoveCheckPointList == null)
		{
			fleetMoveCheckPointList = new TypedListWrapper<JList, Fleet.Move>(Fleet.Move.class, new JList(), FLEETMOVE_SELECTOR);
			fleetMoveCheckPointList.getComponent().setVisibleRowCount(getFleetMoveDirectBtn().getY() / fleetMoveCheckPointList.getComponent().getFixedCellHeight());
			fleetMoveCheckPointList.getComponent().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			fleetMoveCheckPointList.setCellRenderer(new TypedListWrapper.AbstractTypedJListCellRender<Fleet.Move>());
		}
		return fleetMoveCheckPointList;
	}

	private JButton	fleetMoveCheckPointUpBtn;

	private JButton getFleetMoveCheckPointUpBtn()
	{
		if (fleetMoveCheckPointUpBtn == null)
		{
			fleetMoveCheckPointUpBtn = new JButton("up");
			fleetMoveCheckPointUpBtn.setBounds(getFleetMoveCheckPointListScrollPane().getX(), getFleetMoveCheckPointListScrollPane().getY() + getFleetMoveCheckPointListScrollPane().getHeight() + 5, 60, 20);
			fleetMoveCheckPointUpBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					Fleet.Move selection = getFleetMoveCheckPointList().getSelectedElement();
					int selectionIndex = getFleetMoveCheckPointList().getSelectedIndex();

					if (selectionIndex <= 0) return;

					getFleetMoveCheckPointList().remove(selectionIndex);
					getFleetMoveCheckPointList().add(selectionIndex - 1, selection);
					getFleetMoveCheckPointList().setSelectedElement(selection);

					updateUI();

					updateFleetMove();
				}
			});
		}
		return fleetMoveCheckPointUpBtn;
	}

	private JButton	fleetMoveCheckPointRemoveBtn;

	private JButton getFleetMoveCheckPointRemoveBtn()
	{
		if (fleetMoveCheckPointRemoveBtn == null)
		{
			fleetMoveCheckPointRemoveBtn = new JButton("rem");
			fleetMoveCheckPointRemoveBtn.setBounds(getFleetMoveCheckPointListScrollPane().getX() + getFleetMoveCheckPointListScrollPane().getWidth() / 2 - (getFleetMoveCheckPointUpBtn().getWidth() / 2), getFleetMoveCheckPointListScrollPane().getY() + getFleetMoveCheckPointListScrollPane().getHeight() + 5, getFleetMoveCheckPointUpBtn().getWidth(), getFleetMoveCheckPointUpBtn().getHeight());
			fleetMoveCheckPointRemoveBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					int selectionIndex = getFleetMoveCheckPointList().getSelectedIndex();

					if (selectionIndex < 0) return;

					getFleetMoveCheckPointList().remove(selectionIndex);

					updateUI();

					updateFleetMove();
				}
			});
		}
		return fleetMoveCheckPointRemoveBtn;
	}

	private JButton	fleetMoveCheckPointDownBtn;

	private JButton getFleetMoveCheckPointDownBtn()
	{
		if (fleetMoveCheckPointDownBtn == null)
		{
			fleetMoveCheckPointDownBtn = new JButton("dn");
			fleetMoveCheckPointDownBtn.setBounds(getFleetMoveCheckPointListScrollPane().getX() + getFleetMoveCheckPointListScrollPane().getWidth() - getFleetMoveCheckPointUpBtn().getWidth(), getFleetMoveCheckPointListScrollPane().getY() + getFleetMoveCheckPointListScrollPane().getHeight() + 5, getFleetMoveCheckPointUpBtn().getWidth(), getFleetMoveCheckPointUpBtn().getHeight());
			fleetMoveCheckPointDownBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					Fleet.Move selection = getFleetMoveCheckPointList().getSelectedElement();
					int selectionIndex = getFleetMoveCheckPointList().getSelectedIndex();

					if (selectionIndex < 0) return;
					if (selectionIndex >= getFleetMoveCheckPointList().size() - 1) return;

					getFleetMoveCheckPointList().remove(selectionIndex);
					getFleetMoveCheckPointList().add(selectionIndex + 1, selection);
					getFleetMoveCheckPointList().setSelectedElement(selection);

					updateUI();

					updateFleetMove();
				}
			});
		}
		return fleetMoveCheckPointDownBtn;
	}

	private void displayStarshipPlantActionPanel()
	{
		SelectedBuildingInfos<StarshipPlant> infos = getSelectedBuildingInfos(StarshipPlant.class);
		if (infos == null || infos.productiveCelestialBody == null) return;

		JPanel starshipPlantActionPanel = new JPanel(null);

		starshipPlantActionPanel.add(getStarshipPlantWorkshopTitleLabel());
		starshipPlantActionPanel.add(getStarshipPlantWorkshopColumnLabel());
		int y = getStarshipPlantWorkshopColumnLabel().getY() + getStarshipPlantWorkshopColumnLabel().getHeight() + 5;

		Set<StarshipTemplate> starshipTemplates = new TreeSet<StarshipTemplate>();
		starshipTemplates.addAll(SEPUtils.starshipTypes);
		if (infos.productiveCelestialBody.getUnasignedStarships() != null) starshipTemplates.addAll(infos.productiveCelestialBody.getUnasignedStarships().keySet());

		for(StarshipTemplate starshipType : starshipTemplates)
		{
			JLabel typeLabel = getStarshipPlantWorkshopStarshipTypeLabel(starshipType);
			typeLabel.setBounds(0, y, 200, 20);
			starshipPlantActionPanel.add(typeLabel);

			JLabel qtLabel = getStarshipPlantWorkshopStarshipQtLabel(starshipType);
			int qt = infos.productiveCelestialBody.getUnasignedStarships().containsKey(starshipType) ? infos.productiveCelestialBody.getUnasignedStarships().get(starshipType) : 0;
			qtLabel.setText(qt + " (" + qt + ")");
			qtLabel.setBounds(200, y, 100, 20);
			starshipPlantActionPanel.add(qtLabel);

			JTextField makeQt = getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType);
			makeQt.setBounds(300, y, 40, 20);
			makeQt.setEnabled(infos.building != null && SEPUtils.starshipTypes.contains(starshipType));
			starshipPlantActionPanel.add(makeQt);

			JTextField fleetQt = getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType);
			fleetQt.setBounds(400, y, 40, 20);
			starshipPlantActionPanel.add(fleetQt);

			y += 25;
		}

		if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null) for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
		{
			if (specialUnit == null) continue;

			JLabel typeLabel = getStarshipPlantWorkshopSpecialUnitNameLabel(specialUnit);
			typeLabel.setBounds(0, y, 200, 20);
			starshipPlantActionPanel.add(typeLabel);

			JLabel qtLabel = getStarshipPlantWorkshopSpecialUnitQtLabel(specialUnit);
			int qt = 1;
			qtLabel.setText(qt + " (" + qt + ")");
			qtLabel.setBounds(200, y, 100, 20);
			starshipPlantActionPanel.add(qtLabel);

			JTextField fleetQt = getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit);
			fleetQt.setBounds(400, y, 40, 20);
			starshipPlantActionPanel.add(fleetQt);

			y += 25;
		}

		JButton makeStarshipBtn = getStarshipPlantWorkshopMakeStarshipBtn();
		makeStarshipBtn.setBounds(275, y, 90, 20);
		starshipPlantActionPanel.add(makeStarshipBtn);

		JTextField newFleetNameTextField = getStarshipPlantNewFleetNameTextField();
		newFleetNameTextField.setBounds(375, y, 90, 20);
		starshipPlantActionPanel.add(newFleetNameTextField);

		JButton formFleetBtn = getStarshipPlantFormFleetBtn();
		formFleetBtn.setBounds(375, y + 20, 90, 20);
		starshipPlantActionPanel.add(formFleetBtn);

		y += 10;

		JLabel probeLabel = new JLabel(Probe.class.getSimpleName());
		probeLabel.setBounds(0, y + 40, 200, 20);
		starshipPlantActionPanel.add(probeLabel);

		getProbeNamePrefixTextField().setBounds(175, y + 40, 90, 20);
		starshipPlantActionPanel.add(getProbeNamePrefixTextField());

		getProbeMakeQtTextField().setBounds(300, y + 40, 40, 20);
		starshipPlantActionPanel.add(getProbeMakeQtTextField());

		JButton makeProbeBtn = getStarshipPlantWorkshopMakeProbeBtn();
		makeProbeBtn.setBounds(375, y + 40, 90, 20);
		starshipPlantActionPanel.add(makeProbeBtn);

		JLabel antiProbeMissileLbel = new JLabel(AntiProbeMissile.class.getSimpleName());
		antiProbeMissileLbel.setBounds(0, y + 65, 200, 20);
		starshipPlantActionPanel.add(antiProbeMissileLbel);

		getAntiProbeMissileNamePrefixTextField().setBounds(175, y + 65, 90, 20);
		starshipPlantActionPanel.add(getAntiProbeMissileNamePrefixTextField());

		getAntiProbeMissileMakeQtTextField().setBounds(300, y + 65, 40, 20);
		starshipPlantActionPanel.add(getAntiProbeMissileMakeQtTextField());

		JButton makeAntiProbeMissileBtn = getStarshipPlantWorkshopMakeAntiProbeMissileBtn();
		makeAntiProbeMissileBtn.setBounds(375, y + 65, 90, 20);
		starshipPlantActionPanel.add(makeAntiProbeMissileBtn);

		starshipPlantActionPanel.setMinimumSize(new Dimension(500, makeAntiProbeMissileBtn.getY() + makeAntiProbeMissileBtn.getHeight() + 50));
		starshipPlantActionPanel.setPreferredSize(starshipPlantActionPanel.getMinimumSize());

		getStarshipPlantActionScrollPane().setViewportView(starshipPlantActionPanel);

		getActionPanel().removeAll();
		getActionPanel().add(getStarshipPlantActionScrollPane(), BorderLayout.CENTER);

		getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), (infos.building != null ? "Starship plant" : "Unasigned fleet"));

		refreshStarshipPlantActionPanel();

		updateUI();
	}

	private JTextField	probeNamePrefixTextField;

	private JTextField getProbeNamePrefixTextField()
	{
		if (probeNamePrefixTextField == null)
		{
			probeNamePrefixTextField = new JTextField();
			probeNamePrefixTextField.addFocusListener(getStarshipPlantActionFocusListener());
		}
		return probeNamePrefixTextField;
	}

	private JTextField	probeMakeQtTextField;

	private JTextField getProbeMakeQtTextField()
	{
		if (probeMakeQtTextField == null)
		{
			probeMakeQtTextField = new JTextField();
			probeMakeQtTextField.addFocusListener(getStarshipPlantActionFocusListener());
		}
		return probeMakeQtTextField;
	}

	private JTextField	antiProbeMissileNamePrefixTextField;

	private JTextField getAntiProbeMissileNamePrefixTextField()
	{
		if (antiProbeMissileNamePrefixTextField == null)
		{
			antiProbeMissileNamePrefixTextField = new JTextField();
			antiProbeMissileNamePrefixTextField.addFocusListener(getStarshipPlantActionFocusListener());
		}
		return antiProbeMissileNamePrefixTextField;
	}

	private JTextField	antiProbeMissileMakeQtTextField;

	private JTextField getAntiProbeMissileMakeQtTextField()
	{
		if (antiProbeMissileMakeQtTextField == null)
		{
			antiProbeMissileMakeQtTextField = new JTextField();
			antiProbeMissileMakeQtTextField.addFocusListener(getStarshipPlantActionFocusListener());
		}
		return antiProbeMissileMakeQtTextField;
	}

	private JLabel	starshipPlantWorkshopTitleLabel;

	JLabel getStarshipPlantWorkshopTitleLabel()
	{
		if (starshipPlantWorkshopTitleLabel == null)
		{
			starshipPlantWorkshopTitleLabel = new JLabel();
			starshipPlantWorkshopTitleLabel.setText("Landed starships");
			starshipPlantWorkshopTitleLabel.setBounds(0, 0, 400, 20);
		}
		return starshipPlantWorkshopTitleLabel;
	}

	private JLabel	starshipPlantWorkshopColumnLabel;

	JLabel getStarshipPlantWorkshopColumnLabel()
	{
		if (starshipPlantWorkshopColumnLabel == null)
		{
			starshipPlantWorkshopColumnLabel = new JLabel();
			starshipPlantWorkshopColumnLabel.setText("Type                                    Quantity (after)          Make          New fleet");
			starshipPlantWorkshopColumnLabel.setBounds(getStarshipPlantWorkshopTitleLabel().getX(), getStarshipPlantWorkshopTitleLabel().getY() + getStarshipPlantWorkshopTitleLabel().getHeight() + 5, 500, 20);
		}
		return starshipPlantWorkshopColumnLabel;
	}

	private Map<String, JLabel>	starshipPlantWorkshopStarshipTypeLabel	= new HashMap<String, JLabel>();

	JLabel getStarshipPlantWorkshopStarshipTypeLabel(StarshipTemplate starshipType)
	{
		return getStarshipPlantWorkshopLabel(StarshipTemplate.class.getName() + starshipType.getName(), starshipType.getName());
	}

	JLabel getStarshipPlantWorkshopSpecialUnitNameLabel(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopLabel(specialUnit.getClass().getName() + specialUnit.getName(), specialUnit.getName());
	}

	JLabel getStarshipPlantWorkshopLabel(String id, String text)
	{
		if (!starshipPlantWorkshopStarshipTypeLabel.containsKey(id))
		{
			JLabel label = new JLabel();
			label.setText(text);
			starshipPlantWorkshopStarshipTypeLabel.put(id, label);
		}
		return starshipPlantWorkshopStarshipTypeLabel.get(id);
	}

	private Map<String, JLabel>	starshipPlantWorkshopStarshipQtLabel	= new HashMap<String, JLabel>();

	JLabel getStarshipPlantWorkshopStarshipQtLabel(StarshipTemplate starshipType)
	{
		return getStarshipPlantWorkshopQtLabel(starshipType.getClass().getName() + starshipType.getName());
	}

	JLabel getStarshipPlantWorkshopSpecialUnitQtLabel(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopQtLabel(specialUnit.getClass().getName() + specialUnit.getName());
	}

	JLabel getStarshipPlantWorkshopQtLabel(String id)
	{
		if (!starshipPlantWorkshopStarshipQtLabel.containsKey(id))
		{
			JLabel label = new JLabel();
			starshipPlantWorkshopStarshipQtLabel.put(id, label);
		}
		return starshipPlantWorkshopStarshipQtLabel.get(id);
	}

	private Map<StarshipTemplate, JTextField>	starshipPlantWorkshopStarshipQtToMakeTextField	= new HashMap<StarshipTemplate, JTextField>();

	JTextField getStarshipPlantWorkshopStarshipQtToMakeTextField(StarshipTemplate starshipType)
	{
		if (!starshipPlantWorkshopStarshipQtToMakeTextField.containsKey(starshipType))
		{
			JTextField textField = new JTextField("0");
			textField.addFocusListener(getStarshipPlantActionFocusListener());
			starshipPlantWorkshopStarshipQtToMakeTextField.put(starshipType, textField);
		}
		return starshipPlantWorkshopStarshipQtToMakeTextField.get(starshipType);
	}

	private Map<String, JTextField>	starshipPlantWorkshopStarshipNewFleetQtTextField	= new HashMap<String, JTextField>();

	JTextField getStarshipPlantWorkshopStarshipNewFleetQtTextField(StarshipTemplate starshipType)
	{
		return getStarshipPlantWorkshopNewFleetQtTextField(starshipType.getClass().getName() + starshipType.getName());
	}

	JTextField getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopNewFleetQtTextField(specialUnit.getClass().getName() + specialUnit.getName());
	}

	JTextField getStarshipPlantWorkshopNewFleetQtTextField(String id)
	{
		if (!starshipPlantWorkshopStarshipNewFleetQtTextField.containsKey(id))
		{
			JTextField textField = new JTextField("0");
			textField.addFocusListener(getStarshipPlantActionFocusListener());
			starshipPlantWorkshopStarshipNewFleetQtTextField.put(id, textField);
		}
		return starshipPlantWorkshopStarshipNewFleetQtTextField.get(id);
	}

	private JTextField	starshipPlantNewFleetNameTextField;

	private JTextField getStarshipPlantNewFleetNameTextField()
	{
		if (starshipPlantNewFleetNameTextField == null)
		{
			starshipPlantNewFleetNameTextField = new JTextField("fleetName");
			starshipPlantNewFleetNameTextField.addFocusListener(getStarshipPlantActionFocusListener());
		}
		return starshipPlantNewFleetNameTextField;
	}

	void refreshGameBoard()
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{					
					refreshGameBoard(client.getRunningGameInterface().getPlayerGameBoard());
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
		});
	}

	@Override
	public void refreshLocalGameBoard(PlayerGameBoard gameBoard)
	{
		getUniverseRenderer().refreshGameBoard(gameBoard);

		String actionTabTitle = null;

		actionTabTitle = getRunningGameTabbedPanel().getTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()));
		Component comp = (getActionPanel().getComponentCount() > 0 ? getActionPanel().getComponent(0) : null);

		if (currentSelectedArea != null)
		{
			updateSelectedArea(currentSelectedLocation);
		}

		refreshBuildingDetails();
		refreshUnitDetails();
		refreshPlayerList();
		refreshShortcutBtns();

		refreshStarshipPlantActionPanel();
		refreshDiplomacyActionPanel();
		refreshFleetActionPanel();

		getActionPanel().removeAll();

		if (comp != null) getActionPanel().add(comp);
		if (actionTabTitle != null) getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), actionTabTitle);
		
		addLog(gameBoard);
	}
	
	private final Set<ALogEntry> logged = new HashSet<ALogEntry>(); 
	
	private void addLog(PlayerGameBoard gameBoard)
	{
		boolean newLog = false;
		for(ALogEntry log : gameBoard.getLogs())
		{
			boolean found = false;
			for(ALogEntry oldLog : logged)
			{
				if (oldLog.getUID().equals(log.getUID()))
				{
					if (!UnitSeenLogEntry.class.isInstance(oldLog))
					{
						found = true;
						break;
					}
					else
					{
						UnitSeenLogEntry old = UnitSeenLogEntry.class.cast(oldLog);
						UnitSeenLogEntry up = UnitSeenLogEntry.class.cast(log);
						
						if (old.getLastUpdateInstantDate() < up.getLastUpdateInstantDate())
						{
							logged.remove(old);
							break;
						}
					}
				}
			}
			
			if (found) continue;
													
			String htmlText = "<p><b>"+log.getClass().getSimpleName()+"</b> "+log.toString().replaceAll("\n", "<br/>")+"</p>";
			HTMLDocument doc = ((HTMLDocument) getRunningGameLogContentEditorPane().getDocument());

			try
			{
				newLog = true;
				doc.insertBeforeEnd(doc.getDefaultRootElement(), htmlText);
				logged.add(log);					
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
		
		if (newLog)
		{
			getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getRunningGameLogPanel()), "Logs");
		}
	}
	
	void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		currentLocalGame = new LocalGame(this, gameBoard);
		refreshLocalGameBoard(currentLocalGame.getGameBoard());
	}

	private void refreshShortcutBtns()
	{
		try
		{
			checkCommandBtn(getRunningGameUndoBtn(), currentLocalGame.canUndo());
			checkCommandBtn(getRunningGameRedoBtn(), currentLocalGame.canRedo());
			checkCommandBtn(getRunningGameCancelTurnBtn(), currentLocalGame.canResetTurn());
			
			checkCommandBtn(getRunningGameEndTurnBtn(), currentLocalGame.canEndTurn());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private WrappedJLabel	runningGameCelestialBodyDetailsBuildingDetailsContentLabel;

	private WrappedJLabel getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel()
	{
		if (runningGameCelestialBodyDetailsBuildingDetailsContentLabel == null)
		{
			runningGameCelestialBodyDetailsBuildingDetailsContentLabel = new WrappedJLabel();
			runningGameCelestialBodyDetailsBuildingDetailsContentLabel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));
		}
		return runningGameCelestialBodyDetailsBuildingDetailsContentLabel;
	}

	private JTextField getRunningGameChatTextField()
	{
		if (runningGameChatTextField == null)
		{
			runningGameChatTextField = new JTextField();
			runningGameChatTextField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					String msg = runningGameChatTextField.getText();
					if (msg.isEmpty()) return;
					try
					{
						client.getRunningGameInterface().sendMessage(msg);
						runningGameChatTextField.setText("");
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		return runningGameChatTextField;
	}

	private JPanel getRunningGameChatPlayerListPanel()
	{
		if (runningGameChatPlayerListPanel == null)
		{
			runningGameChatPlayerListPanel = new JPanel();
			LayoutManager layout = new SingleRowFlowLayout();
			runningGameChatPlayerListPanel.setLayout(layout);
		}
		return runningGameChatPlayerListPanel;
	}
	
	private JScrollPane runningGameLogScrollPane;
	private JScrollPane getRunningGameLogScrollPane()
	{
		if (runningGameLogScrollPane == null)
		{
			runningGameLogScrollPane = new JScrollPane();
			runningGameLogScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			runningGameLogScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			runningGameLogScrollPane.setViewportView(getRunningGameLogContentEditorPane());
			
			runningGameLogScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
			{

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					if (!e.getValueIsAdjusting())
					{
						JScrollBar vBar = runningGameLogScrollPane.getVerticalScrollBar();
						int newVal = (vBar.getMinimum() + (vBar.getMaximum() - vBar.getMinimum()) * 1);

						if (vBar.getValue() >= (vBar.getMaximum() - vBar.getVisibleAmount() - 30))
						{
							vBar.setValue(newVal);
							RunningGamePanel.this.updateUI();
						}
					}
				}
			});
		}
		return runningGameLogScrollPane;
	}

	private JScrollPane getRunningGameChatScrollPane()
	{
		if (runningGameChatScrollPane == null)
		{
			runningGameChatScrollPane = new JScrollPane();
			runningGameChatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			runningGameChatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			runningGameChatScrollPane.setViewportView(getRunningGameChatContentEditorPane());

			runningGameChatScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
			{

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					if (!e.getValueIsAdjusting())
					{
						System.out.println("runningGameChatScrollPane adjusmentValueChanged");

						JScrollBar vBar = runningGameChatScrollPane.getVerticalScrollBar();
						int newVal = (vBar.getMinimum() + (vBar.getMaximum() - vBar.getMinimum()) * 1);

						if (vBar.getValue() >= (vBar.getMaximum() - vBar.getVisibleAmount() - 30))
						{
							vBar.setValue(newVal);
							RunningGamePanel.this.updateUI();
						}
					}
				}
			});
		}
		return runningGameChatScrollPane;
	}
	
	private JEditorPane runningGameLogEditorPane;
	private JEditorPane getRunningGameLogContentEditorPane()
	{
		if (runningGameLogEditorPane == null)
		{
			runningGameLogEditorPane = new JEditorPane("text/html", "<i>Logs</i>");
			runningGameLogEditorPane.setEditable(false);
		}
		return runningGameLogEditorPane;
	}

	private JEditorPane getRunningGameChatContentEditorPane()
	{
		if (runningGameChatContentEditorPane == null)
		{
			runningGameChatContentEditorPane = new JEditorPane("text/html", "<i>Chat</i> <b>editor</b> <u>pane</u>");
			runningGameChatContentEditorPane.setEditable(false);			
		}
		return runningGameChatContentEditorPane;
	}

	/**
	 * @param fromPlayer
	 * @param msg
	 */
	public void receiveRunningGameMessage(Player fromPlayer, String msg)
	{
		String htmlText = "<br><font color='#" + GUIUtils.getHTMLColor(fromPlayer.getConfig().getColor()) + "'>" + fromPlayer.getName() + "</font> : " + msg + "</br>";
		HTMLDocument doc = ((HTMLDocument) getRunningGameChatContentEditorPane().getDocument());

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

	private void refreshPlayerList()
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				Set<Player> players;
				try
				{
					while(!client.isConnected()) { Thread.sleep(2000); }
					Thread.sleep(3000);
					
					refreshPlayerList(client.getRunningGameInterface().getPlayerList());
				}
				catch(StateMachineNotExpectedEventException e)
				{
					System.err.println("refreshPlayerList error");
					e.printStackTrace();
				}
				catch(RpcException e2)
				{
					System.err.println("refreshPlayerList error");
					e2.printStackTrace();
				}
				catch(InterruptedException e3)
				{
					System.err.println("refreshPlayerList error");
					e3.printStackTrace();
				}
			}
		});
	}

	private Stack<Player>	currentGamePlayers	= new Stack<Player>();
	private Player			currentPlayer		= null;

	public void refreshPlayerList(Set<Player> players)
	{
		System.out.println("RunningGamePanel refreshPlayerList : " + players);
		currentGamePlayers.removeAllElements();
		currentGamePlayers.addAll(players);

		getRunningGameChatPlayerListPanel().removeAll();
		for(Player p : currentGamePlayers)
		{
			if (p.isNamed(client.getLogin()))
			{
				currentPlayer = p;
			}

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JImagePanel(p.getConfig().getPortrait()), BorderLayout.WEST);
			panel.add(new JImagePanel(p.getConfig().getSymbol()), BorderLayout.EAST);
			JLabel name = new JLabel();
			name.setText(p.getName());
			name.setForeground(p.getConfig().getColor());
			panel.add(name, BorderLayout.CENTER);

			getRunningGameChatPlayerListPanel().add(panel);
		}

		updateUI();
	}

	private JScrollPane getRunningGameChatPlayerListScrollPane()
	{
		if (runningGameChatPlayerListScrollPane == null)
		{
			runningGameChatPlayerListScrollPane = new JScrollPane();
			runningGameChatPlayerListScrollPane.setViewportView(getRunningGameChatPlayerListPanel());
		}
		return runningGameChatPlayerListScrollPane;
	}

	public void receiveNewTurnGameBoard(PlayerGameBoard gameBoard)
	{
		refreshGameBoard(gameBoard);
		JOptionPane.showMessageDialog(null, "New turn begins (" + gameBoard.getDate() + ")", "New turn begins", JOptionPane.INFORMATION_MESSAGE);
	}	
}
