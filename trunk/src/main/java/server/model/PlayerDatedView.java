/**
 * @author Escallier Pierre
 * @file PlayerDatedView.java
 * @date 4 juin 2009
 */
package server.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represent an object that can have a different view for each player. And each view is dated. T must be immutable to be sure tracked object won't changed after
 */
class PlayerDatedView<T extends Serializable>
{
	public static class DatedObject<T extends Serializable>
	{
		private final T		object;

		private final int	date;

		/**
		 * Full constructor.
		 */
		public DatedObject(T object, int date)
		{
			this.object = clone(object);
			this.date = date;
		}

		public T getValue()
		{
			return clone(object);
		}

		private static final PipedInputStream	PIPE_IN	= new PipedInputStream();

		private static ObjectOutputStream		OOS;

		private static ObjectInputStream		OIS;

		static <U extends Serializable> U clone(U object)
		{
			if (true) return object;
						
			try
			{
				synchronized (PIPE_IN)
				{
					if (OOS == null)
					{
						OOS = new ObjectOutputStream(new PipedOutputStream(PIPE_IN));
					}
					if (OIS == null)
					{
						OIS = new ObjectInputStream(PIPE_IN);
					}
				}
				
				OOS.writeObject(object);
				U copy = (U) OIS.readObject();

				OIS.close();
				OOS.close();
				PIPE_IN.close();

				return copy;
			}
			catch (Exception e)
			{
				throw new Error("Cannot serialise/deserialise object from class "+object.getClass().getCanonicalName(), e);
			}

		}
	}

	Map<String, DatedObject<T>>	playersViews	= new Hashtable<String, DatedObject<T>>();

	public boolean hasView(String playerLogin)
	{
		return (playersViews.containsKey(playerLogin) && playersViews.get(playerLogin) != null);
	}

	public DatedObject<T> getView(String playerLogin)
	{
		if ( !playersViews.containsKey(playerLogin)) return null;
		return playersViews.get(playerLogin);
	}

	public T getLastValue(String playerLogin, T def)
	{
		DatedObject<T> datedObject = playersViews.get(playerLogin);
		if (datedObject == null) return def;
		return datedObject.getValue();
	}

	public void updateView(String playerLogin, T object, int date)
	{
		playersViews.put(playerLogin, new DatedObject<T>(object, date));
	}
}
