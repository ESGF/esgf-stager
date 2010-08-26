package esgf.node.stager.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import esgf.node.stager.io.FileGrabber.Callback;
import esgf.node.stager.utils.ExtendedProperties;
import esgf.node.stager.utils.Misc;
import esgf.node.stager.utils.MiscUtils;
import esgf.node.stager.utils.PrivilegedAccessor;



/**
 * Tests the HPSSGrabber class
 *
 * @author Estanislao Gonzalez
 */
public class TestFileGrabber {
    private static final Logger LOG = Logger.getLogger(TestFileGrabber.class);

    private static File tmpDir;
    private static String target1;
    private static String dirTaget1;
    private static String fileTarget1;
    private static String target2;
    private static ExtendedProperties testProps;

    @BeforeClass
    public static void setup() throws Exception {
        tmpDir = File.createTempFile("testHPSS", "");
        assertTrue(tmpDir.delete());
        if (!tmpDir.mkdir())
            fail("Could not create temporary directory.");
        LOG.info("Tmp dir created in: " + tmpDir.getAbsolutePath());
        tmpDir.deleteOnExit();

        target1 = "/default_style.css";
        dirTaget1 = "/";
        fileTarget1 = "default_style.css";
        target2 = "/DOCS/docs.old/zen.README";

        //setup the grabber
        testProps = new ExtendedProperties();
        testProps.put("remoteConnectorFactory", "esgf.node.stager.io.connectors.FTPConnectorFactory");
        testProps.put("ftp.serverName", "ftp3.de.postgresql.org");
        testProps.put("ftp.serverPort", "21");
        testProps.put("ftp.serverRootDirectory", "pub/");
        testProps.put("ftp.userName", Misc.transform(false, "Anonymous"));
        testProps.put("ftp.userPassword", Misc.transform(false, "none"));
    }

    @After
    public void cleanAfterEachTest() {
        MiscUtils.emptyDir(tmpDir, LOG);
    }

    /**
     * Checks the properties are properly read and an exception is thrown if not
     * properly set.
     *
     * @throws Exception
     */
    @Test
    public void testProperties() throws Exception {
        new FileGrabber(testProps);
        //no exception should have been thrown.

        try {
            new FileGrabber(null);
            fail("Should have thrown an exception.");
        } catch (StagerException e) {
            // ok!
        }
    }

    /**
     * tests the contents of the HPSSFile when retrieved from ftp.
     * @throws Exception
     */
    @Test
    public void testHPSSFile() throws Exception {
        FileGrabber g = new FileGrabber(testProps);
        try {
            g.getFileInfo("/nonexisting");
            fail("Shouldn't have been there.");
        } catch (FileNotFoundException e) {
            // ok
        }

        RemoteFile hf = g.getFileInfo(target1);
        assertEquals(target1, hf.getTarget());
        assertEquals("/", hf.getDirectory());
        assertEquals(target1.substring(1), hf.getFilename());
        assertEquals(1512, hf.getSize());
        assertEquals(new Date(1031330712000L), hf.getLastMod());
        assertTrue(hf.exists());

        RemoteFile hf2 = g.getFileInfo(target1);
        assertTrue(hf != hf2);			//differente objects
        assertTrue(hf.equals(hf2));		//same content

        //check other paths
        hf = (RemoteFile)PrivilegedAccessor.instantiate(RemoteFile.class, "abc.nc");
        assertEquals("abc.nc", hf.getTarget());
        assertEquals("/", hf.getDirectory());
        assertEquals("abc.nc", hf.getFilename());

        hf = (RemoteFile)PrivilegedAccessor.instantiate(RemoteFile.class, "/abc.nc");
        assertEquals("/abc.nc", hf.getTarget());
        assertEquals("/", hf.getDirectory());
        assertEquals("abc.nc", hf.getFilename());

        hf = (RemoteFile)PrivilegedAccessor.instantiate(RemoteFile.class, "/some/where/abc.nc");
        assertEquals("/some/where/", hf.getDirectory());
        assertEquals("abc.nc", hf.getFilename());

        //now check the other params

        RemoteFile ff = g.getFileInfo(target1);
        assertEquals(dirTaget1, ff.getDirectory());
        assertEquals(fileTarget1, ff.getFilename());

        //this must be adapted when another test server is used.
        assertEquals(1512, ff.getSize());
        assertEquals(new Date(1031330712000L), ff.getLastMod());
    }

    /**
     * grab a file from ftp.
     * @throws Exception
     */
    @Test
    public void testGrabbing() throws Exception {
        String[] files = new String[] {target1, target2};
        for (int i = 0; i < files.length; i++) {
            FileGrabber g = new FileGrabber(testProps);
            RemoteFile hf = g.getFileInfo(files[i]);
            File f = new File(tmpDir, hf.getFilename());
            assertTrue(f.createNewFile());
            f.deleteOnExit();

            LOG.info("Tmp file created: " + f.getAbsolutePath());

            g.grabLater(hf, f, new Callback(){
                public void done(File localFile) {
                    LOG.info("Done with: " + localFile.getAbsolutePath());
                    synchronized (TestFileGrabber.this) {
                        TestFileGrabber.this.notify();
                    }
                }
            });

            //should not be there
            assertEquals(0, f.length());

            //wait until it is
            synchronized (this) {
                //...but don't wait for ever.
                this.wait(10000);
            }

            //file should be there
            assertTrue(f.length() > 0);
            //check we got what we expected
            assertEquals(hf.getFilename(), f.getName());
            assertEquals(hf.getSize(), f.length());
            assertEquals(hf.getLastMod(), new Date(f.lastModified()));


            MiscUtils.showFile(f, LOG);
        }

    }

    @Test
    public void testMassiveGrabbing() throws Exception {
        //well we need a test server that allow multiple connections so this test will be "useful"
        File[] tmpFiles = new File[3];
        for (int i = 0; i < tmpFiles.length; i++) {
            tmpFiles[i] = File.createTempFile("hpssCreationTest", null, tmpDir);
            tmpFiles[i].deleteOnExit();

            System.out.println("Tmp file created: " + tmpFiles[i].getAbsolutePath());

            FileGrabber g = new FileGrabber(testProps);

            g.grabLater(g.getFileInfo(target1), tmpFiles[i], null);
            assertEquals(0, tmpFiles[i].length());

        }

        //the timing here might not work always
        Thread.sleep(3000);
        for (int i = 0; i < tmpFiles.length; i++) {
            assertTrue(tmpFiles[i].length() > 0);
        }

    }

    @Test
    public void testGrabAndWait() throws Exception {
        File f = File.createTempFile("hpssCreationTest", null, tmpDir);
        f.deleteOnExit();

        LOG.info("Tmp file created: " + f.getAbsolutePath());

        FileGrabber g = new FileGrabber(testProps);

        assertTrue(f.length() == 0);
        g.grabAndWait(g.getFileInfo(target1), f);
        assertTrue(f.length() > 0);

        MiscUtils.showFile(f, LOG);
    }




}
