package server;

import server.IdentityService.OffContextTask;

import com.sun.sgs.app.Task;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.TransactionScheduler;


/**
 * The manager for MySQL lazy synchronization.
 * 
 * @author Emanuel Greisen
 * 
 */
public class IdentityManager
{
	/**
	 * The service backing this manager.
	 */
	private IdentityService service;

	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public IdentityManager(IdentityService service)
	{
		this.service = service;
	}
	

	public Identity getIdentity()
	{
		return service.getIdentity();
	}
	
	public void executeOffContectTask(OffContextTask target)
	{
		service.executeOffContextTask(target);
	}
	
	public TransactionScheduler getTransactionScheduler()
	{
		return service.getTransactionScheduler();
	}
}

