package it.sijinn.perceptron.utils.parser;

import java.io.Serializable;

public class PairIO implements Serializable{
	private static final long serialVersionUID = 1L;
	private float[] input;
	private float[] output;
	
	public PairIO(float[] input, float[] output){
		super();
		this.input = input;
		this.output = output;
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
}
