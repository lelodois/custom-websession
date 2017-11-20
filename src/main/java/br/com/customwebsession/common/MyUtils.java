package br.com.customwebsession.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

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

	public static Object fromBase64ToString(String s) {
		byte[] data = DatatypeConverter.parseBase64Binary(s);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String fromObjectToBase64(Object o) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return DatatypeConverter.printBase64Binary(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String toMd5(String md5) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] array = md.digest(md5.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}
}
