package org.axan.sep.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.db.Commands.MakeProbes;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.Commands.AssignStarships;
import org.axan.sep.common.db.Commands.MakeAntiProbeMissile;
import org.axan.sep.common.db.Commands.MakeStarships;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class StarshipPlantActionPanel extends JPanel implements IModalComponent
{
	//////////static attributes
	private final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());

	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;

	////////// ui controls	
	private JLabel label;
	private JPanel starshipsPanel;
	private JPanel probingPanel;
	private final JButton btnMake;
	private final JTextField txtFleetName;
	private final JButton btnJoinFleet;
	private JTextField txtProbesSerieName;
	private JSpinner spnProbesQuantity;
	private JButton btnMakeProbes;
	private JTextField txtAPMSerieName;
	private JSpinner spnAPMQuantity;
	private JButton btnMakeAPM;
	
	////////// bean fields	
	private SEPClient sepClient;
	private RunningGamePanel runningGamePanel;
	private IStarshipPlant starshipPlant;

	////////// no arguments constructor	
	public StarshipPlantActionPanel()
	{
		//SwingJavaBuilderMyUtils.addType(JEditorPane.class);
		build = SwingJavaBuilder.build(this);
		
		btnMake = new JButton(build.getResource("starshipplant.action.btn.make"));
		// TODO: Change text field to list/edit field so we can choose between already existing fleet and creating a new one. 
		txtFleetName = new JTextField();
		txtFleetName.getDocument().addDocumentListener(new DocumentListener()
		{			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				refreshValues();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				refreshValues();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				refreshValues();
			}
		});				
		
		btnJoinFleet = new JButton(build.getResource("starshipplant.action.btn.join"));
		
		addPropertyChangeListener("starshipPlant", new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				refreshUI();
			}
		});
		
		spnProbesQuantity.setModel(new SpinnerNumberModel(0, 0, 20, 1));
		spnProbesQuantity.getModel().addChangeListener(new ChangeListener()
		{
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				updateProbeForm();
			}
		});
		
		txtProbesSerieName.getDocument().addDocumentListener(new DocumentListener()
		{
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateProbeForm();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateProbeForm();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateProbeForm();
			}
		});
		
		spnAPMQuantity.setModel(new SpinnerNumberModel(0, 0, 20, 1));
		spnAPMQuantity.getModel().addChangeListener(new ChangeListener()
		{
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				updateAPMForm();
			}
		});
		
		txtAPMSerieName.getDocument().addDocumentListener(new DocumentListener()
		{
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateAPMForm();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateAPMForm();				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateAPMForm();
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
	
	public IStarshipPlant getStarshipPlant()
	{
		return starshipPlant;
	}
	
	public void setStarshipPlant(IStarshipPlant starshipPlant)
	{
		Object old = this.starshipPlant;
		this.starshipPlant = starshipPlant;
		firePropertyChange("starshipPlant", old, starshipPlant);
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
	
	/** Rebuild panel. */
	private void refreshUI()
	{
		IStarshipPlant starshipPlant = getStarshipPlant();
		
		starshipsPanel.removeAll();
		
		if (starshipPlant == null)
		{
			label.setText("No starship plant selected.");
			setEnabled(false);
			return;
		}
		setEnabled(true);
		
		label.setText(MessageFormat.format(build.getResource("starshipplant.action.label"), starshipPlant.getProductiveCelestialBodyName()));
		starshipsPanel.setLayout(new MigLayout());
		
		int rowCpt = 0;
		
		starshipsPanel.add(new JLabel(build.getResource("starshipplant.action.header.type")));
		starshipsPanel.add(new JLabel(build.getResource("starshipplant.action.header.quantity")));
		starshipsPanel.add(new JLabel(build.getResource("starshipplant.action.header.toMake")));
		starshipsPanel.add(new JLabel(build.getResource("starshipplant.action.header.toJoinFleet")), "wrap");
		
		for(StarshipTemplate template : Rules.getStarshipTemplates())
		{
			++rowCpt;
			starshipsPanel.add(new JLabel(template.getName()));
			starshipsPanel.add(getQuantityLabel(template));
			starshipsPanel.add(getToMakeSpinner(template), "growx");
			starshipsPanel.add(getToJoinFleetSpinner(template), "growx, wrap");			
		}
		
		starshipsPanel.add(txtFleetName, "growx, cell 3 "+(++rowCpt));
		starshipsPanel.add(btnMake, "cell 2 "+ (++rowCpt));
		starshipsPanel.add(btnJoinFleet, "cell 3 "+rowCpt);
		
		refreshAll();
	}	
	
	private void refreshValues()
	{
		IStarshipPlant starshipPlant = getStarshipPlant();
		if (starshipPlant == null) return;
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) getSepClient().getGameboard().getDB().getCelestialBody(starshipPlant.getProductiveCelestialBodyName());		
		IFleet assignedFleet = productiveCelestialBody.getAssignedFleet(getSepClient().getLogin()); 
		Map<StarshipTemplate, Integer> starships = assignedFleet == null ? null : assignedFleet.getStarships();
		Map<String, Integer> starshipsToMake = new HashMap<String, Integer>();
		Map<String, Integer> starshipsToJoinFleet = new HashMap<String, Integer>();
		
		for(StarshipTemplate template : Rules.getStarshipTemplates())
		{			
			int quantity = starships == null ? 0 : starships.containsKey(template) ? starships.get(template) : 0;
			int toMake = (Integer) getToMakeSpinner(template).getValue();
			JSpinner toJoinFleetSpn = getToJoinFleetSpinner(template);
			((SpinnerNumberModel) toJoinFleetSpn.getModel()).setMaximum(quantity);
			int toJoinFleet = (Integer) toJoinFleetSpn.getValue();			
			
			getQuantityLabel(template).setText(String.format("%d (%d)", quantity, quantity + toMake - toJoinFleet));
			
			starshipsToMake.put(template.getName(), toMake);
			starshipsToJoinFleet.put(template.getName(), toJoinFleet);
		}
		
		final MakeStarships makeStarships = new MakeStarships(getSepClient().getLogin(), productiveCelestialBody.getName(), starshipsToMake);
		try
		{
			makeStarships.check(getSepClient().getGameboard().getDB());
			btnMake.setToolTipText(String.format("Cost:%s%s", makeStarships.getCarbonCost()>0 ? " "+makeStarships.getCarbonCost()+"C" : "", makeStarships.getPopulationCost()>0 ? " "+makeStarships.getPopulationCost()+"P" : ""));
			for(ActionListener l : btnMake.getActionListeners()) btnMake.removeActionListener(l);
			btnMake.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(makeStarships);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Make starships command", ex);
						return;
					}
					
					refreshAll();
					
					for(StarshipTemplate template : Rules.getStarshipTemplates())
					{
						getToMakeSpinner(template).setValue(0);
					}
				}
			});
			btnMake.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnMake.setEnabled(false);
			btnMake.setToolTipText("Cannot make starships: "+e.getMessage());
		}
		
		String fleetName = txtFleetName.getText();
		final AssignStarships assignStarships = new AssignStarships(getSepClient().getLogin(), productiveCelestialBody.getName(), starshipsToJoinFleet, fleetName);
		try
		{
			assignStarships.check(getSepClient().getGameboard().getDB());
			btnJoinFleet.setToolTipText((assignStarships.getDestinationFleet() == null ? "Form fleet" : "Joint fleet")+" '"+fleetName+"'");
			for(ActionListener l : btnJoinFleet.getActionListeners()) btnJoinFleet.removeActionListener(l);
			btnJoinFleet.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(assignStarships);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Assign starships command", ex);
						return;
					}
										
					refreshAll();
					
					for(StarshipTemplate template : Rules.getStarshipTemplates())
					{
						getToJoinFleetSpinner(template).setValue(0);
					}
					
					txtFleetName.setText("");
				}
			});
			btnJoinFleet.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnJoinFleet.setEnabled(false);
			btnJoinFleet.setToolTipText("Cannot assign starhips : "+e.getMessage());
		}
	}
	
	private void updateProbeForm()
	{
		IStarshipPlant starshipPlant = getStarshipPlant();
		if (starshipPlant == null) return;
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) getSepClient().getGameboard().getDB().getCelestialBody(starshipPlant.getProductiveCelestialBodyName());		
		final MakeProbes makeProbes = new MakeProbes(getSepClient().getLogin(), productiveCelestialBody.getName(), txtProbesSerieName.getText(), (Integer) spnProbesQuantity.getValue());
		try
		{
			makeProbes.check(getSepClient().getGameboard().getDB());			
			btnMakeProbes.setToolTipText((makeProbes.getLastSerialNumber() == 0 ? "Make" : "Continue")+" probe serie");
			for(ActionListener l : btnMakeProbes.getActionListeners()) btnMakeProbes.removeActionListener(l);
			btnMakeProbes.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(makeProbes);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Assign starships command", ex);
						return;
					}
					
					refreshAll();
					
					spnProbesQuantity.setValue(0);
					txtProbesSerieName.setText("");
				}
			});
			btnMakeProbes.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnMakeProbes.setEnabled(false);
			btnMakeProbes.setToolTipText("Cannot make probe : "+e.getMessage());
		}
	}
	
	private void updateAPMForm()
	{
		IStarshipPlant starshipPlant = getStarshipPlant();
		if (starshipPlant == null) return;
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) getSepClient().getGameboard().getDB().getCelestialBody(starshipPlant.getProductiveCelestialBodyName());		
		final MakeAntiProbeMissile makeAntiProbeMissiles = new MakeAntiProbeMissile(getSepClient().getLogin(), productiveCelestialBody.getName(), txtAPMSerieName.getText(), (Integer) spnAPMQuantity.getValue());
		try
		{
			makeAntiProbeMissiles.check(getSepClient().getGameboard().getDB());			
			btnMakeAPM.setToolTipText((makeAntiProbeMissiles.getLastSerialNumber() == 0 ? "Make" : "Continue")+" APM serie");
			for(ActionListener l : btnMakeAPM.getActionListeners()) btnMakeAPM.removeActionListener(l);
			btnMakeAPM.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						getSepClient().getGameboard().onLocalCommand(makeAntiProbeMissiles);
					}
					catch(GameBoardException ex)
					{
						log.log(Level.SEVERE, "Error on Assign starships command", ex);
						return;
					}					
					
					refreshAll();
					
					spnAPMQuantity.setValue(0);
					txtAPMSerieName.setText("");
				}
			});
			btnMakeAPM.setEnabled(true);
		}
		catch(GameCommandException e)
		{
			btnMakeAPM.setEnabled(false);
			btnMakeAPM.setToolTipText("Cannot make probe : "+e.getMessage());
		}
	}
	
	private void refreshAll()
	{
		//if (refreshRunningGame) getRunningGamePanel().refresh(true);
		updateProbeForm();
		updateAPMForm();
		refreshValues();
	}
	
	//////////ui dynamic controls
	
	private final Map<StarshipTemplate, JLabel> quantityLabels = new HashMap<StarshipTemplate, JLabel>();
	private JLabel getQuantityLabel(StarshipTemplate template)
	{
		if (!quantityLabels.containsKey(template))
		{			
			JLabel lbl = new JLabel();
			quantityLabels.put(template, lbl);
		}
		
		return quantityLabels.get(template);
	}
	
	private final Map<StarshipTemplate, JSpinner> toMakeSpinners = new HashMap<StarshipTemplate, JSpinner>();
	private JSpinner getToMakeSpinner(StarshipTemplate template)
	{
		if (!toMakeSpinners.containsKey(template))
		{			
			JSpinner spn = createSpinner();
			toMakeSpinners.put(template, spn);
		}
		
		return toMakeSpinners.get(template);
	}
	
	private final Map<StarshipTemplate, JSpinner> toJoinFleetSpinners = new HashMap<StarshipTemplate, JSpinner>();
	private JSpinner getToJoinFleetSpinner(StarshipTemplate template)
	{
		if (!toJoinFleetSpinners.containsKey(template))
		{			
			JSpinner spn = createSpinner();
			toJoinFleetSpinners.put(template, spn);
		}
		
		return toJoinFleetSpinners.get(template);
	}
	
	private JSpinner createSpinner()
	{
		SpinnerNumberModel model = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		JSpinner spn = new JSpinner(model);
		spn.getModel().addChangeListener(new ChangeListener()
		{
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				refreshValues();
			}
		});
		return spn;
	}
	
}
