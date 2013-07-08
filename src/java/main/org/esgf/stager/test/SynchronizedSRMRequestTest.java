package org.esgf.stager.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.esgf.json.JSONException;
import org.esgf.json.JSONObject;

public class SynchronizedSRMRequestTest {

	public static void main(String [] args) {
		
		String [] file_urls = {
				"srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-10.nc"
				//"url2"
			};
			
			
		queryESGSRM(file_urls);
		
	}
	
	private static String queryESGSRM(String [] file_urls) {
		
		System.out.println("\n\n---In new query SRM---\n\n");
		
		String queryString = "";
		int file_length = file_urls.length;
        
		
		
		//add the urls
        for(int i=0;i<file_length;i++) {

            System.out.println("fileurls: " + i + " " + file_urls[i]);
            //System.out.println("queryString: " + queryString);
            //System.exit(0);
            if(i == 0 && file_urls.length == 1) {
                queryString += "url=";
                //unencodedQueryString += "url=";
                queryString += encode(file_urls[i]);
                
            } 
            else if(i == 0 && file_urls.length != 1) {
                queryString += "url=";
                //unencodedQueryString += "url=" + (file_urls[i]) + "&";
                queryString += encode(file_urls[i]) + "&";
            }
            else if(i == file_urls.length - 1) {
                queryString += "url=";
                //unencodedQueryString += "url=" + (file_urls[i]);
                queryString += encode(file_urls[i]);
            } 
            else {
                queryString += "url=";
                //unencodedQueryString += "url=" + (file_urls[i]) + "&";
                queryString += encode(file_urls[i]) + "&";
            }
        }

		System.out.println("QueryString -> " + queryString);
		
		
		// create an http client
        HttpClient client = new HttpClient();

        //note: hardcoded
        PostMethod method = new PostMethod("http://esg.ccs.ornl.gov:8080/esgf-stager/service/synchronizedsrmrequestBody?");
        
        method.setQueryString(queryString);
        
        JSONObject json = new JSONObject();
		try {
			json.put("id", "value");
			for(int i=0;i<file_urls.length;i++) {
				json.append("file_urls", file_urls[i]);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			StringRequestEntity requestEntity = new StringRequestEntity(
				    json.toString(),
				    "application/json",
				    "UTF-8");
			
			method.setRequestEntity(requestEntity);
			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		String responseBody = "";
		
		
		System.out.println("trying...");
        try {
            // execute the method
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("statusCode: " + statusCode);
            }

            // read the response
            responseBody = method.getResponseBodyAsString();
        } catch (HTTPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
		
		
		
		System.out.println("ResponseBody: " + responseBody);

		System.out.println("\n\n---End new query SRM---\n\n");
		
		return responseBody;
	}
	


	public static String encode(String queryString) {
    
	    try {
	        queryString = URLEncoder.encode(queryString,"UTF-8").toString();
	    } catch (UnsupportedEncodingException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    }
    
    return queryString;
}




}
