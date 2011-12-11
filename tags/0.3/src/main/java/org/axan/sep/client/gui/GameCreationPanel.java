package org.axan.sep.client.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.SEPUtils;
import org.javabuilders.BuildResult;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.event.CancelStatus;
import org.javabuilders.swing.SwingJavaBuilder;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.EventComboBoxModel;

public class GameCreationPanel extends JPanel implements IModalComponent
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	private EventList<String> savedGames = GlazedLists.threadSafeList(new BasicEventList<String>());
	
	////////// ui controls
	private EventComboBoxModel<String> cbxSavedGamesModel = new EventComboBoxModel<String>(savedGames);
	private JComboBox cbxSavedGames;
	private GameConfigPanel configPanel;
	private GameChatPanel chatPanel;
	private PlayersListPanel playersListPanel;
	private JPanel btnsPanel;
	
	////////// bean fields
	private SEPClient sepClient;		

	////////// no arguments constructor
	public GameCreationPanel()
	{
		SwingJavaBuilderMyUtils.addType(GameConfigPanel.class, GameChatPanel.class, PlayersListPanel.class, BasicEventList.class, EventComboBoxModel.class);
		build = SwingJavaBuilder.build(this);
		
		// Init saved games list
		File workingDir = new File(SEPUtils.SAVE_SUBDIR);
		
		for(String fileName : workingDir.list(new FilenameFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return name.matches("^.+\\.sav$");
			}
		}))
		{
			savedGames.add(SEPUtils.getSaveID(fileName));
		}
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		if (visible)
		{
			btnsPanel.setEnabled(getSepClient().isAdmin());
			btnsPanel.setVisible(getSepClient().isAdmin());			
		}
		
		super.setVisible(visible);
	}
	
	////////// IModal implementation
	
	@Override
	public boolean validateForm()
	{
		if (!configPanel.validateForm()) return false;
		if (!chatPanel.validateForm()) return false;
		if (!playersListPanel.validateForm()) return false;
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
	
	@DoInBackground(blocking=true, cancelable=true, indeterminateProgress=true, progressMessage="Game is starting..")
	public void start(BackgroundEvent evt)
	{
		try
		{
			getSepClient().runGame();
		}
		catch(Exception e)
		{
			evt.setCancelStatus(CancelStatus.PROCESSING);
			JOptionPane.showMessageDialog(this, build.getResource("error.starting.game.msg"), build.getResource("error.starting.game.title"), JOptionPane.ERROR_MESSAGE);
			evt.setCancelStatus(CancelStatus.COMPLETED);
			return;
		}
		
		while(evt.getCancelStatus() != CancelStatus.REQUESTED && isVisible())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException ie)
			{
				evt.setCancelStatus(CancelStatus.REQUESTED);
				break;
			}
		}
		
		if (evt.getCancelStatus() == CancelStatus.REQUESTED)
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
		}		
	}
	
	@DoInBackground(blocking=true, cancelable=false, indeterminateProgress=true, progressMessage="Game is starting..")
	public void load(BackgroundEvent evt)
	{
		try
		{
			getSepClient().loadGame(cbxSavedGames.getSelectedItem().toString());
		}
		catch(Exception e)
		{
			evt.setCancelStatus(CancelStatus.PROCESSING);
			JOptionPane.showMessageDialog(this, build.getResource("error.starting.game.msg"), build.getResource("error.starting.game.title"), JOptionPane.ERROR_MESSAGE);
			evt.setCancelStatus(CancelStatus.COMPLETED);
			return;
		}
		
		while(evt.getCancelStatus() != CancelStatus.REQUESTED && isVisible())
		{
			try
			{
				Thread.sleep(200);
			}
			catch(InterruptedException ie)
			{
				evt.setCancelStatus(CancelStatus.REQUESTED);
				break;
			}
		}
		
		if (evt.getCancelStatus() == CancelStatus.REQUESTED)
		{
			evt.setCancelStatus(CancelStatus.COMPLETED);
		}
	}
}
