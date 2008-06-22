/**
 * @author Escallier Pierre
 * @file command.java
 * @date 20 juin 08
 */
package common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 */
public class Command implements Serializable
{
	/** Serial version */
	private static final long	serialVersionUID	= 1L;
	
	private final String command;
	private final Object[] parameters;
	
	public Command(String command, Serializable ... parameters)
	{
		this.command = command;
		this.parameters = parameters;
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public Object[] getParameters()
	{
		return parameters;
	}
	
	public byte[] encode() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		return baos.toByteArray();
	}
	
	public static Command decode(byte[] bytes) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Command command;
		try
		{
			command = (Command) ois.readObject();
		}
		catch (ClassNotFoundException e)
		{
			throw new IOException(e);
		}
		return command;
	}
}
