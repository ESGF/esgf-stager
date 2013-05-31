package esgf.node.stager.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import esgf.node.stager.io.StagerException.Code;
import esgf.node.stager.utils.ExtendedProperties;

/**
 * Handles the retrieval and cache of files from a remote server.
 *
 * @author Estanislao Gonzalez
 */
public class StagerCache {
    private static final Logger LOG = Logger.getLogger(StagerCache.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();
    /** Logs CacheEntry access. */
    private static final Logger LOG_ENTRY = Logger
            .getLogger(StagerCache.CacheEntry.class);
    private static final boolean DEBUG_ENTRY = LOG_ENTRY.isDebugEnabled();

    private static final String PROP_LOCAL_DIR = "localDirectory";
    private static final String PROP_MAX_CACHE_SIZE = "maxCacheSize";
    private static final String PROP_MAX_CACHE_FILES = "maxCacheFiles";

    /**
     * Encapsulates an entry in the cache table.
     */
    private class CacheEntry implements Comparable<CacheEntry>,
            FileGrabber.Callback {

        private final String target; 		// remote file target
        private final RemoteFile remoteFile;
        private File cachedFile = null; 	// local cache file
        /** If the file is already in the cache. If not it is being retrieved. */
        private boolean inCache = false;

        // times for better control
        long requestTime; 		// time at which the first request for the file was
                                // taken
        long creationTime = -1; // time at which the local file got created
        long lastAccess = -1; 	// last access time to the cached file
        int accessCount = 0; 	// count access (overflow in 7 years@10/sec)
        long size = -1; 		// file size

        /**
         * @param file descriptor of a file@HPSS. Must not be null. (makes no
         *            sense)
         * @pre file != null
         */
        private CacheEntry(RemoteFile file) {
            requestTime = System.currentTimeMillis();
            this.remoteFile = file;
            this.target = file.getTarget();

            if (DEBUG_ENTRY) LOG_ENTRY.debug("Created: " + file.getTarget());
        }

        private RemoteFile getRemoteFile() {
            return remoteFile;
        }

        /**
         * The method is not synchronized what might miss some calls (not critic
         * and rather unexpected)
         *
         * @return the cached file or null if not cached
         */
        private File getFile() {
            accessCount++;
            lastAccess = System.currentTimeMillis();
            if (DEBUG_ENTRY) LOG_ENTRY.debug("Accessing: '"
                    + remoteFile.getTarget() + "'(" + accessCount
                    + ") in cache? " + inCache);

            // reset order (these are thread safe)
            orderedCacheEntries.remove(this);
            orderedCacheEntries.add(this);

            return cachedFile;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(CacheEntry other) {
            // simple logic - lastAccess (LIFO)
            // this could be as complex as required, should be "preferable" fast
            // though as this get's triggered every time a new entry is added or
            // deleted for as maximal log n, n=cache size
            return lastAccess > other.lastAccess ? 1
                    : (lastAccess < other.lastAccess ? -1 : 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof CacheEntry
                    && target.equals(((CacheEntry) obj).target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return target.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("{Entry:{target:%s,file:%s,creationTime:%s",
                    target, cachedFile.getAbsolutePath(), new java.util.Date(
                            creationTime));
        }

        /**
         * Warning we are locking the cache and this cache entry although not at
         * the same time. {@inheritDoc}
         */
        public void done(File localFile) {
            // check result from file gathering
            if (localFile != null) {

                // everything went just swell
                synchronized (this) {
                    cachedFile = localFile;
                    creationTime = System.currentTimeMillis();
                    size = localFile.length();
                    inCache = true;
                    orderedCacheEntries.add(this);
                }

            } else {
                // there was a problem retrieving the file.
                // forget this entry, this will allow to retry it.
                synchronized (cache) {
                    cache.remove(remoteFile.getTarget());
                }
            }

            // Wake up all threads waiting on this.
            // note that if something went wrong the localFile will be
            // null and this entry will deleted from the cache.
            synchronized (this) {
                notifyAll();
            }

            // no thread can wait on this object anymore as it is not listed in
            // the cache.
            if (DEBUG_ENTRY) LOG_ENTRY.debug("Retrieved: "
                    + remoteFile.getTarget() + " duration: "
                    + (creationTime - requestTime) + " millis.");
        }

        /**
         * Removes this entry from cache. (holds cache lock)
         */
        public void remove() {
            if (DEBUG_ENTRY) LOG_ENTRY.debug("Removed: "
                    + remoteFile.getTarget() + " lifespan: "
                    + (System.currentTimeMillis() - creationTime) + " millis.");

            synchronized (cache) {
                cache.remove(remoteFile.getTarget());
            }

        }

    }

    /** Holds all cached entries. */
    private final Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
    /** this is thread safe */
    private final PriorityBlockingQueue<CacheEntry> orderedCacheEntries = new PriorityBlockingQueue<CacheEntry>();
    private long cachedFilesSize = 0;

    private final File localDirectory;
    private long maxCacheSize;
    private boolean limitCacheSize = false;
    private long maxCacheFiles;
    private boolean limitCacheFiles = false;
    private FileGrabber fileGrabber;

    /**
     * Creates the stager with the passed properties.
     *
     * @param props configuration map for Stager and access.
     * @throws StagerException if Stager cannot be properly setup.
     */
    public StagerCache(ExtendedProperties props) throws StagerException {
        // parse own properties and pass the rest to FileGrabber
        localDirectory = new File((String)props.getCheckedProperty(PROP_LOCAL_DIR));

        // perform checks
        if (localDirectory == null || !localDirectory.isDirectory()) { throw new IllegalArgumentException(
                "Directory " + props.getProperty(PROP_LOCAL_DIR)
                        + " cannot be found."); }

        // get cache size or take all of the available space.
        limitCacheSize = props.isPropertySet(PROP_MAX_CACHE_SIZE);
        if (limitCacheSize) {
            try {
                maxCacheSize = props.getCheckedProperty(PROP_MAX_CACHE_SIZE,
                        -0L);

                long available = localDirectory.getUsableSpace();

                if (maxCacheSize == 0) {
                    maxCacheSize = available;
                    LOG.warn("Cache auto-sized, allocating max available space: "
                            + (maxCacheSize >> 20) + "MB.");
                } else if (maxCacheSize > available) {
                    LOG.warn("The available space is lower than the one allocated.");
                }

            } catch (NumberFormatException e) {
                LOG.error("Invalid cache_size value: "
                        + props.getProperty("cache_size"));
                throw e;
            }
        }
        
        limitCacheFiles = props.isPropertySet(PROP_MAX_CACHE_FILES);
        if (limitCacheFiles) {
            try {
                maxCacheFiles = props.getCheckedProperty(PROP_MAX_CACHE_FILES,
                        Long.MAX_VALUE);
            } catch (NumberFormatException e) {
                LOG.error("Invalid cache_max_files value: "
                        + props.getProperty("cache_max_files"));
                throw e;
            }
        }

        // setup grabber
        fileGrabber = new FileGrabber(props);

        // Check what we have in the local directory and add it to the cache or
        // delete it)
        findFileTarget("/", localDirectory.listFiles());
    }

    /**
     * Find target files already cached. This allows adding existing files in
     * the cache directory to the current cache-managed. It is required so that
     * we don't have to start with an empty cache every time the server is
     * restarted.
     *
     * @param cwd current working directory, i.e. string to the path being
     *            currently processed.
     * @param filesInDir all files listed in the curren directory.
     * @throws StagerException in case any IOException is thrown while accessing
     *             the filesystem. None is expected, but it might happen if
     *             someone deletes the directory under which the cwd is hold
     *             while being processed.
     */
    private void findFileTarget(String cwd, File[] filesInDir)
            throws StagerException {
        for (int i = 0; i < filesInDir.length; i++) {
            if (filesInDir[i].isDirectory()) {
                // parse sub-directory recursively
                String newCwd = cwd + filesInDir[i].getName() + "/";
                findFileTarget(newCwd, filesInDir[i].listFiles());
            } else {
                if (filesInDir[i].canWrite()) {
                    String target = cwd + filesInDir[i].getName();
                    LOG.info("Found:" + target);
                    // let's load it
                    try {
                        retrieveFile(target, false);
                    } catch (FileNotFoundException e) {
                        // this file is not in the remote system get it out of here!
                        LOG.info("Deleting file in cache path but not in "
                                + "remote system: "
                                + filesInDir[i].getAbsolutePath() + " : "
                                + filesInDir[i].delete());

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new StagerException(
                                "Exception found while adding the local "
                                        + "file to its cache entry with target: "
                                        + target + "\n"
                                        + e.getMessage());
                    }
                } else {
                    // if we cannot write it, then it cannot be deleted, skip it.
                    // this file may be intended to be served, but certainly not
                    // to be cached by the Stager.
                    LOG.warn("Skipping file in cache:"
                            + filesInDir[i].getAbsolutePath());
                }
            }
        }

    }

    /**
     * Signals the termination of this cache. In this implementation the cash
     * is not being flush to avoid loosing it contents as it can re-serve
     * the existing files after restarted.
     *
     * @param force if the termination should happen abruptly.
     */
    public void terminate(boolean force) {
        fileGrabber.terminate(force);
    }

    /**
     * Retrieves a staged file. If the file is not cached (cache-miss) it will
     * be retrieved from the staged remote storage, which might take time.
     *
     * @param origTarget the target pointing to the file in the remote system
     * @param blocking if the retrieval should block until the file is available
     * @return the retrieved File or null if called in non-blocking modus and a
     *         cache-miss happens.
     * @throws IOException if the retrieval fails.
     */
    public File retrieveFile(String origTarget, boolean blocking)
            throws IOException {
        String target = canonizeTarget(origTarget);
        if (target == null) throw new FileNotFoundException("Invalid Target:"
                + origTarget);

        // try a fast hit recognition before anything else
        CacheEntry e;
        synchronized (cache) {
            e = cache.get(target);
            if (e != null && e.inCache) {
                // this sets the lastAccess time to now (although the file might
                // not exist)
                File f = e.getFile();

                // We don't check if the file was changed at the remote system
                // in that case we expect the local one to be manually deleted
                // and thus forced to be retrieved again.
                if (f != null && f.exists()) { return f; }
            }
        }

        // file to local cached one
        File local = new File(localDirectory, origTarget);
        if (local.exists() && !local.canWrite()) {
            // this file is not handled by this cache object.
            if (DEBUG) LOG.debug("Returning file not in cache:"
                    + local.getAbsolutePath());
            return local;
        }

        // ********* * * ********** * * *********** * * *********** * * ********
        // Up to this point there is no file in the cache, or there is a file in
        // the cache not attached to this manager. In any case the remote system
        // must be contacted to retrieve the file or check that the one already
        // here is the same as the remote one.
        // ********* * * ********** * * *********** * * *********** * * ********

        // get info to file (this operation should retrieve file parameters only
        // and therefore return (almost) immediately
        RemoteFile remoteFile = fileGrabber.getFileInfo(target);

        // check there is a file there
        if (!remoteFile.exists()) throw new StagerException(Code.FILE_NOT_FOUND,
                "No such File:" + target);

        // make sure we have an entry or create one
        synchronized (cache) {

            // cache miss
            if (e == null) {
                // create entry
                e = new CacheEntry(remoteFile);

                // Replicate target name in local structure.
                File dirStruct = new File(localDirectory, remoteFile
                        .getDirectory());
                if (!dirStruct.exists()) {
                    if (!dirStruct.mkdirs()) throw new StagerException(
                            Code.PERMANENT_FAILURE,
                            "Could not create directory structure at local host.");
                }

                // add it to the cache
                cache.put(target, e);

                if (local.isFile()) {
                    // file found but no cache entry.
                    // file already exists! it got probably added from an
                    // external source. Check if it's the same one as in the
                    // remote system.
                    if (remoteFile.getFilename().equals(local.getName())
                            && remoteFile.getSize() == local.length()
                            && remoteFile.getLastMod().getTime() == local
                                    .lastModified()) {
                        // enough that is the same file. Update entry

                        LOG.info("Adding file to cache control: "
                                + local.getAbsolutePath());
                        // (warning this method locks the cache, we already hold
                        // the lock to it, so it's safe)
                        e.done(local);

                        return local; // ok this "was" a cache hit

                    } else {
                        // delete this file as it is not what we expect.
                        LOG.info("Deleting invalid file:"
                                + local.getAbsoluteFile());
                        if (!local.delete()) { throw new StagerException(
                                "Cannot delete file: "
                                        + local.getAbsolutePath()); }
                    }

                } else {
                    // no file, no entry this is the normal cache miss
                    if (!allocate(remoteFile.getSize())) {
                        // We should signal the failure properly
                        throw new StagerException(Code.TEMPORARY_FAILURE,
                                "Not enough room for file ("
                                        + remoteFile.getSize()
                                        + "), try again later.");
                    }

                    // let the grabber work and call e.done callback when ready.
                    fileGrabber.grabLater(remoteFile, local, e);
                }

            }
        }

        // from here on there will always be a CacheEntry
        // that is ready or at least scheduled for execution.
        boolean invalidEntry = false;
        synchronized (e) {
            if (e.inCache) {
                if (e.getFile().exists() && e.getRemoteFile().equals(remoteFile)) {
                    // cache hit
                    return e.getFile();
                } else {
                    // file got deleted from external source or has changed.
                    // don't delete it here. Avoid having more than one lock.
                    invalidEntry = true;
                }
            } else {
                // still not in cache, but coming...
                if (blocking) {
                    // wait until it is
                    try {
                        e.wait();
                    } catch (InterruptedException e1) {
                        // cannot do much.
                    }
                    return e.getFile();
                }
            }
        }

        if (invalidEntry) {
            // delete entry if marked for deletion
            LOG.warn("Deleting invalid entry: " + e);
            synchronized (cache) {
                cache.remove(target);
                orderedCacheEntries.remove(e);
            }
            // substract size of missing file
            cachedFilesSize -= e.size;

            // no locks, now try again with a cleaned cache.
            // this will only break if for some reason the file is marked as
            // retrieved but the file is not and two different threads concur in
            // perfect synch calling recursively this method. So no, this won't
            // ever happen.
            return retrieveFile(target, blocking);
        }

        // file hasn't arrived yet.
        return null;
    }

    /**
     * Assure we have a canonized string to avoid caching a file more than once
     * and preventing injections which might allow to retrieve files other than
     * from the intended directory and sub directories.
     *
     * @param origTarget the original target
     * @return the canonical form of the path
     */
    private String canonizeTarget(String origTarget) {
        try {
            //remove double slashes and indirect references via URI, use dummy for protocol + server 
            String path = new java.net.URI("nothig://nothing/" + origTarget).normalize().getPath();
            
            //this is canonized, but it might point outside root. Check this.
            if (path.startsWith("/..")) return null;
            
            //last check for printable characters. Maybe not required, but still
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) < 32) return null;
            }
            
            return path;
        } catch (URISyntaxException e1) {
            LOG.error("Can't normalize file.", e1);
            return null;
        }
        // String target = origTarget
        // .replace("./", "/") //remove current dir
        // .replaceAll("//+", "/") //remove extra slashes
        // .replaceAll("[^/]+/+[.][.]/+", ""); //simplify parent directories
        //
        // if (target.charAt(0) != '/') {
        // return "/" + target;
        // } else {
        // return target;
        // }
        //
        
           //this doesn't work on windows because canonizing a path implies using lowercase...
        // assure we have a relative path
//        String target = origTarget.replaceFirst("^/+", "");
//
//        // forbid special symbols (regex appears to have problem with \0)
//        if (!target.matches("^[^\\n\\r\\t\\f]+$") || target.contains("\0")) return null;
//
//        try {
//            // we better rely in Java for this, as we might evade some
//            // subtleness of the problem.
//            String current = new File(".").getCanonicalPath();
//            
//            //assuming target lies in cache d !!!
//            String objective = new File(target).getCanonicalPath();
//            
//            //Don't use any other file separator.
//            if (File.separatorChar != '/') {
//                current = current.replace(File.separatorChar, '/');
//                objective = objective.replace(File.separatorChar, '/');
//            }
//            if (objective.length() < current.length()
//                    || !objective.startsWith(current)) {
//                // this is not the same directory as root.
//                return null;
//            }
//            // ok return the canonical path
//            return objective.substring(current.length());
//        } catch (IOException e) {
//            // this path is in any case bad.
//            return null;
//        }
    }

    /**
     * Tries to allocate enough space.
     *
     * @param size size required in cache
     * @return if the allocation succeeded.
     */
    private boolean allocate(long size) {
        synchronized (cache) {
            //get the minimum available space (this is required only if we allow
            //other
            long maxUsableSize = limitCacheSize ? Math.min(
                    localDirectory.getUsableSpace(), maxCacheSize)
                    : localDirectory.getUsableSpace();
            long available = maxUsableSize - cachedFilesSize - size;

            while (available < 0 || (limitCacheFiles && cache.size() > maxCacheFiles)) {
                if (orderedCacheEntries.isEmpty()) {
                    //we cannot solve this, should never happen though
                    LOG.error("Not enough space and cache is empty.");
                    return false;
                }
                
                // we need to make place for this new file
                //the next to be deleted is defined in the CacheEntry.compareTo()
                //method.
                CacheEntry ce = orderedCacheEntries.poll();
                
                //skip files in progress.
                if (!ce.inCache) continue;

                if (!ce.cachedFile.delete()) {
                    // just try another (it might be being downloaded)
                    continue;
                }

                //ok the file was removed. Remove the entry and free the space;
                ce.remove();
                cachedFilesSize -= ce.size;
                available += ce.size;
            }
            //check one last time to see if 
            if (available < 0 || (limitCacheFiles && cache.size() > maxCacheFiles)) {
                // we couldn't allocate it
                LOG.error("Could not allocate space for file. {cache{size:"
                        + cachedFilesSize
                        + (limitCacheSize ? "/" + maxCacheSize : "/-")
                        + ",files:" + cache.size()
                        + (limitCacheFiles ? "/" + maxCacheFiles : "/-")
                        + "}}}");

                return false;
            }
            
            // increase cache used size before leaving the synchronization block
            cachedFilesSize += size;
        }
        
        //everything went ok.
        return true;
    }

    /**
     * Tries to remove all files in cache, if a file is being read from it won't
     * be deleted from the cache. In any case, the retrieval of files is not
     * aborted, so there is no guarantee that after this operation is called the
     * cache will be really empty. It is nevertheless guaranteed that unused
     * files are deleted regardless of the cache deletion strategy.
     */
    public void flush() {
        // clean the whole cache
        List<CacheEntry> toDeleteEntries = new LinkedList<CacheEntry>();
        synchronized (cache) {

            // try to delete all files
            for (CacheEntry ce : cache.values()) {
                if (ce.cachedFile != null) {
                    if (!ce.cachedFile.delete()) {
                        // file is probably being read from.
                        LOG.warn("Could not delete file: " + ce.cachedFile);
                        // this will remain in the cache
                    } else {
                        toDeleteEntries.add(ce);
                    }
                }
            }

            for (CacheEntry ce : toDeleteEntries) {
                cache.remove(ce.target);
                cachedFilesSize -= ce.size;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Stager Cache holding: %d files in %d bytes.", cache
                .size(), cachedFilesSize);
    }
}
