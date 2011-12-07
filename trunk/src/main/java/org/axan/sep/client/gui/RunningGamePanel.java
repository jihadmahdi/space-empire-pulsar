package org.axan.sep.client.gui;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.IUniverseRenderer.IUniverseRendererListener;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IGameEvent;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class RunningGamePanel extends JPanel implements IModalComponent, IUniverseRendererListener
{
	//////////static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// static classes
	public static abstract class AUniverseRendererPanel extends JPanel implements IUniverseRenderer
	{
		
	}
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// ui controls
	private AUniverseRendererPanel universePanel = new SwingUniverseRenderer();

	////////// bean fields
	SEPClient sepClient;

	////////// no arguments constructor
	public RunningGamePanel()
	{
		SwingJavaBuilderMyUtils.addType(AUniverseRendererPanel.class);
		
		build = SwingJavaBuilder.build(this);
	}

	////////// IModal implementation

	@Override
	public boolean validateForm()
	{
		return build.validate();
	};

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
		firePropertyChange("sepClient", old, sepClient);
	}

	////////// ui events
	
	@Override
	public void updateSelectedArea(int x, int y, int z)
	{
		// TODO
	}
	
	//TODO: Should override some common interface to everybody's implementing this method
	void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents)
	{
		universePanel.receiveNewTurnGameBoard(newTurnEvents);
	}
		
}
