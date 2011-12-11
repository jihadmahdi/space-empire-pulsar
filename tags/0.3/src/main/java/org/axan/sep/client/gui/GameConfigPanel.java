package org.axan.sep.client.gui;

import java.util.logging.Logger;

import javax.swing.JPanel;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.db.IGameConfig;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class GameConfigPanel extends JPanel implements IModalComponent
{
	////////// static attributes
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	////////// private attributes
	private final BuildResult build;
	private boolean canceled = false;
	
	////////// bean fields
	private IGameConfig gameConfig;
	
	////////// no arguments constructor
	public GameConfigPanel()
	{
		build = SwingJavaBuilder.build(this);
		gameConfig = new GameConfig();
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
	
	public void setGameConfig(IGameConfig gameConfig)
	{
		IGameConfig old = this.gameConfig;
		this.gameConfig = gameConfig;
		firePropertyChange("gameConfig", old, gameConfig);
	}
	
	public IGameConfig getGameConfig()
	{
		return gameConfig;
	}
	
}
