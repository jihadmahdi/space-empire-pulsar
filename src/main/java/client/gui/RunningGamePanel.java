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
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.HashSet;
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
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import client.SEPClient;
import client.gui.UniverseRenderer.UniverseRendererListener;
import client.gui.lib.GUIUtils;
import client.gui.lib.SingleRowFlowLayout;
import client.gui.lib.WrappedJLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import common.Area;
import common.DefenseModule;
import common.ExtractionModule;
import common.Fleet;
import common.GovernmentModule;
import common.GovernmentStarship;
import common.IBuilding;
import common.ICelestialBody;
import common.Planet;
import common.Player;
import common.PlayerGameBoard;
import common.ProductiveCelestialBody;
import common.PulsarLauchingPad;
import common.SEPUtils;
import common.SpaceCounter;
import common.StarshipPlant;
import common.Unit;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class RunningGamePanel extends javax.swing.JPanel implements UniverseRendererListener
{
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
		this(null, null);
	}

	private final Player	player;
	private final SEPClient client;

	public RunningGamePanel(Player player, SEPClient client)
	{
		super();
		this.player = player;
		this.client = client;
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
		catch (Exception e)
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
			GridLayout runningGameEastPanelLayout = new GridLayout(2, 1);
			runningGameEastPanelLayout.setColumns(1);
			runningGameEastPanelLayout.setRows(2);
			runningGameEastPanelLayout.setVgap(1);
			runningGameEastPanel.setLayout(runningGameEastPanelLayout);
			runningGameEastPanel.add(getRunningGameCelestialBodyDetails());
			runningGameEastPanel.add(getRunningGameFleetDetails());
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
			runningGameSouthPanel.setPreferredSize(new java.awt.Dimension(10, 20));
			runningGameSouthPanel.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, false));
			runningGameSouthPanel.add(getRunningGameShortcutBarLabel());
		}
		return runningGameSouthPanel;
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

	private JPanel	runningGameCelestialBodyDetails;

	private JPanel getRunningGameCelestialBodyDetails()
	{
		if (runningGameCelestialBodyDetails == null)
		{
			runningGameCelestialBodyDetails = new JPanel();
			BorderLayout runningGameCelestialBodyDetailsLayout = new BorderLayout();
			runningGameCelestialBodyDetails.setLayout(runningGameCelestialBodyDetailsLayout);
			runningGameCelestialBodyDetails.setPreferredSize(new java.awt.Dimension(200, 250));
			runningGameCelestialBodyDetails.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, false));
			runningGameCelestialBodyDetails.add(getRunningGameCelestialBodyDetailsLabel(), BorderLayout.NORTH);
			runningGameCelestialBodyDetails.add(getRunningGameCelestialBodyDetailsPanel(), BorderLayout.CENTER);
		}
		return runningGameCelestialBodyDetails;
	}

	private JPanel	runningGameFleetDetails;

	private JPanel getRunningGameFleetDetails()
	{
		if (runningGameFleetDetails == null)
		{
			runningGameFleetDetails = new JPanel();
			BorderLayout runningGameFleetDetailsLayout = new BorderLayout();
			runningGameFleetDetails.setLayout(runningGameFleetDetailsLayout);
			runningGameFleetDetails.setPreferredSize(new java.awt.Dimension(150, 300));
			runningGameFleetDetails.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, false));
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
			runningGameTabbedPanel.setPreferredSize(new java.awt.Dimension(4, 200));
			runningGameTabbedPanel.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, false));
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
			runningGameChatPanel.add(getRunningGaeChatPlayerListPanel(), BorderLayout.EAST);
			runningGameChatPanel.add(getRunningGameChatScrollPane(), BorderLayout.CENTER);
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
			runningGameUniverseRenderingPanel.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, false));
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

	private JPanel	runningGameCelestialBodyDetailsPanel;

	private JPanel getRunningGameCelestialBodyDetailsPanel()
	{
		if (runningGameCelestialBodyDetailsPanel == null)
		{
			runningGameCelestialBodyDetailsPanel = new JPanel(new BorderLayout());
			GridBagLayout runningGameCelestialBodyDetailsPanelLayout = new GridBagLayout();
			runningGameCelestialBodyDetailsPanelLayout.rowWeights = new double[] {1.5, 1.0};
			runningGameCelestialBodyDetailsPanelLayout.rowHeights = new int[] {7, 7};
			runningGameCelestialBodyDetailsPanelLayout.columnWeights = new double[] {0.1};
			runningGameCelestialBodyDetailsPanelLayout.columnWidths = new int[] {7};
			runningGameCelestialBodyDetailsPanel.setLayout(runningGameCelestialBodyDetailsPanelLayout);
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsContentPanel(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			runningGameCelestialBodyDetailsPanel.add(getRunningGameCelestialBodyDetailsBuildingDetailsPanel(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
			runningGameCelestialBodyDetailsPanel.setMinimumSize(new Dimension(200, getRunningGameCelestialBodyDetailsContentPanel().getHeight() + getRunningGameCelestialBodyDetailsBuildingDetailsPanel().getHeight()));
			runningGameCelestialBodyDetailsPanel.setPreferredSize(runningGameCelestialBodyDetailsPanel.getMinimumSize());
		}
		return runningGameCelestialBodyDetailsPanel;
	}

	private JPanel	runningGameCelestialBodyDetailsContentPanel;

	private JPanel getRunningGameCelestialBodyDetailsContentPanel()
	{
		if (runningGameCelestialBodyDetailsContentPanel == null)
		{
			runningGameCelestialBodyDetailsContentPanel = new JPanel();
			GridLayout runningGameCelestialBodyDetailsContentPanelLayout = new GridLayout(3, 1);
			runningGameCelestialBodyDetailsContentPanelLayout.setVgap(5);
			runningGameCelestialBodyDetailsContentPanelLayout.setColumns(1);
			runningGameCelestialBodyDetailsContentPanelLayout.setRows(3);
			runningGameCelestialBodyDetailsContentPanel.setLayout(runningGameCelestialBodyDetailsContentPanelLayout);
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsContentLabel());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsBuildingsListPane());
			runningGameCelestialBodyDetailsContentPanel.add(getRunningGameCelestialBodyDetailsUnitsListPane());
			runningGameCelestialBodyDetailsContentPanel.setMinimumSize(new Dimension(200, getRunningGameCelestialBodyDetailsUnitsList().getBounds().y + getRunningGameCelestialBodyDetailsUnitsList().getHeight()));
			runningGameCelestialBodyDetailsContentPanel.setPreferredSize(runningGameCelestialBodyDetailsContentPanel.getMinimumSize());
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

	private JList	runningGameCelestialBodyDetailsBuildingsList;

	private JList getRunningGameCelestialBodyDetailsBuildingsList()
	{
		if (runningGameCelestialBodyDetailsBuildingsList == null)
		{
			runningGameCelestialBodyDetailsBuildingsList = new JList();
			runningGameCelestialBodyDetailsBuildingsList.setVisible(true);
			runningGameCelestialBodyDetailsBuildingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			runningGameCelestialBodyDetailsBuildingsList.addListSelectionListener(new ListSelectionListener()
			{

				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					System.out.println("BuildingsList valueChanged (" + runningGameCelestialBodyDetailsBuildingsList.getSelectedValue() + ") : " + e);
					if (runningGameCelestialBodyDetailsBuildingsList.getSelectedValue() == null) return;

					String value = runningGameCelestialBodyDetailsBuildingsList.getSelectedValue().toString();
					value = value.substring(0, (value.indexOf(" ") < 0) ? value.length() : value.indexOf(" "));
					refreshBuildingDetails(value);
				}
			});
		}
		return runningGameCelestialBodyDetailsBuildingsList;
	}

	private JScrollPane	runningGameCelestialBodyDetailsBuildingsListPane;

	private JScrollPane getRunningGameCelestialBodyDetailsBuildingsListPane()
	{
		if (runningGameCelestialBodyDetailsBuildingsListPane == null)
		{
			runningGameCelestialBodyDetailsBuildingsListPane = new JScrollPane();
			runningGameCelestialBodyDetailsBuildingsListPane.setViewportView(getRunningGameCelestialBodyDetailsBuildingsList());
		}
		return runningGameCelestialBodyDetailsBuildingsListPane;
	}

	private JLabel	runningGameCelestialBodyDetailsBuildingDetailsContentLabel;

	private JList	runningGameCelestialBodyDetailsUnitsList;

	private JList getRunningGameCelestialBodyDetailsUnitsList()
	{
		if (runningGameCelestialBodyDetailsUnitsList == null)
		{
			runningGameCelestialBodyDetailsUnitsList = new JList();
			runningGameCelestialBodyDetailsUnitsList.setVisible(true);
			runningGameCelestialBodyDetailsUnitsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return runningGameCelestialBodyDetailsUnitsList;
	}

	private JScrollPane	runningGameCelestialBodyDetailsUnitsListPane;

	private JScrollPane getRunningGameCelestialBodyDetailsUnitsListPane()
	{
		if (runningGameCelestialBodyDetailsUnitsListPane == null)
		{
			runningGameCelestialBodyDetailsUnitsListPane = new JScrollPane();
			runningGameCelestialBodyDetailsUnitsListPane.setViewportView(getRunningGameCelestialBodyDetailsUnitsList());
		}
		return runningGameCelestialBodyDetailsUnitsListPane;
	}

	private JPanel		runningGameCelestialBodyDetailsBuildingDetailsPanel;

	private JEditorPane		runningGameChatContentEditorPane;

	private JScrollPane	runningGameChatScrollPane;

	private JPanel		runningGaeChatPlayerListPanel;

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

	private UniverseRenderer universeRenderer;
	private JPanel universePanel;

	private JPanel getUniversePanel()
	{
		if (universePanel == null)
		{
			UniversePanel universeRendererPanel = new UniversePanel();			
			universeRenderer = universeRendererPanel;
			universePanel = universeRendererPanel;
			universeRenderer.setListener(this);
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

		String selectedAreaDisplay = currentSelectedArea.toString();
		getRunningGameCelestialBodyDetailsContentLabel().setBackground(Color.lightGray);
		getRunningGameCelestialBodyDetailsContentLabel().setText("<html>" + selectedAreaDisplay.substring(0, (selectedAreaDisplay.indexOf("Buildings") < 0) ? selectedAreaDisplay.length() : selectedAreaDisplay.indexOf("Buildings")).replace("\n", "<br>") + "</html>");

		Vector<String> buildingsList = new Vector<String>();
		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		if (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			Set<Class<? extends IBuilding>> buildingsTypes = new HashSet<Class<? extends IBuilding>>(SEPUtils.buildingTypes);

			ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
			if (productiveCelestialBody.getBuildings() != null)
			{
				for (IBuilding b : productiveCelestialBody.getBuildings())
				{
					buildingsList.add(b.getClass().getSimpleName() + " (" + b.getBuildSlotsCount() + ")");
					buildingsTypes.remove(b.getClass());
				}
			}

			for (Class<? extends IBuilding> bt : buildingsTypes)
			{
				if (productiveCelestialBody.canBuildType(bt))
				{
					buildingsList.add(bt.getSimpleName() + " (none)");
				}
			}
		}
		getRunningGameCelestialBodyDetailsBuildingsList().setListData(buildingsList);

		Vector<String> unitsList = new Vector<String>();
		Set<Unit> units = currentSelectedArea.getUnits();
		if (units != null)
		{
			for (Unit u : units)
			{
				unitsList.add("[" + u.getOwner().getName() + "] " + u.getName());
			}
		}
		getRunningGameCelestialBodyDetailsUnitsList().setListData(unitsList);

		eraseBuildingDetails();

		updateUI();

		// TODO : Markers
	}

	private void eraseBuildingDetails()
	{
		getRunningGameCelestialBodyDetailsBuildingDetailsContentLabel().setText("");
		getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().removeAll();
	}

	private void refreshBuildingDetails(String buildingTypeName)
	{
		eraseBuildingDetails();

		if (currentSelectedArea == null) return;
		ICelestialBody celestialBody = currentSelectedArea.getCelestialBody();
		if (celestialBody == null) return;
		if ( !ProductiveCelestialBody.class.isInstance(celestialBody)) return;

		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		Set<IBuilding> buildings = productiveCelestialBody.getBuildings();
		IBuilding selectedBuildings = null;
		Class<? extends IBuilding> selectedBuildingType = null;

		if (buildings != null) for (IBuilding b : buildings)
		{
			if (b.getClass().getSimpleName().compareTo(buildingTypeName) == 0)
			{
				selectedBuildings = b;
				break;
			}
		}

		if (selectedBuildings == null)
		{
			for (Class<? extends IBuilding> bt : SEPUtils.buildingTypes)
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

		// Specific panel
		if (productiveCelestialBody.getOwner() != null && productiveCelestialBody.getOwner().isNamed(player.getName()) && (selectedBuildings != null || productiveCelestialBody.canBuildType(selectedBuildingType)))
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
					buildCost = DefenseModule.FIRST_BUILD_COST;
					nbBuild = 0;
				}

				// Build btn
				JPanel buildBtnsPanel = new JPanel(new FlowLayout());

				if (buildCost >= 0)
				{
					JButton buildBtn = new JButton();
					String label = (nbBuild > 0) ? "Upgrade" : "Build";
					buildBtn.setText(label);
					buildBtn.setToolTipText(label + " " + (nbBuild + 1) + " defense modules for " + buildCost + "c.");
					buildBtn.setEnabled(productiveCelestialBody.getFreeSlotsCount() > 0 && productiveCelestialBody.getCarbon() >= buildCost);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(buildBtn);
				}

				if (nbBuild > 0)
				{
					JButton destroyBtn = new JButton();
					destroyBtn.setText("Destroy");
					destroyBtn.setToolTipText("Destroy 1 defense module to free one slot.");
					destroyBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(destroyBtn);
				}

				getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(buildBtnsPanel);
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
					buildCost = ExtractionModule.FIRST_BUILD_COST;
					nbBuild = 0;
				}

				// Build btn
				JPanel buildBtnsPanel = new JPanel(new FlowLayout());

				if (buildCost >= 0)
				{
					JButton buildBtn = new JButton();
					String label = (nbBuild > 0) ? "Upgrade" : "Build";
					buildBtn.setText(label);
					buildBtn.setToolTipText(label + " " + (nbBuild + 1) + " extraction modules for " + buildCost + "c.");
					buildBtn.setEnabled(productiveCelestialBody.getFreeSlotsCount() > 0 && productiveCelestialBody.getCarbon() >= buildCost);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(buildBtn);
				}

				if (nbBuild > 0)
				{
					JButton destroyBtn = new JButton();
					destroyBtn.setText("Destroy");
					destroyBtn.setToolTipText("Destroy 1 extraction module to free one slot.");
					destroyBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(destroyBtn);
				}

				getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(buildBtnsPanel);
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
					buildBtn.setToolTipText("Embark the government on a government starship for " + GovernmentStarship.PRICE_POPULATION + "pop. and " + GovernmentStarship.PRICE_CARBON + "c.");
					buildBtn.setEnabled(planet.getCarbon() >= GovernmentStarship.PRICE_CARBON && planet.getPopulation() >= GovernmentStarship.PRICE_POPULATION);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					btnsPanel.add(buildBtn);

					getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
				}
				else
				{
					for (Unit u : currentSelectedArea.getUnits())
					{
						if (Fleet.class.isInstance(u))
						{
							Fleet f = Fleet.class.cast(u);
							if (f.getOwner().isNamed(player.getName()) && f.isGovernmentFleet())
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
										// TODO
									}
								});
								btnsPanel.add(buildBtn);

								getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);

								break;
							}
						}
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
				buildBtn.setToolTipText(label + " pulsar launching pad for " + PulsarLauchingPad.PRICE_POPULATION + "pop. and " + PulsarLauchingPad.PRICE_CARBON + "c.");
				buildBtn.setEnabled(planet.getCarbon() >= PulsarLauchingPad.PRICE_CARBON && planet.getPopulation() >= PulsarLauchingPad.PRICE_POPULATION && planet.getFreeSlotsCount() > 0);
				buildBtn.addActionListener(new ActionListener()
				{

					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO
					}
				});
				btnsPanel.add(buildBtn);

				JButton fireBtn = new JButton();
				fireBtn.setText("Fire");
				fireBtn.setToolTipText("Get access to pulsar missile launching controls.");
				fireBtn.setEnabled(unusedCount > 0);
				fireBtn.addActionListener(new ActionListener()
				{

					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO
					}
				});
				btnsPanel.add(fireBtn);

				getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
			}
			else if ((selectedBuildings != null && SpaceCounter.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && SpaceCounter.class.equals(selectedBuildingType)))
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
				JPanel buildBtnsPanel = new JPanel(new FlowLayout());

				if (buildCost >= 0)
				{
					JButton buildBtn = new JButton();
					buildBtn.setText("Build");
					buildBtn.setToolTipText("Build a new space counter for " + buildCost + "c.");
					buildBtn.setEnabled(productiveCelestialBody.getFreeSlotsCount() > 0 && productiveCelestialBody.getCarbon() >= buildCost);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(buildBtn);
				}

				if (nbBuild > 0)
				{
					JButton destroyBtn = new JButton();
					destroyBtn.setText("Destroy");
					destroyBtn.setToolTipText("Destroy 1 space counter (and associated road) to free one slot.");
					destroyBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					buildBtnsPanel.add(destroyBtn);
				}

				getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(buildBtnsPanel);
			}
			else if ((selectedBuildings != null && StarshipPlant.class.isInstance(selectedBuildings)) || (selectedBuildingType != null && StarshipPlant.class.equals(selectedBuildingType)))
			{
				Planet planet = Planet.class.cast(productiveCelestialBody);

				int nbBuild;

				if (selectedBuildings != null)
				{
					StarshipPlant starshipPlant = StarshipPlant.class.cast(selectedBuildings);
					nbBuild = starshipPlant.getBuildSlotsCount();
				}
				else
				{
					nbBuild = 0;
				}

				// Actions btn
				JPanel btnsPanel = new JPanel(new FlowLayout());

				if (nbBuild == 0)
				{
					JButton buildBtn = new JButton();
					buildBtn.setText("Build");
					buildBtn.setToolTipText("Build a starship plant for " + StarshipPlant.PRICE_POPULATION + "pop. and " + StarshipPlant.PRICE_CARBON + "c.");
					buildBtn.setEnabled(planet.getFreeSlotsCount() > 0 && planet.getPopulation() >= StarshipPlant.PRICE_POPULATION && planet.getCarbon() >= StarshipPlant.PRICE_CARBON);
					buildBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});

					btnsPanel.add(buildBtn);
				}
				else
				{
					JButton destroyBtn = new JButton();
					destroyBtn.setText("Destroy");
					destroyBtn.setToolTipText("Destroy starship plant to free one slot.");
					destroyBtn.addActionListener(new ActionListener()
					{

						@Override
						public void actionPerformed(ActionEvent e)
						{
							// TODO
						}
					});
					btnsPanel.add(destroyBtn);
				}

				// TODO : Starship plant details + starship factory

				getRunningGameCelestialBodyDetailsBuildingSpecificDetailsPanel().add(btnsPanel);
			}
		}
	}

	void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		currentGameBoard = gameBoard;
		getUniverseRenderer().refreshGameBoard(gameBoard);
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
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		return runningGameChatTextField;
	}

	private JPanel getRunningGaeChatPlayerListPanel()
	{
		if (runningGaeChatPlayerListPanel == null)
		{
			runningGaeChatPlayerListPanel = new JPanel();
			runningGaeChatPlayerListPanel.setPreferredSize(new java.awt.Dimension(100, 10));
		}
		return runningGaeChatPlayerListPanel;
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
					if ( !e.getValueIsAdjusting())
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
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
