package org.esgf.stager.threads;

import org.esgf.stager.Bestman;
import org.esgf.stager.SRMRequestController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import practice.Horse2;
import practice.Race2;

public class StagerTestThreads {

	public static void main(String [] args) {
		
		Race2 race = new Race2();
		
		Bestman b = new Bestman();
		
		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();


		//create new Controller here
		SRMRequestController sc = new SRMRequestController();
		
		//final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		//final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		//sc.addSRMRequest(mockRequest, mockResponse);
		
		/*
		Runnable getFiles = new GetTheFiles(race,"Mine That Bird",b);
		Runnable getFilesAgain = new GetTheFiles(race,"Big Brown",b);
		*/
		
		/*
		Runnable getFiles = new GetTheFiles();
		Runnable getFilesAgain = new GetTheFiles();
		*/

		Runnable getFiles = new GetTheFiles(sc);
		Runnable getFilesAgain = new GetTheFiles(sc);
		
		new Thread(getFiles).start();
		new Thread(getFilesAgain).start();
		
		
	}
	
}
