package br.com.customsession.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class FieldFilter {

    private Field field;

    public FieldFilter(Field field) {
        this.field = field;
    }

    public boolean isValid() {
        return !isTransient() && !isStatic() && (isFrom());
    }

    private boolean isTransient() {
        return (field.getModifiers() & Modifier.TRANSIENT) != 0;
    }

    private boolean isStatic() {
        return (field.getModifiers() & Modifier.STATIC) != 0;
    }

    private boolean isFrom() {
        return field.getType().getPackage() != null && (field.getType().getPackage().getName().startsWith("br.com."));
    }
}


public class FieldExtractor {

    public static List<SerializationField> extractFields(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>();
        Class<?> superClass = o.getClass();
        do {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        } while (superClass != null);
        return filterFields(fields);
    }

    private static List<SerializationField> filterFields(List<Field> fields) {
        List<SerializationField> filteredFields = new ArrayList<>();
        for (Field field : fields) {
            FieldFilter fieldData = new FieldFilter(field);

            if (fieldData.isValid()) {
                filteredFields.add(new SerializationField(field));
            }
        }
        return filteredFields;
    }


}
