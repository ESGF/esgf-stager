package org.esgf.stager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

//import lbnl.legacy.SrmClientSoapBindingImpl;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.esgf.singleton2.Bestman;
import org.esgf.json.JSONArray;
import org.esgf.json.JSONException;
import org.esgf.json.JSONObject;
import org.esgf.json.XML;
import org.esgf.stager.GetBestman;
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
	
	private String file_request_type;
	private String openid;
	
	public static boolean firstThread = true;
	
	
	public static void main(String [] args) {
		
		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        
        //mockRequest.addParameter("dataset_id", "ana4MIPs.NASA-GMAO.MERRA.atmos.mon.v20121221|esgdata1.nccs.nasa.gov");
        
		
		
		SynchronizedSRMRequestController sc = new SynchronizedSRMRequestController();
        
		JSONObject json = new JSONObject();
		try {
			json.put("id", "value");
			json.append("file_urls", "url1");
			json.append("file_urls", "url2");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        //sc.addSynchronizedSRMRequestBody(mockRequest, mockResponse, json.toString());
		
		
		
	}
	
	
	
	@RequestMapping(method=RequestMethod.POST, value="/synchronizedsrmrequestBody") 
	public @ResponseBody String addSynchronizedSRMRequestBody(HttpServletRequest request,
									final HttpServletResponse response,
									@RequestBody String content,
									String thread_id) {
		
		
		System.out.println("in posted request");
		
		//grab params
		System.out.println("Entering addSynchronizedSRMRequest for thread_id: " + thread_id + "\n");

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


		System.out.println("CONTENT\n\n\n" + content);
		
		
		String [] file_urls = null;
		try {
			JSONObject jObj = new JSONObject(content);
			Iterator<String> keysIter = jObj.keys();
		    while ( keysIter.hasNext() ){
				String key = keysIter.next();
				System.out.println("KEY: " + key);
				if(key.equals("file_urls")) {
					JSONArray j = new JSONArray((jObj.get(key)).toString());
					
					System.out.println("SIZE: " + j.length());
					file_urls = processfileURLs(j);
				  
				}
		    }
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String response_message = "";
		
		try {
			Bestman bestman = Bestman.getInstance(thread_id);
			if(isProduction) {
				
				response_message = bestman.get(file_urls,thread_id);

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


		//System.out.println("\n\n\nThread id: " + thread_id + " Returning...");
		//System.out.println("response_message\n" + response_message + "\n\n");
		
		/*
		if(srm_response == null) {
			return "<srm_response>" + Utils.responseMessage + "</srm_response>";
		} else {

			//System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
			return new XmlFormatter().format(srm_response.toXML()) + "\n";
		}
		*/
		//System.out.println(new XmlFormatter().format(response_message) + "\n");
		//return response_message;
		System.out.println("RESPONSE:\n\n" + new XmlFormatter().format(response_message) + "\n");
		return response_message;
	}

	/*
	if(debugFlag) {
		System.out.println("running in production?..." + isProduction);
		for(int i=0;i<file_urls.length;i++) {
			System.out.println("file_url: " + i + " " + file_urls[i] + " thread_id: " + thread_id);
		}
	}
	*/
	

	
	
	
	
	
	
	
	
	private String [] processfileURLs(JSONArray jsonArray) {
		
		String [] file_urls = new String[jsonArray.length()];
		
		System.out.println("process array length " + jsonArray.length());
		for(int i=0;i<jsonArray.length();i++) {
			try {
				System.out.println("json: " + i + " " + (String)jsonArray.get(i));
				file_urls[i] = (String)jsonArray.get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		return file_urls;
	}
	
	
	@RequestMapping(method=RequestMethod.POST, value="/synchronizedsrmrequest")
	//public ModelAndView addEmployee(@RequestBody String body) {
	public @ResponseBody String addSynchronizedSRMRequest(HttpServletRequest request,
														  final HttpServletResponse response,
														  String thread_id) {
		
		
		for(Object key : request.getParameterMap().keySet() ) {
			//System.out.println("Key: " + (String) key);
		}

		//grab params
		System.out.println("Entering addSynchronizedSRMRequest for thread_id: " + thread_id + "\n");

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


		/*
		if(debugFlag) {
			System.out.println("running in production?..." + isProduction);
			for(int i=0;i<file_urls.length;i++) {
				System.out.println("file_url: " + i + " " + file_urls[i] + " thread_id: " + thread_id);
			}
		}
		*/
		


		String response_message = "";
		
		try {
			Bestman bestman = Bestman.getInstance(thread_id);
			if(isProduction) {
				
				response_message = bestman.get(file_urls,thread_id);

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


		//System.out.println("\n\n\nThread id: " + thread_id + " Returning...");
		//System.out.println("response_message\n" + response_message + "\n\n");
		
		/*
		if(srm_response == null) {
			return "<srm_response>" + Utils.responseMessage + "</srm_response>";
		} else {

			//System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
			return new XmlFormatter().format(srm_response.toXML()) + "\n";
		}
		*/
		System.out.println(new XmlFormatter().format(response_message) + "\n");
		return response_message;
	}
	
	
	
}




