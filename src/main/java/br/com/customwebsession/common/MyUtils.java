package br.com.customwebsession.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class MyUtils {

	private MyUtils() {
	}

	public static List<Pattern> loadIgnorePaths(String fullTextIgnore) {
		List<Pattern> ignorePaths = new ArrayList<Pattern>();
		if (fullTextIgnore != null) {
			String[] urls = fullTextIgnore.split(",");
			for (String url : urls) {
				url = StringUtils.remove(url, '\n');
				url = StringUtils.remove(url, '\r');
				url = url.replace(".", "\\.");
				url = url.replace("*", ".*.");
				url = url.trim();
				ignorePaths.add(Pattern.compile("^" + url + "$"));
			}
		}
		return ignorePaths;
	}

	public static String createSessionId(HttpServletResponse response, String cookieName) {
		String sessionId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(cookieName, sessionId);
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		response.addCookie(cookie);
		return sessionId;
	}

	public static String getSessionId(HttpServletRequest request, String cookieName) {
		for (Cookie cookie : request.getCookies() != null ? request.getCookies() : new Cookie[0]) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
