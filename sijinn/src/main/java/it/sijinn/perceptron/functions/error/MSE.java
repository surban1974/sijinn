package it.sijinn.perceptron.functions.error;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;

public class MSE implements IErrorFunctionApplied {

	public float compute(Network network, float initialError) {
		float error = initialError;
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return initialError;
		for(Neuron neuron: network.getLayers().get(network.getLayers().size()-1)){
			if(neuron!=null)
				error+= Math.pow(neuron.getOutput()-neuron.getTarget(),2)/2;
		}
		return error;
	}
	
	public String toSaveString(){
		return "errorfunction="+this.getClass().getSimpleName();
	}	

}
