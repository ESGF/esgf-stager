package esgf.node.stager.web;


import static esgf.node.stager.web.StagerFilter.PROP_PRE;
import static esgf.node.stager.web.StagerFilter.PROP_SERV;
import static esgf.node.stager.web.StagerFilter.PROP_SERV_PATTERN;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import esgf.node.stager.io.StagerCache;
import esgf.node.stager.utils.ExtendedProperties;
import esgf.node.stager.utils.MiscUtils;
import esgf.node.stager.utils.PrivilegedAccessor;


public class TestStagerFilter {
	private static final Logger LOG = Logger.getLogger(TestStagerFilter.class);
	private static final boolean DEBUG = LOG.isDebugEnabled();
	private ExtendedProperties eprop;
	private static Properties prop;
	private static File tmpDir;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		prop = new Properties();
		prop.setProperty("filter.service", "dodS,fileServer");
		prop.setProperty("filter.dodS.pattern", "(.*).html");
		prop.setProperty("filter.fileServer.pattern", "(.*)");
		
		try {
			//create a tmp dir
			tmpDir = File.createTempFile("test_cache", "");
			assertTrue("Could not delete temp file", tmpDir.delete());
			tmpDir.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not setup environment for test: " + e.getMessage());
		}
	}
	
	@AfterClass
	public static void cleanup() {
		MiscUtils.deleteAll(tmpDir, LOG);
	}


	@Before
	public void setUp() throws Exception {
		MiscUtils.emptyDir(tmpDir, LOG);
		eprop = new ExtendedProperties(prop);
	}
	
	@Test
	public void testPatterns() throws Exception {
		String[] test = new String[] {
				"/some/where/over/the_rainbow.nc.html",
				"/some/where/else.nc"
		};
		
		String[] services = ((String)eprop.getCheckedProperty(PROP_PRE + PROP_SERV)).split(",");
		for (int i = 0; i < services.length; i++) {
			String pat = ((String)eprop.getCheckedProperty(PROP_PRE + services[i] + PROP_SERV_PATTERN));
			Pattern p = Pattern.compile(pat);
			if (DEBUG) LOG.debug(test[i] + " : ");
			
			Matcher m = p.matcher(test[i]);
			if (m.find()) {
				LOG.info(m.group(1));
			} else {
				LOG.info("Not found");
			}
		}
	}
	
	@Test
	public void testCreation() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		StagerFilter sf = new StagerFilter();
		
		try {
			sf.init(config);
			fail("Should have failed because of empty properties");			
		} catch (ServletException e) {
			//ok
			LOG.info("Expected exception: " + e.getMessage());
			assertTrue(e.getMessage().matches("[Mm]issing.*configurationFile.*"));
		}
		
		Properties props = config.getProps();
		props.put("configurationFile", "nonExisting");
		
		try {
			sf.init(config);
			fail("Should have failed because of missing properties file");			
		} catch (ServletException e) {
			//ok
			LOG.info("Expected exception: " + e.getMessage());
			assertTrue(e.getMessage().matches("[Mm]issing.*nonExisting.*"));
		}
		
		
		//read basic properties
		ExtendedProperties ep = new ExtendedProperties("resources/test/stager.properties");
		
		//create tmp dir for cache
		File cacheDir = File.createTempFile("cache", "", tmpDir);
		cacheDir.delete();
		cacheDir.mkdir();
		ep.put(PrivilegedAccessor.getField(StagerCache.class, "PROP_LOCAL_DIR"),
				cacheDir.getAbsolutePath());
		
		//write properties in tmp file
		File tmpPropFile = File.createTempFile("stager.test", "properties", tmpDir); 
		ep.write(tmpPropFile.getAbsolutePath());
		
		props.put("configurationFile", tmpPropFile.getAbsolutePath());
		config.getServletContext().setContextPath("");
		sf.init(config);
		

	}

}
