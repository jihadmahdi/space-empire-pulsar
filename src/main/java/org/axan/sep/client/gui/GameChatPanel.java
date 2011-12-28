package org.axan.sep.client.gui;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.lib.GUIUtils;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class GameChatPanel extends JPanel implements IModalComponent
{
	////////// static attributes
	private final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls	
	private JScrollPane scrollPane;
	private JTextField txtMessage;
	private JEditorPane editorPane;
	
	////////// bean fields	
	private SEPClient sepClient;
	
	////////// no arguments constructor	
	public GameChatPanel()
	{
		//SwingJavaBuilderMyUtils.addType(JEditorPane.class);
		build = SwingJavaBuilder.build(this);				
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					JScrollBar vBar = scrollPane.getVerticalScrollBar();
					int newVal = (vBar.getMinimum() + (vBar.getMaximum() - vBar.getMinimum()) * 1);

					if (vBar.getValue() >= (vBar.getMaximum() - vBar.getVisibleAmount() - 30))
					{
						vBar.setValue(newVal);
						updateUI();
					}
				}
			}
		});
	}
	
	////////// IModalComponent implementation
	
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
	
	////////// ui events
	
	private void sendChat()
	{
		if (txtMessage.getText().isEmpty()) return;
		String txt = txtMessage.getText();
		try
		{
			getSepClient().sendMessage(txt);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		txtMessage.setText("");
	}
	
	public void receivedMessages(IPlayer sender, String message)
	{
		IPlayerConfig config = null;
		try
		{
			config = getSepClient().getGameboard().getPlayerConfig(sender.getName());
		}
		catch(GameBoardException e)
		{
			log.log(Level.WARNING, "Cannot retrieve player's config", e);
		}

		if (sender == null || config == null)
		{
			throw new IllegalArgumentException(sender==null?"Sender is null":"Unkown config for player '"+sender.getName()+"'");
		}
		
		String htmlText = String.format("<br><font color='#%s'>%s</font> : %s</br>", GUIUtils.getHTMLColor(Basic.stringToColor(config.getColor())), sender.getName(), message);
		HTMLDocument doc = (HTMLDocument) editorPane.getDocument();

		try
		{
			doc.insertBeforeEnd(doc.getDefaultRootElement(), htmlText);
		}
		catch(BadLocationException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
