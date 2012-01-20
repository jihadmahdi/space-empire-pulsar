package org.axan.sep.client.gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.IUniverseRenderer.IUniverseRendererListener;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.Commands;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.axan.sep.common.db.orm.SEPCommonDB.IAreaChangeListener;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import scala.actors.threadpool.Arrays;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.EventListModel;

public class RunningGamePanel extends JPanel implements IModalComponent, IUniverseRendererListener, IAreaChangeListener
{
	//////////static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// static classes
	public static abstract class AUniverseRendererPanel extends JPanel implements IUniverseRenderer
	{
		
	}
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;	
	
	////////// bean fields
	private SEPClient sepClient;
	private final EventList<eBuildingType> buildings = GlazedLists.threadSafeList(new BasicEventList<eBuildingType>());
	private final EventList<IUnitMarker> units = GlazedLists.threadSafeList(new BasicEventList<IUnitMarker>());
	private IArea selectedArea = null;

	//////////ui controls
	private AUniverseRendererPanel universePanel = new SwingUniverseRenderer();
	private GameChatPanel chatPanel;
	private PlayersListPanel playersListPanel;
	private EventListModel<eBuildingType> buildingsListModel = new EventListModel<eBuildingType>(buildings);
	private JList buildingsList;
	private EventListModel<IUnitMarker> unitsListModel = new EventListModel<IUnitMarker>(units);
	private JList unitsList;
	private JTextPane celestialBodyInfos;
	private JTextPane buildingInfos;	
	private JPanel buildingActionsPanel;
	private JTextPane unitInfos;
	private JPanel unitActionsPanel;
	private JButton btnEndTurn;
	private JTabbedPane tabsPanel;
	private JScrollPane actionTab;
	private StarshipPlantActionPanel starshipPlantActionPanel;
	private ProbeActionsPanel probeActionsPanel;

