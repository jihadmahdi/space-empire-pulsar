package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.RunningGamePanel.AUniverseRendererPanel;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class SwingUniverseRenderer extends AUniverseRendererPanel
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());

	////////// static methods

	static private Map<Integer, JLabel> coordLabels = new HashMap<Integer, JLabel>();

	static private JLabel getCoordLabel(boolean isVertical, int i)
	{
		int key = (isVertical ? 1 : -1) * i;
		if (!coordLabels.containsKey(key))
		{
			JLabel label = new JLabel();
			String txt = (i > 0) ? ((isVertical ? "y" : "x") + (i - 1)) : "";
			label.setText(txt);
			coordLabels.put(key, label);
		}
		return coordLabels.get(key);
	}

	////////// private attributes
	private final BuildResult build;
	private IUniverseRendererListener listener = null;
	//private boolean ignoreZSlider = false;
	private final Map<Integer, JImagePanel> images = new Hashtable<Integer, JImagePanel>();
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
		/*
		zSlider.addChangeListener(new ChangeListener()
		{
			private int previousZValue = -1;

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (previousZValue != zSlider.getValue())
				{
					previousZValue = zSlider.getValue();
					System.out.println("zSlider value changed");
					changeZView(zSlider.getValue());
				}
			}
		});
		*/
		
		addPropertyChangeListener("zSelection", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getNewValue().equals(evt.getOldValue())) return;
				int z = (Integer) evt.getNewValue();
				changeZView(z);
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
	public void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents)
	{
		if (getGameboard() == null)
		{
			setGameboard(getSepClient().getGameboard());
		}
		
		PlayerGameBoard gameboard = getGameboard();
		if (gameboard == null) return;
		IGameConfig config = gameboard.getConfig();

		GridLayout layout = new GridLayout(config.getDimX(), config.getDimY());
		layout.setRows(config.getDimX() + 1);
		layout.setColumns(config.getDimY() + 1);
		layout.setHgap(1);
		layout.setVgap(1);
		universeViewPanel.setLayout(layout);
		universeViewPanel.removeAll();
		for(int row = 0; row < config.getDimY() + 1; ++row)
			for(int col = 0; col < config.getDimX() + 1; ++col)
			{
				if (row == 0)
				{
					universeViewPanel.add(getCoordLabel(false, col));

					if (col == 0)
					{
						getCoordLabel(false, col).setText(String.format("T %d", config.getTurn()));
					}
				}
				else if (col == 0)
				{
					universeViewPanel.add(getCoordLabel(true, row));
				}
				else
				{
					universeViewPanel.add(getImagePanel(col - 1, row - 1));
				}
			}
		
		zSlider.setMinimum(0);
		zSlider.setMaximum(config.getDimZ() - 1);

		if (!zSlider.isEnabled()) // First refresh, force z value to player starting planet z coord.
		{
			zSlider.setEnabled(true);
			
			IPlanet startingPlanet = gameboard.getDB().getStartingPlanet(getSepClient().getLogin());
			
			// Changing zSlider bound to zSelection that fires changeZView UI refresh.
			zSlider.setValue(startingPlanet.getLocation().z);
			return;			
		}		
		
		// Does not change zSelection, only do UI refresh.
		changeZView(getzSelection());
	}

	////////// ui events

	////////// private methods

	private Integer lastRefreshZ = -1;
	private Thread zViewRefresher = null;
	private void changeZView(final int z)
	{
		// SUIS LA: Mesurer quel est l'opération la plus couteuse (instanciation des objets ORM, ou requetes ?) puis concevoir une solution d'optimisation (cache des requetes avec système d'abonnement au "tables" qui force les refresh des caches des requetes seulement lorsqu'une des tables a été update/insert/delete.
		
		// TODO: Instead of refreshing from 0 every time, load every required component in memory for fast z switching.
		// Don't forget to refresh on local events.
		
		if (getGameboard() == null) return;		
		if (!zSlider.isEnabled()) return;
		
		if (lastRefreshZ == z) return;
		lastRefreshZ = z;		
		
		if (zViewRefresher != null)
		{
			zViewRefresher.interrupt();
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
				PlayerGameBoard gamboard = getGameboard();
				IGameConfig config = gameboard.getConfig();
				SEPCommonDB db = gameboard.getDB();
				String playerName = getSepClient().getLogin();

				if (z < 0 || z >= config.getDimZ())
				{
					//ignoreZSlider = false;
					return;
				}
								
				lastRefreshZ = z;

				resetImages(z);
				
				Set<IArea> areas;
				try
				{
					areas = db.getAreasByZ(z);
				}
				catch(Throwable t)
				{
					if (InterruptedException.class.isInstance(t.getCause())) return;
					if (Thread.interrupted()) return;
					
					log.log(Level.SEVERE, "SQL Error during universe redering", t);
					return;
				}								
				
				for(IArea area: areas)
				{	
					if (zViewRefresher.isInterrupted()) return;
					
					try
					{
						boolean isVisible = area.isVisible(playerName);
						
						JImagePanel image = getImagePanel(area.getLocation().x, area.getLocation().y);
						
						String toolTipText = image.getToolTipText()+"\n"+area.toString(playerName);
			
						// If visible, background is gray				
						//image.setBorder((area.isVisible() ? BorderFactory.createLineBorder(Color.green) : null));
						if (isVisible)
						{
							image.setBackground(Color.gray);
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
			
						Set<IUnit> units = area.getUnits(IUnit.class);
						
						Color borderColor = null;
			
						if (units != null && units.size() > 0)
						{
							boolean ownedUnit = false;
							
							toolTipText += "Units :\n";
			
							if (units != null && units.size() > 0)
							{
								for(IUnit u: units)
								{
									toolTipText += "\t" + u.toString() + "\n";
									ownedUnit = (playerName.equals(u.getOwner()));
								}
							}				
			
							borderColor = ownedUnit ? Color.green : Color.red;				
						}
						
						// TODO: Previously seen units and traveling units markers
			
						if (!ownedCelestialBody || borderColor == null)
						{
							if (borderColor == null && ownedCelestialBody)
								borderColor = Color.green.darker();
							if (borderColor != null)
								image.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, borderColor, Color.darkGray));//(borderColor, 2));
						}
						else
						{
							image.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.green.darker(), 2), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, borderColor, Color.darkGray)));
						}
			
						// Border is yellow if selected
						if (selectedLocation != null && selectedLocation.equals(area.getLocation()))
						{
							image.setBorder(BorderFactory.createLineBorder(Color.yellow));
						}
			
						image.setToolTipText("<html>" + toolTipText.replaceAll("\n", "<br>") + "</html>");
					}
					catch(Throwable t)
					{
						if (InterruptedException.class.isInstance(t.getCause())) return;
						if (Thread.interrupted()) return;
						
						log.log(Level.SEVERE, "SQL Error on area rendering, area skipped", t);
						throw new Error(t);
					}
				}
				
				updateUI();
			}
		}, "zViewRefresher");
		
		zViewRefresher.start();
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
	
	private void resetImages(int z)
	{
		int dimX = getGameboard().getConfig().getDimX();
		for(Map.Entry<Integer, JImagePanel> e : images.entrySet())
		{
			int x = e.getKey() / dimX;
			int y = e.getKey() % dimX;
			
			JImagePanel image = e.getValue();
			
			// Default tooltip
			image.setToolTipText("Location [" + x + ";" + y + ";" + z + "]");
			
			// Default background is black.
			image.setBackground(Color.black);

			// Default border is null.
			image.setBorder(null);
			
			// Default image.
			image.setImage((Image) null);
		}
	}
	
	private JImagePanel getImagePanel(final int x, final int y)
	{
		int i = x * getGameboard().getConfig().getDimX() + y;

		if (!images.containsKey(i))
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
					int z = zSlider.getValue();

					if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
					{
						System.out.println("Location [" + x + ";" + y + ";" + z + "] clicked");
						selectLocation(x, y, z);
					}

					super.mouseClicked(e);
				}
			});

			images.put(i, image);
		}

		return images.get(i);
	}

	private void selectLocation(int x, int y, int z)
	{
		selectedLocation = new Location(x, y, z);
		
		// Border is yellow if selected
		getImagePanel(x, y).setBorder(BorderFactory.createLineBorder(Color.yellow));		
		
		if (listener != null)
		{
			listener.updateSelectedArea(x, y, z);
		}
	}
}
