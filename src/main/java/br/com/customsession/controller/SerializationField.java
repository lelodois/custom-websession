package br.com.customsession.controller;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import assai.horus.component.web.session.Transient;
import br.com.caelum.vraptor.ioc.SessionScoped;

public class SerializationField {

    private Field field;
    
    public SerializationField(Field field) {
        this.field = field;
    }

    public boolean isInjected() {
        return field.getAnnotation(Autowired.class) != null;
    }
    
    public boolean isSpringService() {
        if (!isInjected()) {
            return false;
        }
        return field.getType().getAnnotation(Service.class) != null;
    }

    public boolean isHsbService() {
        if (!isInjected()) {
            return false;
        }
        return field.getType().getPackage() != null
                        && field.getType().getPackage().getName().contains("hsb.service");
    }
    
    public boolean isTransient() {
        return field.getAnnotation(Transient.class) != null 
                        || field.getType().getAnnotation(Transient.class) != null
                        || field.getType().getAnnotation(SessionScoped.class) != null;
    }

    public void clearField(Object object) {
        try {
            field.setAccessible(true);
            field.set(object, null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setField(Object value) {
        try {
            field.setAccessible(true);
            field.set(value, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object getFieldValue(Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getType() {
        return field.getType();
    }
    
}
