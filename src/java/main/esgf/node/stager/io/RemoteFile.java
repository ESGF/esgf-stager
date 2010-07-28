package esgf.node.stager.io;

import java.util.Date;

/**
 * Representation of a file at a remote server.
 * <p>
 * <b>Note</b>: Only forward slashes are supported.
 * </p>
 * @author Estanislao Gonzalez
 */
public class RemoteFile {
	private final String target;
	private final String directory;
	private final String filename;
	private long size = -1;
	private Date lastMod = null;
	private boolean exists = false;

	/**
	 * Create an object from a given remote system target (path) string.
	 * @param target path to target file in Remote System. Path separator must
	 *            be forward slashes.
	 */
	public RemoteFile(String target) {
		this.target = target;

		// split target into filename and directory
		int splitpoint = target.lastIndexOf("/");
		if (splitpoint == -1) {
			// no directory given
			filename = target;
			directory = "/";
		} else {
			directory = splitpoint == 0 ? "/" : target.substring(0,
					splitpoint + 1);
			filename = target.substring(splitpoint + 1);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RemoteFile)) return false;

		RemoteFile f2 = (RemoteFile) obj;

		// assure the pointed file is the same and wasn't modified in between.
		return target.equals(f2.target) && size == f2.size
				&& lastMod.equals(f2.lastMod);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// this should identify uniquely this object
		return target.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("RemoteFile{target:%s, dir:%s, file:%s, size:%d, "
				+ "last:%s, there?%b}", target, directory, filename, size,
				lastMod, exists);
	}

	/**
	 * Equals directory + '/' + filename
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Returns only the directory part of target.
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Returns only the filename of target.
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * The size in Bytes of the remote file (if known). -1 if unknown.
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * The last modification time of the file (if knwon). Null if unknown.
	 * @return the lastMod
	 */
	public Date getLastMod() {
		return lastMod;
	}

	/**
	 * file existence on the remote server.
	 * @return if this file exists on the remote server
	 */
	public boolean exists() {
		return exists;
	}

	/**
	 * @param exists the exists to set
	 */
	public void setExists(boolean exists) {
		this.exists = exists;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * @param lastMod the lastMod to set
	 */
	public void setLastMod(Date lastMod) {
		this.lastMod = lastMod;
	}

}