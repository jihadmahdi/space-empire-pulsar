package client.gui.lib;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TypedJList<E> extends JList
{
	private final Class<E> type;
	private final DefaultListModel model;
	private TypedJListCellRenderer<E> cellRenderer;
	private final ListCellRenderer defaultListCellRenderer;
	private final Comparator<E> comparator;

	public static interface TypedJListCellRenderer<E>
	{
		Component getListCellRendererComponent(TypedJList<E> list, E value, int index, boolean isSelected, boolean cellHasFocus);
		String getListCellToolTipText(TypedJList<E> list, E value, int index, boolean isSelected);
	}
	
	public static class AbstractTypedJListCellRender<E> implements TypedJListCellRenderer<E>
	{
		protected final JLabel label = new JLabel();
		
		public Component getListCellRendererComponent(client.gui.lib.TypedJList<E> list, E value, int index, boolean isSelected, boolean cellHasFocus)
		{
			label.setText(value.toString());
			
			if (isSelected)
			{
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
			}
			else
			{
				label.setBackground(list.getBackground());
				label.setForeground(list.getForeground());
			}
			
			label.setEnabled(list.isEnabled());
			label.setFont(list.getFont());
			label.setOpaque(true);
			
			return label;
		}
		
		public String getListCellToolTipText(client.gui.lib.TypedJList<E> list, E value, int index, boolean isSelected)
		{
			return value.toString();
		};
	}
	
	public TypedJList(Class<E> type, Comparator<E> comparator)
	{
		this.comparator = comparator;
		this.type = type;
		model = new DefaultListModel();
		setModel(model);
		defaultListCellRenderer = new DefaultListCellRenderer();
		
		setCellRenderer(new ListCellRenderer()
		{
		
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if (cellRenderer == null || !TypedJList.this.type.isInstance(value))
				{
					return defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
				else
				{
					return cellRenderer.getListCellRendererComponent(TypedJList.this, TypedJList.this.type.cast(value), index, isSelected, cellHasFocus);
				}
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				int index = locationToIndex(e.getPoint());
				if (index < 0) return;

				E element = getElementAt(index);
				if (cellRenderer == null)
				{
					setToolTipText(element.toString());
				}
				else
				{
					setToolTipText(cellRenderer.getListCellToolTipText(TypedJList.this, element, index, isSelectedIndex(index)));
				}		

				super.mouseMoved(e);
			}
		});
				
	}
	
	public void setCellRenderer(TypedJListCellRenderer<E> typedListCellRenderer)
	{
		cellRenderer = typedListCellRenderer;
	}

	//////////////////////
	
	public void addElement(E element)
	{
		model.addElement(element);
	}

	public int capacity()
	{
		return model.capacity();
	}

	public void clear()
	{
		model.clear();
	}

	public boolean contains(E element)
	{
		return model.contains(element);
	}

	public void copyInto(E[] array)
	{
		model.copyInto(array);
	}

	public E elementAt(int index)
	{
		return type.cast(model.elementAt(index));
	}

	public Enumeration<E> elements()
	{
		return (Enumeration<E>) model.elements();
	}

	public void ensureCapacity(int capacity)
	{
		model.ensureCapacity(capacity);
	}

	public E firstElement()
	{
		return type.cast(model.firstElement());
	}

	public E get(int index)
	{
		return type.cast(model.get(index));
	}

	public E getElementAt(int index)
	{
		return type.cast(model.getElementAt(index));
	}

	public int getElementsCount()
	{
		return model.getSize();
	}

	public int indexOf(E element, int index)
	{
		return model.indexOf(element, index);
	}

	public int indexOf(E element)
	{
		return model.indexOf(element);
	}

	public void insertElementAt(E element, int index)
	{
		model.insertElementAt(element, index);
	}

	public boolean isEmpty()
	{
		return model.isEmpty();
	}

	public E lastElement()
	{
		return type.cast(model.lastElement());
	}

	public int lastIndexOf(E element, int index)
	{
		return model.lastIndexOf(element, index);
	}

	public int lastIndexOf(E element)
	{
		return model.lastIndexOf(element);
	}

	public void removeAllElements()
	{
		model.removeAllElements();
	}

	public boolean removeElement(E element)
	{
		return model.removeElement(element);
	}

	public void removeElementAt(int index)
	{
		model.removeElementAt(index);
	}

	public void removeRange(int fromIndex, int toIndex)
	{
		model.removeRange(fromIndex, toIndex);
	}

	public E set(int index, E element)
	{
		return type.cast(model.set(index, element));
	}

	public void setElementAt(E element, int index)
	{
		model.setElementAt(element, index);
	}

	public E[] toArray()
	{
		return (E[]) model.toArray();
	}
	
	//////////////////////

	public E getPrototypeCellValue()
	{
		if (super.getPrototypeCellValue() == null) return null;
		return type.cast(super.getPrototypeCellValue());
	}

	public E getSelectedValue()
	{
		if (super.getSelectedValue() == null) return null;
		return type.cast(super.getSelectedValue());
	}

	public E[] getSelectedValues()
	{
		return (E[]) super.getSelectedValues();
	}

	@Deprecated
	public void setListData(Object[] listData)
	{
		throw new NotImplementedException();
	}

	@Deprecated
	public void setListData(Vector listData)
	{
		throw new NotImplementedException();
	}

	@Deprecated
	public void setPrototypeCellValue(Object prototypeCellValue)
	{
		throw new NotImplementedException();
	}

	@Deprecated
	public void setSelectedValue(Object anObject, boolean shouldScroll)
	{
		throw new NotImplementedException();
	}

	public void setSelectedElement(E element, boolean shouldScroll)
	{
		for(E e : toArray())
		{
			if (comparator.compare(element, e) == 0)
			{
				super.setSelectedValue(e, shouldScroll);
				return;
			}
		}		
	}
	
	/////////////////////////////
	
	public boolean addAll(Collection<? extends E> c)
	{
		for(E e : c)
		{
			addElement(e);
		}
		
		return !c.isEmpty();
	}
	
	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	public static void main(String[] args)
	{
		TypedJList<String> typedJList = new TypedJList<String>(String.class, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				return o1.compareTo(o2);
			}
		});
		typedJList.addElement("Un");
		typedJList.addElement("Deux");
		typedJList.setVisible(true);
		
		JScrollPane scrollPane = new JScrollPane(typedJList);
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(typedJList);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
}
