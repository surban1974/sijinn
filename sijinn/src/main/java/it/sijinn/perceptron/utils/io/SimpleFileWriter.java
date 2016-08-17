package it.sijinn.perceptron.utils.io;

import java.io.File;
import java.io.FileOutputStream;



public class SimpleFileWriter implements IDataWriter {

	private File file = null;
	private FileOutputStream fileOutputStream = null;
	private boolean append = false;
	
	public SimpleFileWriter(File _file){
		super();
		this.file = _file;
	}
	
	public SimpleFileWriter(File _file, boolean _append){
		super();
		this.file = _file;
		this.append = _append;
	}
	
	@Override
	public boolean open() throws Exception {
		if(file!=null){
			fileOutputStream = new FileOutputStream(file, append);	
			return true;
		}else
			return false;
		
	}
	
	@Override
	public boolean writeNext(byte[] obj) throws Exception {
		if(fileOutputStream==null)
			return false;
		else
			fileOutputStream.write(obj);
		return true;
	}
	
	@Override
	public boolean finalizer() throws Exception {
		fileOutputStream = null;
		return true;
	}
	
	@Override
	public boolean close() throws Exception {
		if(fileOutputStream!=null)
			fileOutputStream.close();
		return true;
	}

	public File getFile() {
		return file;
	}

	public boolean isAppend() {
		return append;
	}
	

}
