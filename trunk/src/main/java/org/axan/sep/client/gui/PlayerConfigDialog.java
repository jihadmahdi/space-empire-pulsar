package org.axan.sep.client.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.axan.eplib.utils.Basic;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.lib.FTPRemoteFileSystemView;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.javabuilders.BuildResult;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.event.CancelStatus;
import org.javabuilders.swing.SwingJavaBuilder;

public class PlayerConfigDialog extends JDialog implements IModalComponent
{
	//////////satic attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());

	////////// private attributes
	private final BuildResult build;
	private boolean canceled;	
	private File imageToUpload=null;
	private File choosenImage=null;
	private URL refreshFallbackURL=null;
	private String refreshImageFilename=null;
	private JImagePanel refreshImagePanel=null;

	////////// bean fields		
	private SEPClient sepClient;
	private String portraitFilename="";
	private String symbolFilename="";
	private Color playerColor=null;

	////////// ui controls
	JImagePanel portraitImage;
	JImagePanel symbolImage;
	JPanel colorPanel;
	JLabel lblPlayerName;

	////////// no arguments constructor
	public PlayerConfigDialog()
	{
		super(SpaceEmpirePulsarGUI.getInstance(), true);
		
		SwingJavaBuilderMyUtils.addType(JImagePanel.class);
		build = SwingJavaBuilder.build(this);				
	}

	//////////IModalComponent implementation

