package org.axan.sep.client.gui.lib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileSystemView;

import org.axan.eplib.clientserver.ftp.FTPClient;
import org.axan.eplib.clientserver.ftp.FTPClient.FTPFile;

/**
 * This class is used for browsing a remote file system. This class uses an
 * instance of the {@link org.apache.commons.net.ftp.FTPClient FTPClient}to
 * connect to the underlying FTP Source. Connection timeouts may occur.
 */
public class FTPRemoteFileSystemView extends FileSystemView
{

	//
	// Constants
	//

	/**
	 * The logger instance for this class
	 */
	protected static final Logger logger = Logger.getLogger(FTPRemoteFileSystemView.class.getName());

	/**
	 * The root file system path
	 */
	protected static final String FILE_SYSTEM_ROOT_NAME = "/";

	/**
	 * Seperator character between files
	 */
	public static final String FILE_SEPERATOR = "/";

	//
	// Instance variables
	//

	/**
	 * The FTPClient Object used to browse the underlying source.
	 */
	private final FTPClient ftpClient;

	/**
	 * The URL String to the underlying connection
	 */
	private URL homeUrl;

	/**
	 * The default directory of the connection
	 */
	private String homeDirectory;

	//
	// Constructors
	//

	/**
	 * Creates a new instance of the FTP Remote File System View.
	 * 
	 * @param url Root directory of the viewer.
	 */
	public FTPRemoteFileSystemView(FTPClient ftpClient, URL url)
	{
		this.ftpClient = ftpClient;
		this.homeUrl = url;
	}

	//
	// Accessors
	//

	/**
	 * @return Returns the url.
	 */
	public URL getUrl()
	{
		return homeUrl;
	}

	//
	// Public Methods
	//

	/**
	 * If a connection to the server is still open, disconnects it.
	 */
	public void disconnect()
	{
		if (ftpClient != null)
		{
			try
			{
				ftpClient.disconnect();
			}
			catch(IOException e)
			{
				logger.log(Level.WARNING, "IOEx while disconnecting", e);
			}
		}
	}

	//
	// Utility Methods
	//

	/**
	 * Creates an FTP Connection
	 * 
	 * @throws FTPBrowseException
	 *             If the connection cannot be opened
	 */
	private synchronized void createFTPConnection() throws IOException
	{
		ftpClient.connect();						
	}

	/**
	 * Private method used to check if an open connection is present, else
	 * creates one.
	 * 
	 * @throws FTPBrowseException
	 *             FTPBrowseException is thrown if the connection cannot be
	 *             opened.
	 */
	private void checkConnection() throws IOException
	{
		if (!ftpClient.isConnected())
		{
			createFTPConnection();
		}
		
		if (homeDirectory == null)
		{
			if (homeUrl.getPath().length() > 0)
			{
				ftpClient.changeWorkingDirectory(homeUrl.getPath());
			}
			homeDirectory = ftpClient.printWorkingDirectory();
		}
	}

	//
	// Overridden methods from FileSystemView
	//

	/**
	 * @see javax.swing.filechooser.FileSystemView#createNewFolder(java.io.File)
	 */
	@Override
	public File createNewFolder(File containingDir) throws IOException
	{
		// This is a read only view of the remote system only.
		throw new IOException("This file system view supports READ ONLY support ONLY!");
	}

