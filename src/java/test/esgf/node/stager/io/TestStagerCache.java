package esgf.node.stager.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import esgf.node.stager.utils.ExtendedProperties;
import esgf.node.stager.utils.Misc;
import esgf.node.stager.utils.PrivilegedAccessor;

/**
 * Tests the HPSSCache class
 * @author Estanislao Gonzalez
 *
 */
public class TestStagerCache {
	private static final Logger LOG = Logger.getLogger(TestStagerCache.class);
	private static final boolean DEBUG = LOG.isDebugEnabled();
	
	private static final ExtendedProperties testProps = new ExtendedProperties();
	private static String testHPSStarget1;
	private static String testHPSStarget2;
	private static String testHPSSMediumTarget;

	private static File tmpDir;
	
	@BeforeClass
	public static void setup() throws Exception {
		testProps.put("remoteConnectorFactory", "esgf.node.stager.io.connectors.FTPConnectorFactory");
		testProps.put("ftp.serverName", "ftp3.de.postgresql.org");
		testProps.put("ftp.serverPort", "21");
		testProps.put("ftp.serverRootDirectory", "pub/");
		tmpDir = File.createTempFile("testHPSSCache", "");
		assertTrue(tmpDir.delete());
		assertTrue(tmpDir.mkdir());
		tmpDir.deleteOnExit();		//delete after we have finished.
		testProps.put("ftp.userName", Misc.transform(false, "Anonymous"));
		testProps.put("ftp.userPassword", Misc.transform(false, "none"));
		testProps.put("localDirectory", tmpDir.getAbsolutePath());
		testProps.put("maxCacheSize", "12345");
		
		testHPSStarget1 = "/default_style.css";
		testHPSStarget2 = "/DOCS/docs.old/zen.README";
		testHPSSMediumTarget = "/fedora/linux/releases/12/Everything/i386/debug/vim-debuginfo-7.2.245-3.fc12.i686.rpm";
	}
	
	@After
	public void cleanUp() {
		cleanDir(tmpDir);			//delete everything
		assertTrue(tmpDir.mkdir());	//recreate the basic structure
	}
	
