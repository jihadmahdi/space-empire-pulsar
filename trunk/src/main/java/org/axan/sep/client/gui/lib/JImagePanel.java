/**
 * 
 */
package org.axan.sep.client.gui.lib;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;

public class JImagePanel extends JPanel
{
	private static final long	serialVersionUID	= 2724980460740151616L;
	
	static
	{
		ImageIO.setUseCache(true);
	}

	private Image				_image;  //  @jve:decl-index=0:

	private int					_x;

	private int					_y;

	private boolean				_autoSize;

	private boolean				_keepAspect;
	
	private int					_rang;
	
	public JImagePanel(File file, int x, int y, boolean autoSize, boolean keepAspect) throws IOException
	{
		super();		
		initialize();
		setImage(ImageIO.read(file));
		setX(x);
		setY(y);
		setAutoSize(autoSize);
		setKeepAspect(keepAspect);
	}

	public JImagePanel(File file, int x, int y, boolean autoSize)
			throws IOException
	{
		this(file, x, y, true, false);
	}

	public JImagePanel(File file, int x, int y) throws IOException
	{
		this(file, x, y, true);
	}

	public JImagePanel(File file) throws IOException
	{
		this(file, 0, 0, true);
	}

	public JImagePanel(Image image, int x, int y, boolean autoSize, boolean keepAspect)
	{
		super();
		initialize();

		setImage(image);
		setX(x);
		setY(y);
		setAutoSize(autoSize);
		setKeepAspect(keepAspect);
	}

	public JImagePanel(Image image, int x, int y, boolean autoSize)
	{
		this(image, x, y, true, false);
	}

	public JImagePanel(Image image, int x, int y)
	{
		this(image, x, y, true);
	}

	public JImagePanel(Image image)
	{
		this(image, 0, 0, true);
	}
	
	public JImagePanel(URL imageUrl, int x, int y, boolean autoSize, boolean keepAspect)
	{
		super();
		initialize();

		setImage(imageUrl);
		setX(x);
		setY(y);
		setAutoSize(autoSize);
		setKeepAspect(keepAspect);
	}

	public JImagePanel(URL imageUrl, int x, int y, boolean autoSize)
	{
		this(imageUrl, x, y, true, false);
	}

	public JImagePanel(URL imageUrl, int x, int y)
	{
		this(imageUrl, x, y, true);
	}

	public JImagePanel(URL imageUrl)
	{
		this(imageUrl, 0, 0, true);
	}

	public JImagePanel()
	{
		super();
		initialize();
		setX(0);
		setY(0);
		setImage((Image) null);
		setAutoSize(false);
		
		addAncestorListener(new AncestorListener()
		{
			
			@Override
			public void ancestorRemoved(AncestorEvent event)
			{
				refresh("ancestorRemoved");
			}
			
			@Override
			public void ancestorMoved(AncestorEvent event)
			{
				refresh("ancestorMoved");
			}
			
			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				refresh("ancestorAdded");
			}
		});
		
		addComponentListener(new ComponentListener()
		{
			
			@Override
			public void componentShown(ComponentEvent e)
			{
				refresh("componentShown");
			}
			
			@Override
			public void componentResized(ComponentEvent e)
			{
				refresh("componentResized");
			}
			
			@Override
			public void componentMoved(ComponentEvent e)
			{
				refresh("componentMoved");
			}
			
			@Override
			public void componentHidden(ComponentEvent e)
			{
				refresh("componentHidden");
			}
		});
		
		addContainerListener(new ContainerListener()
		{
			
			@Override
			public void componentRemoved(ContainerEvent e)
			{
				refresh("componentRemoved");
			}
			
			@Override
			public void componentAdded(ContainerEvent e)
			{
				refresh("componentAdded");
			}
		});
		
		addFocusListener(new FocusListener()
		{
			
			@Override
			public void focusLost(FocusEvent e)
			{
				refresh("focusLost");
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{
				refresh("focusGained");
			}
		});
		
		addHierarchyBoundsListener(new HierarchyBoundsListener()
		{
			
			@Override
			public void ancestorResized(HierarchyEvent e)
			{
				refresh("ancestorResized");
			}
			
			@Override
			public void ancestorMoved(HierarchyEvent e)
			{
				refresh("ancestorMoved");
			}
		});
		
		addHierarchyListener(new HierarchyListener()
		{
			
			@Override
			public void hierarchyChanged(HierarchyEvent e)
			{
				refresh("hierarchyChanged");
			}
		});
		
		addPropertyChangeListener(new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				refresh("propertyChange");
			}
		});
				
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize()
	{
	
	}

	private void refresh(final String caller)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				//System.err.println("JImagePanel.refresh("+caller+")");
				doLayout();
				validate();
				invalidate();
				repaint();
			}
		});		
	}
	
	public void setImage(Image image)
	{
		Image old = _image;
		_image = image;
		firePropertyChange("image", old, image);
	}
	
	public void setImage(File file)
	{
		if (file == null)
		{
			setImage((Image) null);
		}
		else
		{
			try
			{
				setImage(ImageIO.read(file));
			}
			catch(IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("Erreur chargement fichier image '" + file + "'");
			}
		}
	}
	
	public void setImage(String sFile)
	{
		if (sFile == null)
		{
			setImage((Image) null);
		}
		else
		{
			URL urlImg = ClassLoader.getSystemResource(sFile);
			if (urlImg == null)
			{
				System.out.println("Img '"+sFile+"' => '"+sFile.replace(File.separatorChar, '/')+"'");
				urlImg = ClassLoader.getSystemResource(sFile.replace(File.separatorChar, '/'));
			}
			if (urlImg == null)
			{
				System.out.println("Can't find resource, trying as external file..");
				setImage(new File(sFile));
			}
			else
			{
				setImage(urlImg);
			}
		}
	}
	
	public void setImage(URL imageUrl)
	{
		try
		{
			setImage(ImageIO.read(imageUrl));
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Erreur chargement image '" + imageUrl + "'");
		}
	}

	public void setX(int x)
	{
		int old = _x;
		_x = x;
		firePropertyChange("x", old, x);
	}

	public void setY(int y)
	{
		int old = _y;
		_y = y;
		firePropertyChange("y", old, y);
	}

	public void setAutoSize(boolean autoSize)
	{
		boolean old = _autoSize;
		_autoSize = autoSize;
		firePropertyChange("autoSize", old, autoSize);
	}

	public void setKeepAspect(boolean keepAspect)
	{
		boolean old = _keepAspect;
		_keepAspect = keepAspect;
		firePropertyChange("keepAspect", old, keepAspect);
	}
	
	public void setRang(int rang)
	{
		int old = _rang;
		_rang = rang;
		firePropertyChange("rang", old, rang);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (_image != null)
		{
			if (!_autoSize)
			{
				g.drawImage(_image, _x, _y, _image.getWidth(null), _image
						.getHeight(null), null);
			}
			else
			{
				Graphics2D g2d = (Graphics2D) g.create();
				Double scaleWidth = new Double(getWidth())
						/ new Double(_image.getWidth(null));
				Double scaleHeight = new Double(getHeight())
						/ new Double(_image.getHeight(null));
				if (_keepAspect)
				{
					if (scaleWidth > scaleHeight)
					{
						scaleWidth = scaleHeight;
					}
					else
					{
						scaleHeight = scaleWidth;
					}
				}
				g2d.scale(scaleWidth, scaleHeight);
				g2d.drawImage(_image, _x, _y, null);
			}
		}
	}
}
