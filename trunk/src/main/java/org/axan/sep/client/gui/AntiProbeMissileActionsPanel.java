package org.axan.sep.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.IUniverseRenderer.IAreaSelectionListener;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProbeMarker;
import org.axan.sep.common.db.Commands.FireAntiProbeMissile;
import org.axan.sep.common.db.Commands.LaunchProbe;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class AntiProbeMissileActionsPanel extends JPanel implements IModalComponent, IAreaSelectionListener
{
	//////////static attributes
	private final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls	
	private JTextField txtTargetOwnerName;
	private JTextField txtTargetName;
	private JButton btnFire;
	private JButton btnSelectTarget;
	
	////////// bean fields	
	private SEPClient sepClient;
	private RunningGamePanel runningGamePanel;
	private IAntiProbeMissile antiProbeMissile;
	
	
	////////// no arguments constructor	
	public AntiProbeMissileActionsPanel()
	{
		//SwingJavaBuilderMyUtils.addType(JEditorPane.class);
		build = SwingJavaBuilder.build(this);
				
		DocumentListener dl = new DocumentListener()
		{
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				refresh();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				refresh();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				refresh();
			}
		};
		
		txtTargetOwnerName.getDocument().addDocumentListener(dl);
		txtTargetName.getDocument().addDocumentListener(dl);
		
		addPropertyChangeListener("antiProbeMissile", new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				refreshUI();
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
	
	public IAntiProbeMissile getAntiProbeMissile()
	{
		return antiProbeMissile;
	}
	
	public void setAntiProbeMissile(IAntiProbeMissile antiProbeMissile)
	{
		Object old = this.antiProbeMissile;
		this.antiProbeMissile = antiProbeMissile;
		firePropertyChange("antiProbeMissile", old, antiProbeMissile);
	}
	
	////////// ui events
	
	private void refreshUI()
	{
		SEPClient client = getSepClient();
		if (client == null) return;		
		PlayerGameBoard gb = client.getGameboard();
		if (gb == null) return;
		
		refresh();
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		txtTargetOwnerName.setEnabled(enabled);
		txtTargetName.setEnabled(enabled);
		btnFire.setEnabled(enabled);
		btnSelectTarget.setEnabled(enabled);
		super.setEnabled(enabled);
		updateUI();
		doLayout();
		repaint();
	}
	
	private void refresh()
	{
		IAntiProbeMissile antiProbeMissile = getAntiProbeMissile();
		
		if (antiProbeMissile == null)
		{
			setEnabled(false);
			return;
		}
		
		IProbeMarker target = antiProbeMissile.getTarget();
		if (target != null)
		{
			if (!txtTargetOwnerName.getText().equals(target.getOwnerName()))
			{
				txtTargetOwnerName.setText(target.getOwnerName());
			}
			
			if (!txtTargetName.getText().equals(target.getName()))
			{
				txtTargetName.setText(target.getName());
			}
			
			setEnabled(false);
			return;
		}
		
		setEnabled(true);
		
		boolean picking = getRunningGamePanel().getUniversePanel().isAreaSelectionListener(this);
		btnSelectTarget.setEnabled(!picking);
		btnSelectTarget.setText(build.getResource("antiProbeMissile.action.btn.selectTarget"+(picking ? ".activated":"")));
		
		SEPCommonDB sepDB = getSepClient().getGameboard().getDB();
		
		final FireAntiProbeMissile fireAntiProbeMissile = new FireAntiProbeMissile(getSepClient().getLogin(), antiProbeMissile.getName(), txtTargetOwnerName.getText(), txtTargetName.getText());
		try
		{
			fireAntiProbeMissile.check(getSepClient().getGameboard().getDB());
			btnFire.setToolTipText("Fire to ["+fireAntiProbeMissile.getTarget().getOwnerName()+"] "+fireAntiProbeMissile.getTarget().getName());
			for(ActionListener l : btnFire.getActionListeners()) btnFire.removeActionListener(l);
			btnFire.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(fireAntiProbeMissile);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Launch probe command", ex);
						return;
					}
					
					getRunningGamePanel().getUniversePanel().unsetAreaSelectionListener(AntiProbeMissileActionsPanel.this);
					//getRunningGamePanel().refresh(true);
					refreshUI();					
				}
			});
			btnFire.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnFire.setEnabled(false);
			btnFire.setToolTipText("Cannot fire anti probe missile: "+e.getMessage());
		}
		
		updateUI();
	}
	
	private void selectTarget()
	{
		getRunningGamePanel().getUniversePanel().setAreaSelectionListener(this);
		refresh();
	}
	
	@Override
	public void updateSelectedArea(int x, int y, int z)
	{
		getRunningGamePanel().getUniversePanel().unsetAreaSelectionListener(this);
				
		Location location = new Location(x, y, z);
		SEPCommonDB sepDB = getSepClient().getGameboard().getDB();
			
		Set<String> possibleTargets = new HashSet<String>();
		
		Set<IProbeMarker> probeMarkers = sepDB.getArea(location).getUnitsMarkers(eUnitType.Probe);
		for(IProbeMarker probeMarker : probeMarkers)
		{
			possibleTargets.add(String.format("[%s] %s", probeMarker.getOwnerName(), probeMarker.getName()));
		}
		
		String target;
		if (possibleTargets.size() <= 1)
		{
			target = possibleTargets.isEmpty() ? null : possibleTargets.iterator().next();
		}
		else
		{
			target = (String) JOptionPane.showInputDialog(null, "Selectable target on "+location.toString(), "Target selection", JOptionPane.QUESTION_MESSAGE, null, possibleTargets.toArray(new String[possibleTargets.size()]), null);
		}
		
		if (target == null) return;
		
		Pattern p = Pattern.compile("\\[(.+)\\] (.+)", Pattern.DOTALL);
		Matcher m = p.matcher(target);
		
		if (m.find())
		{			
			txtTargetOwnerName.setText(m.group(1));
			txtTargetName.setText(m.group(2));		
		}
	}
	
	//////////ui dynamic controls
	
	
}