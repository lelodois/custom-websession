package br.com.customwebsession.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AuthServletRequest extends HttpServletRequestWrapper {

	private Principal userPrincipal;

	public static String USER_PRINCIPAL_KEY = "USER_PRINCIPAL_KEY";
	public static String URL_CALLBACK_KEY = "URL_CALLBACK_KEY";

	public AuthServletRequest(HttpServletRequest request) {
		super(request);
		final String name = (String) ((HttpServletRequest) getRequest()).getSession().getAttribute(USER_PRINCIPAL_KEY);

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

	@Override
	public Principal getUserPrincipal() {
		return userPrincipal;
	}
}
