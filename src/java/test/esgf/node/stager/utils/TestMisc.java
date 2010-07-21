package esgf.node.stager.utils;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;


public class TestMisc {
	private static final Logger LOG = Logger.getLogger(TestMisc.class);
	@Test
	public void testTransform() {
		String pass = "abcde12345!\"§$%äöüßµ\u12332";
		LOG.info("pass:" + pass);
		
		String transf = Misc.transform(false, pass);
		assertTrue(!pass.equals(transf));
		LOG.info("transformed:" + transf);
		
		String back = Misc.transform(true, transf);
		LOG.info("original:" + back);
		
		//and back
		assertTrue(pass.equals(back));
	}

}
