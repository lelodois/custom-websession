package br.com.customwebsession.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import br.com.customwebsession.common.MyUtils;
import br.com.customwebsession.servlet.CustomHttpServletRequest;
import br.com.customwebsession.session.HttpSessionImpl;
import br.com.customwebsession.session.SessionRepo;

public class CustomSessionFilter implements Filter {

	private final static String COOKIENAME = "SSSESSION";

	@Autowired
	private SessionRepo sessionRepo;
	private FilterConfig config;
	private List<Pattern> ignorePaths = new ArrayList<Pattern>();

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		this.ignorePaths = MyUtils.loadIgnorePaths(config.getInitParameter("IGNORE"));
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest r1, ServletResponse r2, FilterChain chain)
			throws IOException, ServletException {

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

	private void filter(FilterChain chain, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		HttpSessionImpl session = null;
		String sessionId = MyUtils.getSessionId(request, COOKIENAME);

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
				sessionId = MyUtils.createSessionId(response, COOKIENAME);
				session = sessionRepo.persist(new HttpSessionImpl(config.getServletContext(), sessionId, sessionRepo));
				session.setNew(true);
			}

			chain.doFilter(new CustomHttpServletRequest(request, session), response);
		} finally {
			if (session != null) {
				session.commit();
			}
		}
	}
}
