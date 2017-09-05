package br.com.customsession.controller;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

public interface SessionRepo {
	
    //FACES_VIEW_MAPS = "com.sun.faces.application.view.activeViewMaps";
    //HORUS_VIEW_MAPS = "br.com.assai.horus.web.faces.view.ViewScope"
    public static final String HORUS_FACES_VIEW_MAPS = "com.sun.faces.application.view.activeViewMaps";
    
	HttpSessionImpl persist(HttpSessionImpl session);
	HttpSessionImpl load(ServletContext servletContext, String id);
	void setAttributes(String id, Map<String, String> attributes, Map<String, Object> sessionCache);
	Object convertAttribute(String name, Map<String, String> str);
	void removeAttribute(String id, String name);
	void remove(String id);
	Set<String> getAttributeNames(String id);
	
}
