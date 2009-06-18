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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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

import sun.awt.VerticalBagLayout;

import client.SEPClient;
import client.gui.UniverseRenderer.UniverseRendererListener;
import client.gui.lib.GUIUtils;
import client.gui.lib.JImagePanel;
import client.gui.lib.SingleRowFlowLayout;
import client.gui.lib.WrappedJLabel;

import common.Area;
import common.DefenseModule;
import common.ExtractionModule;
import common.Fleet;
import common.GovernmentModule;
import common.GovernmentStarship;
import common.IBuilding;
import common.ICelestialBody;
import common.IStarship;
import common.Planet;
import common.Player;
import common.PlayerGameBoard;
import common.ProductiveCelestialBody;
import common.PulsarLauchingPad;
import common.SEPUtils;
import common.SpaceCounter;
import common.StarshipPlant;
import common.Unit;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

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
	private static final int EAST_AREA_WIDTH = 200;
	private static final int CELESTIALBODY_DETAILS_AREA_HEIGHT = 530;
	
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
			runningGameSouthPanel.add(getRunningGameCancelTurnBtn());
			runningGameSouthPanel.add(getRunningGameEndTurnBtn());
		}
		return runningGameSouthPanel;
	}
	
	private JButton runningGameCancelTurnBtn;
	
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
						e1.printStackTrace();
					}
					refreshGameBoard();
				}
			});
		}
		
		return runningGameCancelTurnBtn;
	}
	
	private JButton runningGameEndTurnBtn;

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
					refreshGameBoard();
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
			
			runningGameFleetDetails.setBorder(BorderFactory.createLineBorder(Color.blue));
			runningGameFleetDetails.add(getRunningGameFleetDetailsLabel(), BorderLayout.NORTH);
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
			runningGameShortcutBarLabel.setHorizontalAlignment(SwingConstants.LEFT);
			runningGameShortcutBarLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			runningGameShortcutBarLabel.setVerticalAlignment(SwingConstants.TOP);
			runningGameShortcutBarLabel.setVerticalTextPosition(SwingConstants.TOP);
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
			runningGameTabbedPanel.addTab("Chat panel", null, getRunningGameChatPanel(), null);
			runningGameTabbedPanel.addTab("Action", null, getRunningGameActionPanel(), null);
		}
		return runningGameTabbedPanel;
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

	private JPanel	runningGameActionPanel;

	private JPanel getRunningGameActionPanel()
	{
		if (runningGameActionPanel == null)
		{
			runningGameActionPanel = new JPanel();
			BorderLayout runningGameActionPanelLayout = new BorderLayout();
			runningGameActionPanel.setLayout(runningGameActionPanelLayout);
		}
		return runningGameActionPanel;
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

	private JLabel	runningGameCelestialBodyDetailsLabel;

	private JLabel getRunningGameCelestialBodyDetailsLabel()
	{
		if (runningGameCelestialBodyDetailsLabel == null)
		{
			runningGameCelestialBodyDetailsLabel = new JLabel();
			runningGameCelestialBodyDetailsLabel.setText("Celestial body details");
			runningGameCelestialBodyDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
			runningGameCelestialBodyDetailsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			runningGameCelestialBodyDetailsLabel.setVerticalAlignment(SwingConstants.TOP);
			runningGameCelestialBodyDetailsLabel.setVerticalTextPosition(SwingConstants.TOP);
		}
		return runningGameCelestialBodyDetailsLabel;
	}

	private JLabel	runningGameFleetDetailsLabel;

	private JLabel getRunningGameFleetDetailsLabel()
	{
		if (runningGameFleetDetailsLabel == null)
		{
			runningGameFleetDetailsLabel = new JLabel();
			runningGameFleetDetailsLabel.setText("Fleet details");
			runningGameFleetDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
			runningGameFleetDetailsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			runningGameFleetDetailsLabel.setVerticalAlignment(SwingConstants.TOP);
			runningGameFleetDetailsLabel.setVerticalTextPosition(SwingConstants.TOP);
		}
		return runningGameFleetDetailsLabel;
	}

	private JScrollPane	runningGameCelestialBodyDetailsScrollPanel;

	private JScrollPane getRunningGameCelestialBodyDetailsScrollPane()
	{
		if (runningGameCelestialBodyDetailsScrollPanel == null)
		{
			JPanel runningGameCelestialBodyDetailsPanel = new JPanel();
			BoxLayout runningGameCelestialBodyDetailsPanelLayout = new BoxLayout(runningGameCelestialBodyDetailsPanel, BoxLayout.Y_AXIS);
			runningGameCelestialBodyDetailsPanel.setLayout(runningGameCelestialBodyDetailsPanelLayout);
			runningGameCelestialBodyDetailsPanel.setPreferredSize(new Dimension(EAST_AREA_WIDTH, CELESTIALBODY_DETAILS_AREA_HEIGHT));
			
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsContentPanel());
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsPanel());			
			
			runningGameCelestialBodyDetailsScrollPanel = new JScrollPane(runningGameCelestialBodyDetailsPanel);
			runningGameCelestialBodyDetailsScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			runningGameCelestialBodyDetailsScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			runningGameCelestialBodyDetailsScrollPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}
		return runningGameCelestialBodyDetailsScrollPanel;
	}

	private JPanel	runningGameCelestialBodyDetailsContentPanel;

	private JPanel getRunningGameCelestialBodyDetailsContentPanel()
	{
		if (runningGameCelestialBodyDetailsContentPanel == null)
		{
			runningGameCelestialBodyDetailsContentPanel = new JPanel();
			//BoxLayout runningGameCelestialBodyDetailsContentPanelLayout = new BoxLayout(runningGameCelestialBodyDetailsContentPanel, BoxLayout.Y_AXIS);			
			FlowLayout runningGameCelestialBodyDetailsContentPanelLayout = new SingleRowFlowLayout(FlowLayout.LEFT);			
			
			runningGameCelestialBodyDetailsContentPanel.setLayout(runningGameCelestialBodyDetailsContentPanelLayout);
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsContentLabel());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsBuildingsListScrollPane());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsUnitsListScrollPane());
		}
		return runningGameCelestialBodyDetailsContentPanel;
	}

	private JLabel	runningGameCelestialBodyDetailsContentLabel;

	private JLabel getRunningGameCelestialBodyDetailsContentLabel()
	{
		if (runningGameCelestialBodyDetailsContentLabel == null)
		{
			runningGameCelestialBodyDetailsContentLabel = new JLabel();
		}
		return runningGameCelestialBodyDetailsContentLabel;
	}

	private JScrollPane runningGameCelestialBodyDetailsBuildingsListScrollPane;
	private JScrollPane getRunningGameCelestialBodyDetailsBuildingsListScrollPane()
	{
		if (runningGameCelestialBodyDetailsBuildingsListScrollPane == null)
		{
			runningGameCelestialBodyDetailsBuildingsListScrollPane = new JScrollPane(getRunningGameCelestialBodyDetailsBuildingsList());
			runningGameCelestialBodyDetailsBuildingsListScrollPane.setPreferredSize(new Dimension(180, getRunningGameCelestialBodyDetailsBuildingsList().getPreferredScrollableViewportSize().height));
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

	private JLabel	runningGameCelestialBodyDetailsBuildingDetailsContentLabel;

	private JList	runningGameCelestialBodyDetailsUnitsList;

	private JList getRunningGameCelestialBodyDetailsUnitsList()
	{
		if (runningGameCelestialBodyDetailsUnitsList == null)
		{
			runningGameCelestialBodyDetailsUnitsList = new JList();
			runningGameCelestialBodyDetailsUnitsList.setVisibleRowCount(5);			
			runningGameCelestialBodyDetailsUnitsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			runningGameCelestialBodyDetailsUnitsList.addMouseMotionListener(new MouseMotionAdapter()
			{				
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = runningGameCelestialBodyDetailsUnitsList.locationToIndex(e.getPoint());					
					if (index < 0) return;
					
					String label = runningGameCelestialBodyDetailsUnitsList.getModel().getElementAt(index).toString();
					if (label == null) return;
					
					runningGameCelestialBodyDetailsUnitsList.setToolTipText(label);
					
					super.mouseMoved(e);
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
			runningGameCelestialBodyDetailsUnitsListPane = new JScrollPane(getRunningGameCelestialBodyDetailsUnitsList());
			runningGameCelestialBodyDetailsUnitsListPane.setPreferredSize(new Dimension(180, getRunningGameCelestialBodyDetailsUnitsList().getPreferredScrollableViewportSize().height));
			runningGameCelestialBodyDetailsUnitsListPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return runningGameCelestialBodyDetailsUnitsListPane;
	}

	private JPanel		runningGameCelestialBodyDetailsBuildingDetailsPanel;
	private JScrollPane	runningGameChatPlayerListScrollPane;

	private JEditorPane	runningGameChatContentEditorPane;

	private JScrollPane	runningGameChatScrollPane;

	private JPanel		runningGameChatPlayerListPanel;

	private JTextField	runningGameChatTextField;

	private JPanel getRunningGameCelestialBodyDetailsBuildingDetailsPanel()
	{
		if (runningGameCelestialBodyDetailsBuildingDetailsPanel == null)
		{
			runningGameCelestialBodyDetailsBuildingDetailsPanel = new JPanel();
			SingleRowFlowLayout runningGameCelestialBodyDetailsBuildingDetailsPanelLayout = new SingleRowFlowLayout();
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

	private JPanel	runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel;

	private JPanel getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel()
	{
		if (runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel == null)
		{
			runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel = new JPanel();
		}
		return runningGameCelestialBodyDetailsBuildingSpecificDetailsPanel;
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
	private int[]			currentSelectedLocation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.gui.UniverseRenderer.UniverseRendererListener#updateSelectedArea(int, int, int)
	 */
	@Override
	public void updateSelectedArea(int x, int y, int z)
	{
		if (currentGameBoard == null) return;

		Area newSelection = currentGameBoard.getArea(x, y, z);
		if (currentSelectedArea == newSelection) return;

		currentSelectedArea = newSelection;
		currentSelectedLocation = new int[]{x, y, z};

		String selectedAreaDisplay = currentSelectedArea.toString();
		getRunningGameCelestialBodyDetailsContentLabel().setText("<html>"
				+ selectedAreaDisplay.substring(0, (selectedAreaDisplay.indexOf("Buildings") < 0) ? selectedAreaDisplay.length() : selectedAreaDisplay.indexOf("Buildings")).replace("\n", "<br>")
				+ "</html>");
		
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
					buildingsList.add(b.getClass().getSimpleName() + " (" + b.getBuildSlotsCount() + ")");
					buildingsTypes.remove(b.getClass());
				}
			}

			for(Class<? extends IBuilding> bt : buildingsTypes)
			{
				if (productiveCelestialBody.canBuildType(bt))
				{
					buildingsList.add(bt.getSimpleName() + " (none)");
				}
			}
			
			Object lastSelectedValue = getRunningGameCelestialBodyDetailsBuildingsList().getSelectedValue();
			getRunningGameCelestialBodyDetailsBuildingsList().setListData(buildingsList);
			if (lastSelectedValue != null)
			{
				getRunningGameCelestialBodyDetailsBuildingsList().setSelectedValue(lastSelectedValue, true);				
			}
			getRunningGameCelestialBodyDetailsBuildingsList().setVisible(true);
		}
		else
		{
			getRunningGameCelestialBodyDetailsBuildingsList().setVisible(false);
		}
		
		
		Set<Unit> units = currentSelectedArea.getUnits();
		if (units != null)
		{
			Vector<String> unitsList = new Vector<String>();
			
			for(Unit u : units)
			{
				unitsList.add("[" + u.getOwner().getName() + "] " + u.getName());
			}
			
			Object lastSelectedValue = getRunningGameCelestialBodyDetailsUnitsList().getSelectedValue();
			getRunningGameCelestialBodyDetailsUnitsList().setListData(unitsList);
			if (lastSelectedValue != null)
			{
				getRunningGameCelestialBodyDetailsUnitsList().setSelectedValue(lastSelectedValue, true);				
			}
			getRunningGameCelestialBodyDetailsUnitsList().setVisible(true);
		}
		else
		{
			getRunningGameCelestialBodyDetailsUnitsList().setVisible(false);
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

		JButton buildBtn = new JButton();
		String label = (nbBuild > 0) ? "Upgrade" : "Build";
		buildBtn.setText(label);
		buildBtn.setToolTipText(label + " " + (nbBuild + 1) + " " + buildingType.getSimpleName() + " for " + ((buildCosts[0] > 0) ? buildCosts[0] + "c" : "")
				+ ((buildCosts.length > 1 && buildCosts[1] > 0) ? buildCosts[1] + "pop." : ""));
		buildBtn.setEnabled(client.getRunningGameInterface().canBuild(productiveCelestialBody.getName(), buildingType));
		buildBtn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					client.getRunningGameInterface().build(productiveCelestialBody.getName(), buildingType);
					refreshGameBoard();
				}
				catch(RunningGameCommandException e1)
				{
					e1.printStackTrace();
				}
				catch(StateMachineNotExpectedEventException e1)
				{
					e1.printStackTrace();
				}
				catch(RpcException e1)
				{
					e1.printStackTrace();
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
			}
		});
		buildBtnsPanel.add(destroyBtn);

		getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(buildBtnsPanel);
	}

	private void eraseBuildingDetails()
	{
		getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel().setText("");
		getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().removeAll();
	}

	private void refreshBuildingDetails()
	{
		Object obj = getRunningGameCelestialBodyDetailsBuildingsList().getSelectedValue();
		if (obj == null)
		{
			eraseBuildingDetails();
			return;
		}
		
		String value = obj.toString();
		value = value.substring(0, (value.indexOf(" ") < 0) ? value.length() : value.indexOf(" "));
		refreshBuildingDetails(value);
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
			if (productiveCelestialBody.getOwner() != null && productiveCelestialBody.getOwner().isNamed(player.getName())
					&& (selectedBuildings != null || productiveCelestialBody.canBuildType(selectedBuildingType)))
			{
				if ((selectedBuildings != null && DefenseModule.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && DefenseModule.class.equals(selectedBuildingType)))
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
						buildCost = DefenseModule.FIRST_BUILD_COST;
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, DefenseModule.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && ExtractionModule.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && ExtractionModule.class.equals(selectedBuildingType)))
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
						buildCost = ExtractionModule.FIRST_BUILD_COST;
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, ExtractionModule.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && GovernmentModule.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && GovernmentModule.class.equals(selectedBuildingType)))
				{
					Planet planet = Planet.class.cast(productiveCelestialBody);

					if (selectedBuildings != null)
					{
						GovernmentModule governmentModule = GovernmentModule.class.cast(selectedBuildings);

						// Actions btn
						JPanel btnsPanel = new JPanel(new FlowLayout());

						JButton buildBtn = new JButton();
						buildBtn.setText("Embark");
						buildBtn.setToolTipText("Embark the government on a government starship for " + GovernmentStarship.PRICE_POPULATION + "pop. and "
								+ GovernmentStarship.PRICE_CARBON + "c.");
						buildBtn.setEnabled(client.getRunningGameInterface().canEmbarkGovernment());
						buildBtn.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e)
							{
								try
								{
									client.getRunningGameInterface().embarkGovernment();
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
										client.getRunningGameInterface().settleGovernment();
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
								}
							});
							btnsPanel.add(buildBtn);

							getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
						}
					}
				}
				else if ((selectedBuildings != null && PulsarLauchingPad.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && PulsarLauchingPad.class.equals(selectedBuildingType)))
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
					buildBtn.setToolTipText(label + " pulsar launching pad for " + PulsarLauchingPad.PRICE_POPULATION + "pop. and "
							+ PulsarLauchingPad.PRICE_CARBON + "c.");
					buildBtn.setEnabled(client.getRunningGameInterface().canBuild(productiveCelestialBody.getName(), PulsarLauchingPad.class));
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								client.getRunningGameInterface().build(productiveCelestialBody.getName(), PulsarLauchingPad.class);
								refreshGameBoard();
							}
							catch(RunningGameCommandException e1)
							{
								e1.printStackTrace();
							}
							catch(StateMachineNotExpectedEventException e1)
							{
								e1.printStackTrace();
							}
							catch(RpcException e1)
							{
								e1.printStackTrace();
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
						}
					});
					btnsPanel.add(fireBtn);

					getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
				}
				else if ((selectedBuildings != null && SpaceCounter.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && SpaceCounter.class.equals(selectedBuildingType)))
				{
					int buildCost = SpaceCounter.PRICE;
					int nbBuild;

					if (selectedBuildings != null)
					{
						SpaceCounter spaceCounter = SpaceCounter.class.cast(selectedBuildings);
						nbBuild = spaceCounter.getBuildSlotsCount();
					}
					else
					{
						nbBuild = 0;
					}

					// Build btn
					addBuildBtns(productiveCelestialBody, SpaceCounter.class, buildCost, nbBuild);
				}
				else if ((selectedBuildings != null && StarshipPlant.class.isInstance(selectedBuildings))
						|| (selectedBuildingType != null && StarshipPlant.class.equals(selectedBuildingType)))
				{
					Planet planet = Planet.class.cast(productiveCelestialBody);

					int nbBuild;
					int[] buildCosts = new int[] { StarshipPlant.PRICE_CARBON, StarshipPlant.PRICE_POPULATION };

					if (selectedBuildings != null)
					{
						StarshipPlant starshipPlant = StarshipPlant.class.cast(selectedBuildings);
						nbBuild = starshipPlant.getBuildSlotsCount();
						displayStarshipPlantActionPanel(currentSelectedArea, planet, selectedBuildings);
					}
					else
					{
						nbBuild = 0;
					}

					// Actions btn
					addBuildBtns(productiveCelestialBody, StarshipPlant.class, buildCosts, nbBuild);
					
					// TODO : Refresh Action panel
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
	}

	private void refreshStarshipPlantActionPanel(Planet planet, StarshipPlant plant)
	{
		getStarshipPlantWorkshopTitleLabel().setText("Available starships on "+planet.getName());
		
		int availableQt, toMake, toFleet;
		int makeCarbonPrice = 0, makePopulationPrice = 0;
		int carbonPrice, populationPrice;
		
		Map<Class<? extends IStarship>, Integer> starshipsToMake = new HashMap<Class<? extends IStarship>, Integer>();
		Map<Class<? extends IStarship>, Integer> fleetToForm = new HashMap<Class<? extends IStarship>, Integer>();
		
		for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
		{
			if (plant.getLandedStarships() == null)
			{
				availableQt = 0;
			}
			else
			{
				availableQt = (plant.getLandedStarships().get(starshipType)==null)?0:plant.getLandedStarships().get(starshipType);
			}
			
			toMake = Basic.intValueOf(getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).getText(), 0);				
			toFleet = Basic.intValueOf(getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).getText(), 0);
			
			try
			{
				carbonPrice = starshipType.getField("PRICE_CARBON").getInt(null);
				populationPrice = starshipType.getField("PRICE_POPULATION").getInt(null);
			}
			catch(Throwable t)
			{
				throw new Error("Static constants PRICE_CARBON and PRICE_POPULATION not defined for class "+starshipType.getName(), t);
			}
			
			makeCarbonPrice += toMake * carbonPrice;
			makePopulationPrice += toMake * populationPrice;
			
			starshipsToMake.put(starshipType, toMake);
			fleetToForm.put(starshipType, toFleet);
			
			// Update display			
			getStarshipPlantWorkshopStarshipQtLabel(starshipType).setText(String.format("%d (%d)", availableQt, availableQt - toFleet));
			getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).setText(String.valueOf(toMake));
			getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).setText(String.valueOf(toFleet));
			
		}
		
		String fleetName = getStarshipPlantNewFleetNameTextField().getText();
		if (fleetName == null || fleetName.isEmpty())
		{
			fleetName = Unit.generateName();
			getStarshipPlantNewFleetNameTextField().setText(fleetName);
		}
		
		getStarshipPlantWorkshopMakeStarshipBtn().setToolTipText("Make starships for "+makeCarbonPrice+"c and "+makePopulationPrice+"pop.");
		try
		{
			getStarshipPlantWorkshopMakeStarshipBtn().setEnabled(client.getRunningGameInterface().canMakeStarships(planet.getName(), starshipsToMake));		
			getStarshipPlantFormFleetBtn().setEnabled(client.getRunningGameInterface().canFormFleet(planet.getName(), fleetName, fleetToForm));
		}
		catch(RpcException e)
		{
			e.printStackTrace();
		}
		catch(StateMachineNotExpectedEventException e)
		{
			e.printStackTrace();
		}
	}
	
	private void displayStarshipPlantActionPanel(Area selectedArea, final Planet selectedPlanet, IBuilding selectedBuildings)
	{
		StarshipPlant starshipPlant = null;
		for(IBuilding b : selectedPlanet.getBuildings())
		{
			if (StarshipPlant.class.isInstance(b))
			{
				starshipPlant = StarshipPlant.class.cast(b);
				break;
			}
		}
		
		if (starshipPlant == null) return;
		
		final StarshipPlant fStarshipPlant = starshipPlant;
		
		FocusListener starshipPlantActionFocusListener = new FocusAdapter()
		{
			private final Planet planet = selectedPlanet;
			private final StarshipPlant plant = fStarshipPlant;
			
			@Override
			public void focusLost(FocusEvent arg0)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
				
					@Override
					public void run()
					{
						refreshStarshipPlantActionPanel(planet, plant);
					}
				});				
			}
					
		};
		
		ActionListener starshipPlantActionMakeStarshipsBtnActionListener = new ActionListener()
		{
			private final Planet planet = selectedPlanet;
			private final StarshipPlant plant = fStarshipPlant;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Map<Class<? extends IStarship>, Integer> starshipsToMake = new HashMap<Class<? extends IStarship>, Integer>();
				
				for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
				{								
					int toMake = Basic.intValueOf(getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).getText(), 0);									
					if (toMake > 0) starshipsToMake.put(starshipType, toMake);										
				}
				
				try
				{
					client.getRunningGameInterface().makeStarships(planet.getName(), starshipsToMake);
					
					for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
					{								
						getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType).setText("0");																									
					}
					
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
					e1.printStackTrace();
				}				
			}
		};
		
		ActionListener starshipPlantActionFormFleetBtnActionListener = new ActionListener()
		{
			private final Planet planet = selectedPlanet;
			private final StarshipPlant plant = fStarshipPlant;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Map<Class<? extends IStarship>, Integer> fleetToForm = new HashMap<Class<? extends IStarship>, Integer>();
				
				for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
				{								
					int fleetToFormQt = Basic.intValueOf(getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).getText(), 0);									
					if (fleetToFormQt > 0) fleetToForm.put(starshipType, fleetToFormQt);					
				}
				
				try
				{
					client.getRunningGameInterface().formFleet(planet.getName(), getStarshipPlantNewFleetNameTextField().getText(), fleetToForm);
					
					for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
					{								
						getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType).setText("0");									
						getStarshipPlantNewFleetNameTextField().setText(Unit.generateName());
					}
					
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
					e1.printStackTrace();
				}				
			}
		};
		
		JScrollPane starshipPlantActionScrollPane = new JScrollPane();
		JPanel starshipPlantActionPanel = new JPanel(null);
		
		starshipPlantActionPanel.add(getStarshipPlantWorkshopTitleLabel());
		starshipPlantActionPanel.add(getStarshipPlantWorkshopColumnLabel());
		int y = getStarshipPlantWorkshopColumnLabel().getY() + getStarshipPlantWorkshopColumnLabel().getHeight() + 5;
		
		for(Class<? extends IStarship> starshipType : SEPUtils.starshipTypes)
		{
			JLabel typeLabel = getStarshipPlantWorkshopStarshipTypeLabel(starshipType);
			typeLabel.setBounds(0, y, 200, 20);
			starshipPlantActionPanel.add(typeLabel);
			
			JLabel qtLabel = getStarshipPlantWorkshopStarshipQtLabel(starshipType);
			int qt = starshipPlant.getLandedStarships().containsKey(starshipType)?starshipPlant.getLandedStarships().get(starshipType):0;			
			qtLabel.setText(qt+" ("+qt+")");
			qtLabel.setBounds(200, y, 100, 20);
			starshipPlantActionPanel.add(qtLabel);
			
			JTextField makeQt = getStarshipPlantWorkshopStarshipQtToMakeTextField(starshipType);
			makeQt.setBounds(300, y, 40, 20);
			for(FocusListener l : makeQt.getFocusListeners()) makeQt.removeFocusListener(l);
			makeQt.addFocusListener(starshipPlantActionFocusListener);
			starshipPlantActionPanel.add(makeQt);
			
			JTextField fleetQt = getStarshipPlantWorkshopStarshipNewFleetQtTextField(starshipType);
			fleetQt.setBounds(400, y, 40, 20);
			for(FocusListener l : fleetQt.getFocusListeners()) fleetQt.removeFocusListener(l);
			fleetQt.addFocusListener(starshipPlantActionFocusListener);
			starshipPlantActionPanel.add(fleetQt);
			
			y += 25;
		}
		
		JButton makeStarshipBtn = getStarshipPlantWorkshopMakeStarshipBtn();
		for(ActionListener l : makeStarshipBtn.getActionListeners()) makeStarshipBtn.removeActionListener(l);
		makeStarshipBtn.addActionListener(starshipPlantActionMakeStarshipsBtnActionListener);
		makeStarshipBtn.setBounds(275, y, 90, 20);
		starshipPlantActionPanel.add(makeStarshipBtn);
		
		JTextField newFleetNameTextField = getStarshipPlantNewFleetNameTextField();
		for(FocusListener l : newFleetNameTextField.getFocusListeners()) newFleetNameTextField.removeFocusListener(l);
		newFleetNameTextField.addFocusListener(starshipPlantActionFocusListener);
		newFleetNameTextField.setBounds(375, y, 90, 20);
		starshipPlantActionPanel.add(newFleetNameTextField);
		
		JButton formFleetBtn = getStarshipPlantFormFleetBtn();
		for(ActionListener l : formFleetBtn.getActionListeners()) formFleetBtn.removeActionListener(l);
		formFleetBtn.addActionListener(starshipPlantActionFormFleetBtnActionListener);
		formFleetBtn.setBounds(375, y+20, 90, 20);
		starshipPlantActionPanel.add(formFleetBtn);
		
		starshipPlantActionPanel.setMinimumSize(new Dimension(500, y+50));
		starshipPlantActionPanel.setPreferredSize(starshipPlantActionPanel.getMinimumSize());
		
		getRunningGameActionPanel().removeAll();
		starshipPlantActionScrollPane.setViewportView(starshipPlantActionPanel);
		getRunningGameActionPanel().add(starshipPlantActionScrollPane, BorderLayout.CENTER);
		
		getRunningGameTabbedPanel().setSelectedComponent(getRunningGameActionPanel());
		
		refreshStarshipPlantActionPanel(selectedPlanet, starshipPlant);
		
		updateUI();
	}

	private JButton starshipPlantWorkshopMakeStarshipBtn;
	private JButton getStarshipPlantWorkshopMakeStarshipBtn()
	{
		if (starshipPlantWorkshopMakeStarshipBtn == null)
		{
			starshipPlantWorkshopMakeStarshipBtn = new JButton("Make");
		}
		return starshipPlantWorkshopMakeStarshipBtn;
	}
	
	private JButton starshipPlantFormFleetBtn;
	private JButton getStarshipPlantFormFleetBtn()
	{
		if (starshipPlantFormFleetBtn == null)
		{
			starshipPlantFormFleetBtn = new JButton("Form fleet");
		}
		return starshipPlantFormFleetBtn;
	}
	
	private JLabel starshipPlantWorkshopTitleLabel;
	JLabel getStarshipPlantWorkshopTitleLabel()
	{
		if (starshipPlantWorkshopTitleLabel == null)
		{
			starshipPlantWorkshopTitleLabel = new JLabel("Landed starships");
			starshipPlantWorkshopTitleLabel.setBounds(0, 0, 400, 20);
		}
		return starshipPlantWorkshopTitleLabel;
	}
	
	private JLabel starshipPlantWorkshopColumnLabel;
	JLabel getStarshipPlantWorkshopColumnLabel()
	{
		if (starshipPlantWorkshopColumnLabel == null)
		{
			starshipPlantWorkshopColumnLabel = new JLabel("Type                                    Quantity (after)          Make          New fleet");
			starshipPlantWorkshopColumnLabel.setBounds(getStarshipPlantWorkshopTitleLabel().getX(), getStarshipPlantWorkshopTitleLabel().getY()+getStarshipPlantWorkshopTitleLabel().getHeight()+5, 500, 20);			
		}
		return starshipPlantWorkshopColumnLabel;
	}
	
	private Map<Class<? extends IStarship>, JLabel> starshipPlantWorkshopStarshipTypeLabel = new HashMap<Class<? extends IStarship>, JLabel>();
	JLabel getStarshipPlantWorkshopStarshipTypeLabel(Class<? extends IStarship> starshipType)
	{
		if (!starshipPlantWorkshopStarshipTypeLabel.containsKey(starshipType))
		{
			JLabel label = new JLabel(starshipType.getSimpleName());
			starshipPlantWorkshopStarshipTypeLabel.put(starshipType, label);
		}
		return starshipPlantWorkshopStarshipTypeLabel.get(starshipType);
	}
	
	private Map<Class<? extends IStarship>, JLabel> starshipPlantWorkshopStarshipQtLabel = new HashMap<Class<? extends IStarship>, JLabel>();
	JLabel getStarshipPlantWorkshopStarshipQtLabel(Class<? extends IStarship> starshipType)
	{
		if (!starshipPlantWorkshopStarshipQtLabel.containsKey(starshipType))
		{
			JLabel label = new JLabel("0 (0)");
			starshipPlantWorkshopStarshipQtLabel.put(starshipType, label);
		}
		return starshipPlantWorkshopStarshipQtLabel.get(starshipType);
	}
	
	private Map<Class<? extends IStarship>, JTextField> starshipPlantWorkshopStarshipQtToMakeTextField = new HashMap<Class<? extends IStarship>, JTextField>();
	JTextField getStarshipPlantWorkshopStarshipQtToMakeTextField(Class<? extends IStarship> starshipType)
	{
		if (!starshipPlantWorkshopStarshipQtToMakeTextField.containsKey(starshipType))
		{
			JTextField textField = new JTextField("0");
			starshipPlantWorkshopStarshipQtToMakeTextField.put(starshipType, textField);
		}
		return starshipPlantWorkshopStarshipQtToMakeTextField.get(starshipType);
	}
	
	private Map<Class<? extends IStarship>, JTextField> starshipPlantWorkshopStarshipNewFleetQtTextField = new HashMap<Class<? extends IStarship>, JTextField>();
	JTextField getStarshipPlantWorkshopStarshipNewFleetQtTextField(Class<? extends IStarship> starshipType)
	{
		if (!starshipPlantWorkshopStarshipNewFleetQtTextField.containsKey(starshipType))
		{
			JTextField textField = new JTextField("0");
			starshipPlantWorkshopStarshipNewFleetQtTextField.put(starshipType, textField);
		}
		return starshipPlantWorkshopStarshipNewFleetQtTextField.get(starshipType);
	}
	
	private JTextField starshipPlantNewFleetNameTextField;
	private JTextField getStarshipPlantNewFleetNameTextField()
	{
		if (starshipPlantNewFleetNameTextField == null)
		{
			starshipPlantNewFleetNameTextField = new JTextField("fleetName");
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
		
		if (currentSelectedArea != null)
		{
			updateSelectedArea(currentSelectedLocation[0], currentSelectedLocation[1], currentSelectedLocation[2]);
		}
		
		refreshBuildingDetails();
		refreshPlayerList();
		refreshShortcutBtns();
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

	private JLabel getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel()
	{
		if (runningGameCelestialBodyDetailsBuildingDetailsContentLabel == null)
		{
			runningGameCelestialBodyDetailsBuildingDetailsContentLabel = new WrappedJLabel();
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
		String htmlText = "<br><font color='#" + GUIUtils.getHTMLColor(fromPlayer.getConfig().getColor()) + "'>" + fromPlayer.getName() + "</font> : " + msg
				+ "</br>";
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

	public void refreshPlayerList(Set<Player> players)
	{
		System.out.println("RunningGamePanel refreshPlayerList : " + players);
		getRunningGameChatPlayerListPanel().removeAll();
		for(Player p : players)
		{
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JImagePanel(p.getConfig().getPortrait()), BorderLayout.WEST);
			panel.add(new JImagePanel(p.getConfig().getSymbol()), BorderLayout.EAST);
			JLabel name = new JLabel(p.getName());
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
		JOptionPane.showMessageDialog(null, "New turn begins ("+gameBoard.getDate()+")", "New turn begins", JOptionPane.INFORMATION_MESSAGE);
	}
}
