package br.com.customsession.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class SessionFilter implements Filter {

    private static String HRSSESSION_COOKIENAME = "HRSSESSION";

    @Autowired
    SessionRepo sessionRepo;
    private FilterConfig config;
    private List<Pattern> ignorePaths = new ArrayList<Pattern>();

    private String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        for (Cookie cookie : cookies) {
            if (HRSSESSION_COOKIENAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void destroy() {}

    public void doFilter(ServletRequest r1, ServletResponse r2, FilterChain chain) throws IOException, ServletException {
        if (this.sessionRepo == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        }

        HttpServletRequest request = (HttpServletRequest) r1;
        HttpServletResponse response = (HttpServletResponse) r2;

        if (ignorePath(request.getRequestURL().toString())) {
            chain.doFilter(r1, r2);
        } else {
            filter(chain, request, response);
        }
    }

    private boolean ignorePath(String url) {
        for (Pattern pattern : ignorePaths) {
            if (pattern.matcher(url).find()) {
                return true;
            }
        }
        return false;
    }

    private void filter(FilterChain chain, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSessionImpl session = null;
        String sessionId = getSessionId(request);

        try {
            if (sessionId != null) {
                session = sessionRepo.load(config.getServletContext(), sessionId);
            }

            if (session != null && session.getMaxInactiveInterval() > 0) {
                long dt = (System.currentTimeMillis() - session.getLastAccessedTime()) / 1000;

                if (dt > session.getMaxInactiveInterval()) {
                    session.invalidate();
                    session = null;
                }
            }

            if (sessionId == null || session == null) {
                sessionId = createSessionId(response);
                session = sessionRepo.persist(new HttpSessionImpl(config.getServletContext(), sessionId, sessionRepo));
                session.setNew(true);
            }

            chain.doFilter(new SessionHttpServletRequest(request, session), response);
        } finally {
            if (session != null) {
                session.commit();
            }
        }
    }

    private String createSessionId(HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(HRSSESSION_COOKIENAME, sessionId);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
        return sessionId;
    }

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        ignorePaths = UrlUtils.loadIgnorePaths(config.getInitParameter("IGNORE"));
    }
}
