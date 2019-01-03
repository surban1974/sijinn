package it.sijinn.perceptron.utils.io;

import it.sijinn.perceptron.utils.Utils;

public class SimpleArrayReader implements IDataReader{
	private int current = 0;
	private int finish = 0;
	private float[][][] data = new float[0][0][0];
	
	public SimpleArrayReader(float[][][] _data){
		super();
		this.data=_data;
	}
	
	@Override
	public byte[] readAll() throws Exception {
		return Utils.serialize(data);
	}
	
	@Override
	public boolean open() throws Exception {
		finish = data.length;
		return true;
	}
	
	@Override
	public Object readNext() throws Exception {
		float[][] result = null;
		if(current<finish){
			result = data[current];
			current++;
		}
		return result;
	}
	
	@Override
	public boolean finalizer() throws Exception {
		return true;
	}
	
	@Override
	public boolean close() throws Exception {
		current=0;
		return true;
	}

}
