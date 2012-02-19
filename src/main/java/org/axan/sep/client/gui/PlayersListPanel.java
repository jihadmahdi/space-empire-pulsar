package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.lib.JImagePanel;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.EventListModel;

public class PlayersListPanel extends JPanel implements IModalComponent
{
	////////// satic attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean playersListUpdated = true;
	private boolean canceled;
	
	////////// bean fields		
	private SEPClient sepClient;
	private final EventList<String> players = GlazedLists.threadSafeList(new BasicEventList<String>());
	private boolean configurationEnabled = true;
	
	////////// ui controls
	private EventListModel<String> playersListModel = new EventListModel<String>(players);
	private JList playersList;
	private PlayerConfigDialog playerConfigDialog;
	
	////////// no arguments constructor
	public PlayersListPanel()
	{
		SwingJavaBuilderMyUtils.addType(EventListModel.class, BasicEventList.class);
		
		build = SwingJavaBuilder.build(this);
		
		/*
		playersList.setFixedCellWidth(200);
		playersList.setFixedCellHeight(100);
		*/
		
		playersList.setFixedCellHeight(50);
		playersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersList.setEnabled(true);
		playersList.setFocusable(true);
		playersList.setCellRenderer(new ListCellRenderer()
		{
			
			Map<String, JPanel> cachedPanels = new HashMap<String, JPanel>();
			Map<String, Boolean> refreshed = new HashMap<String, Boolean>();
			
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if (PlayersListPanel.this.getSepClient() == null || !PlayersListPanel.this.isEnabled())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						
						@Override
						public void run()
						{
							try
							{
								Thread.sleep(2000);
							}
							catch(InterruptedException ie) {}
							
							log.log(Level.INFO, "invalidate");
							playersList.invalidate();
							playersList.doLayout();
						}
					});
					
					return new JLabel("loading...");
				}												
				
				String playerName = (String) value;
				
				if (playersListUpdated)
				{
					refreshed.clear();
					playersListUpdated = false;
				}
				
				if (refreshed.containsKey(playerName) && refreshed.get(playerName) == true && cachedPanels.containsKey(playerName) && cachedPanels.get(playerName) != null)
				{
					return cachedPanels.get(playerName);
				}								
				
				IPlayerConfig config = null;
				try
				{
					config = getSepClient().getGameboard().getPlayerConfig(playerName);
				}
				catch(Throwable t)
				{
					log.log(Level.SEVERE, "Cannot retreive player '"+playerName+"' config", t);
				}

				if (playerName == null || config == null)
				{
					return new JLabel(playerName==null?"'Unknown player'":"'"+playerName+"'");
				}
				
				JPanel playerPanel = new JPanel(new BorderLayout(0, 0));
									
				URL portraitUrl;
				URL fallbackUrl = SpaceEmpirePulsarGUI.fallbackPortrait;
				
				if (config.getPortrait() == null)
				{
					portraitUrl = fallbackUrl;
				}
				else try
				{
					portraitUrl = new URL(String.format("%s/%s", getSepClient().getUserDirectoryURL(playerName).toExternalForm(), config.getPortrait()));					
				}
				catch(Exception e)
				{
					portraitUrl = fallbackUrl;
				}
				
				JImagePanel portrait;
				try
				{
					portrait = new JImagePanel(portraitUrl, 0, 0, true, true);					
				}
				catch(Exception e)
				{
					portrait = new JImagePanel(fallbackUrl, 0, 0, true, true);
				}
				portrait.setPreferredSize(new Dimension(50, 50));
				portrait.setMinimumSize(portrait.getPreferredSize());
				playerPanel.add(portrait, BorderLayout.WEST);				
				
				JLabel name = new JLabel(playerName, SwingConstants.CENTER);
				playerPanel.setBackground(config.getColor());
				if (cellHasFocus)
				{
					name.setFont(name.getFont().deriveFont(Font.BOLD));
				}
				
				name.setForeground(isSelected ? (new Color(0x00, 0xCC, 0xFF)) : Color.black);				
				
				playerPanel.add(name, BorderLayout.CENTER);
								
				URL symbolUrl;
				fallbackUrl = Reflect.getResource(SpaceEmpirePulsarGUI.class.getPackage().getName()+".img", "symbol_todo.png");
				
				if (config.getSymbol() == null)
				{
					symbolUrl = fallbackUrl;
				}
				else try
				{
					symbolUrl = new URL(String.format("%s/%s", getSepClient().getUserDirectoryURL(playerName).toExternalForm(), config.getSymbol()));					
				}
				catch(Exception e)
				{
					symbolUrl = fallbackUrl;
				}
				
				JImagePanel symbol;
				try
				{
					symbol = new JImagePanel(symbolUrl, 0, 0, true, true);
				}
				catch(Exception e)
				{
					symbol = new JImagePanel(fallbackUrl, 0, 0, true, true);
				}
				symbol.setPreferredSize(new Dimension(50, 50));
				symbol.setMinimumSize(new Dimension(50, 50));
				playerPanel.add(symbol, BorderLayout.EAST);								
				
				cachedPanels.put(playerName, playerPanel);
				refreshed.put(playerName, true);
				
				return playerPanel;
			}
		});
	}
	
	////////// IModalComponent implementation
	
	@Override
	public boolean validateForm()
	{
		//if (!playerConfigDialog.validateForm()) return false;
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
	
	public boolean isConfigurationEnabled()
	{
		return configurationEnabled;
	}
	
	public void setConfigurationEnabled(boolean configurationEnabled)
	{
		Object old = this.configurationEnabled;
		this.configurationEnabled = configurationEnabled;
		firePropertyChange("configurationEnabled", old, configurationEnabled);
	}
		
	////////// ui events
	
	public synchronized void refreshPlayers(Map<String, IPlayerConfig> players)
	{
		this.players.clear();
		this.players.addAll(players.keySet());
		this.playersListUpdated = true;
	}
		
	public void click()
	{
		// TODO: Player selection as chat target
		
		if (!isConfigurationEnabled()) return;
		
		IPlayer player = (IPlayer) playersList.getSelectedValue();
		
		if (player == null || !getSepClient().getLogin().equals(player.getName())) return;
				
		playerConfigDialog.pack();
		playerConfigDialog.setVisible(true);
		
		// No need to look for cancel status, PlayerConfigDialog internally manage ok/cancel events (calling SEPClient methods).
	}
}
