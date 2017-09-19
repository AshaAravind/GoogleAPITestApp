package com.gtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;

/**
 * Servlet implementation class CallBackServlet
 */
@WebServlet("/callback")
public class CallBackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CallBackServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (request.getParameter("error") != null) {
			response.getWriter().println(request.getParameter("error"));
			return;
		}

		// google returns a code that can be exchanged for a access token
		String code = request.getParameter("code");

		// get the access token by post to Google
		String body = post("https://accounts.google.com/o/oauth2/token",
				ImmutableMap.<String, String> builder().put("code", code).put("client_id", GTestUtil.getConfigValue("clientId"))
						.put("client_secret", GTestUtil.getConfigValue("clientSecret")).put("redirect_uri", "http://localhost:8080/GoogleAPITestApp/callback")
						.put("grant_type", "authorization_code").build());
		
		JSONObject jsonObject = null;

		// get the access token from json and request info from Google
		try {
			jsonObject = (JSONObject) new JSONParser().parse(body);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + body);
		}

		// google tokens expire after an hour, but since we requested offline
		// access we can get a new token without user involvement via the
		// refresh token
		String accessToken = (String) jsonObject.get("access_token");
		String refreshToken = (String) jsonObject.get("refresh_token");

		// you may want to store the access token in session
		request.getSession().setAttribute("access_token", accessToken);
		request.getSession().setAttribute("refresh_token", refreshToken);
		
		RequestDispatcher rd = request.getRequestDispatcher("tokentest");
		rd.forward(request,response);
		
	}
	
	// makes a GET request to url and returns body as a string
		public String get(String url) throws ClientProtocolException, IOException {
			return execute(new HttpGet(url));
		}

		// makes a POST request to url with form parameters and returns body as a
		// string
		public String post(String url, Map<String, String> formParameters) throws ClientProtocolException, IOException {
			HttpPost request = new HttpPost(url);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			for (String key : formParameters.keySet()) {
				nvps.add(new BasicNameValuePair(key, formParameters.get(key)));
			}

			request.setEntity(new UrlEncodedFormEntity(nvps));

			return execute(request);
		}

		// makes request and checks response code for 200
		private String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
			HttpClient httpClient = new DefaultHttpClient(); 
			HttpResponse response = httpClient.execute(request);

			HttpEntity entity = response.getEntity();
			String body = EntityUtils.toString(entity);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
			}

			return body;
		}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
