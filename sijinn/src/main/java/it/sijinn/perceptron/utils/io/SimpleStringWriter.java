package it.sijinn.perceptron.utils.io;





public class SimpleStringWriter implements IDataWriter {

	private String output = null;
	
	public SimpleStringWriter(){
		super();
		this.output = "";
	}
	

	@Override
	public boolean open() throws Exception {
		if(output!=null){
			output = "";	
			return true;
		}else
			return false;
		
	}
	
	@Override
	public boolean writeNext(byte[] obj) throws Exception {
		if(output==null)
			return false;
		else
			output+=new String(obj);
		return true;
	}
	
	@Override
	public boolean finalizer() throws Exception {
		return true;
	}
	
	@Override
	public boolean close() throws Exception {
		return true;
	}


	public String getOutput() {
		return output;
	}

	

}
