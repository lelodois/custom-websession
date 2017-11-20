package br.com.customwebsession.servlet;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class CustomHttpServletRequest extends HttpServletRequestWrapper {

	public static String USER_PRINCIPAL_KEY = "USER_PRINCIPAL_KEY";
	private Principal userPrincipal;
	private HttpSession session;

	public CustomHttpServletRequest(HttpServletRequest request, HttpSession session) {
		super(request);
		this.session = session;
	}

	public CustomHttpServletRequest(HttpServletRequest request) {
		super(request);
		final String name = getUserPrincipalKey();

		if (name != null) {
			this.userPrincipal = new Principal() {
				public String getName() {
					return name;
				}
			};
		} else {
			this.userPrincipal = null;
		}
	}

	private String getUserPrincipalKey() {
		return (String) getSession().getAttribute(USER_PRINCIPAL_KEY);
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return getSession();
	}

	@Override
	public Principal getUserPrincipal() {
		return userPrincipal;
	}
}
