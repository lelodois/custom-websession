package br.com.customwebsession.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HttpSessionImpl implements HttpSession {

	private long creationTime;
	private long lastAccessedTime;
	private String id;
	private SessionRepo repo;

	private Map<String, Object> sessionCache = new HashMap<String, Object>();
	private Map<String, String> attributes;
	private boolean _new = false;
	private ServletContext servletContext;

	public HttpSessionImpl(ServletContext servletContext, String id, long creationTime, long lastAccessedTime,
			SessionRepo repo, Map<String, String> attributes) {
		this.servletContext = servletContext;
		this.id = id;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.repo = repo;
		this.attributes = attributes;
	}

	public HttpSessionImpl(ServletContext servletContext, String sessionId, SessionRepo sessionRepo) {
		this(servletContext, sessionId, System.currentTimeMillis(), System.currentTimeMillis(), sessionRepo,
				Collections.<String, String>emptyMap());
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		setAttribute("__MaxInactiveInterval", interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		Object value = getAttribute("__MaxInactiveInterval");
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return value != null ? Integer.valueOf(value.toString()) : 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		if (!sessionCache.containsKey(name)) {
			sessionCache.put(name, convertAttribute(name));
		}

		return sessionCache.get(name);
	}

	private Object convertAttribute(String name) {
		return repo.convertAttribute(name, attributes);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Vector<String>(repo.getAttributeNames(id)).elements();
	}

	@Override
	public String[] getValueNames() {
		return repo.getAttributeNames(id).toArray(new String[] {});
	}

	@Override
	public void setAttribute(String name, Object value) {
		sessionCache.put(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		sessionCache.remove(name);
		repo.removeAttribute(id, name);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
		sessionCache.clear();
		repo.remove(id);
	}

	@Override
	public boolean isNew() {
		return this._new;
	}

	public void commit() {
		repo.setAttributes(id, attributes, sessionCache);
	}

	public void setNew(boolean b) {
		this._new = b;
	}

}
