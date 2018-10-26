package com.sarlmoclen.shotfix;

import java.lang.reflect.Field;

public class ShotFixUtils {

    /**
     * 类反射获取私有变量
     */
    public static Object getField(Object object, Class<?> c, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * 类反射获取私有变量重新赋值
     */
    public static void setField(Object object, Class<?> c, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

}
