package com.gtest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SampleServlet
 */
@WebServlet("/signin")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// redirect to google for authorization
		StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
				.append("?client_id=").append(GTestUtil.getConfigValue("clientId")).append("&response_type=code")
				.append("&scope=https://www.googleapis.com/auth/calendar")
				.append("&redirect_uri=http://localhost:8080/GoogleAPITestApp/callback")
				.append("&state=google_calendar_api_test").append("&access_type=offline")
				.append("&approval_prompt=force");
		response.sendRedirect(oauthUrl.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
