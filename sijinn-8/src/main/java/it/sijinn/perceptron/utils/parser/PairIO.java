package it.sijinn.perceptron.utils.parser;

import java.io.Serializable;

public class PairIO implements Serializable{
	private static final long serialVersionUID = 1L;
	private float[] input;
	private float[] output;
	private int linenumber;
	
	public PairIO(float[] input, float[] output){
		super();
		this.input = input;
		this.output = output;
	}
	
	public PairIO(float[] input, float[] output, int linenumber){
		super();
		this.input = input;
		this.output = output;
		this.linenumber = linenumber;
	}	

	public float[] getInput() {
		return input;
	}

	public void setInput(float[] input) {
		this.input = input;
	}

	public float[] getOutput() {
		return output;
	}

	public void setOutput(float[] output) {
		this.output = output;
	}

	public int getLinenumber() {
		return linenumber;
	}

	public void setLinenumber(int linenumber) {
		this.linenumber = linenumber;
	}
}
