package org.esgf.stager.threads;

import org.esgf.stager.Bestman;
import org.esgf.stager.SRMRequestController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import practice.Race2;

public class GetTheFiles implements Runnable {

	private Race2 race;
	private String name;
	private Bestman bestman;
	private SRMRequestController controller;
	
	public GetTheFiles() {
		
	}
	
	public GetTheFiles(SRMRequestController controller) {
		this.controller = controller;
	}
	
	public GetTheFiles(Race2 race, String name, Bestman bestman) {
		this.race = race;
		this.name = name;
		this.bestman = bestman;
	}
	
	
	@Override
	public void run() {
		
				
		race = new Race2();
		
		int hash_id = System.identityHashCode(race);
		
		String thread_id = Integer.toString(hash_id);
		
		
		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		mockRequest.addParameter("thread_id", thread_id);
		
		controller.addSRMRequest(mockRequest, mockResponse);
		
		
		/*
		
		*/
		
		/*
		
		try {
			//this.race.getReadyToRace();
			
			int place = this.race.crossFinishLine(this.name);
			
			bestman.synchroTest(this.name);
			
			
		} catch(InterruptedException ie) {
			System.out.println(name + " apparently broke a leg and wont be finishing");
		}
		*/
		
		
		
		/*
		//create new Controller here
		SRMRequestController sc = new SRMRequestController();
		
		int hash_id = System.identityHashCode(sc);
		
		String thread_id = Integer.toString(hash_id);
		
		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		mockRequest.addParameter("thread_id", thread_id);
		
		//sc.addSRMRequest(mockRequest, mockResponse);
		
		Bestman b = new Bestman();
		
		try {
			b.synchroTest(thread_id);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
	}

}
