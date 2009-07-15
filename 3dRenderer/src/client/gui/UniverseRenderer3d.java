package client.gui;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import com.jme.input.KeyInput;
import com.jme.system.DisplaySystem;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.lwjgl.LWJGLSystemProvider;
import com.jmex.awt.input.AWTMouseInput;
import com.jmex.awt.lwjgl.LWJGLAWTCanvasConstructor;
import com.jmex.awt.lwjgl.LWJGLCanvas;

import common.PlayerGameBoard;

public class UniverseRenderer3d implements UniverseRenderer {

	private final LWJGLCanvas canvas;
	private final JPanel panel;
	private final UniverseCanvasImpl impl;

	private int width = 600;
	private int height = 480;	

	public UniverseRenderer3d() {
		// -------------GL STUFF------------------

		// make the canvas:
		DisplaySystem display = DisplaySystem
				.getDisplaySystem(LWJGLSystemProvider.LWJGL_SYSTEM_IDENTIFIER);
		display.registerCanvasConstructor("AWT",
				LWJGLAWTCanvasConstructor.class);
		canvas = (LWJGLCanvas) display.createCanvas(width, height);
		canvas.setUpdateInput(true);
		canvas.setTargetRate(60);

		// add a listener... if window is resized, we can do something about
		// it.
		canvas.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
				doResize();
			}
		});

		// Setup key and mouse input
		KeyInput.setProvider(KeyInput.INPUT_AWT);
		KeyListener kl = (KeyListener) KeyInput.get();
		canvas.addKeyListener(kl);
		AWTMouseInput.setup(canvas, false);

		// Important! Here is where we add the guts to the panel:
		impl = new UniverseCanvasImpl(width, height);
		canvas.setImplementor(impl);

		// -----------END OF GL STUFF-------------

		panel = new JPanel(new BorderLayout());
		canvas.setBounds(0, 0, width, height);
		panel.add(canvas, BorderLayout.CENTER);
	}

	protected void doResize()
	{
		impl.resizeCanvas(canvas.getWidth(), canvas.getHeight());
		((JMECanvas) canvas).makeDirty();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void refreshGameBoard(PlayerGameBoard gameBoard) {
		impl.refreshGameBoard(gameBoard);
	}

	@Override
	public void setListener(UniverseRendererListener listener) {
		impl.setListener(listener);
	}

}
