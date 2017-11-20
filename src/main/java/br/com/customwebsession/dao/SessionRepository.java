package br.com.customwebsession.dao;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import br.com.customwebsession.session.HttpSessionImpl;

public interface SessionRepository {

	HttpSessionImpl persist(HttpSessionImpl session);

	HttpSessionImpl load(ServletContext servletContext, String id);

	void setAttributes(String id, Map<String, String> attributes, Map<String, Object> sessionCache);

	Object convertAttribute(String name, Map<String, String> str);

	void removeAttribute(String id, String name);

	void remove(String id);

	Set<String> getAttributeNames(String id);

}
