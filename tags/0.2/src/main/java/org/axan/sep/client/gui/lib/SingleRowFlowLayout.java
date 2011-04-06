/**
 * @author Escallier Pierre
 * @file SingleRowFlowLayout.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui.lib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A modified version of FlowLayout that allows containers using this
 * Layout to behave in a reasonable manner when placed inside a
 * JScrollPane
       
 * @author Babu Kalakrishnan
 */
public class SingleRowFlowLayout extends FlowLayout
{
    public SingleRowFlowLayout()
    {
        super();
    }

    public SingleRowFlowLayout(int align)
    {
        super(align);
    }

    public SingleRowFlowLayout(int align, int hgap, int vgap)
    {
        super(align, hgap, vgap);
    }

    public Dimension minimumLayoutSize(Container target)
    {
        return computeSize(target, false);
    }

    public Dimension preferredLayoutSize(Container target)
    {
        return computeSize(target, true);
    }

    private Dimension computeSize(Container target, boolean minimum)
    {
        synchronized (target.getTreeLock())
        {
            int hgap = getHgap();
            int vgap = getVgap();
            int w = target.getWidth();

           // Let this behave like a regular FlowLayout (single row)
           // if the container hasn't been assigned any size yet
            if (w == 0)
                w = Integer.MAX_VALUE;

            Insets insets = target.getInsets();
            if (insets == null)
                insets = new Insets(0, 0, 0, 0);
            int reqdWidth = 0;

            int maxwidth = w - (insets.left + insets.right + hgap * 2);
            int n = target.getComponentCount();
            int x = 0;
            int y = insets.top;
            int rowHeight = 0;

            for (int i = 0; i < n; i++)
            {
                Component c = target.getComponent(i);
                if (c.isVisible())
                {
                    Dimension d =
                        minimum ? c.getMinimumSize() :
                                 c.getPreferredSize();
                    if ((x == 0) || ((x + d.width) <= maxwidth))
                    {
                        if (x > 0)
                        {
                            x += hgap;
                        }
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else
                    {
                        x = d.width;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
            }
            y += rowHeight;
            return new Dimension(reqdWidth+insets.left+insets.right, y);
        }
    }
} 