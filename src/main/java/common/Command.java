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
import java.nio.ByteBuffer;
import java.util.Hashtable;

import common.metier.ConfigPartie;

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
	
	public ByteBuffer encode() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		return ByteBuffer.wrap(baos.toByteArray());
	}
	
	public static Command decode(ByteBuffer bytes) throws IOException
	{
		byte buffer[] = new byte[bytes.capacity()];
		bytes.get(buffer);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
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
	
	public static void main(String[] args) throws IOException
	{
		ConfigPartie cfgPartie = new ConfigPartie();
		Hashtable<String, ConfigPartie> shm = new Hashtable<String, ConfigPartie>();
		
		Command cmd = new Command("TestCmd", new String("p0.String"), cfgPartie, shm);
		ByteBuffer bb = cmd.encode();
		Command reCmd = Command.decode(bb);
		
		System.out.println("reCmd: "+reCmd.getCommand());
		Object[] parameters = reCmd.getParameters();
		
		for(int i=0; i < parameters.length; ++i)
		{
			System.out.println("p"+i+"] "+parameters[i].toString());
		}
	}
}
