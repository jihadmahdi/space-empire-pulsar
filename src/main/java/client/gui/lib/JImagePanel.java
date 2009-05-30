/**
 * 
 */
package client.gui.lib;

import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.net.URL;

public class JImagePanel extends JPanel
{
	private static final long	serialVersionUID	= 2724980460740151616L;

	private Image				_image;  //  @jve:decl-index=0:

	private int					_x;

	private int					_y;

	private boolean				_autoSize;

	private boolean				_keepAspect;
	
	private int					_rang;
	
	public JImagePanel(File file, int x, int y, boolean autoSize,
			boolean keepAspect) throws IOException
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

	public JImagePanel(Image image, int x, int y, boolean autoSize,
			boolean keepAspect)
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

	public JImagePanel()
	{
		super();
		initialize();
		setX(0);
		setY(0);
		setImage((Image) null);
		setAutoSize(false);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize()
	{
	
	}

	public void setImage(Image image)
	{
		_image = image;
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
				try
				{
					setImage(ImageIO.read(urlImg));
				}
				catch(IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("Erreur chargement image '" + urlImg + "'");
				}
			}
		}
	}

	public void setX(int x)
	{
		_x = x;
	}

	public void setY(int y)
	{
		_y = y;
	}

	public void setAutoSize(boolean autoSize)
	{
		_autoSize = autoSize;
	}

	public void setKeepAspect(boolean keepAspect)
	{
		_keepAspect = keepAspect;
	}
	
	public void setRang(int rang)
	{
		_rang = rang;
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
