package org.esgf.singleton2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

//import lbnl.legacy.SrmClientSoapBindingImpl;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.esgf.singleton2.Bestman;
import org.esgf.singleton2.GetBestman;
import org.esgf.stager.utils.Utils;
import org.esgf.stager.utils.XmlFormatter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class SynchronizedSRMRequestController {

	
	private static boolean isProduction = true;

	private static boolean serviceTest = false;
	
	private static boolean oldImpl = false;
	
	private static boolean debugFlag = true;
	
	private Bestman bestman;
	
	private String file_request_type;
	private String openid;
	
	public static boolean firstThread = true;
	
	
	
	@RequestMapping(method=RequestMethod.POST, value="/synchronizedsrmrequest")
	//public ModelAndView addEmployee(@RequestBody String body) {
	public @ResponseBody String addSRMRequest3(HttpServletRequest request,final HttpServletResponse response,String thread_id) {
	
	
		/*
		for(Object key : request.getParameterMap().keySet() ) {
			System.out.println("Key: " + (String) key);
		}
		*/
		
		for(Object key : request.getParameterMap().keySet() ) {
			System.out.println("Key: " + (String) key);
		}



		//grab params
		System.out.println("In HTTP POST: addSRMRequest3 for thread_id: " + thread_id + "\n\n\n");

		String openid = request.getParameter("openid");
		if(openid == null) {
			this.openid = "jfharney";
		} else {
			this.openid = openid;
		}

		String length = request.getParameter("length");
		if(length == null) {
			length = "1";
		}

		String file_request_type = request.getParameter("file_request_type");
		if(file_request_type == null) {
			this.file_request_type = "http";
			//file_request_type = "http";
		} else if(file_request_type.equals("http")){
			this.file_request_type = "http";
		} else if(file_request_type.equals("gridftp")) {
			this.file_request_type = "gridftp";
		} else if(file_request_type.equals("globusonline")) {
			this.file_request_type = "globusonline";
		} else {
			this.file_request_type = "http";
		}



		String [] file_urls = null;
		
		
		if(length.equals("1")) {
			file_urls = new String[1];
			String file_url = request.getParameter("url");
			//System.out.println("file_url: " + file_url);
			file_urls[0] = file_url;
		}
		else {
			file_urls = new String[Integer.parseInt(length)];
			String [] urls = request.getParameterValues("url");
			for(int i=0;i<urls.length;i++) {
				file_urls[i] = urls[i];
			}
		}



		if(debugFlag) {
			System.out.println("running in production?..." + isProduction);
			for(int i=0;i<file_urls.length;i++) {
				System.out.println("file_url: " + i + " " + file_urls[i] + " thread_id: " + thread_id);
			}
		}


		try {
			Bestman3 bestman = Bestman3.getInstance(thread_id);
			if(isProduction) {
				
				bestman.get(file_urls);

				//send the response from bestman
				//this.srm_response = this.bestman.getSrm_response();

				//System.out.println("\nSRM RESPONSE\n\n" + new XmlFormatter().format(srm_response.toXML()) + "\n\n\n");
			} else {
				//srm_response = SRMUtils.simulateSRM(file_urls);
			}
		} catch(Exception e) {
			System.out.println("Exception triggered in SRMRequestController");
			e.printStackTrace();
		}


		System.out.println("\n\n\nThread id: " + thread_id + " Returning...");

		/*
		if(srm_response == null) {
			return "<srm_response>" + Utils.responseMessage + "</srm_response>";
		} else {

			//System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
			return new XmlFormatter().format(srm_response.toXML()) + "\n";
		}
		*/
		
		return "done3";
	}
	
	
	
}




