package br.com.customsession.controller;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringSerializationPostProcessor implements SerializationPostProcessor {

    @Autowired
    private ApplicationContext context;

    @Override
    public void process(Object value) {
        if (value instanceof Map) {
            Collection<?> children = ((Map<?, ?>) value).values();
            for (Object child : children) {
                process(child);
            }
        } else if (value != null) {
            injectFields(value);
        }
    }

    private void injectFields(Object value) {
        context.getAutowireCapableBeanFactory().autowireBean(value);
    }

}
