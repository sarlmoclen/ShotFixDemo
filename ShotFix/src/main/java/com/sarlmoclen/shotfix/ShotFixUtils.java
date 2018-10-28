package com.sarlmoclen.shotfix;

import java.lang.reflect.Field;

public class ShotFixUtils {

    /**
     * 类反射获取私有变量
     */
    public static Object getField(Object object, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(object,fieldName);
        return field.get(object);
    }

    /**
     * 类反射获取私有变量重新赋值
     */
    public static void setField(Object object, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(object,fieldName);
        field.set(object, value);
    }

    /**
     * 获取field
     */
    public static Field findField(Object object, String fieldName) throws NoSuchFieldException {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in " + object.getClass());
    }

}
