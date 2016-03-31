package it.sijinn.perceptron.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Utils {
	static public <T extends Object> T clone(T oldObj, Class<T> type) throws Exception {
		
		Object retVal = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(oldObj); 
			oos.flush(); 
	
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); 
			ois = new ObjectInputStream(bin); 
			retVal = ois.readObject(); 
	
		} catch (Exception e) {
			throw (e);
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (java.io.IOException e) {
				throw (e);
			}
		}
		
		return type.cast(retVal);
	}
	
	static public int sizeByte(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
		try {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(oldObj); 
			oos.flush();
			return bos.toByteArray().length; 
		} catch (Exception e) {
			throw (e);
		} finally {
			try {
				oos.close();	
			} catch (java.io.IOException e) {
				throw (e);
			}
		}	
	}
	
	static public String print(float[] array, String separator){
		String result = "";
		for(float f:array)
			result+=f+separator;
		return result;
	}
	
	static public String print(float[][] array, String[] separators){
		String result = "";
		for(float[] f:array)
			result+=print(f,separators[0])+separators[1];
		return result;
	}

}
