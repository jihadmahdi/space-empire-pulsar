/**
 * @author Escallier Pierre
 * @file SEPServerConsoleManager.java
 * @date 26 juin 08
 */
package server;

import java.io.Serializable;

import utils.SEPUtils.SerializableTask;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

/**
 * 
 */
public class SEPServerConsoleManager
{	
	/**
	 * The service backing this manager.
	 */
	private SEPServerConsoleService service;
	
	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public SEPServerConsoleManager(SEPServerConsoleService service)
	{
		System.out.println("SEPServerConsoleManager CTOR");
		this.service = service;
	}
}
