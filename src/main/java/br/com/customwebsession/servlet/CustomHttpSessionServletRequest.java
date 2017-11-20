package br.com.customwebsession.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class CustomHttpSessionServletRequest extends HttpServletRequestWrapper {

	private HttpSession session;

	public CustomHttpSessionServletRequest(HttpServletRequest request, HttpSession session) {
		super(request);
		this.session = session;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return getSession();
	}

}
