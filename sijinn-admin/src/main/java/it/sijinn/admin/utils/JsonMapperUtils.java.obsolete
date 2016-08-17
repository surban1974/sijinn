package it.sijinn.admin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class JsonMapperUtils {



	public static Map<String, Object> jsonToMap(JsonObject json) throws JsonException {
	        Map<String, Object> retMap = new HashMap<String, Object>();

	    if(json != JsonObject.NULL) {
	        retMap = toMap(json);
	    }
	    return retMap;
	}

	public static Map<String, Object> toMap(JsonObject object) throws JsonException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keySet().iterator();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JsonArray) {
	            value = toList((JsonArray) value);
	        }

	        else if(value instanceof JsonObject) {
	            value = toMap((JsonObject) value);
	        }
	        
	        if(value instanceof JsonValue && ((JsonValue)value).getValueType().equals(ValueType.STRING))
	        	map.put(key, object.getString(key));
	        else 
	        	map.put(key, value);
	        
//	        map.put(key, value);
	    }
	    return map;
	}

	public static List<Object> toList(JsonArray array) throws JsonException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.size(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JsonArray) {
	            value = toList((JsonArray) value);
	        }

	        else if(value instanceof JsonObject) {
	            value = toMap((JsonObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}	
	
}
