package esgf.node.stager.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esgf.node.stager.io.StagerException;

public class TestExtendedProperties {
    private static final Logger LOG = Logger
            .getLogger(TestExtendedProperties.class);
    private static File propFile;
    private static Properties props;
    private static String[] propKeys;
    private static String[] propVals;

    @BeforeClass
    public static void setup() throws Exception {
        propKeys = new String[] { "key1", "key2", "test1.something",
                "test1.some" };
        propVals = new String[] { "value1", "value2", "http://something/sdasd",
                "!$\"%WERASDF§$" };
        props = new Properties();

        final StringBuilder propString = new StringBuilder();
        for (int i = 0; i < propVals.length; i++) {
            props.put(propKeys[i], propVals[i]);
            propString.append(propKeys[i]).append('=');
            propString.append(propVals[i]).append('\n');
        }
        LOG.debug("Property file with content:\n" + propString.toString());

        propFile = File.createTempFile("test", "properties");
        Writer out = new OutputStreamWriter(new FileOutputStream(propFile),
                "UTF-8");
        out.write(propString.toString());
        out.close();
    }

    @AfterClass
    public static void teardown() {
        if (!propFile.delete()) LOG.warn("can't delete property file.");
    }

    @Test
    public void testCreation() throws Exception {
        // turn on debugging
        Logger.getLogger(ExtendedProperties.class).setLevel(Level.DEBUG);

        // empty
        ExtendedProperties ep1 = new ExtendedProperties();
        assertTrue(ep1.isEmpty());

        // form properties
        ExtendedProperties ep2 = new ExtendedProperties(props);
        assertEquals(props.size(), ep2.size());
        assertEquals(props.entrySet(), ep2.entrySet());

        // form properties but filter by prefix
        ExtendedProperties ep4 = new ExtendedProperties("test1", props);
        assertEquals(2, ep4.size());
        assertEquals(ep4.get(propKeys[2]), propVals[2]);
        assertEquals(ep4.get(propKeys[3]), propVals[3]);
        // from file
        ExtendedProperties ep3 = new ExtendedProperties(propFile
                .getAbsolutePath());
        assertEquals(props.size(), ep3.size());
        assertEquals(props.entrySet(), ep3.entrySet());
    }

    @Test
    public void testRetrieval() throws Exception {
        // turn off debugging
        Logger.getLogger(ExtendedProperties.class).setLevel(Level.INFO);

        ExtendedProperties ep = new ExtendedProperties(props);

        String val = "Yikes";
        assertEquals(propVals[0], ep.getCheckedProperty(propKeys[0], val));

        try {
            ep.getCheckedProperty("non-existent");
            fail("Should have thrown an exception.");
        } catch (StagerException e) {
            // ok
        }

        //test different types
        assertEquals(val, ep.getCheckedProperty("non-existent", val));
        assertEquals(new Integer(1), ep.getCheckedProperty("non-existent", 1));
        assertEquals(new Long(1), ep.getCheckedProperty("non-existent", 1L));
        assertEquals(new Float(1.0f), ep.getCheckedProperty("non-existent", 1.0f));
        assertEquals(new Double(1.0), ep.getCheckedProperty("non-existent", 1.0));
        assertEquals(Boolean.TRUE, ep.getCheckedProperty("non-existent", true));

    }

    @Test
    public void testWriteToFile() throws Exception {
        //put some properties
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("key1", "value1");
        ep.put("key2", "value2");

        //create temporary file
        File f = File.createTempFile("testWriteToFile", "properties");

        //write props down
        ep.write(f.getAbsolutePath());
        MiscUtils.showFile(f, LOG);

        //retrieve them again and check the are the same we put
        ExtendedProperties ep2 = new ExtendedProperties(f.getAbsolutePath());
        assertEquals(ep, ep2);

        //clean up
        f.delete();
    }
}
