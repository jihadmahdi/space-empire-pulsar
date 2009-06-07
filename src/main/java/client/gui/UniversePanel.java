package client.gui;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.swing.BorderFactory;

import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.axan.eplib.utils.Basic;

import client.gui.lib.JImagePanel;

import common.Area;
import common.ICelestialBody;
import common.Planet;
import common.PlayerGameBoard;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class UniversePanel extends javax.swing.JPanel implements UniverseRenderer
{
	private final int			PLANET_TILES	= 16;

	private final int			ASTEROID_TILES	= 4;

	private JSlider				zSlider;

	private final int			NEBULA_TILES	= 9;

	private JPanel				controlPanel;

	private JPanel				universeViewPanel;

	private PlayerGameBoard		currentGameBoard;
	
	private UniverseRendererListener listener;
	
	public UniversePanel()
	{
		super();
		initGUI();
	}

	private void initGUI()
	{
		try
		{
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				controlPanel = new JPanel();
				BorderLayout controlPanelLayout = new BorderLayout();
				this.add(controlPanel, BorderLayout.SOUTH);
				controlPanel.setLayout(controlPanelLayout);
				controlPanel.setPreferredSize(new java.awt.Dimension(10, 30));				
				controlPanel.add(getZSlider(), BorderLayout.CENTER);
				controlPanel.add(getZLabel(), BorderLayout.WEST);
			}
			{
				this.add(getUniverseViewPanel(), BorderLayout.CENTER);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private JLabel	zLabel;

	private JLabel getZLabel()
	{
		if (zLabel == null)
		{
			zLabel = new JLabel();
		}
		return zLabel;
	}

	private JPanel getUniverseViewPanel()
	{
		if (universeViewPanel == null)
		{
			universeViewPanel = new JPanel();
			universeViewPanel.setOpaque(false);
			universeViewPanel.setBorder(BorderFactory.createLineBorder(Color.white));
			universeViewPanel.setBackground(Color.yellow);
			universeViewPanel.addComponentListener(new ComponentAdapter()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
				 */
				@Override
				public void componentResized(ComponentEvent e)
				{
					/*
					if (getUniverseViewPanel().getSize().width > getUniverseViewPanel().getSize().height)
					{
						Dimension size = new Dimension(getUniverseViewPanel().getSize().height, getUniverseViewPanel().getMaximumSize().height);
						getUniverseViewPanel().setMaximumSize(size);
						getUniverseViewPanel().setSize(size);						
						return;
					}
					*/
					super.componentResized(e);
				}
			});
		}
		return universeViewPanel;
	}

	public synchronized void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		currentGameBoard = gameBoard;
		GridLayout layout = new GridLayout(currentGameBoard.getDimX(), currentGameBoard.getDimY());
		layout.setRows(currentGameBoard.getDimX()+1);
		layout.setColumns(currentGameBoard.getDimY()+1);
		layout.setHgap(1);
		layout.setVgap(1);
		getUniverseViewPanel().setLayout(layout);
		getUniverseViewPanel().removeAll();
		for (int row = 0; row < currentGameBoard.getDimY()+1; ++row)			
			for (int col = 0; col < currentGameBoard.getDimX()+1; ++col)
			{
				if (row == 0)
				{
					getUniverseViewPanel().add(getCoordLabel(false, col));
				}
				else if (col == 0)
				{
					getUniverseViewPanel().add(getCoordLabel(true, row));
				}
				else
				{
					getUniverseViewPanel().add(getImagePanel(col-1, row-1));
				}
			}
		getZSlider().setMinimum(0);
		getZSlider().setMaximum(currentGameBoard.getDimZ()-1);
		getZSlider().setValue(currentGameBoard.getDimZ()/2);
		changeZView(currentGameBoard.getDimZ()/2);
	}

	
	
	private boolean	ignoreZSlider	= false;

	private void changeZView(int z)
	{
		if (currentGameBoard == null) return;
		if (z < 0 || z >= currentGameBoard.getDimZ()) return;

		ignoreZSlider = true;

		getZSlider().setValue(z);
		
		for (int x = 0; x < currentGameBoard.getDimX(); ++x)
			for (int y = 0; y < currentGameBoard.getDimY(); ++y)
			{
				Area area = currentGameBoard.getArea(x, y, z);
				JImagePanel image = getImagePanel(x, y);
				
				String toolTipText = "Location ["+x+";"+y+";"+z+"]\n"+area.toString();				
				
				// If visible, border is green.				
				image.setBorder((area.isVisible()?BorderFactory.createLineBorder(Color.green):null));				
				
				// Border is yellow if selected
				if (selectedLocation != null && selectedLocation[2] == z && x == selectedLocation[0] && y == selectedLocation[1])
				{
					image.setBorder(BorderFactory.createLineBorder(Color.yellow));
				}

				// If sun, background is red.
				image.setBackground(area.isSun()?Color.red:Color.black);				
				
				// Celestial body
				ICelestialBody celestialBody = area.getCelestialBody();
				if (celestialBody != null)
				{  					
					String type = celestialBody.getClass().getSimpleName().toLowerCase();

					// TODO : Crappy code to replace later
					int nbTiles = 1;
					if (type.compareTo("planet") == 0) nbTiles = PLANET_TILES;
					else if (type.compareTo("asteroidfield") == 0) nbTiles = ASTEROID_TILES;
					else if (type.compareTo("nebula") == 0) nbTiles = NEBULA_TILES;

					int tile  = getCelestialBodyTile(celestialBody.getName(), nbTiles);					
					image.setImage(SpaceEmpirePulsarGUI.IMG_PATH + type + File.separatorChar + type + tile + ".png");
				}
				else
				{
					image.setImage((String) null);
				}
				
				image.setToolTipText("<html>"+toolTipText.replaceAll("\n", "<br>")+"</html>");								
			}

		getUniverseViewPanel().updateUI();

		ignoreZSlider = false;
	}
	
	private final Map<String, Integer> celestialBodiesTiles = new Hashtable<String, Integer>();
	
	private int getCelestialBodyTile(String name, int nbTiles)
	{
		if (!celestialBodiesTiles.containsKey(name))
		{
			int tile;
			String id;
			try
			{
				id = Basic.md5(name);
			}
			catch (NoSuchAlgorithmException e)
			{				
				e.printStackTrace();
				id = name;
			}
			tile = new Random(id.hashCode()).nextInt(nbTiles)+1;
			
			celestialBodiesTiles.put(name, tile);
		}
		return celestialBodiesTiles.get(name);
	}

	private final Map<Integer, JLabel> coords = new Hashtable<Integer, JLabel>();
	
	private JLabel getCoordLabel(boolean isVertical, int i)
	{
		int key = (isVertical?1:-1)*i;
		if (!coords.containsKey(key))
		{
			JLabel label = new JLabel();
			String txt = (i>0)?((isVertical?"y":"x")+(i-1)):"";
			label.setText(txt);
			coords.put(key, label);
		}
		return coords.get(key);
	}
	
	private final Map<Integer, JImagePanel>	images	= new Hashtable<Integer, JImagePanel>();

	private JImagePanel getImagePanel(final int x, final int y)
	{
		int i = x * currentGameBoard.getDimX() + y;

		if ( !images.containsKey(i))
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
					int z = getZSlider().getValue();
					
					if (e.getClickCount() == 1 && e.getButton() == e.BUTTON1)
					{
						System.out.println("Location ["+x+";"+y+";"+z+"] clicked");
						selectLocation(x, y, z);
					}
					
					super.mouseClicked(e);
				}
			});
			
			images.put(i, image);
		}

		return images.get(i);
	}
	
	private int[] selectedLocation;
	private void selectLocation(int x, int y, int z)
	{
		selectedLocation = new int[] {x, y, z};
		changeZView(getZSlider().getValue());		
		if (listener != null) listener.updateSelectedArea(x, y, z);
	}

	private JSlider getZSlider()
	{
		if (zSlider == null)
		{
			zSlider = new JSlider();
			zSlider.setPreferredSize(new java.awt.Dimension(398, 28));
			zSlider.addChangeListener(new ChangeListener()
			{

				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (ignoreZSlider) return;

					System.out.println("zSlider stateChanged : " + e);
					getZLabel().setText("Z: " + getZSlider().getValue());
					changeZView(getZSlider().getValue());
				}
			});
		}
		return zSlider;
	}

	/* (non-Javadoc)
	 * @see client.gui.UniverseRenderer#setListener(client.gui.UniverseRenderer.UniverseRendererListener)
	 */
	@Override
	public void setListener(UniverseRendererListener listener)
	{
		this.listener = listener;
	}
}