package client.gui;

import javax.swing.SwingUtilities;

public class SpaceEmpirePulsar3dGUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				UniverseRenderer renderer = new UniverseRenderer3d();
				SpaceEmpirePulsarGUI inst = new SpaceEmpirePulsarGUI(renderer);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

}