	////////// no arguments constructor
	public RunningGamePanel()
	{
		SwingJavaBuilderMyUtils.addType(AUniverseRendererPanel.class, StarshipPlantActionPanel.class, ProbeActionsPanel.class);
		
		build = SwingJavaBuilder.build(this);
		
		universePanel.setUniverseRendererListener(this);		
		
		starshipPlantActionPanel.setRunningGamePanel(this);
		probeActionsPanel.setRunningGamePanel(this);
		
		buildingActionsPanel.setLayout(new FlowLayout());
		
		final ListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
		
		buildingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildingsList.setEnabled(false);
		buildingsList.setFocusable(true);
		buildingsList.setCellRenderer(new ListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				/*
				if (RunningGamePanel.this.getSepClient() == null || !RunningGamePanel.this.isEnabled())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						
						@Override
						public void run()
						{
							try
							{
								Thread.sleep(2000);
							}
							catch(InterruptedException ie) {}
							
							log.log(Level.INFO, "invalidate");
							buildingsList.invalidate();
							buildingsList.doLayout();
						}
					});
					
					return new JLabel("loading...");
				}
				*/
				
				if (RunningGamePanel.this.selectedArea == null)
				{
					return defaultListCellRenderer.getListCellRendererComponent(list, "No area selected", index, isSelected, cellHasFocus);
				}
				
				ICelestialBody celestialBody = RunningGamePanel.this.selectedArea.getCelestialBody();
				if (!IProductiveCelestialBody.class.isInstance(celestialBody))
				{
					return defaultListCellRenderer.getListCellRendererComponent(list, "No productive celestial body selected", index, isSelected, cellHasFocus);
				}

				IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) celestialBody;								
				eBuildingType buildingType = (eBuildingType) value;
				SEPCommonDB db = RunningGamePanel.this.getSepClient().getGameboard().getDB();
				IBuilding building = db.getBuilding(productiveCelestialBody.getName(), buildingType);								
				
				return defaultListCellRenderer.getListCellRendererComponent(list, String.format("%s (%s)", buildingType, (building == null || building.getNbSlots() == 0) ? "none" : building.getNbSlots()), index, isSelected, cellHasFocus);
			}
		});
		
		unitsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		unitsList.setEnabled(false);
		unitsList.setFocusable(true);		
		unitsList.setCellRenderer(new ListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				/*
				if (RunningGamePanel.this.getSepClient() == null || !RunningGamePanel.this.isEnabled())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						
						@Override
						public void run()
						{
							try
							{
								Thread.sleep(2000);
							}
							catch(InterruptedException ie) {}
							
							log.log(Level.INFO, "invalidate");
							unitsList.invalidate();
							unitsList.doLayout();
						}
					});
					
					return new JLabel("loading...");
				}
				*/
				
				IUnitMarker unit = (IUnitMarker) value;
				return defaultListCellRenderer.getListCellRendererComponent(list, String.format("%s [%s] %s", unit.getOwnerName(), unit.getType().toString(), unit.getName()), index, isSelected, cellHasFocus);
			}
		});
		
		unitsList.addListSelectionListener(new ListSelectionListener()
		{
			
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting()) return;
				if (unitsList.getSelectedIndex() < 0) return;
				if (unitsList.getSelectedIndex() >= units.size()) return;
				
				SwingUtilities.invokeLater(new Runnable()
				{
					
					@Override
					public void run()
					{
						updateSelectedUnit((IUnitMarker) unitsList.getSelectedValue());
					}
				});
			}
		});
		
		buildingsList.addListSelectionListener(new ListSelectionListener()
		{
			
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting()) return;
				if (buildingsList.getSelectedIndex() < 0) return;
				if (buildingsList.getSelectedIndex() >= buildings.size()) return;
				
				SwingUtilities.invokeLater(new Runnable()
				{
					
					@Override
					public void run()
					{
						updateSelectedBuilding((eBuildingType) buildingsList.getSelectedValue());
					}
				});
			}
		});
	}

	@Override
	public void onAreaChanged(final Location location)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				getUniversePanel().onAreaChanged(location);
				
				if (selectedArea != null && selectedArea.getLocation().equals(location))
				{
					updateSelectedArea(location.x, location.y, location.z);
				}
			}
		});		
	}
	
	////////// IModal implementation

	@Override
	public boolean validateForm()
	{
		return build.validate();
	};

	@Override
	public boolean isCanceled()
	{
		return canceled;
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

	public GameChatPanel getChatPanel()
	{
		return chatPanel;
	}
	
	public PlayersListPanel getPlayersListPanel()
	{
		return playersListPanel;
	}
	
	public AUniverseRendererPanel getUniversePanel()
	{
		return universePanel;
	}
	
	////////// ui events
	
	@Override
	public void updateSelectedArea(int x, int y, int z)
	{
		PlayerGameBoard gb = getSepClient().getGameboard();
		Location newLocation = new Location(x, y, z);
		boolean selectionChanged = (selectedArea == null || !selectedArea.getLocation().equals(newLocation));
		selectedArea = gb.getDB().getArea(newLocation);
		String areaInfos = selectedArea.toString(getSepClient().getLogin());
		celestialBodyInfos.setText(areaInfos.substring(0, (areaInfos.indexOf("Buildings") < 0) ? areaInfos.length() : areaInfos.indexOf("Buildings")));
		
		// Buildings
		
		eBuildingType selectedBuilding = (eBuildingType) buildingsList.getSelectedValue();
		int selectedBuildingIndex = -1;
		
		buildingsList.clearSelection();
		buildings.clear();
		
		ICelestialBody celestialBody = selectedArea.getCelestialBody();
		
		Set<eBuildingType> buildingsTypes = new LinkedHashSet<eBuildingType>(Arrays.asList(eBuildingType.values()));
		
		if (IProductiveCelestialBody.class.isInstance(celestialBody))
		{
			IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) celestialBody;
			for(IBuilding b : productiveCelestialBody.getBuildings())
			{
				buildings.add(b.getType());
				buildingsTypes.remove(b.getType());				
			}
			buildings.addAll(buildingsTypes);
		}
		
		if (selectedBuilding != null )for(int i=0; i < buildings.size(); ++i)
		{
			if (buildings.get(i).equals(selectedBuilding))
			{
				selectedBuildingIndex = i;
				break;
			}
		}
		
		buildingsList.setEnabled(!buildings.isEmpty());
		
		if (selectedBuildingIndex >= 0)
		{
			buildingsList.setSelectedIndex(selectedBuildingIndex);
			//buildingsList.setSelectedValue(selectedBuilding, true);
		}
		else
		{
			updateSelectedBuilding(null);
		}
		
		// Units
		
		IUnitMarker selectedUnit = (IUnitMarker) unitsList.getSelectedValue();
		int selectedUnitIndex = -1;
		
		unitsList.clearSelection();
		units.clear();
		
		
		//units.addAll(selectedArea.getUnits(null));
		units.addAll(selectedArea.getUnitsMarkers(null));
		
		if (selectedUnit != null) for(int i=0; i < units.size(); ++i)
		{
			if (units.get(i).equals(selectedUnit))
			{
				selectedUnitIndex = i;
				break;
			}
		}
		
		unitsList.setEnabled(!units.isEmpty());
		
		if (selectedUnitIndex >= 0)
		{
			unitsList.setSelectedIndex(selectedUnitIndex);
		}
		else
		{
			updateSelectedUnit(null);
		}
	}
	
	void updateSelectedUnit(IUnitMarker unit)
	{
		// Erase
		unitInfos.setText("");
		unitActionsPanel.removeAll();
		
		if (selectedArea == null) return;
		if (unit == null) return;
		
		/*
		ICelestialBody celestialBody = selectedArea.getCelestialBody();
		if (celestialBody == null || !IProductiveCelestialBody.class.isInstance(celestialBody)) return;
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) celestialBody;
		PlayerGameBoard gb = getSepClient().getGameboard();
		 */
		
		unitInfos.setText(unit.toString());

		if (IUnit.class.isInstance(unit))
		{
			switch(unit.getType())
			{
				case Probe:
				{
					probeActionsPanel.setProbe((IProbe) unit);
					unitActionsPanel.add(probeActionsPanel);
					break;
				}
			}
		}
	}
	
	/**
	 * Update selected building UI, or reset it (if buildingType == null)
	 * @param buildingType
	 */
	void updateSelectedBuilding(eBuildingType buildingType)
	{
		// Erase
		buildingInfos.setText("");
		buildingActionsPanel.removeAll();
		
		if (selectedArea == null) return;		
		if (buildingType == null) return;
		
		ICelestialBody celestialBody = selectedArea.getCelestialBody();
		if (celestialBody == null || !IProductiveCelestialBody.class.isInstance(celestialBody)) return;
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) celestialBody;
		PlayerGameBoard gb = getSepClient().getGameboard();
		IBuilding building = gb.getDB().getBuilding(productiveCelestialBody.getName(), buildingType);
		
		if (building == null)
		{
			buildingInfos.setText(String.format("No %s built yet.", buildingType));
		}
		else
		{
			buildingInfos.setText(building.toString());
		}
		
		JButton btnBuild = new JButton(building == null ? "Build" : "Upgrade");
		final Commands.Build buildCmd = new Commands.Build(getSepClient().getLogin(), productiveCelestialBody.getName(), buildingType);
		try
		{
			buildCmd.check(gb.getDB());
			btnBuild.setToolTipText(String.format("Cost:%s%s", buildCmd.getCarbonCost()>0 ? " "+buildCmd.getCarbonCost()+"C" : "", buildCmd.getPopulationCost()>0 ? " "+buildCmd.getPopulationCost()+"P" : ""));
			btnBuild.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(buildCmd);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Build command", ex);
						return;
					}
				}
			});			
		}
		catch(GameCommandException e)
		{
			btnBuild.setEnabled(false);
			btnBuild.setToolTipText("Cannot build: "+e.getMessage());
		}
		
		buildingActionsPanel.add(btnBuild);
		
		if (building != null && IStarshipPlant.class.isInstance(building) && building.getBuiltDate() < gb.getConfig().getTurn())
		{
			starshipPlantActionPanel.setStarshipPlant((IStarshipPlant) building);
			setActionPanel("Starship plant", starshipPlantActionPanel);
		}
	}	
	
	void setActionPanel(String title, JPanel actionPanel)
	{
		int i;
		for(i=0; i < tabsPanel.getComponentCount(); ++i) if (actionTab.equals(tabsPanel.getComponent(i))) break;
		tabsPanel.setTitleAt(i, title);
		actionTab.setViewportView(actionPanel);
		actionTab.invalidate();
	}
	
	/*
	void refresh(boolean refreshUniversePanel)
	{
		if (refreshUniversePanel) universePanel.refresh(true);
		if (selectedArea != null)
		{
			Location loc = selectedArea.getLocation();
			updateSelectedArea(loc.x, loc.y, loc.z);
		}
	}
	*/
	
	//TODO: Should override some common interface to everybody's implementing this method
	void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents)
	{		
		getSepClient().getGameboard().getDB().addAreaChangeListener(this);
		universePanel.receiveNewTurnGameBoard(newTurnEvents);
		playersListPanel.setEnabled(true);
		btnEndTurn.setEnabled(true);
		
		JOptionPane.showConfirmDialog(null, "Turn n°"+getSepClient().getGameboard().getConfig().getTurn()+" begins !", "New turn", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}
	
	void endTurn()
	{
		try
		{
			getSepClient().endTurn();
			btnEndTurn.setEnabled(false);
		}
		catch(Throwable t)
		{
			// TODO: better error handling
			t.printStackTrace();
			return;
		}
	}
		
}
