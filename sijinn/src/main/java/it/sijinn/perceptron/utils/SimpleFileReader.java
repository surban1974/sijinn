package it.sijinn.perceptron.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;



public class SimpleFileReader implements IDataReader {

	private File file = null;
	private BufferedReader breader = null;
	
	
	public SimpleFileReader(File _file){
		super();
		this.file = _file;
	}
	
	@Override
	public boolean open() throws Exception {
		if(file!=null && file.exists()){
			breader = new BufferedReader(new FileReader(file));	
			return true;
		}else
			return false;
		
	}
	
	@Override
	public Object readNext() throws Exception {
		if(breader==null)
			return null;
		else
			return breader.readLine();
	}
	
	@Override
	public boolean finalizer() throws Exception {
		breader = null;
		return true;
	}
	
	@Override
	public boolean close() throws Exception {
		if(breader!=null)
			breader.close();
		return true;
	}
}
