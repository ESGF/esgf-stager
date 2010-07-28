package esgf.node.stager.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import esgf.node.stager.io.connectors.RemoteConnector;
import esgf.node.stager.io.connectors.RemoteConnectorFactory;
import esgf.node.stager.utils.ExtendedProperties;


/**
 * Grabs a file from the remote system and copies to a local directory.
 * 
 * @author Estanislao Gonzalez
 * 
 */
public class FileGrabber {
	private static final Logger LOG = Logger.getLogger(FileGrabber.class);
	
	private static final String PROP_CORE_THREADS = "coreThreads";
	private static final String PROP_MAX_THREADS = "maxThreads";
	private static final String PROP_KEEP_ALIVE = "keepAlive";
	private static final String PROP_REMOTE_FACTORY = "remoteConnectorFactory";
	
	
	/**
	 * Encapsulates the call to be performed when finished retrieving the file.
	 */
	public static interface Callback {
		/**
		 * File was retrieved from Remote system.
		 * 
		 * @param localFile
		 *            file where the local copy of the retrieved data can be
		 *            found.
		 */
		public void done(File localFile);
	}
	

	
	/**
	 * Encapsulates a job request for grabbing a file from the remote system and 
	 * writing it down to the cache.
	 */
	private static class Job{
		private final RemoteFile remoteFile;
		private final File localFile;
		private Callback callback;
		
		/**
		 * @param hpssFilename
		 *            info to access the file on the remote system
		 * @param localFile
		 *            path to the local cache where this file will be written to
		 * @param callback
		 *            If given it will get called, when the job is done.
		 */
		public Job(RemoteFile hpssFile, File localFile,
				Callback callback) {
			this.remoteFile = hpssFile;
			this.localFile = localFile;
			this.callback = callback;
		}
	}

	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor poolExecutor;
	private RemoteConnectorFactory remoteConnectorFactory;
	
	/**
	 * Creates a HPSSGrabber which is able to retrieve files from the HPSS
	 * server upon request.
	 * 
	 * @param config configuration parameters encapsulated in a Properties
	 *            object.
	 * @throws StagerException if HPSS configuration fails.
	 */
	public FileGrabber (ExtendedProperties config) throws StagerException {
		//to simplify parameter check
		if (config == null) config = new ExtendedProperties();
		
		//get configurations parameters and set default if missing
		int coreThreads = config.getCheckedProperty(PROP_CORE_THREADS, 5);
		int maxThreads = config.getCheckedProperty(PROP_MAX_THREADS, 10);
		int keepAlive = config.getCheckedProperty(PROP_KEEP_ALIVE, 1440);
		
		//Remote server access parameters
		String factoryClass = config.getCheckedProperty(true, PROP_REMOTE_FACTORY);

		try {
			Class<?> fc = Class.forName(factoryClass);
			
			//create factory
			remoteConnectorFactory = (RemoteConnectorFactory) fc.getConstructor(
					(Class<?>[]) null).newInstance((Object[]) null);
			
			//pass properties to factory
			remoteConnectorFactory.setup(config);
			
		} catch (ClassNotFoundException e) {
			LOG.error("No RemoteConnectorFactory named '" + factoryClass
					+ "' found");
		} catch (Exception e) {
			LOG.error("Could not instantiate RemoteConnectorFactory: "
					+ factoryClass, e);
		}
			
		//create job queue
		queue = new LinkedBlockingQueue<Runnable>();
		
		poolExecutor = new ThreadPoolExecutor(coreThreads, maxThreads,
				keepAlive, TimeUnit.MINUTES, queue);
	}
	
	/**
	 * Stops the file grabbing service, i.e. running threads and threads' pools.
	 * 
	 * @param force if the stop should also stop running threads (this might
	 *            corrupt data in cache!)
	 */
	public void terminate(boolean force) {
		if (force) {
			poolExecutor.shutdownNow();
		} else {
			poolExecutor.shutdown();
		}
		LOG.info("File Grabber Terminated.");
	}
	
	/**
	 * Retrieve information on the target file.
	 * 
	 * @param target the path to the target file
	 * @return the representation of a file at the remote server
	 * @throws IOException if the file cannot be retrieven
	 */
	public RemoteFile getFileInfo(String target) throws IOException {
		
		RemoteFile result = new RemoteFile(target);
		RemoteConnector rc = remoteConnectorFactory.getInstance();
		rc.completeDataInfo(result);
		
		return result;
	}
	
	/**
	 * Schedule the retrieval of a file but don't wait for it to happen.
	 * 
	 * @param remoteFile
	 *            file description point to the remote one.
	 * @param localFile
	 *            local file file which will hold the information.
	 * @param callback
	 *            if provided it will be called upon completion.
	 */
	public void grabLater(RemoteFile remoteFile,
			File localFile, Callback callback) {
		poolExecutor.execute(makeTask(new Job(remoteFile,
				localFile, callback)));
	}

	/**
	 * Schedule the retrieval of a file and wait until it is finished.
	 * 
	 * @param remoteFile
	 *            file description point to the remote one.
	 * @param localFile
	 *            local file file which will hold the information.
	 */
	public void grabAndWait(RemoteFile remoteFile,
			File localFile) throws InterruptedException, ExecutionException {
		Future<?> f = poolExecutor.submit(makeTask(new Job(remoteFile, localFile, null)));
		f.get();
	}
	
	/**
	 * Encapsulates the retrieval in a Runnable object.
	 * 
	 * @param j Information for a particular Job (i.e., HPSS target and local
	 *            file where to safe the retrieved data to)
	 * @return a runnable that grabs the data from the HPSS
	 */
	private Runnable makeTask(final Job j) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					RemoteConnector rc = remoteConnectorFactory.getInstance();
					rc.retrieveFile(j.remoteFile, j.localFile);
				} catch (StagerException e1) {
					LOG.error(String.format(
							"Could not retrieve file: target:%s,local:%s"
							,j.remoteFile, j.localFile));
					//fail fast for now we should to this only in debug environments
					throw new RuntimeException(e1);
					//in any other case just allow the callback to return with
					//a null object and log (i.e. mail the admin) this situation
				} finally {
					//in any case call the callback if one was provided
					if (j.callback != null) {
						j.callback.done(j.localFile);
					}
				}

			}
		};
	}
	
}
