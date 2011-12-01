package org.axan.sep.client.gui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.SpaceEmpirePulsarGUI.HostGamePanel;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;
import org.javabuilders.swing.SwingJavaBuilderUtils;

public class GameCreationPanel extends JPanel implements IModalComponent
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls
	private GameConfigPanel configPanel;
	private GameChatPanel chatPanel;
	private PlayersListPanel playersListPanel;
	
	////////// bean fields
	private SEPClient sepClient;		

	////////// no arguments constructor
	public GameCreationPanel()
	{
		SwingJavaBuilderMyUtils.addType(GameConfigPanel.class, GameChatPanel.class, PlayersListPanel.class);
		build = SwingJavaBuilder.build(this);
	}
	
	////////// IModal implementation
	
	@Override
	public boolean validateForm()
	{
		if (configPanel.validateForm()) return false;
		if (chatPanel.validateForm()) return false;
		if (playersListPanel.validateForm()) return false;
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
	
	public GameConfigPanel getConfigPanel()
	{
		return configPanel;
	}
	
	public GameChatPanel getChatPanel()
	{
		return chatPanel;
	}
	
	public PlayersListPanel getPlayersListPanel()
	{
		return playersListPanel;
	}
	
	////////// ui events
	
	public void start()
	{
		// TODO: Start game
	}
}
