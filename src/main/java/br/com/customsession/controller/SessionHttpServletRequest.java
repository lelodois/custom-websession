package br.com.customsession.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class SessionHttpServletRequest extends HttpServletRequestWrapper {

    private HttpSession session;

    public SessionHttpServletRequest(HttpServletRequest request, HttpSession session) {
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
