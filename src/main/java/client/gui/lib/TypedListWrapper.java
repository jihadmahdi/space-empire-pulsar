package client.gui.lib;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TypedListWrapper<ComponentType extends JComponent, ElementType> implements List<ElementType>
{
	public static interface TypedListCellRenderer<ElementType>
	{
		Component getListCellRendererComponent(JList list, ElementType value, int index, boolean isSelected, boolean cellHasFocus);
		<ComponentType extends JComponent> String getListCellToolTipText(TypedListWrapper<ComponentType, ElementType> typedWrapper, ElementType value, int index, boolean isSelected);
	}
	
	public static interface TypedListElementSelector<ElementType>
	{
		boolean equals(ElementType o1, ElementType o2);
	}
	
	public static class AbstractTypedJListCellRender<ElementType> implements TypedListCellRenderer<ElementType>
	{
		protected final JLabel label = new JLabel();
		
		public Component getListCellRendererComponent(JList list, ElementType value, int index, boolean isSelected, boolean cellHasFocus)
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
		
		public <ComponentType extends JComponent> String getListCellToolTipText(TypedListWrapper<ComponentType, ElementType> list, ElementType value, int index, boolean isSelected)
		{
			return value.toString();
		};
	}	
	
	private static abstract class TypedListWrappedComponentModel<ElementType> extends AbstractList<ElementType>
	{
		@Override
		abstract public ElementType get(int index);

		@Override
		abstract public int size();
		
		@Override
		abstract public ElementType set(int index, ElementType element);
		
		@Override
		abstract public void add(int index, ElementType element);		
		
		@Override
		abstract public ElementType remove(int index);
		
		abstract public ElementType getSelectedElement();
		
		abstract public void setSelectedElement(ElementType element);
		
		abstract public int getSelectedIndex();
	}
	
	private final Class<ElementType> wrappedType;
	private final ComponentType wrappedComponent;
	private final TypedListElementSelector<ElementType> selector;
	
	private TypedListCellRenderer<ElementType> cellRenderer;
	private final ListCellRenderer defaultListCellRenderer;
	private final TypedListWrappedComponentModel<ElementType> model;
	
	private TypedListWrapper(Class<ElementType> wrappedType, ComponentType wrappedComponent)
	{
		this(wrappedType, wrappedComponent, null);
	}
	
	public TypedListWrapper(Class<ElementType> wrappedType, ComponentType wrappedComponent, TypedListElementSelector<ElementType> selector)
	{
		this.wrappedType = wrappedType;
		this.wrappedComponent = wrappedComponent;
		this.selector = (selector != null) ? selector : new TypedListElementSelector<ElementType>()
		{
			
			@Override
			public boolean equals(ElementType o1, ElementType o2)
			{
				return (o1 == null) ? o2 == null : o1.equals(o2);
			}
		};
		
		this.defaultListCellRenderer = new DefaultListCellRenderer();
		
		ListCellRenderer listCellRenderer = new ListCellRenderer()
		{
		
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if (cellRenderer == null || !TypedListWrapper.this.wrappedType.isInstance(value))
				{
					return defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
				else
				{
					return cellRenderer.getListCellRendererComponent(list, TypedListWrapper.this.wrappedType.cast(value), index, isSelected, cellHasFocus);
				}
			}
		};				
		
		if (JList.class.isInstance(this.wrappedComponent))
		{
			final JList wrappedList = JList.class.cast(this.wrappedComponent);
			wrappedList.setCellRenderer(listCellRenderer);
			final DefaultListModel wrappedListModel = new DefaultListModel();
			wrappedList.setModel(wrappedListModel);
			this.model = new TypedListWrappedComponentModel<ElementType>()
			{

				@Override
				public ElementType get(int index)
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedListModel.get(index));
				}

				@Override
				public int size()
				{
					return wrappedListModel.size();
				}
				
				@Override
				public ElementType set(int index, ElementType element)
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedListModel.set(index, element));
				};
				
				@Override
				public void add(int index, ElementType element)
				{
					wrappedListModel.add(index, element);
				};
				
				@Override
				public ElementType remove(int index)
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedListModel.remove(index));
				}

				@Override
				public ElementType getSelectedElement()
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedList.getSelectedValue());
				}

				@Override
				public void setSelectedElement(ElementType element)
				{
					for(Object e : wrappedListModel.toArray())
					{	
						if (TypedListWrapper.this.selector.equals(element, TypedListWrapper.this.wrappedType.cast(e)))
						{
							wrappedList.setSelectedValue(e, true);
							return;
						}
					}
				}

				@Override
				public int getSelectedIndex()
				{
					return wrappedList.getSelectedIndex();
				}
			};
			
			MouseMotionListener mouseMotionListener = new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					int index = wrappedList.locationToIndex(e.getPoint());
					if (index < 0) return;

					ElementType element = TypedListWrapper.this.model.get(index);
					if (cellRenderer == null)
					{
						wrappedList.setToolTipText(element.toString());
					}
					else
					{
						wrappedList.setToolTipText(cellRenderer.getListCellToolTipText(TypedListWrapper.this, element, index, wrappedList.isSelectedIndex(index)));
					}		

					super.mouseMoved(e);
				}
			};
			
			wrappedList.addMouseMotionListener(mouseMotionListener);
		}
		else if (JComboBox.class.isInstance(this.wrappedComponent))
		{
			final JComboBox wrappedComboBox = JComboBox.class.cast(this.wrappedComponent);
			wrappedComboBox.setRenderer(listCellRenderer);
			final DefaultComboBoxModel wrappedComboBoxModel = new DefaultComboBoxModel();
			wrappedComboBox.setModel(wrappedComboBoxModel);
			this.model = new TypedListWrappedComponentModel<ElementType>()
			{

				@Override
				public ElementType get(int index)
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedComboBoxModel.getElementAt(index));
				}

				@Override
				public int size()
				{
					return wrappedComboBoxModel.getSize();
				}
				
				@Override
				public ElementType set(int index, ElementType element)
				{
					wrappedComboBoxModel.insertElementAt(element, index);
					return remove(index+1);
				};
				
				@Override
				public void add(int index, ElementType element)
				{
					wrappedComboBoxModel.insertElementAt(element, index);
				};
				
				@Override
				public ElementType remove(int index)
				{
					ElementType oldVal = get(index);
					wrappedComboBoxModel.removeElementAt(index);
					return oldVal;
				}

				@Override
				public ElementType getSelectedElement()
				{
					return TypedListWrapper.this.wrappedType.cast(wrappedComboBox.getSelectedItem());
				}

				@Override
				public void setSelectedElement(ElementType element)
				{
					wrappedComboBox.setSelectedItem(element);
				}

				@Override
				public int getSelectedIndex()
				{
					return wrappedComboBox.getSelectedIndex();
				}
			};
		}
		else
		{
			throw new NotImplementedException();
		}		
	}
	
	public void setCellRenderer(TypedListCellRenderer<ElementType> typedListCellRenderer)
	{
		cellRenderer = typedListCellRenderer;
	}
	
	public ComponentType getComponent()
	{
		return wrappedComponent;
	}

	public void clear()
	{
		model.clear();
	}

	public boolean contains(Object o)
	{
		return model.contains(o);
	}

	public boolean containsAll(Collection<?> c)
	{
		return model.containsAll(c);
	}

	public boolean equals(Object o)
	{
		return model.equals(o);
	}

	public int hashCode()
	{
		return model.hashCode();
	}

	public int indexOf(Object o)
	{
		return model.indexOf(o);
	}

	public boolean isEmpty()
	{
		return model.isEmpty();
	}

	public int lastIndexOf(Object o)
	{
		return model.lastIndexOf(o);
	}

	public boolean remove(Object o)
	{
		return model.remove(o);
	}

	public boolean removeAll(Collection<?> c)
	{
		return model.removeAll(c);
	}

	public boolean retainAll(Collection<?> c)
	{
		return model.retainAll(c);
	}

	public int size()
	{
		return model.size();
	}

	public Object[] toArray()
	{
		return model.toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		return model.toArray(a);
	}

	public boolean add(ElementType e)
	{
		return model.add(e);
	}

	public void add(int index, ElementType element)
	{
		model.add(index, element);
	}

	public boolean addAll(Collection<? extends ElementType> c)
	{
		return model.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends ElementType> c)
	{
		return model.addAll(index, c);
	}

	public ElementType get(int index)
	{
		return model.get(index);
	}

	public Iterator<ElementType> iterator()
	{
		return model.iterator();
	}

	public ListIterator<ElementType> listIterator()
	{
		return model.listIterator();
	}

	public ListIterator<ElementType> listIterator(int index)
	{
		return model.listIterator(index);
	}

	public ElementType remove(int index)
	{
		return model.remove(index);
	}

	public ElementType set(int index, ElementType element)
	{
		return model.set(index, element);
	}

	public List<ElementType> subList(int fromIndex, int toIndex)
	{
		return model.subList(fromIndex, toIndex);
	}

	public ElementType getSelectedElement()
	{
		return model.getSelectedElement();
	}

	public void setSelectedElement(ElementType element)
	{
		model.setSelectedElement(element);
	}		
	
	public int getSelectedIndex()
	{
		return model.getSelectedIndex();
	}
}
