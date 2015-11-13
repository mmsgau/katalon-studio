package com.kms.katalon.core.setting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum DriverPropertyValueType {
    String, Integer, Boolean, Dictionary, List;
    
    public static String[] stringValues() {
        DriverPropertyValueType[] values = values();
        String[] stringValues = new String[values.length];
        for (int i = 0; i < values.length ; i++) {
            stringValues[i] = values[i].toString();
        }
        return stringValues;
    }
    
    public static DriverPropertyValueType fromValue(Object value) {
        if (value instanceof Integer) {
            return Integer;
        } else if (value instanceof Boolean) {
            return Boolean;
        } else if (value instanceof Map) {
            return Dictionary;
        } else if (value instanceof List) {
            return List;
        } 
        return String;
    }
    
    public Object getDefaultValue() {
        switch (this) {
        case Boolean:
            return new Boolean(true);
        case Integer:
            return new Integer(0);
        case List:
            return new ArrayList<Object>();
        case Dictionary:
            return new LinkedHashMap<String, Object>();
        case String:
            return new String("");
        }
        return "";
    }
}
