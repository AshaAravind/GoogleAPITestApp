package com.gtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.common.collect.ImmutableMap;

/**
 * Servlet implementation class CallBackServlet
 */
@WebServlet("/tokentest")
public class TokenTestServlet extends HttpServlet {
	private static final String DESCRIPTION = "description";
	private static final String SUMMARY = "summary";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String PATH_EVENTDETAILS = "eventdetails.txt";
	private static final String PATH_CONFIG_PROPERTIES = "src/main/resources/config.properties";
	private static final String END_DATE = "endDate";
	private static final String ST_DATE = "stDate";
	private static final String LOCATION = "location";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(TokenTestServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TokenTestServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (request.getParameter("error") != null) {
			response.getWriter().println(request.getParameter("error"));
			return;
		}
		
		if(GTestUtil.getConfigValue("clientSecret").equals("") || GTestUtil.getConfigValue("clientSecret").equals("")){
			LOG.error("Please add config details in the config.properties file");
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Client id/ secret not configured");
			return;
		}

		if (request.getSession().getAttribute(ACCESS_TOKEN) != null) {
			String accessToken = (String) request.getSession().getAttribute(ACCESS_TOKEN);
			String refreshToken = (String) request.getSession().getAttribute(REFRESH_TOKEN);

			saveRefreshToken(refreshToken);
			request.getSession().removeAttribute(ACCESS_TOKEN);
			request.getSession().removeAttribute(REFRESH_TOKEN);
			Map<String, String> eventDetails = populateEventDetailsFromFile();
			addCalendarEvent(accessToken, eventDetails);
		} else {
			String refreshToken = GTestUtil.getConfigValue("refreshToken");
			if (refreshToken == null || refreshToken.equals("")) {
				saveEventDetails(request);
				response.sendError(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED, "Not authenticated");
			} else {
				String accessToken = getAccessToken(refreshToken);
				Map<String, String> eventDetails = populateEventDetails(request);
				addCalendarEvent(accessToken, eventDetails);
			}
		}
		// return the json of the user's basic info
		response.getWriter().println("Success");
	}

	private Map<String, String> populateEventDetailsFromFile() {
		File fp = new File(PATH_EVENTDETAILS);
		Map<String, String> eventDetails = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fp))){
			String details = br.readLine();
			String[] detailsArray = details.split(",");
			for (String detail : detailsArray) {
				int index = detail.indexOf(":");
				eventDetails.put(detail.substring(0, index), detail.substring(index + 1));
			}
		} catch (FileNotFoundException e) {
			LOG.error("Error while retrieving the event details. File not found", e);
		} catch (IOException e) {
			LOG.error("Error while retrieving the event details from the file", e);
		}
		fp.delete();
		return eventDetails;
	}

	private Map<String, String> populateEventDetails(HttpServletRequest request) {
		Map<String, String> eventDetails = new HashMap<>();
		eventDetails.put(LOCATION, request.getParameter(LOCATION));
		eventDetails.put(ST_DATE, getFormattedDateTime(request.getParameter(ST_DATE)));
		eventDetails.put(END_DATE, getFormattedDateTime(request.getParameter(END_DATE)));
		eventDetails.put(SUMMARY, request.getParameter(SUMMARY));
		eventDetails.put(DESCRIPTION, request.getParameter(DESCRIPTION));
		return eventDetails;
	}

	private String getFormattedDateTime(String dateString) {
		String formattedDate = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			Date date = sdf.parse(dateString);
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			formattedDate = sdf2.format(date);
		} catch (java.text.ParseException e) {
			LOG.error("Error while parsing the date {}", dateString, e);
		}
		return formattedDate;
	}

	private void saveRefreshToken(String refreshToken) {
		File fp = new File(PATH_CONFIG_PROPERTIES);
		try (FileWriter writer = new FileWriter(fp, true)){
			writer.write("\nrefreshToken=" + refreshToken);
			writer.close();
		} catch (IOException e) {
			LOG.error("Error while saving the refresh token into the config file", e);
		}
	}

	private String getAccessToken(String refreshToken) {
		try {
			String body = post("https://www.googleapis.com/oauth2/v4/token",
					ImmutableMap.<String, String> builder().put("client_id", GTestUtil.getConfigValue("clientId"))
							.put("client_secret", GTestUtil.getConfigValue("clientSecret"))
							.put(REFRESH_TOKEN, refreshToken).put("grant_type", REFRESH_TOKEN).build());

			JSONObject jsonObject = null;

			// get the access token from json and request info from Google
			try {
				jsonObject = (JSONObject) new JSONParser().parse(body);
			} catch (ParseException e) {
				throw new RuntimeException("Unable to parse json " + body);
			}

			return (String) jsonObject.get(ACCESS_TOKEN);
		} catch (IOException e) {
			LOG.error("Error while fetching the access token. Could not authenticate", e);
		}
		return null;
	}

	private void saveEventDetails(HttpServletRequest request) {
		File fp = new File(PATH_EVENTDETAILS);
		try {
			fp.createNewFile();
			FileWriter writer = new FileWriter(fp);
			String location = request.getParameter(LOCATION);
			String stDate = getFormattedDateTime(request.getParameter(ST_DATE));
			String endDate = getFormattedDateTime(request.getParameter(END_DATE)); 
			String summary = request.getParameter(SUMMARY);
			String description = request.getParameter(DESCRIPTION);
			writer.write("Location:" + location + ",stDate:" + stDate + ",endDate:" + endDate + ",summary:" + summary
					+ ",description:" + description);
			writer.close();
		} catch (IOException e) {
			LOG.error("Error while saving the event details to a file");
		}
	}

	private void addCalendarEvent(String accessToken, Map<String, String> eventDetails) throws IOException {

		TokenResponse tr = new TokenResponse();
		tr.setAccessToken(accessToken);
		GoogleCredential credentials = new GoogleCredential().setFromTokenResponse(tr);
		try {
			Calendar client = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(),
					credentials).build();
			Event event = new Event().setSummary(eventDetails.get(SUMMARY)).setLocation(eventDetails.get(LOCATION))
					.setDescription(eventDetails.get(DESCRIPTION));

			DateTime startDateTime = new DateTime(eventDetails.get(ST_DATE));
			EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Los_Angeles");
			event.setStart(start);

			DateTime endDateTime = new DateTime(eventDetails.get(END_DATE));
			EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Los_Angeles");
			event.setEnd(end);

			EventAttendee[] attendees = new EventAttendee[] {};
			event.setAttendees(Arrays.asList(attendees));

			EventReminder[] reminderOverrides = new EventReminder[] {};
			Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
					.setOverrides(Arrays.asList(reminderOverrides));
			event.setReminders(reminders);

			String calendarId = "primary";
			client.events().insert(calendarId, event).execute();
		} catch (GeneralSecurityException e) {
			LOG.error("Error while adding the event to the calendar.", e);
		}

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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
