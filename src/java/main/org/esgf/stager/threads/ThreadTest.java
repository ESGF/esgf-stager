package org.esgf.stager.threads;

import org.esgf.stager.SRMRequestController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ThreadTest {

	public static void main(String [] args) {
		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		
		SRMRequestController sc = new SRMRequestController();
		
		sc.addSRMRequest(mockRequest, mockResponse);
		
	}
	
	

	
}