	@Override
	public boolean validateForm()
	{
		return build.validate();
	}

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
		setEnabled(sepClient != null);
		firePropertyChange("sepClient", old, sepClient);
	}
	
	public String getPortraitFilename()
	{
		return portraitFilename;
	}
	
	public void setPortraitFilename(String portraitFilename)
	{
		String old = this.portraitFilename;
		this.portraitFilename = portraitFilename;
		firePropertyChange("portraitFilename", old, portraitFilename);
	}
	
	public String getSymbolFilename()
	{
		return symbolFilename;
	}
	
	public void setSymbolFilename(String symbolFilename)
	{
		String old = this.symbolFilename;
		this.symbolFilename = symbolFilename;
		firePropertyChange("symbolFilename", old, symbolFilename);
	}
	
	public Color getPlayerColor()
	{
		return playerColor;
	}
	
	public void setPlayerColor(Color playerColor)
	{
		Color old = this.playerColor;
		this.playerColor = playerColor;
		firePropertyChange("playerColor", old, playerColor);
	}

	////////// ui events
	
	@Override
	public void setVisible(boolean b)
	{
		if (!isVisible() && b)
		{
			try
			{
				IPlayerConfig config = getSepClient().getGameboard().getPlayerConfig(getSepClient().getLogin());
				setPortraitFilename(config.getPortrait());
				setSymbolFilename(config.getSymbol());
				setPlayerColor(Basic.stringToColor(config.getColor()));
			}
			catch(Exception e)
			{
				log.log(Level.WARNING, "Cannot retreive player config", e);
			}
			
			refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackPortrait;
			refreshImagePanel = portraitImage;
			refreshImageFilename = getPortraitFilename();
			doRefresh(null);
			
			refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackSymbol;
			refreshImagePanel = symbolImage;
			refreshImageFilename = getSymbolFilename();
			doRefresh(null);
			
			lblPlayerName.setFont(lblPlayerName.getFont().deriveFont(58));
			refreshColor();
		}
		
		super.setVisible(b);
	}
	
	public void ok()
	{
		IPlayerConfig config = SEPCommonDB.makePlayerConfig(Basic.colorToString(getPlayerColor()), getSymbolFilename(), getPortraitFilename());
		try
		{
			getSepClient().getGameCreationInterface().updatePlayerConfig(config);
		}
		catch(Exception e)
		{
			log.log(Level.WARNING, "Cannot update player config", e);
			JOptionPane.showMessageDialog(this, "Error trying to update player config", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		canceled = false;
		setVisible(false);
	}
	
	public void cancel()
	{
		canceled = true;
		setVisible(false);
	}
	
	public void chooseColor()
	{
		Color newColor = JColorChooser.showDialog(this, build.getResource("color.chooser.title"), getPlayerColor());
		if (newColor != null)
		{
			setPlayerColor(newColor);
			refreshColor();
		}
	}		
		
	public synchronized void uploadPortrait()
	{
		refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackPortrait;
		refreshImagePanel = portraitImage;		
		if (uploadImage(build.getResource("chooser.btn.upload")))
		{
			setPortraitFilename(imageToUpload.getName());
		}
		imageToUpload = null;		
	}
	
	public synchronized void uploadSymbol()
	{
		refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackSymbol;
		refreshImagePanel = symbolImage;
		if (uploadImage(build.getResource("chooser.btn.upload")))
		{
			setSymbolFilename(imageToUpload.getName());
		}
		imageToUpload = null;		
	}
	
	public synchronized void choosePortrait()
	{
		refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackPortrait;
		refreshImagePanel = portraitImage;
		if (chooseImage(build.getResource("chooser.btn.use")))
		{
			setPortraitFilename(choosenImage.getName());
		}
		choosenImage = null;
	}
	
	public synchronized void chooseSymbol()
	{
		refreshFallbackURL = SpaceEmpirePulsarGUI.fallbackSymbol;
		refreshImagePanel = symbolImage;
		if (chooseImage(build.getResource("chooser.btn.use")))
		{
			setSymbolFilename(choosenImage.getName());
		}
		choosenImage = null;
	}
	
	////////// private methods
	
	@DoInBackground(blocking=true, cancelable=false, indeterminateProgress=true, progressMessage="Images are loading..")
	public void doRefresh(BackgroundEvent evt)
	{
		if (refreshFallbackURL == null || refreshImagePanel == null)
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			return;
		}
		
		URL imageUrl = refreshFallbackURL;
		if (getSepClient() != null && refreshImageFilename != null)
		{
			URL home = getSepClient().getHomeDirectoryURL();
			if (home != null) try
			{
				imageUrl = new URL(String.format("%s/%s;type=i", home.toExternalForm(), refreshImageFilename));
			}
			catch(Exception e)
			{
				imageUrl = refreshFallbackURL;
			}
		}
		
		try
		{
			refreshImagePanel.setImage(imageUrl);
		}
		catch(Exception e)
		{
			refreshImagePanel.setImage(refreshFallbackURL);
		}
		
		refreshImageFilename=null;
		refreshFallbackURL = null;
		refreshImagePanel = null;
	}
	
	private boolean uploadImage(String btnText)
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(GUIUtils.getFilter("Images", "jpeg", "jpg", "gif", "tiff", "tif", "png"));
		fc.setAccessory(new GUIUtils.ImagePreviewAccessory(fc, null));
		
		if (fc.showDialog(this, btnText) == JFileChooser.APPROVE_OPTION)
		{
			imageToUpload = fc.getSelectedFile();
			SwingJavaBuilderMyUtils.callBackgroundMethod(build, "doUpload", this);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean chooseImage(String btnText)
	{		
		JFileChooser fc = new JFileChooser(new FTPRemoteFileSystemView(getSepClient().getFTPClient(), getSepClient().getHomeDirectoryURL()));
		fc.setFileFilter(GUIUtils.getFilter("Images", "jpeg", "jpg", "gif", "tiff", "tif", "png"));
		fc.setAccessory(new GUIUtils.ImagePreviewAccessory(fc, getSepClient().getHomeDirectoryURL()));
		
		if (fc.showDialog(this, btnText) == JFileChooser.APPROVE_OPTION)
		{
			choosenImage = fc.getSelectedFile();
			refreshImageFilename = choosenImage.getName();
			SwingJavaBuilderMyUtils.callBackgroundMethod(build, "doRefresh", this);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@DoInBackground(blocking=true, cancelable=false, indeterminateProgress=true, progressMessage="Image is uploading..")
	public void doUpload(BackgroundEvent evt)
	{
		if (imageToUpload == null)
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
			return;
		}
	
		try
		{
			getSepClient().uploadFile(imageToUpload.getPath(), imageToUpload.getName());
		}
		catch(IOException e)
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
		}
		
		refreshImageFilename = imageToUpload.getName();
		doRefresh(evt);		
	}
	
	private void refreshColor()
	{
		if (getSepClient() == null) return;
		
		lblPlayerName.setText(getSepClient().getLogin());		
		Color color;
		if (getPlayerColor() != null)
		{
			color = getPlayerColor();
		}
		else
		{
			try
			{
				color = Basic.stringToColor(getSepClient().getGameboard().getPlayerConfig(getSepClient().getLogin()).getColor());		
			}
			catch(Exception e)
			{
				log.log(Level.WARNING, "Cannot retreive player color", e);
				color = Color.gray;
			}
		}
		
		colorPanel.setBackground(color);
	}
}
