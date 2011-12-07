package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import org.axan.eplib.orm.SQLDataBaseException;
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
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.Area;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;
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
	private boolean ignoreZSlider = false;
	private final Map<Integer, JImagePanel> images = new Hashtable<Integer, JImagePanel>();
	private Location selectedLocation = null;

	////////// ui controls
	private JPanel universeViewPanel;
	private JSlider zSlider;

	////////// bean fields
	SEPClient sepClient;
	private PlayerGameBoard gameboard = null;

	////////// no arguments constructor
	public SwingUniverseRenderer()
	{
		build = SwingJavaBuilder.build(this);

		zSlider.setEnabled(false);
		zSlider.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (ignoreZSlider)
					return;

				System.out.println("zSlider stateChanged : " + e);
				changeZView(zSlider.getValue());
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

		// Never used zSlider
		boolean freshSlider = !zSlider.isEnabled();

		zSlider.setMinimum(0);
		zSlider.setMaximum(config.getDimZ() - 1);

		if (freshSlider)
		{
			zSlider.setValue(config.getDimZ() / 2);
			zSlider.setEnabled(true);
		}

		changeZView(zSlider.getValue());
	}

	////////// ui events

	////////// private methods

	private void changeZView(int z)
	{
		if (getGameboard() == null || !zSlider.isEnabled())
			return;

		PlayerGameBoard gameboard = getGameboard();
		SEPCommonDB db = gameboard.getDB();
		IGameConfig config = gameboard.getConfig();
		String playerName = getSepClient().getLogin();

		if (z < 0 || z >= config.getDimZ())
			return;

		ignoreZSlider = true;

		zSlider.setValue(z);

		Set<IArea> areas;
		try
		{
			areas = Area.select(db, IArea.class, null, "location_z = ?", z);
		}
		catch(SQLDataBaseException e)
		{
			log.log(Level.SEVERE, "SQL Error during universe redering", e);
			return;
		}
		
		for(IArea area: areas)
		{
			try
			{
				boolean isVisible = area.isVisible(db, playerName);
				
				JImagePanel image = getImagePanel(area.getLocation().x, area.getLocation().y);
	
				String toolTipText = "Location [" + area.getLocation().x + ";" + area.getLocation().y + ";" + area.getLocation().z + "]\nTODO: Area description\n";
	
				// Default background is black.
				image.setBackground(Color.black);
	
				// Default border is null.
				image.setBorder(null);
	
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
				ICelestialBody celestialBody = area.getCelestialBody(db);
				if (celestialBody != null)
				{				
					image.setImage(getTile(celestialBody.getType().toString(), celestialBody.getName()));
					
					if (IProductiveCelestialBody.class.isInstance(celestialBody) && playerName.equals(IProductiveCelestialBody.class.cast(celestialBody).getOwner()))
					{
						ownedCelestialBody = true;
					}
				}
				else
				{
					image.setImage((String) null);
				}
	
				Set<IUnit> units = area.getUnits(db, IUnit.class);
				
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
			catch(SQLDataBaseException e)
			{
				log.log(Level.SEVERE, "SQL Error on area rendering, area skipped", e);
				continue;
			}
		}
		
		updateUI();
		
		ignoreZSlider = false;
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
		changeZView(zSlider.getValue());
		if (listener != null)
		{
			listener.updateSelectedArea(x, y, z);
		}
	}
}
