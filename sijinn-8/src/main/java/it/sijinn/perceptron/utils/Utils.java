package it.sijinn.perceptron.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;


public class Utils {
	public static <T extends Object> T clone(T oldObj, Class<T> type) throws Exception {
		
		Object retVal = null;
		
		try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
		){		
			oos.writeObject(oldObj); 
			oos.flush(); 
			try (
					ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); 
					ObjectInputStream ois = new ObjectInputStream(bin); 
			){
				retVal = ois.readObject(); 
			}
	
		} catch (Exception e) {
			throw (e);
		}
		
		return type.cast(retVal);
	}

	public static <T extends Object> Class<?> getClass(T type){
		if (type instanceof Class){
			return (Class<?>)type;
		}else if (type instanceof ParameterizedType){
			return getClass(((ParameterizedType)type).getRawType());
		}else if (type instanceof GenericArrayType){
			Class<?> componentClass = getClass(((GenericArrayType)type).getGenericComponentType());
			if (componentClass != null){
				return Array.newInstance(componentClass, 0).getClass();
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	
	public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
	
	public static int sizeByte(Object oldObj) throws Exception {		
		try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
		){
			oos.writeObject(oldObj); 
			oos.flush();
			return bos.toByteArray().length; 
		} catch (Exception e) {
			throw (e);
		}
	}
	
	public static String print(float[] array, String separator){
		StringBuilder result = new StringBuilder("");
		for(float f:array)
			result.append(f+separator);
		return result.toString();
	}
	
	public static String print(float[][] array, String[] separators){
		StringBuilder result = new StringBuilder("");
		for(float[] f:array)
			result.append(print(f,separators[0])+separators[1]);
		return result.toString();
	}
	
	public static String normalXML(String input, String charSet) {
		
		if (input==null) return input;
		
		if(input.indexOf("<![CDATA[")==0){
			return input;
		}
		
		try{
			if(charSet!=null) input = new String(input.getBytes(),charSet);
		}catch(Exception e){

		}

		StringBuilder result = new StringBuilder("");
		if (input.indexOf('&')>-1 ||
			input.indexOf('\\')>-1 ||
			input.indexOf('>')>-1 ||
			input.indexOf('<')>-1 ||
			input.indexOf('"')>-1) { 

			for (int i=0;i<input.length();i++) {
				if (input.charAt(i)=='&') result.append("&amp;");
				else if (input.charAt(i)=='>') result.append("&gt;");
				else if (input.charAt(i)=='<') result.append("&lt;");
				else if (input.charAt(i)=='"') result.append("&quot;");
				else result.append(input.charAt(i));
			}
			return result.toString();
		}
		else 
			return input;
	}

	public static String normalASCII(String input){
		if(input==null || input.length()==0) return "";
		StringBuilder result = new StringBuilder("");
		for(int i=0;i<input.length();i++){
			char c = input.charAt(i); 
			int ascii = (int)c;
			
			if ((ascii == 0x9) ||
	            (ascii == 0xA) ||
	            (ascii == 0xD) ||
	            ((ascii >= 0x20) && (ascii <= 0xD7FF)) ||
	            ((ascii >= 0xE000) && (ascii <= 0xFFFD)) ||
	            ((ascii >= 0x10000) && (ascii <= 0x10FFFF))){
				result.append("&#"+ascii+';');
	        }		
			
		}
		
		return result.toString();
		
	}

	public static String normalHTML(String input, String charSet) {	
		if (input==null) return "";
		
		try{
			if(charSet!=null) input = new String(input.getBytes(),charSet);
		}catch(Exception e){

		}

		StringBuilder result = new StringBuilder("");
		if (input.indexOf('&')>-1 ||
			input.indexOf('\\')>-1 ||
			input.indexOf('>')>-1 ||
			input.indexOf('<')>-1 ||
			input.indexOf('"')>-1) { 

			for (int i=0;i<input.length();i++) {
				if (input.charAt(i)=='&') result.append("&amp;");
				else if (input.charAt(i)=='\'') result.append("&apos;");
				else if (input.charAt(i)=='>') result.append("&gt;");
				else if (input.charAt(i)=='<') result.append("&lt;");
				else if (input.charAt(i)=='"') result.append("&quot;");
				else result.append(input.charAt(i));
			}
			return result.toString();
		}
		else 
			return input;
	}
	
	public static byte[] getBytesFromFile(File file) throws Exception {

		 if(!file.exists()){
			 throw new IOException("File "+file.getAbsolutePath()+" non exist");
		 }
		 try(InputStream is = new FileInputStream(file)){		     
		     long length = file.length();
		     byte[] bytes = new byte[(int)length];
		     int offset = 0;
		     int numRead = 0;
		     while (offset < bytes.length
		           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		           offset += numRead;
		     }
		     if (offset < bytes.length) {
	    		 throw new IOException("Could not completely read file "+file.getName());
		     }
		     return bytes;
		 }
	     
	 }	
}
