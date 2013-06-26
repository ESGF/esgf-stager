package org.esgf.stager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class BestmanTestThreads {

	private static String PREFIX = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?";
	private static String SFN = "SFN=mss://esg2-sdnl1.ccs.ornl.gov";
	private static String DIRECTORY = "//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/";
	
	private static String NUM_THREADS = "3";
	
	private static String NUM_FILES_REQUESTED = "2";
	
	public static void main(String[] args){

		MockHttpServletRequest [] threadMockRequest = new MockHttpServletRequest[Integer.parseInt(NUM_THREADS)];
		MockHttpServletResponse [] threadMockResponse = new MockHttpServletResponse[Integer.parseInt(NUM_THREADS)];
		
		for(int i=0;i<Integer.parseInt(NUM_THREADS);i++) {
			threadMockRequest[i] = new MockHttpServletRequest();
			threadMockResponse[i] = new MockHttpServletResponse();
		}
		
		/*
		final MockHttpServletRequest thread1MockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse thread1MockResponse = new MockHttpServletResponse();
		
		final MockHttpServletRequest thread2MockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse thread2MockResponse = new MockHttpServletResponse();
		*/
		
		
		
		List<String> urlList = getURLList();

		
		Random randNum = new Random();
		
		//String [][] thread_urls = new String[Integer.parseInt(NUM_THREADS)][Integer.parseInt(NUM_FILES_REQUESTED)];
		int numFilesRequest = 2;
		for(int i=0;i<Integer.parseInt(NUM_THREADS);i++) {
		
			String [] thread_urls = new String [Integer.parseInt(NUM_FILES_REQUESTED)];
			for(int j=0;j<Integer.parseInt(NUM_FILES_REQUESTED);j++) {
				
				int num = randNum.nextInt(40);
				thread_urls[j] = urlList.get(num);
			
			}

			threadMockRequest[i].addParameter("url", thread_urls);
			threadMockRequest[i].addParameter("length", Integer.toString(thread_urls.length));
		
		}
		
		//call each thread
		for(int i=0;i<Integer.parseInt(NUM_THREADS);i++) {
		
			System.out.println("Running thread: " + i);
			for(int j=0;j<Integer.parseInt(NUM_FILES_REQUESTED);j++) {
				System.out.println("\turl: " + threadMockRequest[i].getParameterValues("url")[j]);
			}
			
			Runnable getBestman = new GetBestman(threadMockRequest[i],threadMockResponse[i]);
			new Thread(getBestman).start();
			
		}
		
		
		
    }
	
	public static List<String> getURLList() {
		
		List<String> urlList = new ArrayList<String>();
		String url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1978-09.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1978-10.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1978-11.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1978-12.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-01.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-02.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-03.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-04.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-05.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-06.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-07.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-08.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-09.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-10.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-11.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1979-12.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-01.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-02.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-03.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-04.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-05.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-06.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-07.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-08.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-09.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-10.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-11.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1980-12.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-01.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-02.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-03.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-04.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-05.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-06.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-07.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-08.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-09.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-10.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-11.nc";
		urlList.add(url);
		url = PREFIX + SFN + DIRECTORY + "t341f02.FAMIPr.cam2.h0.1981-12.nc";
		urlList.add(url);
		
		return urlList;
	}
	
}


/*
 * String [] urls = {
				"srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-10.nc",
				"srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1979-06.nc"
			};

		String length = "2";
		*/
