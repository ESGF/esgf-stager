package esgf.node.stager.io.connectors;

import java.io.File;
import java.io.IOException;

import esgf.node.stager.io.RemoteFile;
import esgf.node.stager.io.StagerException;

/**
 * Represents an object capable of retrieving a remote file as well as its info.
 * 
 * @author Estanislao Gonzalez
 */
public interface RemoteConnector {

	/**
	 * Retrieves information on the remote file.
	 * 
	 * @param file file descriptor to be completed (must have target filed set
	 *            though)
	 * @throws IOException if access fails.
	 */
	void completeDataInfo(RemoteFile file) throws IOException;

	/**
	 * Retrieves the remote file at the given location.
	 * 
	 * @param source remote file descriptor
	 * @param target target local file
	 * @throws StagerException to signal the retrieval has failed.
	 */
	void retrieveFile(RemoteFile source, File target)
			throws StagerException;

}
