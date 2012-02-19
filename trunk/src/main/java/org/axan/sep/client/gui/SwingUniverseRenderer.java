package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Profiling;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.RunningGamePanel.AUniverseRendererPanel;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class SwingUniverseRenderer extends AUniverseRendererPanel
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());

	////////// static methods

	static private Map<String, JLabel> coordLabels = new HashMap<String, JLabel>();

	static private JLabel getCoordLabel(boolean isVertical, int i, int z)
	{
		String k = String.format("%d-%d", (isVertical ? 1 : -1) * i, z);
		if (!coordLabels.containsKey(k))
		{
			JLabel label = new JLabel();
			String txt = (i > 0) ? ((isVertical ? "y" : "x") + (i - 1)) : "";
			label.setText(txt);
			coordLabels.put(k, label);
		}
		return coordLabels.get(k);
	}

	////////// private attributes
	private final BuildResult build;
	private IUniverseRendererListener listener = null;
	private IAreaSelectionListener areaSelectionListener = null;
	//private boolean ignoreZSlider = false;
	private final Map<Location, JImagePanel> images = new Hashtable<Location, JImagePanel>();
	private Location selectedLocation = null;

	////////// ui controls
	private JPanel universeViewPanel;
	private JSlider zSlider;

	////////// bean fields
	SEPClient sepClient;
	int zSelection;
	private PlayerGameBoard gameboard = null;

	////////// no arguments constructor
	public SwingUniverseRenderer()
	{
		build = SwingJavaBuilder.build(this);

		zSlider.setEnabled(false);
		
		universeViewPanel.setLayout(new BorderLayout());
		
		addPropertyChangeListener("zSelection", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getNewValue().equals(evt.getOldValue())) return;
				
				refreshZView(false);
			}
		});
	}

	////////// bean getters/setters

	@Override
	public SEPClient getSepClient()
	{
		return sepClient;
	}

	@Override
	public void setSepClient(SEPClient sepClient)
	{
		SEPClient old = this.sepClient;
		this.sepClient = sepClient;
		firePropertyChange("sepClient", old, sepClient);
	}

	public PlayerGameBoard getGameboard()
	{
		return gameboard;
	}

	public void setGameboard(PlayerGameBoard gameboard)
	{
		Object old = this.gameboard;
		this.gameboard = gameboard;
		firePropertyChange("gameboard", old, gameboard);
	}
	
	public int getzSelection()
	{
		return zSelection;
	}
	
	public void setzSelection(int zSelection)
	{
		Object old = this.zSelection;
		this.zSelection = zSelection;
		firePropertyChange("zSelection", old, zSelection);
	}

	////////// IUniverseRenderer implementation 

	@Override
	public void setUniverseRendererListener(IUniverseRendererListener listener)
	{
		IUniverseRendererListener old = this.listener;
		this.listener = listener;
		firePropertyChange("universeRendererListener", old, listener);
	}
	
	@Override
	public void setAreaSelectionListener(IAreaSelectionListener listener)
	{
		this.areaSelectionListener = listener;
	}
	
	@Override
	public boolean isAreaSelectionListener(IAreaSelectionListener listener)
	{
		return this.areaSelectionListener == listener;
	}
	
	@Override
	public void unsetAreaSelectionListener(IAreaSelectionListener listener)
	{
		if (isAreaSelectionListener(listener)) this.areaSelectionListener = null;
	}

	@Override
	public void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents)
	{
		if (getGameboard() == null)
		{
			setGameboard(getSepClient().getGameboard());
		}
		
		PlayerGameBoard gameboard = getGameboard();
		if (gameboard == null) return;
		IGameConfig config = gameboard.getConfig();		
		
		//if (universeViewPanel.getComponentCount() == 0)		
		if (!zSlider.isEnabled())
		{			
			zSlider.setMinimum(0);
			zSlider.setMaximum(config.getDimZ() - 1);			
			
			IPlanet startingPlanet = gameboard.getDB().getStartingPlanet(getSepClient().getLogin());
			
			// TODO: Force initial all Z painting
			
			// Changing zSlider bound to zSelection that fires changeZView UI refresh.
			zSlider.setValue(startingPlanet.getLocation().z);
			
			reload();
			
			// First refresh, force z value to player starting planet z coord.
			zSlider.setEnabled(true);
			
			return;			
		}		
		
		// Force turn counter refresh
		getZGrid(getzSelection());
	}
	
	////////// ui events

	////////// private methods

	private Thread viewReloader = null;
	private Boolean viewReloaderInterrupt = false;
	
	/**
	 * Reload all areas (all z values).
	 */
	private void reload()
	{
		if (getGameboard() == null) return;
		
		if (viewReloader != null)
		{
			viewReloaderInterrupt = true;
			try
			{
				viewReloader.join();
			}
			catch(InterruptedException ie) {}
		}
		
		viewReloader = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				viewReloaderInterrupt = false;
				universeViewPanel.setVisible(false);
				
				PlayerGameBoard gamboard = getGameboard();
				IGameConfig config = gameboard.getConfig();
				SEPCommonDB db = gameboard.getDB();
				String playerName = getSepClient().getLogin();
				Location sunLocation = Rules.getSunLocation(config);
				int sunRadius = config.getSunRadius();
								
				Map<Location, IArea> areas = new HashMap<Location, IArea>();
				Set<IArea> areasSet = db.getAreas();
				try
				{
					for(IArea area : areasSet)
					{
						if (viewReloaderInterrupt) return;
						
						areas.put(area.getLocation(), area);
					}
				}
				catch(Throwable t)
				{
					if (InterruptedException.class.isInstance(t.getCause())) return;
					if (Thread.interrupted()) return;
					
					log.log(Level.SEVERE, "DB error during universe redering", t);
					return;
				}
				
				for(int x=0; x < config.getDimX(); ++x)
				{
					for(int y=0; y < config.getDimY(); ++y)
					{
						for(int z=0; z < config.getDimZ(); ++z)
						{
							if (viewReloaderInterrupt) return;							
							
							Location location = new Location(x, y, z);
							
							if (SEPUtils.getDistance(location, sunLocation) <= sunRadius)
							{
								areas.put(location, db.getArea(location));
							}
							
							JImagePanel image = getImagePanel(location);
														
							resetImagePanel(image, location);
							
							if (viewReloaderInterrupt) return;
							
							if (areas.containsKey(location))
							{
								try
								{
									refreshArea(areas.get(location), playerName);
								}
								catch(Throwable t)
								{
									if (InterruptedException.class.isInstance(t.getCause())) return;
									if (Thread.interrupted()) return;
									
									log.log(Level.SEVERE, "DB error on area rendering, area skipped", t);
									throw new Error(t);
								}
							}
						}
					}
				}
				
				refreshSelection();
				
				if (viewReloaderInterrupt) return;
								
				refreshZView(true); // Force refresh
			}
		}, "View Reloader");
		
		viewReloader.start();
	}
	
	private Integer lastRefreshZ = -1;
	
	private void refreshZView(boolean force)
	{
		if (getGameboard() == null) return;
		if (!zSlider.isEnabled()) return;
		
		int z = getzSelection();
		
		if (!force && lastRefreshZ == z) return;
		lastRefreshZ = z;
		
		universeViewPanel.removeAll();
		universeViewPanel.add(getZGrid(z), BorderLayout.CENTER);
		/*
		universeViewPanel.doLayout();
		universeViewPanel.updateUI();
		*/		
		universeViewPanel.setVisible(true);
		updateUI();
	}
	
	@Override
	public void onAreaChanged(Location location)
	{
		PlayerGameBoard gb = getGameboard();
		if (gb == null) return;
		
		refreshArea(gb.getDB().getArea(location), getSepClient().getLogin());
	}
	
	private Map<Integer, JPanel> zGrids = new HashMap<Integer, JPanel>();
	
	private JPanel getZGrid(int z)
	{
		if (!zGrids.containsKey(z))
		{
			JPanel zGrid = new JPanel();
			
			PlayerGameBoard gb = getGameboard();
			if (gb == null) return zGrid;
			
			IGameConfig config = gb.getConfig();
			
			GridLayout layout = new GridLayout(config.getDimX(), config.getDimY());
			layout.setRows(config.getDimX() + 1);
			layout.setColumns(config.getDimY() + 1);
			layout.setHgap(1);
			layout.setVgap(1);
			zGrid.setLayout(layout);
			
			for(int row = 0; row < config.getDimY() + 1; ++row)
			{
				for(int col = 0; col < config.getDimX() + 1; ++col)
				{
					if (row == 0)
					{
						zGrid.add(getCoordLabel(false, col, z));						
					}
					else if (col == 0)
					{
						zGrid.add(getCoordLabel(true, row, z));
					}
					else
					{
						Location location = new Location(col - 1, row - 1, z);
						zGrid.add(getImagePanel(location));
					}
				}
			}
			
			zGrids.put(z, zGrid);
		}
		
		getCoordLabel(false, 0, z).setText(String.format("T %d", getGameboard().getConfig().getTurn()));
		
		return zGrids.get(z);
	}
	
	/*
	private Integer lastRefreshZ = -1;
	private Thread zViewRefresher = null;
	private Boolean interruptRefresh = false;
	
	/**
	 * Refresh view if z selection has changed, or force is true.
	 * @param force
	 *//*
	public void refresh(boolean force)
	{
		// TODO: Instead of refreshing from 0 every time, load every required component in memory for fast z switching.
		// Don't forget to refresh on local events.		
		if (getGameboard() == null) return;		
		if (!zSlider.isEnabled()) return;
		
		final int z = getzSelection();
		
		if (!force && lastRefreshZ == z) return;
		lastRefreshZ = z;		
		
		if (zViewRefresher != null)
		{
			//zViewRefresher.interrupt();
			interruptRefresh = true;
			try
			{
				zViewRefresher.join();
			}
			catch(InterruptedException ie) {}
		}
		
		// Outdated call must abort.
		if (lastRefreshZ != z) return;				
		
		zViewRefresher = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				interruptRefresh = false;
				universeViewPanel.setVisible(false);
				
				PlayerGameBoard gamboard = getGameboard();
				IGameConfig config = gameboard.getConfig();
				SEPCommonDB db = gameboard.getDB();
				String playerName = getSepClient().getLogin();

				if (z < 0 || z >= config.getDimZ())
				{
					//ignoreZSlider = false;
					return;
				}
								
				ExecTimeMeasures etm = new ExecTimeMeasures();
				
				etm.measures("resetImages");
				
				lastRefreshZ = z;

				if (interruptRefresh) return;
				resetImages(z);
				
				etm.measures("getAreasByZ");
				
				Set<IArea> areas;
				try
				{
					if (interruptRefresh) return;
					areas = db.getAreasByZ(z); // Will select any previously created area in Z slice.
				}
				catch(Throwable t)
				{
					if (InterruptedException.class.isInstance(t.getCause())) return;
					if (Thread.interrupted()) return;
					
					log.log(Level.SEVERE, "SQL Error during universe redering", t);
					return;
				}
				
				etm.measures("foreach area");
				for(IArea area: areas)
				{	
					if (interruptRefresh) return;
					
					try
					{
						etm.measures("area "+area.getLocation());
						refreshArea(area, playerName);
					}
					catch(Throwable t)
					{
						if (InterruptedException.class.isInstance(t.getCause())) return;
						if (Thread.interrupted()) return;
						
						log.log(Level.SEVERE, "SQL Error on area rendering, area skipped", t);
						throw new Error(t);
					}
				}
				etm.measures("refreshSelection");
				
				refreshSelection(false);
				
				if (interruptRefresh) return;
				
				etm.measures("updateUI");
				updateUI();
				
				etm.measures("universeViewPanel.setVisible(true)");
				universeViewPanel.setVisible(true);
				
				etm.measures("end");				
				
				System.err.println(etm.toString());
			}
		}, "zViewRefresher");
		
		zViewRefresher.start();
	}
	*/

	private void resetImagePanel(JImagePanel image, Location location)
	{
		// Default tooltip
		image.setToolTipText("Location "+location);
		
		// Default background is black.
		image.setBackground(Color.black);

		// Default border is null.
		image.setBorder(null);
		
		// Default image.
		image.setImage((Image) null);
	}
	
	private void refreshArea(IArea area, String playerName)
	{

		Location location = area.getLocation();
		boolean isVisible = area.isVisible(playerName);
		
		JImagePanel image = getImagePanel(location);
		
		// Reset
		resetImagePanel(image, location);
		
		String toolTipText = image.getToolTipText()+"\n"+area.toString(playerName);

		// If visible, background is gray				
		//image.setBorder((area.isVisible() ? BorderFactory.createLineBorder(Color.green) : null));		
		if (isVisible)
		{
			image.setBackground(Color.darkGray);
		}

		// If sun, background is red.
		if (area.isSun())
		{
			image.setBackground(Color.red);
		}

		// Celestial body
		boolean ownedCelestialBody = false;
		ICelestialBody celestialBody = area.getCelestialBody();
		if (celestialBody != null)
		{				
			image.setImage(getTile(celestialBody.getType().toString(), celestialBody.getName()));
			
			if (IProductiveCelestialBody.class.isInstance(celestialBody) && playerName.equals(IProductiveCelestialBody.class.cast(celestialBody).getOwner()))
			{
				ownedCelestialBody = true;
			}
		}

		boolean ownedUnit = false;
		boolean otherUnit = false;
		boolean ownedMarker = false;
		boolean otherMarker = false;
		int mostRecent = 0;
		Set<? extends IUnitMarker> units = area.getUnitsMarkers(null);
								
		if (units != null && units.size() > 0)
		{
			for(IUnitMarker unitMarker: units)
			{
				if (mostRecent > unitMarker.getTurn())
				{
					continue;
				}
				else if (mostRecent < unitMarker.getTurn())
				{
					ownedUnit = false;
					otherUnit = false;
					ownedMarker = false;
					otherMarker = false;
				}
					
				mostRecent = unitMarker.getTurn();
				
				// TODO: Distinguer l'âge du marker!
				boolean owned = playerName.equals(unitMarker.getOwnerName());
				
				if (IUnit.class.isInstance(unitMarker))
				{					
					ownedUnit |= owned;
					otherUnit |= !owned;
				}
				else
				{
					ownedMarker |= owned;
					otherMarker |= !owned;
				}
				
				//toolTipText += "   "+(owned ? unitMarker.toString() : "["+unitMarker.getOwnerName()+"] "+unitMarker.getName()+"\n");
			}
		}
		
		Color borderColor = ownedMarker ? Color.blue : ownedUnit ? Color.green : otherMarker ? Color.red.darker() : otherUnit ? Color.red : null; 
		if (borderColor != null)
		{
			int currentTurn = getSepClient().getGameboard().getConfig().getTurn();
			for(int i=mostRecent; i < currentTurn; ++i)
			{
				borderColor = borderColor.darker();
			}
			
			image.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, borderColor, Color.lightGray));
		}
		
		if (ownedCelestialBody)
		{
			image.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.green.darker()), image.getBorder()));
		}
		
		if (location.equals(selectedLocation))
		{
			image.setBorder(BorderFactory.createLineBorder(Color.yellow));
		}

		image.setToolTipText("<html>" + toolTipText.replaceAll("\n", "<br>") + "</html>");	
	}
	
	private final Map<String, Integer> nbTiles = new HashMap<String, Integer>();
	private int getNbTiles(String rawType)
	{
		final String type = rawType.toLowerCase();
		if (!nbTiles.containsKey(type))
		{
			URL pathURL = Reflect.getResource(SpaceEmpirePulsarGUI.class.getPackage().getName()+".img", type);
			File path = new File(pathURL.getPath());
			String[] files = path.list(new FilenameFilter()
			{
				
				@Override
				public boolean accept(File dir, String name)
				{
					return name.matches(type+"[0-9]+\\.png");
				}
			});
			int nb = files.length;
			nbTiles.put(type, nb);
		}
		return nbTiles.get(type);
	}
	
	private URL getTile(String type, String id)
	{
		type = type.toLowerCase();
		int nbTiles = getNbTiles(type);
		int choosenTile = new Random(id.hashCode()).nextInt(nbTiles)+1;
		
		return Reflect.getResource(SpaceEmpirePulsarGUI.class.getPackage().getName()+".img."+type, String.format("%s%d.png", type, choosenTile));		
	}
	
	private JImagePanel getImagePanel(final Location location)
	{
		if (!images.containsKey(location))
		{
			JImagePanel image = new JImagePanel();
			image.setBackground(Color.black);
			image.setAutoSize(true);
			image.setKeepAspect(false);

			image.addMouseListener(new MouseAdapter()
			{
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
					{
						//System.out.println("Location [" + x + ";" + y + ";" + z + "] clicked");
						selectLocation(location.x, location.y, location.z);
					}

					super.mouseClicked(e);
				}
			});

			images.put(location, image);
		}

		return images.get(location);
	}

	//private Border previousBorder = null;
	private void selectLocation(int x, int y, int z)
	{
		if (areaSelectionListener != null)
		{
			areaSelectionListener.updateSelectedArea(x, y, z);
			return;
		}
		
		refreshSelection(x, y, z);
		
		if (listener != null)
		{
			listener.updateSelectedArea(x, y, z);
		}
	}
	
	private void refreshSelection()
	{
		if (selectedLocation == null) return;
		refreshSelection(selectedLocation.x, selectedLocation.y, selectedLocation.z);
	}
	private void refreshSelection(int x, int y, int z)
	{
		SEPCommonDB db = getGameboard().getDB();
		String playerName = getSepClient().getLogin();
		if (getzSelection() != z) return;
		
		Location oldSelection = selectedLocation;
		selectedLocation = new Location(x, y, z);
		
		if (oldSelection != null && oldSelection.equals(selectedLocation)) return;
		
		if (oldSelection != null)
		{
			refreshArea(db.getArea(oldSelection), playerName);
		}
		
		refreshArea(db.getArea(selectedLocation), playerName);				
	}
}
