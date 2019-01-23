package it.sijinn.perceptron.utils.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;



public class SimpleStringReader implements IDataReader{

	private static final long serialVersionUID = 1L;
	private String input = null;
	private transient InputStream stream = null;
	private String encoding = null;
	private transient BufferedReader breader = null;
	
	public SimpleStringReader(String _input, String _encoding) {
		super();
		if(_input!=null)
			this.input = _input;
		if(_encoding!=null)
			this.encoding = _encoding;
	}
	
	@Override
	public byte[] readAll() throws Exception {

		if(input!=null){
			if(encoding==null)
				return input.getBytes();
			else
				return input.getBytes(encoding);
		}else
			return null;
	}	
	
	@Override
	public boolean open() throws Exception {
		if(this.input!=null){
			stream = new ByteArrayInputStream(input.getBytes());
			breader = new BufferedReader(new InputStreamReader(stream));
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
		stream = null;				
		return true;
	}
	
	@Override
	public boolean close() throws Exception {
		if(breader!=null)
			breader.close();
		if(stream!=null)
			stream.close();
		return true;
	}

}
