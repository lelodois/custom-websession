package br.com.customwebsession.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import br.com.customwebsession.common.MyUtils;
import br.com.customwebsession.servlet.CustomHttpServletRequest;

public class AuthorizationFilter implements Filter {

	private List<Pattern> noRestrictedAccess;
	private List<String> noRestrictedContexts = Collections.emptyList();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		CustomHttpServletRequest hrsReq = new CustomHttpServletRequest((HttpServletRequest) servletRequest);

		if (checkNoRestrictedAccess(hrsReq.getContextPath(), hrsReq.getRequestURI())) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		if (hrsReq.getUserPrincipal() == null) {
			String u = hrsReq.getRequestURI();
			if (hrsReq.getQueryString() != null) {
				u += "?";
				u += hrsReq.getQueryString();
			}

			hrsReq.setAttribute("backurl", u);

			hrsReq.getRequestDispatcher("/login.jsp").forward(hrsReq, servletResponse);
			return;
		}

		filterChain.doFilter(hrsReq, servletResponse);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		noRestrictedAccess = MyUtils.loadIgnorePaths(filterConfig.getInitParameter("IGNORE"));

		String context = filterConfig.getInitParameter("NO_RESTRICTED_CONTEXT");
		if (context != null) {
			noRestrictedContexts = Arrays.asList(context.split(","));
		}
	}

	public List<Pattern> getNoRestrictedAccess() {
		return noRestrictedAccess;
	}

	public boolean checkNoRestrictedAccess(String context, String url) {

		if (noRestrictedContexts.contains(context)) {
			return true;
		}

		if (url.startsWith(context)) {
			url = url.substring(context.length());
		}

		for (Pattern pattern : getNoRestrictedAccess()) {
			if (pattern.matcher(url).find()) {
				return true;
			}
		}

		return false;
	}
}
