package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.IUniverseRenderer.IAreaSelectionListener;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Rules;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.Commands.UpdateDiplomacy;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.IDiplomacyMarker;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.Commands.LaunchProbe;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.axan.sep.common.db.orm.SEPCommonDB.IPlayerChangeListener;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class DiplomacyActionPanel extends JPanel implements IModalComponent, IPlayerChangeListener
{
	//////////static attributes
	private final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls	
	private JPanel dynamicPanel;
	private JLabel label;
	
	private static final class DiplomacyUpdater implements ActionListener
	{
		private final String targetName;
		private final DiplomacyActionPanel panel;
		
		DiplomacyUpdater(String targetName, DiplomacyActionPanel panel)
		{
			this.targetName = targetName;
			this.panel = panel;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SEPClient client = panel.getSepClient();
			if (client == null) return;
			PlayerGameBoard gb = client.getGameboard();
			if (gb == null) return;
			SEPCommonDB sepDB = gb.getDB();
			if (sepDB == null) return;
			
			boolean isAllowedToLand = panel.getIsAllowedToLandCheckBox(targetName).isSelected();
			JComboBox cb = panel.getForeignPolicyComboBox(targetName);
			eForeignPolicy foreignPolicy = cb == null ? null : cb.getSelectedItem() == null ? null : panel.getForeignPolicy((String) cb.getSelectedItem());
			if (foreignPolicy == null)
			{
				JOptionPane.showMessageDialog(null, "You must select a foreign policy.", "Cannot update diplomacy", JOptionPane.ERROR_MESSAGE);
				return;
			}			
			
			UpdateDiplomacy updateDiplomacy = new UpdateDiplomacy(client.getLogin(), targetName, isAllowedToLand, foreignPolicy);
			try
			{
				gb.onLocalCommand(updateDiplomacy);
			}
			catch(GameBoardException ge)
			{
				Throwable t = ge;
				while(t.getCause() != null && t.getCause() != t) t = t.getCause();
				JOptionPane.showMessageDialog(null, t.getMessage(), "Cannot update diplomacy", JOptionPane.ERROR_MESSAGE);
			}			
		}
	}
	
	////////// bean fields	
	private SEPClient sepClient;
	private RunningGamePanel runningGamePanel;	
	
	////////// no arguments constructor	
	public DiplomacyActionPanel()
	{
		//SwingJavaBuilderMyUtils.addType(JEditorPane.class);
		build = SwingJavaBuilder.build(this);
				
		addPropertyChangeListener("sepClient", new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				SEPClient client = (SEPClient) evt.getOldValue();
				SEPCommonDB oldSepDB = client == null ? null : client.getGameboard() == null ? null : client.getGameboard().getDB();
				client = (SEPClient) evt.getNewValue();
				SEPCommonDB newSepDB = client == null ? null : client.getGameboard() == null ? null : client.getGameboard().getDB();
				
				if (oldSepDB != null) oldSepDB.removePlayerChangeListener(DiplomacyActionPanel.this);
				if (newSepDB != null)
				{
					newSepDB.addPlayerChangeListener(DiplomacyActionPanel.this);
					refreshUI();
				}				
			}
		});
		
		addPropertyChangeListener("runningGamePanel", new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				SEPClient client = getSepClient();
				SEPCommonDB sepDB = client == null ? null : client.getGameboard() == null ? null : client.getGameboard().getDB();
				
				if (sepDB != null)
				{
					sepDB.addPlayerChangeListener(DiplomacyActionPanel.this);
					refreshUI();
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
	
	//////////IPlayerChangeListener implementation
	
	public void onPlayerChanged(String playerName)
	{
		refresh();
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
	
	public RunningGamePanel getRunningGamePanel()
	{
		return runningGamePanel;
	}
	
	public void setRunningGamePanel(RunningGamePanel runningGamePanel)
	{
		Object old = this.runningGamePanel;
		this.runningGamePanel = runningGamePanel;
		firePropertyChange("runningGamePanel", old, runningGamePanel);
	}
	
	////////// ui events
	
	private void refreshUI()
	{
		SEPClient client = getSepClient();
		if (client == null) return;		
		PlayerGameBoard gb = client.getGameboard();
		if (gb == null) return;
		SEPCommonDB sepDB = gb.getDB();
		IGameConfig config = gb.getConfig();
		IPlayer player = sepDB.getPlayer(client.getLogin());
		
		dynamicPanel.removeAll();
		dynamicPanel.setLayout(new MigLayout());
		int rowCpt = 0;
		
		Set<String> playerNames = sepDB.getPlayersNames();
		playerNames.remove(player.getName());
		Iterator<String> names = playerNames.iterator();
		
		// Header row
		label = new JLabel(build.getResource("diplomacy.action.label"));
		dynamicPanel.add(label);
		dynamicPanel.add(getPlayerNameLabel(player.getName()), names.hasNext() ? "" : "wrap");
						
		while(names.hasNext())
		{
			String playerName = names.next();
			dynamicPanel.add(getPlayerNameLabel(playerName), names.hasNext() ? "" : "wrap");			
		}
		++rowCpt;
		
		// Current player diplomacy row
		names = playerNames.iterator();
		dynamicPanel.add(getPlayerNameLabel(player.getName()));
		dynamicPanel.add(new JLabel(build.getResource("diplomacy.action.label.na")), names.hasNext() ? "" : "wrap");
				
		while(names.hasNext())
		{
			String playerName = names.next();
			JPanel diplomacyPanel = new JPanel(new BorderLayout());
			diplomacyPanel.add(getIsAllowedToLandCheckBox(playerName), BorderLayout.NORTH);
			diplomacyPanel.add(getForeignPolicyComboBox(playerName), BorderLayout.SOUTH);
			
			dynamicPanel.add(diplomacyPanel, names.hasNext() ? "" : "wrap");			
		}
		++rowCpt;
		
		names = playerNames.iterator();
		while(names.hasNext())
		{
			String ownerName = names.next();
			Iterator<String> target = playerNames.iterator();
			
			dynamicPanel.add(getPlayerNameLabel(ownerName));			
			dynamicPanel.add(getDiplomacyTextPane(ownerName, player.getName()), target.hasNext() ? "" : "wrap");			
			
			while(target.hasNext())
			{
				String targetName = target.next();
				
				if (ownerName.endsWith(targetName))
				{
					dynamicPanel.add(new JLabel(build.getResource("diplomacy.action.label.na")), target.hasNext() ? "" : "wrap");
					continue;
				}
				
				dynamicPanel.add(getDiplomacyTextPane(ownerName, targetName), target.hasNext() ? "" : "wrap");
			}
			
			++rowCpt;
		}
		
		refresh();
	}	
	
	void refresh()
	{
		if (dynamicPanel.getComponentCount() == 0)
		{
			refreshUI();
			return;
		}
		
		SEPClient client = getSepClient();
		if (client == null) return;		
		PlayerGameBoard gb = client.getGameboard();
		if (gb == null) return;
		SEPCommonDB sepDB = gb.getDB();
		IGameConfig config = gb.getConfig();
		IPlayer player = sepDB.getPlayer(client.getLogin());
		
		Set<String> playerNames = sepDB.getPlayersNames();
		playerNames.remove(player.getName());
		Iterator<String> names = playerNames.iterator();
		while(names.hasNext())
		{			
			String targetName = names.next();
			IDiplomacy diplomacy = player.getDiplomacy(targetName);			
			getIsAllowedToLandCheckBox(targetName).setSelected(diplomacy.isAllowedToLand());
			getForeignPolicyComboBox(targetName).setSelectedItem(getForeignPolicy(diplomacy.getForeignPolicy()));					
		}
		
		names = playerNames.iterator();
		while(names.hasNext())
		{
			String ownerName = names.next();
			IPlayer owner = sepDB.getPlayer(ownerName);
			IDiplomacyMarker diplomacyMarker = owner.getDiplomacyMarker(player.getName());
			
			getDiplomacyTextPane(ownerName, player.getName()).setText(diplomacyMarker == null ? "unknown" : diplomacyMarker.toString());
			
			Iterator<String> target = playerNames.iterator();
			while(target.hasNext())
			{
				String targetName = target.next();
				
				diplomacyMarker = owner.getDiplomacyMarker(targetName);
				getDiplomacyTextPane(ownerName, targetName).setText(diplomacyMarker == null ? "unknown" : diplomacyMarker.toString());
			}
			
		}
		
		updateUI();
	}
	
	//////////ui dynamic controls
	
	private final Map<String, JLabel> playerNamesLabel = new HashMap<String, JLabel>();	
	private JLabel getPlayerNameLabel(String playerName)
	{
		if (!playerNamesLabel.containsKey(playerName))
		{			
			IPlayerConfig playerCfg = getSepClient().getGameboard().getDB().getPlayer(playerName).getConfig();
			JLabel label = new JLabel(playerName);
			label.setForeground(playerCfg.getColor());
			return label;
			//playerNamesLabel.put(playerName, label); // Force new label creation
		}
		
		return playerNamesLabel.get(playerName);
	}
	
	private final Map<String, JCheckBox> isAllowedToLandCheckBoxes = new HashMap<String, JCheckBox>();
	private JCheckBox getIsAllowedToLandCheckBox(String targetName)
	{
		if (!isAllowedToLandCheckBoxes.containsKey(targetName))
		{
			JCheckBox cb = new JCheckBox(build.getResource("diplomacy.action.isAllowedToLand"));
			cb.addActionListener(new DiplomacyUpdater(targetName, this));			
			isAllowedToLandCheckBoxes.put(targetName, cb);
		}
		
		return  isAllowedToLandCheckBoxes.get(targetName);
	}
		
	private Map<String, eForeignPolicy> foreignPolicies;
	private Map<eForeignPolicy, String> foreignPolicyTexts;
	private eForeignPolicy getForeignPolicy(String text)
	{
		if (foreignPolicies == null)
		{
			foreignPolicies = new HashMap<String, eForeignPolicy>();
			foreignPolicyTexts = new HashMap<eForeignPolicy, String>();
			for(eForeignPolicy fp: eForeignPolicy.values())
			{
				String txt = build.getResource("diplomacy.foreignPolicy."+fp.toString());
				foreignPolicies.put(txt, fp);
				foreignPolicyTexts.put(fp, txt);
			}
		}
		
		return text == null ? null : foreignPolicies.get(text);
	}
	
	private String getForeignPolicy(eForeignPolicy fp)
	{
		getForeignPolicy((String) null);
		return foreignPolicyTexts.get(fp);
	}
	
	private Set<String> getForeignPolicies()
	{
		getForeignPolicy((String) null);
		return foreignPolicies.keySet();
	}
	
	private final Map<String, JComboBox> foreignPolicyComboBoxes = new HashMap<String, JComboBox>();
	private JComboBox getForeignPolicyComboBox(String targetName)
	{
		if (!foreignPolicyComboBoxes.containsKey(targetName))
		{
			JComboBox cb = new JComboBox(new Vector(getForeignPolicies()));
			cb.addActionListener(new DiplomacyUpdater(targetName, this));
			foreignPolicyComboBoxes.put(targetName, cb);
		}
		
		return foreignPolicyComboBoxes.get(targetName);
	}
	
	private final Map<String, JTextPane> diplomacyTextPanes = new HashMap<String, JTextPane>();
	private JTextPane getDiplomacyTextPane(String ownerName, String targetName)
	{
		String k = String.format("%sV%s", ownerName, targetName);
		if (!diplomacyTextPanes.containsKey(k))
		{
			JTextPane tp = new JTextPane();
			diplomacyTextPanes.put(k, tp);
		}
		
		return diplomacyTextPanes.get(k);
	}
}
