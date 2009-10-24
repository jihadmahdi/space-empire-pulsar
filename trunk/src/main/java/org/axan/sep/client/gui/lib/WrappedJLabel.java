/**
 * @author Escallier Pierre
 * @file WrappedJLabel.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui.lib;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.BreakIterator;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 
 */
public class WrappedJLabel extends JScrollPane
{
	private WrappedTextArea	textComp;
	
	public WrappedJLabel()
	{
		textComp = new WrappedTextArea();
		setViewportView(textComp);
	}
	
	public WrappedJLabel(String text)
	{
		this();
		setText(text);
	}

	class WrappedTextArea extends JTextArea
	{

		public WrappedTextArea()
		{
			// TODO Auto-generated constructor stub
			setLineWrap(true);
			setWrapStyleWord(true);
			setEditable(false);
			setFocusable(false);
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

	}

	public void setText(String text)
	{
		textComp.setText(text);
		
		if (true)
		{
			return;
		}

		Container container = this.getParent();

		if (container == null)
		{
			//super.setText(text);
			return;
		}

		int containerWidth = container.getWidth();
		Font f = this.getFont();
		if (f == null) f = UIManager.getFont("Label.font");
		FontMetrics fm = this.getFontMetrics(f);

		/*
		int offset = 0;
		StringBuffer currentLine = new StringBuffer();
		
		Stack<Integer> tagBegins = new Stack<Integer>();
		int currentTagBegin;
		int currentTagEnd;
		
		currentTagBegin = text.indexOf("<", offset);
		currentTagEnd = text.indexOf(">", offset);
		
		// Begin tag first
		if (currentTagBegin >= 0 && (currentTagEnd < 0 || currentTagBegin < currentTagEnd))
		{
			tagBegins.add(currentTagBegin);
			offset = currentTagBegin+1;
		}
		else if (currentTagEnd >= 0)
		{
			
		}
		else
		{
			
		}
		*/

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");

		int start = boundary.first();
		for(int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next())
		{
			String word = text.substring(start, end);
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

		//super.setText(real.toString());
	}
}
