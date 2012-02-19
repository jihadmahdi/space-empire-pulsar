package org.axan.sep.client.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.Rules;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.db.Commands.UpdateFleetMovesPlan;
import org.axan.sep.common.db.FleetMove;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.Commands.AssignStarships;
import org.axan.sep.common.db.Commands.MakeAntiProbeMissile;
import org.axan.sep.common.db.Commands.MakeProbes;
import org.axan.sep.common.db.Commands.MakeStarships;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventListModel;

public class FleetActionPanel extends JPanel implements IModalComponent
{
	//////////static attributes
	private static final Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	private static final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();	

	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;

	//////////bean fields	
	private SEPClient sepClient;
	private RunningGamePanel runningGamePanel;
	private IFleet fleet;
	private final EventList<String> destinations = GlazedLists.threadSafeList(new BasicEventList<String>());
	private final EventList<FleetMove> moves = GlazedLists.threadSafeList(new BasicEventList<FleetMove>());

	////////// ui controls
	private JTextPane label;
	private JComboBox destinationsList;
	private EventComboBoxModel<String> destinationsModel = new EventComboBoxModel<String>(destinations);
	private JSpinner spnDelay;
	private ButtonGroup group = new ButtonGroup();
	private JRadioButton rbGo;
	private JRadioButton rbAttack;
	private JButton btnDirect;
	private JButton btnAdd;
	private JList movesList;
	private EventListModel<FleetMove> movesModel = new EventListModel<FleetMove>(moves);
	private JButton btnUp;
	private JButton btnRemove;
	private JButton btnDown;

	////////// no arguments constructor	
	public FleetActionPanel()
	{
		//SwingJavaBuilderMyUtils.addType(EventComboBoxModel.class, EventListModel.class);
		build = SwingJavaBuilder.build(this);
		
		group.add(rbAttack);
		group.add(rbGo);
		
		// TODO: Change text field to list/edit field so we can choose between already existing fleet and creating a new one. 
		addPropertyChangeListener("fleet", new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				refreshUI();
			}
		});

		spnDelay.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		
		movesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		movesList.setEnabled(true);
		movesList.setFocusable(true);
		movesList.setCellRenderer(new ListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				FleetMove move = (FleetMove) value;
				
				String text = move == null ? "Error" : move.toString();
				return defaultListCellRenderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
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

	public IFleet getFleet()
	{
		return fleet;
	}
	
	public void setFleet(IFleet fleet)
	{
		IFleet oldFleet = this.fleet;
		this.fleet = fleet;				
		firePropertyChange("fleet", oldFleet, fleet);		
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
		IFleet fleet = getFleet();		
		
		if (fleet == null)
		{
			label.setText("No fleet selected.");
			setEnabled(false);
			return;
		}
		setEnabled(true);
		
		label.setText(fleet.toString());
		destinations.addAll(getSepClient().getGameboard().getDB().getCelestialBodiesNames());
		
		spnDelay.setValue(0);
		rbGo.setSelected(true);
		rbAttack.setSelected(false);

		refreshMoves();
	}
	
	private void refreshMoves()
	{
		moves.clear();
		moves.addAll(getFleet().getMoves());
	}	
	
	private String destinationName;
	private int delay;
	private boolean isAnAttack;
	
	private boolean checkCurrentMove()
	{
		destinationName = (String) destinationsList.getSelectedItem();
		delay = (Integer) spnDelay.getValue();
		isAnAttack = rbAttack.isSelected();
		
		if (destinationName == null) return false;
		if (!isAnAttack && !rbGo.isSelected()) return false;
		
		return true;
	}
	
	private void updateMovesPlan()
	{
		UpdateFleetMovesPlan updateFleetMovesPlan = new UpdateFleetMovesPlan(getSepClient().getLogin(), getFleet().getName(), moves);
		try
		{
			getSepClient().getGameboard().onLocalCommand(updateFleetMovesPlan);
		}
		catch(GameBoardException e)
		{
			Throwable t = e;
			while(t.getCause() != null && t.getCause() != t) t = t.getCause();
			JOptionPane.showMessageDialog(null, t.getMessage(), "Cannot update fleet moves plan", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void direct()
	{
		if (!checkCurrentMove()) return;
		moves.clear();
		moves.add(new FleetMove(destinationName, delay, isAnAttack));
		updateMovesPlan();
		refreshMoves();
	}
	
	private void addMove()
	{
		if (!checkCurrentMove()) return;
		moves.add(new FleetMove(destinationName, delay, isAnAttack));
		updateMovesPlan();
		refreshMoves();
	}
	
	private void up()
	{
		FleetMove move = (FleetMove) movesList.getSelectedValue();
		if (move == null) return;
		int i = moves.indexOf(move);
		i = Math.max(0, --i);
		moves.remove(move);
		moves.add(i, move);
		updateMovesPlan();
		refreshMoves();
		movesList.setSelectedValue(move, false);
	}
	
	private void down()
	{
		FleetMove move = (FleetMove) movesList.getSelectedValue();
		if (move == null) return;
		int i = moves.indexOf(move);
		i = Math.min(moves.size()-1, ++i);
		moves.remove(move);
		moves.add(i, move);
		updateMovesPlan();
		refreshMoves();
		movesList.setSelectedValue(move, false);
	}
	
	private void removeMove()
	{
		FleetMove move = (FleetMove) movesList.getSelectedValue();
		if (move == null) return;
		moves.remove(move);
		updateMovesPlan();
		refreshMoves();
	}
}
