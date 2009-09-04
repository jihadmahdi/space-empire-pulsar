package client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTMLDocument;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Test;

import com.sun.org.apache.bcel.internal.util.JavaWrapper;

import sun.awt.VerticalBagLayout;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import client.SEPClient;
import client.gui.UniverseRenderer.UniverseRendererListener;
import client.gui.lib.GUIUtils;
import client.gui.lib.JImagePanel;
import client.gui.lib.SingleRowFlowLayout;
import client.gui.lib.TypedListWrapper;
import client.gui.lib.WrappedJLabel;

import common.AntiProbeMissile;
import common.Area;
import common.CarbonCarrier;
import common.DefenseModule;
import common.ExtractionModule;
import common.Fleet;
import common.GovernmentModule;
import common.GovernmentStarship;
import common.IBuilding;
import common.ICelestialBody;
import common.IMarker;
import common.ISpecialUnit;
import common.LaunchedPulsarMissile;
import common.Planet;
import common.Player;
import common.PlayerGameBoard;
import common.Diplomacy;
import common.Probe;
import common.ProductiveCelestialBody;
import common.PulsarLauchingPad;
import common.SEPUtils;
import common.SpaceCounter;
import common.SpaceRoad;
import common.StarshipPlant;
import common.StarshipTemplate;
import common.Unit;
import common.UnitMarker;
import common.Fleet.Move;
import common.Diplomacy.PlayerPolicies;
import common.Protocol.ServerRunningGame.RunningGameCommandException;
import common.SEPUtils.RealLocation;

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
public class RunningGamePanel extends javax.swing.JPanel implements UniverseRendererListener
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
			runningGameSouthPanel.add(getRunningGameCancelTurnBtn());
			runningGameSouthPanel.add(getRunningGameEndTurnBtn());
		}
		return runningGameSouthPanel;
	}

	private static void showRunningGameCommandExceptionMsg(RunningGameCommandException e)
	{
		e.printStackTrace();
		JOptionPane.showMessageDialog(null, e.getMessage(), "Game command exception", JOptionPane.ERROR_MESSAGE);
	}

	private JButton diplomacyBtn;
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
		}
		return diplomacyBtn;
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
						client.getRunningGameInterface().resetTurn();
						refreshGameBoard();
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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
						client.getRunningGameInterface().endTurn();
						refreshGameBoard();
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
						refreshGameBoard();
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

	private JButton runningGameCelestialBodyDetailsAttackFleetsBtn;
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
					if (!currentPlayer.isNamed(productiveCelestialBody.getOwnerName())) return;
					
					try
					{
						client.getRunningGameInterface().attackEnemiesFleet(productiveCelestialBody.getName());
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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

	private TypedListWrapper<JList, Unit>	runningGameCelestialBodyDetailsUnitsList;

	private TypedListWrapper<JList, Unit> getRunningGameCelestialBodyDetailsUnitsList()
	{
		if (runningGameCelestialBodyDetailsUnitsList == null)
		{			
			runningGameCelestialBodyDetailsUnitsList = new TypedListWrapper<JList, Unit>(Unit.class, new JList(), new Comparator<Unit>()
			{
				@Override
				public int compare(Unit o1, Unit o2)
				{
					int owner = o1.getOwnerName().compareTo(o2.getOwnerName());
					if (owner != 0) return owner;
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			runningGameCelestialBodyDetailsUnitsList.setCellRenderer(new TypedListWrapper.AbstractTypedJListCellRender<Unit>()
			{
				@Override
				public Component getListCellRendererComponent(JList list, Unit unit, int index, boolean isSelected, boolean cellHasFocus)
				{
					super.getListCellRendererComponent(list, unit, index, isSelected, cellHasFocus);
					label.setText(unit.getClass().getSimpleName() + " [" + ((unit.getOwnerName() != null) ? unit.getOwnerName() : "unknown") + "] " + unit.getName());
					return label;
				}
			});

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

	private PlayerGameBoard	currentGameBoard;

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
		if (currentGameBoard == null) return;

		Area newSelection = currentGameBoard.getArea(location);
		if (currentSelectedArea == newSelection) return;

		currentSelectedArea = newSelection;
		currentSelectedLocation = location;

		String selectedAreaDisplay = currentSelectedArea.toString();
		getRunningGameCelestialBodyDetailsContentLabel().setText(selectedAreaDisplay.substring(0, (selectedAreaDisplay.indexOf("Buildings") < 0) ? selectedAreaDisplay.length() : selectedAreaDisplay.indexOf("Buildings")));

		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();

		if (celestialBody != null && celestialBody.getLastObservation() >= 0 && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			Vector<String> buildingsList = new Vector<String>();

			Set<Class<? extends IBuilding>> buildingsTypes = new HashSet<Class<? extends IBuilding>>(SEPUtils.buildingTypes);

			ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
			if (productiveCelestialBody.getBuildings() != null)
			{
				for(IBuilding b : productiveCelestialBody.getBuildings())
				{
					buildingsList.add(buildingToLabel(b));
					buildingsTypes.remove(b.getClass());
				}
			}

			for(Class<? extends IBuilding> bt : buildingsTypes)
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
			
			if (currentPlayer.isNamed(productiveCelestialBody.getOwnerName()))
			{
				getRunningGameCelestialBodyDetailsAttackFleetsBtn().setVisible(true);
				try
				{
					getRunningGameCelestialBodyDetailsAttackFleetsBtn().setEnabled(client.getRunningGameInterface().canAttackEnemiesFleet(productiveCelestialBody.getName()));
				}
				catch(StateMachineNotExpectedEventException e)
				{
					e.printStackTrace();
				}
				catch(RpcException e)
				{
					e.printStackTrace();
				}				
			}
		}
		else
		{
			getRunningGameCelestialBodyDetailsBuildingsList().setVisible(false);
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

	private void addBuildBtns(ProductiveCelestialBody productiveCelestialBody, Class<? extends IBuilding> buildingType, int carbonCost, int nbBuild) throws StateMachineNotExpectedEventException, RpcException
	{
		addBuildBtns(productiveCelestialBody, buildingType, new int[] { carbonCost }, nbBuild);
	}

	private void addBuildBtns(final ProductiveCelestialBody productiveCelestialBody, final Class<? extends IBuilding> buildingType, int[] buildCosts, int nbBuild) throws StateMachineNotExpectedEventException, RpcException
	{
		JPanel buildBtnsPanel = new JPanel(new FlowLayout());
		buildBtnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

		JButton buildBtn = new JButton();
		String label = (nbBuild > 0) ? "Upgrade" : "Build";
		buildBtn.setText(label);
		buildBtn.setToolTipText(label + " " + (nbBuild + 1) + " " + buildingType.getSimpleName() + " for " + ((buildCosts[0] > 0) ? buildCosts[0] + "c" : "") + ((buildCosts.length > 1 && buildCosts[1] > 0) ? buildCosts[1] + "pop." : ""));
		buildBtn.setEnabled(client.getRunningGameInterface().canBuild(productiveCelestialBody.getName(), buildingType));
		buildBtn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					client.getRunningGameInterface().build(productiveCelestialBody.getName(), buildingType);					
				}
				catch(RunningGameCommandException e1)
				{
					showRunningGameCommandExceptionMsg(e1);
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
					refreshGameBoard();
				}
			}
		});
		buildBtnsPanel.add(buildBtn);

		JButton destroyBtn = new JButton();
		destroyBtn.setText("Demolish");
		destroyBtn.setToolTipText("Demolish 1 defense module to free one slot.");
		destroyBtn.setEnabled(client.getRunningGameInterface().canDemolish(productiveCelestialBody.getName(), buildingType));
		destroyBtn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					client.getRunningGameInterface().demolish(productiveCelestialBody.getName(), buildingType);					
				}
				catch(StateMachineNotExpectedEventException e1)
				{
					e1.printStackTrace();
				}
				catch(RpcException e1)
				{
					e1.printStackTrace();
				}
				catch(RunningGameCommandException e1)
				{
					showRunningGameCommandExceptionMsg(e1);
				}
				finally
				{
					refreshGameBoard();
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
		
		RealLocation unitLocation = currentGameBoard.getUnitLocation(unit.getOwnerName(), unit.getName());
		if (unitLocation == null) return;

		Area area = currentGameBoard.getArea(unitLocation);

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

				if (fleet.isUnasignedFleet())
				{
					displayStarshipPlantActionPanel();
				}
				else
				{

					// Actions btn	
					JPanel btnsPanel = new JPanel(new FlowLayout());
					btnsPanel.setPreferredSize(new Dimension(EAST_AREA_COMPONENTS_MAX_WIDTH, TEXTAREA_MAX_HEIGHT));

					// Dismantle fleet
					JButton dismantleBtn = new JButton();
					dismantleBtn.setText("Dismantle");
					dismantleBtn.setToolTipText("Dismantle fleet so starships land on plant.");
					dismantleBtn.setEnabled(isUnitOwner && client.getRunningGameInterface().canDismantleFleet(unit.getName()));
					dismantleBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								client.getRunningGameInterface().dismantleFleet(unit.getName());
							}
							catch(StateMachineNotExpectedEventException e1)
							{
								e1.printStackTrace();
							}
							catch(RpcException e1)
							{
								e1.printStackTrace();
							}
							catch(RunningGameCommandException e1)
							{
								showRunningGameCommandExceptionMsg(e1);
							}
							finally
							{
								refreshGameBoard();
							}
						}
					});
					btnsPanel.add(dismantleBtn);

					// Settle governement fleet
					JButton settleGvnmt = new JButton();
					settleGvnmt.setText("Settle government");
					settleGvnmt.setToolTipText("Settle governement.");
					settleGvnmt.setEnabled(isUnitOwner && fleet.isGovernmentFleet() && client.getRunningGameInterface().canSettleGovernment(celestialBody.getName()));
					settleGvnmt.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								client.getRunningGameInterface().settleGovernment(celestialBody.getName());
							}
							catch(StateMachineNotExpectedEventException e1)
							{
								e1.printStackTrace();
							}
							catch(RpcException e1)
							{
								e1.printStackTrace();
							}
							catch(RunningGameCommandException e1)
							{
								showRunningGameCommandExceptionMsg(e1);
							}
							finally
							{
								refreshGameBoard();
							}
						}
					});
					btnsPanel.add(settleGvnmt);

					getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);

					displayFleetActionPanel();
				}
			}
			else if (AntiProbeMissile.class.isInstance(unit))
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
							if (client.getRunningGameInterface().canFireAntiProbeMissile(infos.unit.getName(), target.getOwnerName(), target.getName()))
							{
								client.getRunningGameInterface().fireAntiProbeMissile(infos.unit.getName(), target.getOwnerName(), target.getName());
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Cannot fire antiprobe missile '" + infos.unit.getName() + "' onto '" + target.toString() + "'", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
						catch(StateMachineNotExpectedEventException e)
						{
							e.printStackTrace();
						}
						catch(RpcException e)
						{
							e.printStackTrace();
						}
						catch(RunningGameCommandException e)
						{
							showRunningGameCommandExceptionMsg(e);
						}
						finally
						{
							refreshGameBoard();
						}
					}
				});

				Set<Probe> probes = currentGameBoard.getUnits(Probe.class);				
				getAntiPulsarMissileTargetComboBox().clear();
				getAntiPulsarMissileTargetComboBox().addAll(probes);
				
				btnsPanel.add(getAntiPulsarMissileTargetComboBox().getComponent());
				btnsPanel.add(fireBtn);

				getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);
			}
			else if (CarbonCarrier.class.isInstance(unit))
			{

			}
			else if (LaunchedPulsarMissile.class.isInstance(unit))
			{

			}
			else if (Probe.class.isInstance(unit))
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
							if (!client.getRunningGameInterface().canLaunchProbe(infos.unit.getName(), dest))
							{
								JOptionPane.showMessageDialog(null, "Impossible", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}						
						
							client.getRunningGameInterface().launchProbe(infos.unit.getName(), dest);
						}						
						catch(StateMachineNotExpectedEventException e1)
						{
							e1.printStackTrace();
						}
						catch(RpcException e1)
						{
							e1.printStackTrace();
						}
						catch(RunningGameCommandException e1)
						{
							showRunningGameCommandExceptionMsg(e1);
						}
						finally
						{
							refreshGameBoard();
						}
					}
				});
				btnsPanel.add(launchBtn);

				getRunningGameFleetDetailsSpecificDetailsPanel().add(btnsPanel);
			}
			else
			{
				throw new Error(unit.getClass().getSimpleName() + " unit class details display not implemented yet");
			}
		}
		catch(RpcException e)
		{
			e.printStackTrace();
		}
		catch(StateMachineNotExpectedEventException e)
		{
			e.printStackTrace();
		}
		finally
		{
			updateUI();
		}
	}

	private JTextField launchProbeDestinationXTextField;
	private JTextField getLaunchProbeDestinationXTextField()
	{
		if (launchProbeDestinationXTextField == null)
		{
			launchProbeDestinationXTextField = new JTextField();
			launchProbeDestinationXTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationXTextField;
	}
	
	private JTextField launchProbeDestinationYTextField;
	private JTextField getLaunchProbeDestinationYTextField()
	{
		if (launchProbeDestinationYTextField == null)
		{
			launchProbeDestinationYTextField = new JTextField();
			launchProbeDestinationYTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationYTextField;
	}
	
	private JTextField launchProbeDestinationZTextField;
	private JTextField getLaunchProbeDestinationZTextField()
	{
		if (launchProbeDestinationZTextField == null)
		{
			launchProbeDestinationZTextField = new JTextField();
			launchProbeDestinationZTextField.setPreferredSize(new Dimension(20, 20));
		}
		return launchProbeDestinationZTextField;
	}
	
	private TypedListWrapper<JComboBox, Probe> antiPulsarMissileTargetComboBox;

	private TypedListWrapper<JComboBox, Probe> getAntiPulsarMissileTargetComboBox()
	{
		if (antiPulsarMissileTargetComboBox == null)
		{			
			antiPulsarMissileTargetComboBox = new TypedListWrapper<JComboBox, Probe>(Probe.class, new JComboBox(), new Comparator<Probe>()
			{
				
				@Override
				public int compare(Probe o1, Probe o2)
				{
					int owner = o1.getOwnerName().compareTo(o2.getOwnerName());
					if (owner != 0) return owner;
					return o1.getName().compareTo(o2.getName());					
				}
			});
			
			antiPulsarMissileTargetComboBox.setCellRenderer(new TypedListWrapper.AbstractTypedJListCellRender<Probe>()
			{
				@Override
				public Component getListCellRendererComponent(JList list, Probe value, int index, boolean isSelected, boolean cellHasFocus)
				{
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					label.setText("["+value.getOwnerName()+"] "+value.getName());
					return label;
				}
			});						
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

	private String buildingTypeToLabel(Class<? extends IBuilding> buildingType)
	{
		return buildingType.getSimpleName() + " (none)";
	}

	private String buildingToLabel(IBuilding building)
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

		Set<IBuilding> buildings = productiveCelestialBody.getBuildings();
		IBuilding selectedBuildings = null;
		Class<? extends IBuilding> selectedBuildingType = null;

		if (buildings != null) for(IBuilding b : buildings)
		{
			if (b.getClass().getSimpleName().compareTo(buildingTypeName) == 0)
			{
				selectedBuildings = b;
				break;
			}
		}

		if (selectedBuildings == null)
		{
			for(Class<? extends IBuilding> bt : SEPUtils.buildingTypes)
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
						buildCost = defenseModule.getNextBuildCost();
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
						buildCost = extractionModule.getNextBuildCost();
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
						buildBtn.setToolTipText("Embark the government on a government starship for " + GovernmentStarship.POPULATION_PRICE + "pop. and " + GovernmentStarship.CARBON_PRICE + "c.");
						buildBtn.setEnabled(client.getRunningGameInterface().canEmbarkGovernment());
						buildBtn.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e)
							{
								try
								{
									client.getRunningGameInterface().embarkGovernment();									
								}
								catch(StateMachineNotExpectedEventException e1)
								{
									e1.printStackTrace();
								}
								catch(RpcException e1)
								{
									e1.printStackTrace();
								}
								catch(RunningGameCommandException e1)
								{
									showRunningGameCommandExceptionMsg(e1);
								}
								finally
								{
									refreshGameBoard();
								}
							}
						});
						btnsPanel.add(buildBtn);

						getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
					}
					else
					{
						if (client.getRunningGameInterface().canSettleGovernment(productiveCelestialBody.getName()))
						{
							// Actions btn
							JPanel btnsPanel = new JPanel(new FlowLayout());

							JButton buildBtn = new JButton();
							buildBtn.setText("Settle");
							buildBtn.setToolTipText("Settle the government on this planet");
							buildBtn.setEnabled(true);
							buildBtn.addActionListener(new ActionListener()
							{

								@Override
								public void actionPerformed(ActionEvent e)
								{
									try
									{
										client.getRunningGameInterface().settleGovernment(productiveCelestialBody.getName());
									}
									catch(StateMachineNotExpectedEventException e1)
									{
										e1.printStackTrace();
									}
									catch(RpcException e1)
									{
										e1.printStackTrace();
									}
									catch(RunningGameCommandException e1)
									{
										showRunningGameCommandExceptionMsg(e1);
									}
									finally
									{
										refreshGameBoard();
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
					buildBtn.setToolTipText(label + " pulsar launching pad for " + PulsarLauchingPad.POPULATION_COST + "pop. and " + PulsarLauchingPad.CARBON_COST + "c.");
					buildBtn.setEnabled(client.getRunningGameInterface().canBuild(productiveCelestialBody.getName(), PulsarLauchingPad.class));
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								client.getRunningGameInterface().build(productiveCelestialBody.getName(), PulsarLauchingPad.class);
							}
							catch(RunningGameCommandException e1)
							{
								showRunningGameCommandExceptionMsg(e1);
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
								refreshGameBoard();
							}
						}
					});
					btnsPanel.add(buildBtn);

					JButton fireBtn = new JButton();
					fireBtn.setText("Fire");
					fireBtn.setToolTipText("Get access to pulsar missile launching controls.");
					fireBtn.setEnabled(client.getRunningGameInterface().canFirePulsarMissile(productiveCelestialBody.getName()));
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
		Set<ISpecialUnit> fleetToFormSpecialUnits = new HashSet<ISpecialUnit>();

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

		if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null)
		for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
		{
			if (specialUnit == null) continue;
			
			availableQt = 1;

			toFleet = Basic.intValueOf(getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(specialUnit).getText(), 0);
			
			if (toFleet > 0)
			{
				toFleet = 1;
				fleetToFormSpecialUnits.add(specialUnit);
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
			getStarshipPlantWorkshopMakeStarshipBtn().setEnabled(client.getRunningGameInterface().canMakeStarships(infos.productiveCelestialBody.getName(), starshipsToMake));
			getStarshipPlantFormFleetBtn().setEnabled(client.getRunningGameInterface().canFormFleet(infos.productiveCelestialBody.getName(), fleetName, fleetToFormStarships, fleetToFormSpecialUnits));

			getStarshipPlantWorkshopMakeProbeBtn().setEnabled(infos.building != null && client.getRunningGameInterface().canMakeProbes(infos.productiveCelestialBody.getName(), probeName, probeToMake));
			getStarshipPlantWorkshopMakeAntiProbeMissileBtn().setEnabled(infos.building != null && client.getRunningGameInterface().canMakeAntiProbeMissiles(infos.productiveCelestialBody.getName(), antiProbeMissileName, antiProbeMissileToMake));
		}
		catch(RpcException e)
		{
			e.printStackTrace();
		}
		catch(StateMachineNotExpectedEventException e)
		{
			e.printStackTrace();
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
					Set<ISpecialUnit> fleetToFormSpecialUnits = new HashSet<ISpecialUnit>();

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
						if (fleetToFormQt > 0) fleetToFormSpecialUnits.add(specialUnit);	
					}

					try
					{
						client.getRunningGameInterface().formFleet(infos.productiveCelestialBody.getName(), getStarshipPlantNewFleetNameTextField().getText(), fleetToFormStarships, fleetToFormSpecialUnits);

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
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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
							client.getRunningGameInterface().makeAntiProbeMissiles(infos.planet.getName(), antiProbeMissileBaseName, antiProbeMissileToMake);

							getAntiProbeMissileMakeQtTextField().setText("0");
						}
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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
							client.getRunningGameInterface().makeProbes(infos.planet.getName(), probeBaseName, probeToMake);

							getProbeMakeQtTextField().setText("0");
						}
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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
						client.getRunningGameInterface().makeStarships(infos.planet.getName(), starshipsToMake);

						for(StarshipTemplate starshipType : SEPUtils.starshipTypes)
						{
							getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).setText("0");
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
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
					}
				}
			});
		}
		return starshipPlantWorkshipMakeStarshipBtn;
	}

	private static class SelectedBuildingInfos<B extends IBuilding>
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

	private <B extends IBuilding> SelectedBuildingInfos<B> getSelectedBuildingInfos(Class<B> buildingType)
	{
		if (currentSelectedArea == null) return null;
		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		if (celestialBody == null) return null;
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) return null;
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		B building = null;
		for(IBuilding b : productiveCelestialBody.getBuildings())
		{
			if (buildingType.isInstance(b))
			{
				building = buildingType.cast(b);
				break;
			}
		}
		//if (building == null) return null;

		return new SelectedBuildingInfos(productiveCelestialBody, building);
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
		spaceCounterActionPanel.add(getSpaceRoadDestinationComboBox());
		spaceCounterActionPanel.add(getSpaceRoadCreateButton());
		spaceCounterActionPanel.add(getSpaceRoadsRemoveButton());

		spaceCounterActionPanel.add(getNextCarbonOrdersLabel());
		spaceCounterActionPanel.add(getNextCarbonOrdersScrollPane());
		spaceCounterActionPanel.add(getCarbonFreightLabel());
		spaceCounterActionPanel.add(getCarbonFreightDestinationComboBox());
		spaceCounterActionPanel.add(getCarbonFreightAmountTextField());
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
		
		getSpaceRoadsList().clear();
		getSpaceRoadsList().addAll(infos.building.getSpaceRoadsBuilt());
		getSpaceRoadsList().addAll(infos.building.getSpaceRoadsLinked());				
				
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

	private TypedListWrapper<JList, SpaceRoad>	spaceRoadsList;

	private TypedListWrapper<JList, SpaceRoad> getSpaceRoadsList()
	{
		if (spaceRoadsList == null)
		{
			spaceRoadsList = new TypedListWrapper<JList, SpaceRoad>(SpaceRoad.class, new JList(), new Comparator<SpaceRoad>()
			{
				@Override
				public int compare(SpaceRoad o1, SpaceRoad o2)
				{
					return o1.getCreationDate() - o2.getCreationDate();
				}
			});
			
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
						client.getRunningGameInterface().demolishSpaceRoad(infos.productiveCelestialBody.getName(), (infos.productiveCelestialBody.getName().compareTo(spaceRoad.getSource()) == 0 ? spaceRoad.getDestination() : spaceRoad.getSource()));
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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

	private JComboBox	spaceRoadDestinationComboBox;

	private JComboBox getSpaceRoadDestinationComboBox()
	{
		if (spaceRoadDestinationComboBox == null)
		{
			Set<ProductiveCelestialBody> dest = currentGameBoard.getCelestialBodiesWithBuilding(SpaceCounter.class);

			String[] cbData = new String[dest.size()];
			int i = 0;
			for(ProductiveCelestialBody d : dest)
			{
				cbData[i] = d.getName();
				++i;
			}
			spaceRoadDestinationComboBox = new JComboBox(cbData);
			spaceRoadDestinationComboBox.setBounds(getSpaceRoadCreateLabel().getX(), getSpaceRoadCreateLabel().getY() + getSpaceRoadCreateLabel().getHeight(), (int) (getSpaceRoadCreateLabel().getWidth() * 0.3), getSpaceRoadCreateLabel().getHeight());
		}
		return spaceRoadDestinationComboBox;
	}

	private JButton	spaceRoadCreateButton;

	private JButton getSpaceRoadCreateButton()
	{
		if (spaceRoadCreateButton == null)
		{
			spaceRoadCreateButton = new JButton("Create");
			spaceRoadCreateButton.setBounds(getSpaceRoadDestinationComboBox().getX() + getSpaceRoadDestinationComboBox().getWidth() + 2, getSpaceRoadDestinationComboBox().getY(), getSpaceRoadCreateLabel().getWidth() - getSpaceRoadDestinationComboBox().getWidth() - 4, getSpaceRoadDestinationComboBox().getHeight());
			spaceRoadCreateButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SelectedBuildingInfos<SpaceCounter> infos = getSelectedBuildingInfos(SpaceCounter.class);
					if (infos == null || infos.building == null || infos.productiveCelestialBody == null) return;
					
					Object obj = getSpaceRoadDestinationComboBox().getSelectedItem();
					if (obj == null) return;
					
					try
					{
						client.getRunningGameInterface().buildSpaceRoad(infos.productiveCelestialBody.getName(), obj.toString());
					}
					catch(StateMachineNotExpectedEventException e1)
					{
						e1.printStackTrace();
					}
					catch(RpcException e1)
					{
						e1.printStackTrace();
					}
					catch(RunningGameCommandException e1)
					{
						showRunningGameCommandExceptionMsg(e1);
					}
					finally
					{
						refreshGameBoard();
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
			nextCarbonOrdersScrollPane = new JScrollPane(getNextCarbonOrdersList());
			nextCarbonOrdersScrollPane.setPreferredSize(new Dimension(200, getNextCarbonOrdersList().getPreferredScrollableViewportSize().height));
			nextCarbonOrdersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			nextCarbonOrdersScrollPane.setBounds(getNextCarbonOrdersLabel().getX(), getNextCarbonOrdersLabel().getY() + getNextCarbonOrdersLabel().getHeight(), getNextCarbonOrdersLabel().getWidth(), getSpaceRoadsScrollPane().getHeight());
		}
		return nextCarbonOrdersScrollPane;
	}

	private JList	nextCarbonOrdersList;

	private JList getNextCarbonOrdersList()
	{
		if (nextCarbonOrdersList == null)
		{
			nextCarbonOrdersList = new JList();
			nextCarbonOrdersList.setVisibleRowCount(3);
			nextCarbonOrdersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			nextCarbonOrdersList.addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = nextCarbonOrdersList.locationToIndex(e.getPoint());
					if (index < 0) return;

					String label = nextCarbonOrdersList.getModel().getElementAt(index).toString();
					if (label == null) return;

					nextCarbonOrdersList.setToolTipText(label);

					super.mouseMoved(e);
				}
			});
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

	private JComboBox	carbonFreightDestinationComboBox;

	private JComboBox getCarbonFreightDestinationComboBox()
	{
		if (carbonFreightDestinationComboBox == null)
		{
			Set<ProductiveCelestialBody> dest = currentGameBoard.getCelestialBodiesWithBuilding(SpaceCounter.class);
			String[] cbData = new String[dest.size()];
			int i = 0;
			for(ProductiveCelestialBody d : dest)
			{
				cbData[i] = d.getName();
				++i;
			}
			carbonFreightDestinationComboBox = new JComboBox(cbData);
			carbonFreightDestinationComboBox.setBounds(getCarbonFreightLabel().getX(), getCarbonFreightLabel().getY() + getCarbonFreightLabel().getHeight(), (int) (getCarbonFreightLabel().getWidth() * 0.3), getCarbonFreightLabel().getHeight());
		}
		return carbonFreightDestinationComboBox;
	}

	private JTextField	carbonFreightAmountTextField;

	private JTextField getCarbonFreightAmountTextField()
	{
		if (carbonFreightAmountTextField == null)
		{
			carbonFreightAmountTextField = new JTextField();
			carbonFreightAmountTextField.setBounds(getCarbonFreightDestinationComboBox().getX() + getCarbonFreightDestinationComboBox().getWidth() + 2, getCarbonFreightDestinationComboBox().getY(), getCarbonFreightLabel().getWidth() - getCarbonFreightDestinationComboBox().getWidth() - 4, getCarbonFreightDestinationComboBox().getHeight());
		}
		return carbonFreightAmountTextField;
	}

	private JButton	carbonFreightAddButton;

	private JButton getCarbonFreightAddButton()
	{
		if (carbonFreightAddButton == null)
		{
			carbonFreightAddButton = new JButton("Add");
			carbonFreightAddButton.setBounds(getCarbonFreightLabel().getX(), getCarbonFreightAmountTextField().getY() + getCarbonFreightAmountTextField().getHeight() + 2, getCarbonFreightLabel().getWidth(), getCarbonFreightLabel().getHeight());
			carbonFreightAddButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					// TODO
					showTodoMsg();
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
					// TODO
					showTodoMsg();
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
					// TODO
					showTodoMsg();
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
					// TODO
					showTodoMsg();
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
			currentCarbonOrdersScrollPane = new JScrollPane(getCurrentCarbonOrdersList());
			currentCarbonOrdersScrollPane.setPreferredSize(new Dimension(200, getCurrentCarbonOrdersList().getPreferredScrollableViewportSize().height));
			currentCarbonOrdersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			currentCarbonOrdersScrollPane.setBounds(getCurrentCarbonOrdersLabel().getX(), getCurrentCarbonOrdersLabel().getY() + getCurrentCarbonOrdersLabel().getHeight(), getCurrentCarbonOrdersLabel().getWidth(), getNextCarbonOrdersScrollPane().getHeight());
		}
		return currentCarbonOrdersScrollPane;
	}

	private JList	currentCarbonOrdersList;

	private JList getCurrentCarbonOrdersList()
	{
		if (currentCarbonOrdersList == null)
		{
			currentCarbonOrdersList = new JList();
			currentCarbonOrdersList.setVisibleRowCount(3);
			currentCarbonOrdersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			currentCarbonOrdersList.addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = currentCarbonOrdersList.locationToIndex(e.getPoint());
					if (index < 0) return;

					String label = currentCarbonOrdersList.getModel().getElementAt(index).toString();
					if (label == null) return;

					currentCarbonOrdersList.setToolTipText(label);

					super.mouseMoved(e);
				}
			});
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
			carbonOrdersToReceiveScrollPane = new JScrollPane(getCarbonOrdersToReceiveList());
			carbonOrdersToReceiveScrollPane.setPreferredSize(new Dimension(200, getCarbonOrdersToReceiveList().getPreferredScrollableViewportSize().height));
			carbonOrdersToReceiveScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			carbonOrdersToReceiveScrollPane.setBounds(getCarbonOrdersToReceiveLabel().getX(), getCarbonOrdersToReceiveLabel().getY() + getCarbonOrdersToReceiveLabel().getHeight(), getCurrentCarbonOrdersScrollPane().getWidth(), getCurrentCarbonOrdersScrollPane().getHeight());
		}
		return carbonOrdersToReceiveScrollPane;
	}

	private JList	carbonOrdersToReceiveList;

	private JList getCarbonOrdersToReceiveList()
	{
		if (carbonOrdersToReceiveList == null)
		{
			carbonOrdersToReceiveList = new JList();
			carbonOrdersToReceiveList.setVisibleRowCount(3);
			carbonOrdersToReceiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			carbonOrdersToReceiveList.addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = carbonOrdersToReceiveList.locationToIndex(e.getPoint());
					if (index < 0) return;

					String label = carbonOrdersToReceiveList.getModel().getElementAt(index).toString();
					if (label == null) return;

					carbonOrdersToReceiveList.setToolTipText(label);

					super.mouseMoved(e);
				}
			});
		}
		return carbonOrdersToReceiveList;
	}

	///////

	private void displayDiplomacyActionPanel()
	{
		Map<String, Diplomacy> policies = currentGameBoard.getPlayersPolicies();
		
		JPanel diplomacyActionPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(policies.size()+1, policies.size()+1, 1, 1);
		diplomacyActionPanel.setLayout(gridLayout);
		
		diplomacyActionPanel.add(getDiplomacyPanelPlayerFieldLabel());
		
		diplomacyActionPanel.add(getDiplomacyPlayerLabel(currentPlayer));
		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;
			diplomacyActionPanel.add(getDiplomacyPlayerLabel(player));
		}
		
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
		
		for(Player owner : currentGamePlayers)
		{
			if (currentPlayer.isNamed(owner.getName())) continue;
			
			diplomacyActionPanel.add(getDiplomacyPlayerLabel(owner));
			
			Diplomacy diplomacy = policies.get(owner.getName());
			
			diplomacyActionPanel.add(diplomacy == null || diplomacy.getPolicies(currentPlayer.getName()) == null ? new JLabel() : new WrappedJLabel(diplomacy.getPolicies(currentPlayer.getName()).toString()));
			
			for(Player player : currentGamePlayers)
			{
				if (currentPlayer.isNamed(player.getName())) continue;
				if (owner.isNamed(player.getName()))
				{
					diplomacyActionPanel.add(new JLabel());
				}
				else
				{
					diplomacyActionPanel.add(diplomacy == null || diplomacy.getPolicies(player.getName()) == null ? new JLabel() : new WrappedJLabel(diplomacy.getPolicies(player.getName()).toString()));
				}
			}
		}

		getDiplomacyActionScrollPane().setViewportView(diplomacyActionPanel);

		getActionPanel().removeAll();
		getActionPanel().add(getDiplomacyActionScrollPane(), BorderLayout.CENTER);

		getRunningGameTabbedPanel().setTitleAt(getRunningGameTabbedPanel().indexOfComponent(getActionPanel()), "Diplomacy");

		refreshDiplomacyActionPanel();

		updateUI();
	}
	
	private void refreshDiplomacyActionPanel()
	{
		if (currentGameBoard == null || currentPlayer == null) return;
		
		Diplomacy diplomacy = currentGameBoard.getPlayersPolicies().get(currentPlayer.getName());
		
		for(Player player : currentGamePlayers)
		{
			if (currentPlayer.isNamed(player.getName())) continue;
			
			PlayerPolicies policies = diplomacy.getPolicies(player.getName());
			
			getDiplomacyPlayerHomeDiplomacyPanel(player).setSelected(policies.isAllowedToLandFleetInHomeTerritory());
			getDiplomacyPlayerForeignDiplomacyPanel(player).setSelected(policies.isAlwaysEngagedInConflictOnStrangerTerritory());
		}
				
		updateUI();
	}
	
	private JScrollPane diplomacyActionScrollPane;
	private JScrollPane getDiplomacyActionScrollPane()
	{
		if (diplomacyActionScrollPane == null)
		{
			diplomacyActionScrollPane = new JScrollPane();
		}
		return diplomacyActionScrollPane;
	}
	
	private JLabel diplomacyPanelPlayerFieldLabel;
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
	
	private Map<String, JCheckBox> playersHomeDiplomacy = new Hashtable<String, JCheckBox>();
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
	
	private Map<String, JCheckBox> playersForeignDiplomacy = new Hashtable<String, JCheckBox>();
	private JCheckBox getDiplomacyPlayerForeignDiplomacyPanel(Player player)
	{
		if (!playersForeignDiplomacy.containsKey(player.getName()))
		{
			JCheckBox checkbox = new JCheckBox("Hostile in foreign conflicts");
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
			if (currentPlayer.isNamed(player.getName())) continue;;;;
			boolean allowFleetAtHome = getDiplomacyPlayerHomeDiplomacyPanel(player).isSelected();
			boolean alwaysEngageFightInStrangerTerritory = getDiplomacyPlayerForeignDiplomacyPanel(player).isSelected();
			policies.put(player.getName(), new PlayerPolicies(player.getName(), allowFleetAtHome, alwaysEngageFightInStrangerTerritory));
		}
		
		try
		{
			client.getRunningGameInterface().changeDiplomacy(new Diplomacy(currentPlayer, policies));
		}
		catch(RpcException e)
		{
			e.printStackTrace();
		}
		catch(StateMachineNotExpectedEventException e)
		{
			e.printStackTrace();
		}
		catch(RunningGameCommandException e)
		{
			showRunningGameCommandExceptionMsg(e);
		}
		finally
		{
			refreshGameBoard();
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
			Set<ICelestialBody> celestialBodies = currentGameBoard.getCelestialBodies();
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
			client.getRunningGameInterface().moveFleet(infos.unit.getName(), checkpoints);
		}
		catch(StateMachineNotExpectedEventException e)
		{
			e.printStackTrace();
		}
		catch(RpcException e)
		{
			e.printStackTrace();
		}
		catch(RunningGameCommandException e)
		{
			showRunningGameCommandExceptionMsg(e);
		}
		finally
		{
			getFleetMoveCheckPointList().getComponent().setEnabled(false);
			refreshGameBoard();
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

	private TypedListWrapper<JList, Fleet.Move>	fleetMoveCheckPointList;

	private TypedListWrapper<JList, Fleet.Move> getFleetMoveCheckPointList()
	{
		if (fleetMoveCheckPointList == null)
		{
			fleetMoveCheckPointList = new TypedListWrapper<JList, Fleet.Move>(Fleet.Move.class, new JList(), new Comparator<Fleet.Move>()
			{
				@Override
				public int compare(Move o1, Move o2)
				{
					return o1.getDestinationName().compareTo(o2.getDestinationName());
				}
			});
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
		
		if (infos.productiveCelestialBody.getUnasignedSpecialUnits() != null)
		for(ISpecialUnit specialUnit : infos.productiveCelestialBody.getUnasignedSpecialUnits())
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
		return getStarshipPlantWorkshopLabel(StarshipTemplate.class.getName()+starshipType.getName(), starshipType.getName());
	}
	
	JLabel getStarshipPlantWorkshopSpecialUnitNameLabel(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopLabel(specialUnit.getClass().getName()+specialUnit.getName(), specialUnit.getName());
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
		return getStarshipPlantWorkshopQtLabel(starshipType.getClass().getName()+starshipType.getName());
	}
	
	JLabel getStarshipPlantWorkshopSpecialUnitQtLabel(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopQtLabel(specialUnit.getClass().getName()+specialUnit.getName());
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
		return getStarshipPlantWorkshopNewFleetQtTextField(starshipType.getClass().getName()+starshipType.getName());
	}
	
	JTextField getStarshipPlantWorkshopSpecialUnitNewFleetQtTextField(ISpecialUnit specialUnit)
	{
		return getStarshipPlantWorkshopNewFleetQtTextField(specialUnit.getClass().getName()+specialUnit.getName());
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
					currentGameBoard = client.getRunningGameInterface().getPlayerGameBoard();
					refreshGameBoard(currentGameBoard);
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
		});
	}

	void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		currentGameBoard = gameBoard;
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
	}

	private void refreshShortcutBtns()
	{
		try
		{
			getRunningGameCancelTurnBtn().setEnabled(client.getRunningGameInterface().canResetTurn());
			getRunningGameEndTurnBtn().setEnabled(client.getRunningGameInterface().canEndTurn());
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
					players = client.getRunningGameInterface().getPlayerList();
					refreshPlayerList(players);
				}
				catch(StateMachineNotExpectedEventException e)
				{
					e.printStackTrace();
				}
				catch(RpcException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private Stack<Player> currentGamePlayers = new Stack<Player>();
	private Player currentPlayer = null;
	
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
