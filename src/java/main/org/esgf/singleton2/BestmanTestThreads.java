package org.esgf.singleton2;

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
	
	private static String THREAD1_NUM_FILES_REQUESTED = "2";
	private static String THREAD2_NUM_FILES_REQUESTED = "2";
	
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

		System.out.println("size: " + urlList.size());
		
		Random randNum = new Random();
		
		String [] thread_urls = new String[Integer.parseInt(NUM_THREADS)];
		int numFilesRequest = 2;
		for(int i=0;i<Integer.parseInt(NUM_THREADS);i++) {
		
				//thread_urls[i] = new String[2];
				
		
		}
		/*
		for(int i=0;i<Integer.parseInt(NUM_THREADS);i++) {
			thread_urls[i] = new String [numFilesRequest];
		}
		*/
		/*
		String [] thread1_urls = new String [Integer.parseInt(THREAD1_NUM_FILES_REQUESTED)]; 
		String [] thread2_urls = new String [Integer.parseInt(THREAD2_NUM_FILES_REQUESTED)]; 
		*/
		/*
		for(int i=0;i<Integer.parseInt(THREAD1_NUM_FILES_REQUESTED);i++) {
			int num = randNum.nextInt(40);
			thread1_urls[i] = urlList.get(num);
		}
		thread1MockRequest.addParameter("url", thread1_urls);
		thread1MockRequest.addParameter("length", Integer.toString(thread1_urls.length));
		
		
		for(int i=0;i<Integer.parseInt(THREAD2_NUM_FILES_REQUESTED);i++) {
			int num = randNum.nextInt(40);
			thread2_urls[i] = urlList.get(num);
		}
		thread2MockRequest.addParameter("url", thread2_urls);
		thread2MockRequest.addParameter("length", Integer.toString(thread2_urls.length));
		
		//thread 1
        Runnable getBestman = new GetBestman(thread1MockRequest,thread1MockResponse);

        //thread 2
        Runnable getBestman2 = new GetBestman(thread2MockRequest,thread2MockResponse);

        new Thread(getBestman).start();
        new Thread(getBestman2).start();
        */
/*
		mockRequest.addParameter("url", url);
		mockRequest.addParameter("length", length);
		
		//thread 1
        Runnable getBestman = new GetBestman(mockRequest,mockResponse);

        //thread 2
        Runnable getBestman2 = new GetBestman(mockRequest,mockResponse);
        
        //thread 3
        Runnable getBestman3 = new GetBestman(mockRequest,mockResponse);
                 
        // Call for the code in the method run to execute
                 
        new Thread(getBestman3).start();
*/        
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
