package esgf.node.stager.io.connectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.globus.ftp.FTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;

import esgf.node.stager.io.RemoteFile;
import esgf.node.stager.io.StagerException;
import esgf.node.stager.utils.ExtendedProperties;
import esgf.node.stager.utils.Misc;

/**
 * Access a remote system for file retrieval through FTP.
 * 
 * @author Estanislao Gonzalez
 */
public class FTPConnector implements RemoteConnector {
	private static final String PROP_SERV_NAME="ftp.serverName";
	private static final String PROP_SERV_PORT="ftp.serverPort";
	private static final String PROP_ROOT_DIR="ftp.serverRootDirectory";
	private static final String PROP_USER_NAME="ftp.userName";
	private static final String PROP_USER_PASS="ftp.userPassword";
	
	private String serverName;
	private Integer serverPort;
	private String serverRootDir;
	private String serverUserName;
	private String serverUserPass;

	/**
	 * Creates the connector with the given properties.
	 * @param config properties for setting up the connector.
	 * @throws StagerException if the setup fails.
	 */
	public FTPConnector(ExtendedProperties config) throws StagerException {
		// to simplify parameter check
		if (config == null) config = new ExtendedProperties();

		// HPSS Access parameters
		serverName = config.getCheckedProperty(PROP_SERV_NAME);
		serverPort = config.getCheckedProperty(PROP_SERV_PORT, 21);
		String tmp = config.getCheckedProperty(PROP_ROOT_DIR);
		if (tmp.charAt(tmp.length() - 1) == '/') {
			// clean last slash from root
			tmp = tmp.substring(0, tmp.length() - 1);
		}
		serverRootDir = tmp;
		serverUserName = Misc.transform(true, (String) config
				.getCheckedProperty(PROP_USER_NAME));

		// don't write pass to log! and deobfuscate it.
		serverUserPass = Misc.transform(true, (String) config
				.getCheckedProperty(false, PROP_USER_PASS));

	}

	/**
	 * {@inheritDoc}
	 */
	public void completeDataInfo(RemoteFile file) throws IOException {
		FTPClient client = null;
		String filename = null;
		try {
			filename = serverRootDir + file.getDirectory() + file.getFilename();
			client = new FTPClient(serverName, serverPort);

			client.authorize(serverUserName, serverUserPass);

			// get Data
			// result.size = client.getSize(filename); //unexpected protocol...
			// workaround

			// BUGFIX: this retrieves the REAL size of the file, if not ASCII is
			// used
			// the PADDED size is retrieved instead, which is larger.
			client.setType(Session.TYPE_IMAGE);

			String tmp = client.quote("SIZE " + filename).getMessage();
			tmp = tmp.substring(tmp.lastIndexOf(" ") + 1);
			file.setSize(Long.parseLong(tmp));

			file.setLastMod(client.getLastModified(filename));
			file.setExists(true);

		} catch (ServerException se) {
			if (se.getCode() == 550 ||
			// workaround because this is not always properly parsed...
					(se.getCode() == 1 && se.getMessage().contains("550"))) {
				throw new FileNotFoundException(String.format(
						"Could not retrieve target from FTP server: "
								+ "%s (at %s)", file.getTarget(), filename));
			} else {
				se.printStackTrace();
				throw new StagerException(
						StagerException.Code.PERMANENT_FAILURE,
						String.format("Could not access FTP server."
								+ " target %s (filename %s)", file.getTarget(),
								filename));
			}

		} catch (IOException se) {
			se.printStackTrace();
			throw new StagerException(StagerException.Code.PERMANENT_FAILURE,
					String.format("Could not access HPSS. target %s "
							+ "(filename %s)", file.getTarget(), filename));
		} finally {
			if (client != null) try {
				client.close();
			} catch (Exception e) {}
		}

	}
	

	/**
	 * Returns the name of the FTP server.
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}
	/**
	 * Returns the port of the FTP server.
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * Return the root dir of the FTP server to which all targets are going to
	 * be understood. For example:
	 * <p>
	 * serverRootDir=/pub/files target=/my_path/my_file (or even without the
	 * first slash)
	 * </p>
	 * Resolves in the file located at /pub/files/my_path/my_file. No file
	 * outside {@link #serverRootDir} can be accessed (but this doesn't imply it
	 * could still be referenced from a link within this path)
	 * 
	 * @return the serverRootDir
	 */
	public String getServerRootDir() {
		return serverRootDir;
	}

	/**
	 * Return the user name used for connecting to the FTP server.
	 * @return the serverUserName
	 */
	public String getServerUserName() {
		return serverUserName;
	}


	/**
	 * {@inheritDoc}
	 */
	public void retrieveFile(RemoteFile source, File target)
			throws StagerException {
		FTPClient client = null;
		try {
			//try to open channel
			client = new FTPClient(serverName, serverPort);
			client.authorize(serverUserName, serverUserPass);
			client.setType(Session.TYPE_IMAGE);
			
			//grab file locally
			client.get(serverRootDir + source.getTarget(), target);
			//TODO move to file grabber
			if (source.getLastMod() != null) {
				// we copy the last modification timestamp from the remote file
				// so we can tell if it ever get's change
				if (!target.setLastModified(source.getLastMod().getTime())) {
					throw new ClientException(ClientException.OTHER, "Cannot modify date of local file: "
							+ target.getAbsolutePath());
				}
			}
			
		} catch (IOException e) {
			throw new StagerException(StagerException.Code.UNDEFINED,
					"IO error while accessing the HPSS server.", e);
		} catch (ServerException e) {
			throw new StagerException(StagerException.Code.UNDEFINED,
					"Server error accessing the FTP server.", e);
		} catch (ClientException e) {
			throw new StagerException(StagerException.Code.UNDEFINED,
					"client error accessing the HPSS server.", e);
		} finally {
			if (client != null) try {
				client.close();
			} catch (Exception e) {}
		}
	}

}
