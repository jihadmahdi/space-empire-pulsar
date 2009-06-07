/**
 * @author Escallier Pierre
 * @file WrappedJLabel.java
 * @date 7 juin 2009
 */
package client.gui.lib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.BreakIterator;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 
 */
public class WrappedJLabel extends JLabel
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JLabel#setText(java.lang.String)
	 */
	@Override
	public void setText(String text)
	{
		Container container = this.getParent();
		
		if (container == null)
		{
			super.setText(text);
			return;
		}
		
		int containerWidth = container.getWidth();
		Font f = this.getFont();
		if (f == null) f = UIManager.getFont("Label.font");
		FontMetrics fm = this.getFontMetrics(f);
		
		
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);
		 
		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");
		 
		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next())
		{
			String word = text.substring(start,end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			if (trialWidth > containerWidth)
			{
				trial = new StringBuffer(word);
				real.append("<br>");
			}
			real.append(word);
		}
		 
		real.append("</html>");
		 
		super.setText(real.toString());
	}
}
