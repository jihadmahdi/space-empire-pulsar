package org.axan.sep.client.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.RunningGamePanel.AUniverseRendererPanel;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.orm.Area;
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
	public void refreshGameBoard(PlayerGameBoard gameboard)
	{
		if (gameboard != null)
			setGameboard(gameboard);
		gameboard = getGameboard();
		IGameConfig config = gameboard.getConfig();

		if (gameboard == null)
			return;

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
		boolean freshSlider = zSlider.getValue() < 0;

		zSlider.setMinimum(0);
		zSlider.setMaximum(config.getDimZ() - 1);

		if (freshSlider)
		{
			zSlider.setValue(config.getDimZ() / 2);
		}

		changeZView(zSlider.getValue());
	}

	////////// ui events

	////////// private methods

	private void changeZView(int z)
	{
		if (getGameboard() == null)
			return;

		PlayerGameBoard gameboard = getGameboard();
		IGameConfig config = gameboard.getConfig();

		if (z < 0 || z >= config.getDimZ())
			return;

		ignoreZSlider = true;

		zSlider.setValue(z);

		Set<IArea> areas = Area.select(gameboard.getDB(), IArea.class, null, "location_z = ?", z);

		for(IArea area: areas)
		{			
			JImagePanel image = getImagePanel(area.getLocation().x, area.getLocation().y);

			String toolTipText = "Location [" + area.getLocation().x + ";" + area.getLocation().y + ";" + area.getLocation().z + "]\nTODO: Area description";

			// Default background is black.
			image.setBackground(Color.black);

			// Default border is null.
			image.setBorder(null);

			// If visible, background is gray				
			//image.setBorder((area.isVisible() ? BorderFactory.createLineBorder(Color.green) : null));
			if (area.isVisible())
				image.setBackground(Color.gray);

			// If sun, background is red.
			if (area.isSun())
				image.setBackground(Color.red);

			// Celestial body
			boolean ownedCelestialBody = false;
			ICelestialBody celestialBody = area.getCelestialBody();
			if (celestialBody != null)
			{
				String type = celestialBody.getClass().getSimpleName().toLowerCase();

				// TODO : Crappy code to replace later
				int nbTiles = 1;
				if (type.compareTo("planet") == 0)
					nbTiles = PLANET_TILES;
				else if (type.compareTo("asteroidfield") == 0)
					nbTiles = ASTEROID_TILES;
				else if (type.compareTo("nebula") == 0)
					nbTiles = NEBULA_TILES;

				int tile = getCelestialBodyTile(celestialBody.getName(), nbTiles);
				image.setImage(OLDSpaceEmpirePulsarGUI.IMG_PATH + type + File.separatorChar + type + tile + ".png");

				if (ProductiveCelestialBody.class.isInstance(celestialBody) && currentGameBoard.getPlayerName().equals(ProductiveCelestialBody.class.cast(celestialBody).getOwnerName()))
				{
					ownedCelestialBody = true;
				}
			}
			else
			{
				image.setImage((String) null);
			}

			Set<UnitMarker> unitMarkers = area.getMarkers(UnitMarker.class);
			Set<Unit> units = area.getUnits();

			Color borderColor = null;

			if ((units != null && units.size() > 0) || (unitMarkers != null && unitMarkers.size() > 0))
			{
				boolean ownedUnit = false;
				boolean otherUnit = false;
				boolean ownedMarker = false;
				boolean otherMarker = false;

				toolTipText += "Units :\n";

				if (units != null && units.size() > 0)
				{
					for(Unit u: units)
					{
						toolTipText += "\t" + u.toString() + "\n";
						if (currentGameBoard.getPlayerName().equals(u.getOwnerName()))
						{
							ownedUnit = true;
						}
						else
						{
							otherUnit = true;
						}
					}
				}

				if (unitMarkers != null && unitMarkers.size() > 0)
				{
					for(UnitMarker um: unitMarkers)
					{
						toolTipText += "\t" + um.getUnit().toString() + "\n";

						if (currentGameBoard.getPlayerName().equals(um.getUnit().getOwnerName()))
						{
							ownedMarker = true;
						}
						else
						{
							otherMarker = true;
						}
					}
				}

				if (ownedMarker)
					borderColor = Color.blue;
				if (ownedUnit)
					borderColor = Color.green;
				if (otherMarker)
					borderColor = Color.red.darker();
				if (otherUnit)
					borderColor = Color.red;
			}

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
			if (selectedLocation != null && selectedLocation.equals(new RealLocation(x + 0.5, y + 0.5, z + 0.5)))
			{
				image.setBorder(BorderFactory.createLineBorder(Color.yellow));
			}

			image.setToolTipText("<html>" + toolTipText.replaceAll("\n", "<br>") + "</html>");
		}

		getUniverseViewPanel().updateUI();

		ignoreZSlider = false;
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

					if (e.getClickCount() == 1 && e.getButton() == e.BUTTON1)
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
		changeZView(zSlider.getValue());
		if (listener != null)
			listener.updateSelectedArea(x, y, z);
	}
}
