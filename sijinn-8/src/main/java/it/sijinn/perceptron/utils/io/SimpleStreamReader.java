package it.sijinn.perceptron.utils.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;



public class SimpleStreamReader implements IDataReader{

	private static final long serialVersionUID = 1L;
	private transient InputStream stream = null;
	private transient BufferedReader breader = null;
	private IStreamWrapper streamWrapper = null;
	private IStreamWrapper wrapper = null;
	
	public SimpleStreamReader(IStreamWrapper _streamWrapper) {
		super();
		if(_streamWrapper!=null)
			this.streamWrapper = _streamWrapper;
	}
	
	@Override
	public byte[] readAll() throws Exception {
		InputStream is = streamWrapper.instance().openStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) 
		  buffer.write(data, 0, nRead);
		

		buffer.flush();
		is.close();

		return buffer.toByteArray();
	}	
	
	@Override
	public boolean open() throws Exception {
		if(this.streamWrapper!=null)
			wrapper = streamWrapper.instance();
		if(wrapper!=null){
			stream = wrapper.openStream();
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
		if(wrapper!=null)
			wrapper.closeStream();
		return true;
	}

}
