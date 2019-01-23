package it.sijinn.perceptron.utils.io;

import java.io.InputStream;



public class ResourceStreamWrapper implements IStreamWrapper {

	private static final long serialVersionUID = 1L;
	protected String path = null;
	protected transient InputStream stream = null;
	
	public ResourceStreamWrapper(String _path){
		super();
		this.path = _path;
	}

	@Override
	public InputStream openStream() throws Exception {
		if(path!=null){
			stream = this.getClass().getClassLoader().getResourceAsStream(path);
			if(stream==null)
				stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		}
		return stream;
	}
	
	@Override
	public boolean closeStream() throws Exception {
		if(stream!=null)
			stream.close();
		return true;
	}
	
	@Override
	public IStreamWrapper instance() throws Exception {
		return new ResourceStreamWrapper(path);
	}	

}
