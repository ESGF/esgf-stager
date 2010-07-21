package esgf.node.stager.web;


import static esgf.node.stager.web.StagerFilter.PROP_PRE;
import static esgf.node.stager.web.StagerFilter.PROP_SERV;
import static esgf.node.stager.web.StagerFilter.PROP_SERV_PATTERN;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import esgf.node.stager.utils.ExtendedProperties;


public class TestStagerFilter {

	private static Properties prop;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		prop = new Properties();
		prop.setProperty("filter.service", "dodS,fileServer");
		prop.setProperty("filter.dodS.pattern", "(.*).html");
		prop.setProperty("filter.fileServer.pattern", "(.*)");
		
	}

	private ExtendedProperties eprop;

	@Before
	public void setUp() throws Exception {
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
			System.out.print(test[i] + " : ");
			Matcher m = p.matcher(test[i]);
			if (m.find()) {
				System.out.println(m.group(1));
			} else {
				System.out.println("Not found");
			}
		}
	}

}
