package org.axan.sep.client.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.javabuilders.swing.SwingJavaBuilder;

class JavaBuilderGUITester
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("syntax: containerClass [requiredType1 ... requiredTypen]");
			return;
		}
		
		try
		{
			Class<? extends Container> containerClass = Class.forName(args[0]).asSubclass(Container.class);			
			Class<?>[] requiredTypes = new Class<?>[args.length-1];
			for(int i=0; i < args.length-1; ++i)
			{
				requiredTypes[i] = Class.forName(args[i+1]);
			}
			
			test(containerClass, requiredTypes);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void test(final Class<? extends Container> containerClass, final Class<?> ... requiredTypes)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SwingJavaBuilderMyUtils.addType(requiredTypes);
				SwingJavaBuilder.getConfig().addResourceBundle(SpaceEmpirePulsarGUI.class.getName());
				try
				{
					final JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setPreferredSize(new Dimension(800, 600));
					frame.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowActivated(WindowEvent e)
						{
							System.out.println("activated");
							try
							{
								frame.setContentPane(containerClass.newInstance());
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
							}							
							frame.pack();
							super.windowActivated(e);
						}
						
						@Override
						public void windowDeactivated(WindowEvent e)
						{
							System.out.println("deactivated");
							frame.setContentPane(new JPanel());
							super.windowDeactivated(e);
						}
					});
					
					frame.pack();
					frame.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

}
