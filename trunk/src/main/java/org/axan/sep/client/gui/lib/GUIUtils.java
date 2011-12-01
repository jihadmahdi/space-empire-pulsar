/**
 * @author Escallier Pierre
 * @file GUIUtils.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui.lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * 
 */
public abstract class GUIUtils
{
	/**
	 * Return HTML color string from the given Color object.
	 * 
	 * @param c
	 * @return
	 */
	public static String getHTMLColor(Color c)
	{
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	/**
	 * Return file extension, or empty string if no '.' is found in the
	 * filename.
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(String file)
	{
		int i = file.lastIndexOf('.');

		if (i > 0 && i < file.length() - 1)
		{
			return file.substring(i + 1).toLowerCase();
		}

		return "";
	}

	/**
	 * Generate a FileFilter with the given description and valid list of
	 * extensions.
	 * 
	 * @param description
	 * @param extensions
	 *            e.g. {"jpg", "png"}
	 * @return
	 */
	public static FileFilter getFilter(final String description, final String ... extensions)
	{
		return new FileFilter()
		{

			@Override
			public String getDescription()
			{
				return description;
			}

			@Override
			public boolean accept(File f)
			{
				if (f.isDirectory())
					return true;

				String ext = getExtension(f.getName());

				for(String validExtension: extensions)
				{
					if (validExtension.equals(ext))
						return true;
				}

				return false;
			}
		};
	}

	/**
	 * Warning: Assume that baseURL + file.getName() is the correct final URL.
	 */
	public static class ImagePreviewAccessory extends JPanel implements PropertyChangeListener
	{
		private final URL baseURL;
		private ImageIcon thumbnail = null;
		private File file = null;

		public ImagePreviewAccessory(JFileChooser fc, URL baseURL)
		{
			this.baseURL = baseURL;
			setPreferredSize(new Dimension(100, 50));
			fc.addPropertyChangeListener(this);
		}

		public void loadImage()
		{
			if (file == null)
			{
				thumbnail = null;
				return;
			}

			//Don't use createImageIcon (which is a wrapper for getResource)
			//because the image we're trying to load is probably not one
			//of this program's own resources.
			ImageIcon tmpIcon=null;
			URL imgLocation=null;
			
			if (baseURL == null)
			{
				tmpIcon = new ImageIcon(file.getPath());
			}
			else
			{
				try
				{
					imgLocation = new URL(baseURL.toExternalForm()+"/"+file.getName());
				}
				catch(MalformedURLException e)
				{
					try
					{
						imgLocation = file.toURI().toURL();
					}
					catch(MalformedURLException e2)
					{
						tmpIcon = new ImageIcon(file.getPath());						
					}										
				}
				
				if (tmpIcon == null) tmpIcon = new ImageIcon(imgLocation);
			}						
			
			if (tmpIcon != null)
			{
				if (tmpIcon.getIconWidth() > 90)
				{
					thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(90, -1, Image.SCALE_DEFAULT));
				}
				else
				{ //no need to miniaturize
					thumbnail = tmpIcon;
				}
			}
		}

		public void propertyChange(PropertyChangeEvent e)
		{
			boolean update = false;
			String prop = e.getPropertyName();

			//If the directory changed, don't show an image.
			if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop))
			{
				file = null;
				update = true;

				//If a file became selected, find out which one.
			}
			else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop))
			{
				file = (File) e.getNewValue();
				update = true;
			}

			//Update the preview accordingly.
			if (update)
			{
				thumbnail = null;
				if (isShowing())
				{
					repaint();
					loadImage();
					repaint();
				}
			}
		}

		protected void paintComponent(Graphics g)
		{
			if (thumbnail == null)
			{
				loadImage();
			}
			if (thumbnail != null)
			{
				g.clearRect(0, 0, getWidth(), getHeight());
				
				int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
				int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

				if (y < 0)
				{
					y = 0;
				}

				if (x < 5)
				{
					x = 5;
				}
				thumbnail.paintIcon(this, g, x, y);
			}
		}
	}

}
