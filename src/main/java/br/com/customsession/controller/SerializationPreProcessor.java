package br.com.customsession.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Classe responsavel por processar os campos antes da serializacao
 * 
 * Remove dependencias injetadas pelo spring que utilizem a anotação {@link Service} ou classes que
 * estejam no pacote *.hsb.service.*
 * 
 * @author abner
 *
 */
public class SerializationPreProcessor {

    public void process(Object value) {
        if (value instanceof Map) {
            Collection<?> children = ((Map<?, ?>) value).values();
            for (Object child : children) {
                process(child);
            }
        } else if (value != null) {
            removeService(value);
        }
    }

    private List<Object> objects = new ArrayList<>();

    private void removeService(Object o) {
        for (Object object : objects) {
            if (object == o) {
                return;
            }
        }
        objects.add(o);
        
        for (SerializationField field : FieldExtractor.extractFields(o)) {
            processField(o, field);
        }
    }

    private void processField(Object object, SerializationField field) {
        if (field.isSpringService() || field.isHsbService() || field.isTransient()) {
            field.clearField(object);
        } else {
            Object value = field.getFieldValue(object);
            if (value != null) {
                removeService(value);
            }
        }
    }

}