	/**
	 * Used for recursive deletion of a directory structure.
	 * @param file directory/file being currently handled
	 */
	private static void cleanDir(File file) {
		if (!file.isDirectory()) {
			boolean result = file.delete();
			if (DEBUG)
				LOG.debug("Deleting file: " + file.getAbsolutePath() + " result: "
					+ result);
		} else {
			File[] content = file.listFiles();
			for (int i = 0; i < content.length; i++) {
				cleanDir(content[i]);
			}
			if (!file.delete()) {
				LOG.warn("Could not delete directory: "
						+ file.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Test that the configuration parameters are properly set and read.
	 * @throws Exception
	 */
	@Test
	public void testConfig() throws Exception {

		// test the grabber is properly setup
		StagerCache cache = new StagerCache(testProps);

		assertEquals(testProps.getProperty("localDirectory"),
				((File) PrivilegedAccessor.getField(cache, "localDirectory"))
						.getAbsolutePath());
		assertEquals(testProps.getProperty("maxCacheSize"), ""
				+ PrivilegedAccessor.getField(cache, "maxCacheSize"));

	}

	@Test
	public void testTmp() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		String[] possibilities = new String[]{
				"abc/test1.txt",
				"/abc/test1.txt",
				"abc///test1.txt",
				"abc/dfr/../test1.txt",
				"abc/test1.txt",
		};
		for (int i = 0; i < possibilities.length; i++) {
			assertEquals("Error on " + i + ") " + possibilities[i],
					"/abc/test1.txt", PrivilegedAccessor.callMethod(cache,
							"canonizeTarget", possibilities[i]));
		}
		
		String[] misformed = new String[]{
				"abc/../../test1.txt",
				"../otherpath/abc/test1.txt",
				"abc/test1.txt\nalskdj",
				"abc/test1.txt\0alskdj",
		};

		for (int i = 0; i < misformed.length; i++) {
			assertNull("Error on "
					+ i
					+ ") "
					+ " - result: "
					+ PrivilegedAccessor.callMethod(cache,
							"canonizeTarget", misformed[i])
					+ " - "
					+ misformed[i],
					PrivilegedAccessor.callMethod(cache,
							"canonizeTarget", misformed[i]));
		}
		
		
	}
	
	@Test
	public void testRetrieval() throws Exception {
		StagerCache cache = new StagerCache(testProps);

		//now some awkward but functioning paths
		String[] tries =  new String[]{
				//normal file
				"default_style.css",
				"/default_style.css",
				"./default_style.css",
				"DOCS/docs.old/zen.README",
				"/DOCS/docs.old/zen.README",
				"./DOCS/docs.old/zen.README",
				"DOCS/docs.old/.///zen.README",
				
				//go up dir (not traversing parent to root dir)
				"DOCS/docs.old/../docs.old/zen.README"
				
		};
		
		for (int i = 0; i < tries.length; i++) {
			try {
				if (DEBUG) LOG.debug("procesing:" + tries[i]);
				File f = cache.retrieveFile(tries[i], true);
				assertTrue(f.canWrite());
				
			} catch (FileNotFoundException e) {
				fail("File missed!!!: " + tries[i]);
				throw e;
			}
		}
	}
	@Test
	public void testInjections() throws Exception {
		ExtendedProperties p = (ExtendedProperties)testProps.clone();
		p.setProperty("root_dir","pub/DOCS");
		StagerCache cache = new StagerCache(p);
		String[] tries =  new String[]{
				//go to the parent directory
				"../default_style.css",
				"./../default_style.css",
				"/../default_style.css",
				"docs.old/../../default_style.css",
	
				
				//execute FTP command
				"docs.old/zen.README\nHELP\n",
				"docs.old/zen.README\0HELP\n"
				};
		
		for (int i = 0; i < tries.length; i++) {
			try {
				cache.retrieveFile(tries[i], true);
				LOG.info(i);
				fail("File retrieved!!!: " + tries[i]);
			} catch (FileNotFoundException e) {
				// ok!
			}
		}
		
		
		
	}
	
	/**
	 * Test if files are really held in the cache.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCaching() throws Exception {
		File f = new File(testProps.getProperty("localDirectory"));
		LOG.info("Max free space: " + (f.getUsableSpace() >> 20) + "Mb");
		
		StagerCache cache = new StagerCache(testProps);
		
		Map<String, ?> cacheMap = (Map<String, ?>) PrivilegedAccessor.getField(
				cache, "cache");

		// check everything is empty.
		assertTrue(cacheMap.isEmpty());
		assertTrue(0 == (Long) PrivilegedAccessor.getField(cache,
				"cachedFilesSize"));

		f = cache.retrieveFile(testHPSStarget1, true);
		assertNotNull(f);

		assertEquals(1, cacheMap.size());
		assertEquals(f.length(), ((Long) PrivilegedAccessor.getField(cache,
				"cachedFilesSize")).longValue());

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheSizeOverflow() throws Exception {
		FileGrabber g = new FileGrabber(testProps);
		RemoteFile hpssFile1 = g.getFileInfo(testHPSStarget1);
		RemoteFile hpssFile2 = g.getFileInfo(testHPSStarget2);
		
		//be sure both files are there
		assertTrue(hpssFile1.exists());
		assertTrue(hpssFile2.exists());
		
		//assure file 1 is smaller than file 2
		if (hpssFile1.getSize() > hpssFile2.getSize()) {
			RemoteFile tmp = hpssFile1;
			hpssFile1 = hpssFile2;
			hpssFile2 = tmp;
		}
		
		
		
		ExtendedProperties p = (ExtendedProperties)testProps.clone();
		p.put("maxCacheSize", "" + hpssFile2.getSize());
		
		// test the grabber is properly setup
		StagerCache cache = new StagerCache(p);
		Map<String, ?> cacheMap = (Map<String, ?>) PrivilegedAccessor.getField(
				cache, "cache");
		
		File f1 = cache.retrieveFile(hpssFile1.getTarget(), true);
		assertNotNull(f1);
		assertEquals(1, cacheMap.size());
		LOG.info(cacheMap.values().iterator().next());
		assertEquals(f1.length(), hpssFile1.getSize());
		assertEquals(f1.length(), ((Long) PrivilegedAccessor.getField(cache,
		"cachedFilesSize")).longValue());
		
		//no more room for f2!
		File f2 = cache.retrieveFile(hpssFile2.getTarget(), true);
		assertNotNull(f2);
		assertEquals(1, cacheMap.size());
		assertTrue(cacheMap.containsKey(hpssFile2.getTarget()));
		assertEquals(f2.length(), hpssFile2.getSize());
		assertEquals(f2.length(), ((Long) PrivilegedAccessor.getField(cache,
		"cachedFilesSize")).longValue());
		
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheMaxFilesOverflow() throws Exception {
		FileGrabber g = new FileGrabber(testProps);
		RemoteFile hpssFile1 = g.getFileInfo(testHPSStarget1);
		RemoteFile hpssFile2 = g.getFileInfo(testHPSStarget2);
		
		//be sure both files are there
		assertTrue(hpssFile1.exists());
		assertTrue(hpssFile2.exists());
		
		ExtendedProperties p = (ExtendedProperties)testProps.clone();
		p.put("maxCacheFiles", "1");
		
		// test the grabber is properly setup
		StagerCache cache = new StagerCache(p);
		Map<String, ?> cacheMap = (Map<String, ?>) PrivilegedAccessor.getField(
				cache, "cache");
		
		File f1 = cache.retrieveFile(hpssFile1.getTarget(), true);
		assertNotNull(f1);
		assertEquals(1, cacheMap.size());
		LOG.info(cacheMap.values().iterator().next());
		assertEquals(f1.length(), hpssFile1.getSize());
		
		//no more room for f2! Only 1 file allowed!
		File f2 = cache.retrieveFile(hpssFile2.getTarget(), true);
		assertNotNull(f2);
		assertEquals(1, cacheMap.size());
		assertTrue(cacheMap.containsKey(hpssFile2.getTarget()));
		assertEquals(f2.length(), hpssFile2.getSize());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheAllocationError() throws Exception {
		FileGrabber g = new FileGrabber(testProps);
		RemoteFile hpssFile1 = g.getFileInfo(testHPSSMediumTarget);
		RemoteFile hpssFile2 = g.getFileInfo(testHPSStarget2);
		
		//be sure both files are there
		assertTrue(hpssFile1.exists());
		assertTrue(hpssFile2.exists());
		
		ExtendedProperties p = (ExtendedProperties)testProps.clone();
		p.put("maxCacheSize", "" + (5 << 20));	//we need more space for this file
		p.put("maxCacheFiles", "1");
		
		// test the grabber is properly setup
		StagerCache cache = new StagerCache(p);
		Map<String, ?> cacheMap = (Map<String, ?>) PrivilegedAccessor.getField(
				cache, "cache");
		assertEquals(0, cacheMap.size());
		
		cache.retrieveFile(hpssFile1.getTarget(), false);
		try {
			//no more room for f2! Only 1 file allowed!
			cache.retrieveFile(hpssFile2.getTarget(), true);
			fail("Shouldn't have been able to allocate it!");
		} catch (StagerException e) {
			assertEquals(StagerException.Code.TEMPORARY_FAILURE, e.getErrorCode());
		}
		
		//block until first file arrives
		assertTrue(cache.retrieveFile(hpssFile1.getTarget(), true).delete());
	}
	
	/**
	 * Test a non blocking retrieval of files. After return the file should not
	 * be available; after a while it should.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNonBlockingRetrieve() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		int maxTries = 5;
		
		File f = cache.retrieveFile(testHPSStarget1, false);
		assertNull(f); //no blocking should return null if file is not cached
		
		//try a couple of times until we get the file or we give up
		while((f = cache.retrieveFile(testHPSStarget1, false)) == null && --maxTries > 0){
			Thread.sleep(1000);			
			LOG.info("Trying to retrieve the file again.");
		}
		assertNotNull(f);	//if it fails, maxTries == 0
		showFile(f);
		
		maxTries = 5;
		while((f = cache.retrieveFile(testHPSStarget2, false)) == null && --maxTries > 0){
			Thread.sleep(1000);			
			LOG.info("Trying to retrieve the file again.");
		}
		assertNotNull(f);
		showFile(f);
		
		//clean up this cache
		LOG.info("Cache status:" + cache);
		cache.flush();
		LOG.info("Cache status after flush:" + cache);
	}
	
	/**
	 * Assure the file is there just after returning from this method.
	 * @throws Exception
	 */
	@Test
	public void testBlockingRetrieve() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		
		File f = cache.retrieveFile(testHPSStarget1, true);
		assertNotNull(f);
		showFile(f);
		File newf = cache.retrieveFile(testHPSStarget1, false);
		assertNotNull(newf);
		assertTrue(f == newf);									//must be same object

		f = cache.retrieveFile(testHPSStarget2, true);
		assertNotNull(f);
		showFile(f);
		
		//clena up this cache		
		LOG.info("Cache status:" + cache);
		cache.flush();
		LOG.info("Cache status after flush:" + cache);
	}

	/**
	 * This test assures all calls to the same target file trigger only one file
	 * retrieval procedure.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMasiveNonBlockingRetrieve() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		
		File f;
		//grab CacheEntry logger to shut it down for a while		
		Logger LOG_ENTRY = Logger.getLogger(
				PrivilegedAccessor.getInternalClass(StagerCache.class, "CacheEntry"));
		Level old = LOG_ENTRY.getLevel();
		
		boolean firstRun = true;
		final int maxRun = 50000;
		for (int i = 0; i < maxRun; i++) {
			f = cache.retrieveFile(testHPSStarget1, false);
			if (f != null) {
				if (firstRun) {
					firstRun = false;
					LOG.info("File loaded at iteration: " + i);
					//turn off logging
					LOG_ENTRY.setLevel(Level.INFO);
				}
			}

			//let other threads work
			if (i % 100 == 0)
				Thread.sleep(1);
		}
		
		//turn on cacheEntry logging again
		LOG_ENTRY.setLevel(old);
		//log last access
		cache.retrieveFile(testHPSStarget1, false);

		//assure we got the file
		assertTrue(!firstRun);

		//clean up this cache		
		LOG.info("Cache status:" + cache);
		cache.flush();
		LOG.info("Cache status after flush:" + cache);
	}
	
	/**
	 * This test assures all calls to the same target file trigger only one file
	 * retrieval procedure.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMasiveBlockingRetrieve() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		
		//grab CacheEntry logger to shut it down for a while		
		Logger LOG_ENTRY = Logger.getLogger(
				PrivilegedAccessor.getInternalClass(StagerCache.class, "CacheEntry"));
		Level old = LOG_ENTRY.getLevel();
		
		boolean firstRun = true;
		final int maxRun = 10000;

		File f;
		for (int i = 0; i < maxRun; i++) {
			f = cache.retrieveFile(testHPSStarget1, true);
			assertNotNull(f);
			if (firstRun) {
				firstRun = false;
				//turn off logging
				LOG_ENTRY.setLevel(Level.INFO);
			}
		}
		//turn on cacheEntry logging again
		LOG_ENTRY.setLevel(old);
		//log last access
		cache.retrieveFile(testHPSStarget1, false);

		//clena up this cache		
		LOG.info("Cache status:" + cache);
		cache.flush();
		LOG.info("Cache status after flush:" + cache);
	}

	/**
	 * Tests that this implementation can cope with the deletion of its cache
	 * contents from an external source.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExternalDeletion() throws Exception {
		StagerCache cache = new StagerCache(testProps);
		File f = cache.retrieveFile(testHPSStarget1, true);
		assertNotNull(f);
		File newf = cache.retrieveFile(testHPSStarget1, true);
		assertTrue(f == newf);
		
		//delete the file.
		assertTrue(f.delete());
		
		//retrieve it again
		newf = cache.retrieveFile(testHPSStarget1, true);
		assertNotNull(newf);
		
		assertTrue(f != newf); 										//objects should be different
		assertEquals(f.getAbsolutePath(), newf.getAbsolutePath());	//paths should not differ
	}
	
	/**
	 * Tests the case when there are files in the cache structure.
	 * @throws Exception
	 */
	@Test
	public void testFileExistance() throws Exception {
		FileGrabber g = new FileGrabber(testProps);
		RemoteFile hpssFile1 = g.getFileInfo(testHPSStarget1);
		LOG.info("hpssFIle: " + hpssFile1.getLastMod());

		StagerCache cache = new StagerCache(testProps);
		File f = cache.retrieveFile(testHPSStarget1, true);
		long lastModTime = f.lastModified();
		LOG.info("localFile: " + new Date(lastModTime));
		
		//stop the cache without cleaning it
		cache.terminate(true);
		
		//assure we still have the file
		assertTrue(f.exists());
		assertTrue(f.length() > 0);

		//create a file without hpss reference and write enabled (cache handled)
		File dummy = new File(f.getParentFile(), "dummyfile");
		assertTrue(dummy.createNewFile());

		//create a file without hpss reference and write disabled (NON cache handled)
		String dummy_nondel = "dummyfile_nondelet";
		File dummy_outsidecache = new File(f.getParentFile(), dummy_nondel);
		assertTrue(dummy_outsidecache.createNewFile());
		assertTrue(dummy_outsidecache.setWritable(false));
		
		//create a new cache in the same location
		cache = new StagerCache(testProps);
		
		//retrieve file again
		File f2 = cache.retrieveFile(testHPSStarget1, true);
		assertTrue(f != f2 );
		assertEquals(f.getAbsolutePath(), f2.getAbsolutePath());
		assertTrue(!dummy.exists());
		assertTrue(dummy_outsidecache.exists());
		File f3 = cache.retrieveFile(dummy_nondel, true);
		assertTrue(dummy_outsidecache != f3 );
		assertTrue(dummy_outsidecache.equals(f3));
	}
	
	private void showFile(File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		LOG.info("Showing file:" + f.getAbsolutePath());
				
		byte[] buff = new byte[4096];
		int read = 0;
		while ((read = in.read(buff)) > 0) {
			LOG.info(new String(buff, 0, read));
		}
		
		in.close();
	}

}