	/**
	 * In the remote view home and default directory are considered to be the
	 * same.
	 * 
	 * @see javax.swing.filechooser.FileSystemView#getDefaultDirectory()
	 */
	@Override
	public File getDefaultDirectory()
	{
		return getHomeDirectory();
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getHomeDirectory()
	 */
	@Override
	public File getHomeDirectory()
	{
		try
		{
			checkConnection();
			//If home directory is the root directory then return that
			if (homeDirectory.equals(FILE_SYSTEM_ROOT_NAME))
			{
				return getRoots()[0];
			}
	
			// Get the file information
			String parent = homeDirectory.substring(0, homeDirectory.lastIndexOf(FILE_SEPERATOR)); // path
			String dirName = homeDirectory.substring(homeDirectory.lastIndexOf(FILE_SEPERATOR) + 1); // name
			ftpClient.changeWorkingDirectory(parent);
			for(File f : ftpClient.getFiles(this))
			{
				if (f.getName().equals(dirName))
				{
					return f;
				}
			}
		}
		catch(IOException e)
		{
			logger.log(Level.WARNING, "FTBEx", e);
		}

		return null;
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getRoots()
	 */
	@Override
	public File[] getRoots()
	{
		File[] ftpFiles = null;
		try
		{
			checkConnection();			
			
			ftpFiles = new File[1];
			ftpFiles[0] = getHomeDirectory();
			
			/*
			FTPFile ftpFile = new FTPFile(FILE_SYSTEM_ROOT_NAME, this);
			ftpFile.setType(FTPFile.DIRECTORY_TYPE);
			ftpFiles = new File[1];
			ftpFiles[0] = ftpFile;
			*/			
		}
		catch(IOException e)
		{
			logger.log(Level.WARNING, "Could not get root file", e);
		}
		return ftpFiles;
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#createFileObject(java.io.File,
	 *      java.lang.String)
	 */
	@Override
	public File createFileObject(File dir, String filename)
	{

		// We should never get here. If we ever do, call the parent and hope for
		// the best!!!
		logger.fine("Calling Super with: " + dir.toString() + " " + filename);
		return super.createFileObject(dir, filename);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#createFileObject(java.lang.String)
	 */
	@Override
	public File createFileObject(String path)
	{

		// We should never get here. If we ever do, call the parent and hope for
		// the best!!!
		logger.fine("Calling Super with: " + path);
		return super.createFileObject(path);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getChild(java.io.File,
	 *      java.lang.String)
	 */
	@Override
	public File getChild(File parent, String fileName)
	{
		if (FTPFile.class.isInstance(parent))
		{
			FTPFile parentDir = ((FTPFile) parent);
			try
			{
				checkConnection();
				ftpClient.changeWorkingDirectory(parentDir.getPath()); // path
				
				// Check if a path name is present.
				if (fileName.indexOf(FILE_SEPERATOR) > -1)
				{
					fileName = fileName.substring(fileName.lastIndexOf(FILE_SEPERATOR) + 1); // name
				}
				
				for(FTPFile f : ftpClient.getFiles(this))
				{
					if (f.getName().equals(fileName))
					{
						return f;
					}
				}								
			}
			catch(IOException e)
			{
				logger.log(Level.WARNING, "Problem browsing file system", e);
			}
			return null;
		}
		else
		{
			// Should never get here!!!
			logger.fine("Calling Super with: " + parent.toString() + " " + fileName);
			return super.getChild(parent, fileName);
		}
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getFiles(java.io.File,
	 *      boolean)
	 */
	@Override
	public synchronized File[] getFiles(File dir, boolean useFileHiding)
	{
		if (FTPFile.class.isInstance(dir) && dir.isDirectory())
		{
			FTPFile ftpFile = (FTPFile) dir;
			String path = ftpFile.getPath(); // path
			try
			{
				checkConnection();
				String pwd = ftpClient.printWorkingDirectory(); // path
				if (null == pwd || !pwd.equals(path))
				{
					ftpClient.changeWorkingDirectory(path);
				}
				pwd = ftpClient.printWorkingDirectory(); // path
				return ftpClient.getFiles(this);
			}
			catch(IOException e)
			{
				logger.log(Level.WARNING, "Could not operate on host", e);
				return new FTPFile[0];
			}
		}
		// Should never get here
		logger.fine("Calling Super with: " + dir.toString() + " " + String.valueOf(useFileHiding));
		return super.getFiles(dir, useFileHiding);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getParentDirectory(java.io.File)
	 */
	@Override
	public File getParentDirectory(File dir)
	{
		if (FTPFile.class.isInstance(dir))
		{			
			FTPFile ftpFile = ((FTPFile) dir);
			String path = ftpFile.getPath(); // path
			
			if (isRoot(ftpFile))			
			{
				return null;
			}

			String parent = path.substring(0, path.lastIndexOf(FILE_SEPERATOR) < 0 ? 0 : path.lastIndexOf(FILE_SEPERATOR)); // path

			// Parent is the root
			if (parent.isEmpty() || parent.equals(FILE_SYSTEM_ROOT_NAME))
			{
				return getRoots()[0];
			}

			// Parent of the parent to list the parent
			String pparent = parent.substring(0, parent.lastIndexOf(FILE_SEPERATOR)); // path
			if (pparent.length() == 0)
			{
				pparent = FILE_SYSTEM_ROOT_NAME;
			}

			FTPFile parentFile = null;

			try
			{
				checkConnection();
				ftpClient.changeWorkingDirectory(pparent); // path			
				String parentName = parent.substring(parent.lastIndexOf(FILE_SEPERATOR) + 1); // name
				for(FTPFile f : ftpClient.getFiles(this))
				{
					if (f.getName().equals(parentName)) // name
					{
						return  f;
					}
				}
			}
			catch(IOException e)
			{
				logger.log(Level.WARNING, "Problem browsing file system", e);
			}

			if (null == parentFile)
			{
				return getRoots()[0];
			}
		}

		logger.fine("Calling Super with: " + dir.toString());
		return super.getParentDirectory(dir);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#getSystemDisplayName(java.io.File)
	 */
	@Override
	public String getSystemDisplayName(File f)
	{
		if (FTPFile.class.isInstance(f))
		{
			FTPFile ftpFile = ((FTPFile) f);
			String name = ftpFile.getName(); // name
			
			if (isRoot(ftpFile))
			{
				return homeUrl.getHost()+(FILE_SYSTEM_ROOT_NAME.equals(name)?"":":"+name);
			}
			else
			{
				return f.getName();
			}
		}
		else
		{
			logger.fine("Calling Super with: " + f.getPath());
			return super.getSystemDisplayName(f);
		}
	}

	/**
	 * Always returns null. The super class uses this to return special folder
	 * names such as 'Desktop' on Windows.
	 * 
	 * @see javax.swing.filechooser.FileSystemView#getSystemTypeDescription(java.io.File)
	 */
	@Override
	public String getSystemTypeDescription(File f)
	{
		return null;
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#isComputerNode(java.io.File)
	 */
	@Override
	public boolean isComputerNode(File dir)
	{
		if (FTPFile.class.isInstance(dir))
		{
			FTPFile ftpFile = ((FTPFile) dir);
			String name = ftpFile.getName(); // name
			
			return isRoot(ftpFile);							
		}
		else
		{
			return super.isComputerNode(dir);
		}
	}

	/**
	 * Returns false, drives not supported on remote systems
	 * 
	 * @see javax.swing.filechooser.FileSystemView#isDrive(java.io.File)
	 */
	@Override
	public boolean isDrive(File dir)
	{
		return false;
	}

	/**
	 * Determines if the file is a real file or a link to another file.
	 * 
	 * @see javax.swing.filechooser.FileSystemView#isFileSystem(java.io.File)
	 * @return <code>true</code> if it is an absolute file or <code>false</code>
	 */
	@Override
	public boolean isFileSystem(File f)
	{
		if (FTPFile.class.isInstance(f))
		{
			FTPFile ftpFile = ((FTPFile) f);
			return !ftpFile.isSymbolicLink();
		}
		logger.fine("Calling Super for: " + f.toString());
		return super.isFileSystem(f);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#isFileSystemRoot(java.io.File)
	 */
	@Override
	public boolean isFileSystemRoot(File dir)
	{
		if (FTPFile.class.isInstance(dir))
		{
			FTPFile ftpFile = ((FTPFile) dir);
			String name = ftpFile.getName(); // name
			
			if (FILE_SYSTEM_ROOT_NAME.equals(name))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		logger.fine("Calling Super for: " + dir.toString());
		return super.isFileSystemRoot(dir);
	}

	/**
	 * Returns false. No floppy drives are viewable.
	 * 
	 * @see javax.swing.filechooser.FileSystemView#isFloppyDrive(java.io.File)
	 */
	@Override
	public boolean isFloppyDrive(File dir)
	{
		return false;
	}

	/**
	 * Hidden files are not supported now. Maybe later!
	 * 
	 * @see javax.swing.filechooser.FileSystemView#isHiddenFile(java.io.File)
	 */
	@Override
	public boolean isHiddenFile(File f)
	{
		return false;
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#isParent(java.io.File,
	 *      java.io.File)
	 */
	@Override
	public boolean isParent(File folder, File file)
	{
		if (FTPFile.class.isInstance(folder) && FTPFile.class.isInstance(file))
		{
			// If file is a FTPFile, you will always get back an FTPFile
			FTPFile calculatedParent = (FTPFile) getParentDirectory(file);
			String parentPath = ((FTPFile) folder).getPath(); // path
			if (parentPath.equals(calculatedParent.getPath())) // path
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		logger.fine("Calling Super for: " + folder.toString() + " " + file.toString());
		return super.isParent(folder, file);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#isRoot(java.io.File)
	 */
	@Override
	public boolean isRoot(File f)
	{
		if (FTPFile.class.isInstance(f))
		{
			FTPFile ftpFile = ((FTPFile) f);
			String path = ftpFile.getPath();
			if (FILE_SYSTEM_ROOT_NAME.equals(path))
			{
				return true;
			}
			
			if (homeDirectory.equals(path))
			{
				return true;
			}
			
			return false;
		}
		logger.fine("Calling super for: " + (f==null?"null":f.toString()));
		return super.isRoot(f);
	}

	/**
	 * @see javax.swing.filechooser.FileSystemView#isTraversable(java.io.File)
	 */
	@Override
	public Boolean isTraversable(File f)
	{
		if (FTPFile.class.isInstance(f))
		{
			FTPFile ftpFile = ((FTPFile) f);
			return new Boolean(ftpFile.isDirectory());
		}
		logger.fine("Calling super for: " + f.toString());
		return super.isTraversable(f);
	}
}
