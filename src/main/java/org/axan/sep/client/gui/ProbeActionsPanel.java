package org.axan.sep.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.IUniverseRenderer.IAreaSelectionListener;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Rules;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.Commands.LaunchProbe;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.Commands.AssignStarships;
import org.axan.sep.common.db.Commands.MakeAntiProbeMissile;
import org.axan.sep.common.db.Commands.MakeProbes;
import org.axan.sep.common.db.Commands.MakeStarships;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class ProbeActionsPanel extends JPanel implements IModalComponent, IAreaSelectionListener
{
	//////////static attributes
	private final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls	
	private JSpinner spnDestinationX;
	private JSpinner spnDestinationY;
	private JSpinner spnDestinationZ;
	private JButton btnLaunch;
	private JButton btnSelectDestination;
	
	////////// bean fields	
	private SEPClient sepClient;
	private RunningGamePanel runningGamePanel;
	private IProbe probe;
	
	
	////////// no arguments constructor	
	public ProbeActionsPanel()
	{
		//SwingJavaBuilderMyUtils.addType(JEditorPane.class);
		build = SwingJavaBuilder.build(this);
		
		spnDestinationX.setModel(new SpinnerNumberModel());
		spnDestinationY.setModel(new SpinnerNumberModel());
		spnDestinationZ.setModel(new SpinnerNumberModel());
		
		ChangeListener cl = new ChangeListener()
		{
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				refresh();
			}
		};
		
		spnDestinationX.getModel().addChangeListener(cl);
		spnDestinationY.getModel().addChangeListener(cl);
		spnDestinationZ.getModel().addChangeListener(cl);
		
		addPropertyChangeListener("probe", new PropertyChangeListener()
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
	
	public IProbe getProbe()
	{
		return probe;
	}
	
	public void setProbe(IProbe probe)
	{
		Object old = this.probe;
		this.probe = probe;
		firePropertyChange("probe", old, probe);
	}
	
	////////// ui events
	
	private void refreshUI()
	{
		SEPClient client = getSepClient();
		if (client == null) return;		
		PlayerGameBoard gb = client.getGameboard();
		if (gb == null) return;
		IGameConfig config = gb.getConfig();
		
		((SpinnerNumberModel) spnDestinationX.getModel()).setMaximum(config.getDimX());
		((SpinnerNumberModel) spnDestinationY.getModel()).setMaximum(config.getDimY());
		((SpinnerNumberModel) spnDestinationZ.getModel()).setMaximum(config.getDimZ());
		
		refresh();
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		spnDestinationX.setEnabled(enabled);
		spnDestinationY.setEnabled(enabled);
		spnDestinationZ.setEnabled(enabled);
		btnLaunch.setEnabled(enabled);
		btnSelectDestination.setEnabled(enabled);
		super.setEnabled(enabled);
		updateUI();
		doLayout();
		repaint();
	}
	
	private void refresh()
	{
		IProbe probe = getProbe();
		
		if (probe == null)
		{
			setEnabled(false);
			return;
		}
		
		Location destination = probe.getDestination();
		if (destination != null)
		{
			// Note: infinite loop ? (setValue -> refresh -> setValue -> ...)
			if (!spnDestinationX.getValue().equals(destination.x))
			{
				spnDestinationX.setValue(destination.x);
			}
			
			if (!spnDestinationY.getValue().equals(destination.y))
			{
				spnDestinationY.setValue(destination.y);
			}
			
			if (!spnDestinationZ.getValue().equals(destination.z))
			{
				spnDestinationZ.setValue(destination.z);
			}
			
			setEnabled(false);
			return;
		}
		
		setEnabled(true);
		
		boolean picking = getRunningGamePanel().getUniversePanel().isAreaSelectionListener(this);
		btnSelectDestination.setEnabled(!picking);
		btnSelectDestination.setText(build.getResource("probe.action.btn.selectDestination"+(picking ? ".activated":"")));
		
		destination = new Location((Integer) spnDestinationX.getValue(), (Integer) spnDestinationY.getValue(), (Integer) spnDestinationZ.getValue());
		
		final LaunchProbe launchProbe = new LaunchProbe(getSepClient().getLogin(), probe.getName(), destination);
		try
		{
			launchProbe.check(getSepClient().getGameboard().getDB());
			btnLaunch.setToolTipText("Launch to "+destination.toString());
			for(ActionListener l : btnLaunch.getActionListeners()) btnLaunch.removeActionListener(l);
			btnLaunch.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(launchProbe);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Launch probe command", ex);
						return;
					}
					
					getRunningGamePanel().getUniversePanel().unsetAreaSelectionListener(ProbeActionsPanel.this);
					//getRunningGamePanel().refresh(true);
					refreshUI();					
				}
			});
			btnLaunch.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnLaunch.setEnabled(false);
			btnLaunch.setToolTipText("Cannot launch probe: "+e.getMessage());
		}
		
		updateUI();
	}
	
	private void selectDestination()
	{
		getRunningGamePanel().getUniversePanel().setAreaSelectionListener(this);
		refresh();
	}
	
	@Override
	public void updateSelectedArea(int x, int y, int z)
	{
		getRunningGamePanel().getUniversePanel().unsetAreaSelectionListener(this);
		spnDestinationX.setValue(x);
		spnDestinationY.setValue(y);
		spnDestinationZ.setValue(z);		
	}
	
	//////////ui dynamic controls
	
	
}
