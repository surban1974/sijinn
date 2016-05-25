package it.sijinn.perceptron.functions.error;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;

public class MSE implements IErrorFunctionApplied {
	
	public float compute(Network network, float initialError, boolean reversed) {
		if(!reversed)
			return compute(network, initialError);
		else
			return computeReversed(network, initialError);
	}

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
	
	public float computeReversed(Network network, float initialError) {
		float error = initialError;
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return initialError;
		for(Neuron neuron: network.getLayers().get(0)){
			if(neuron!=null)
				error+= Math.pow(neuron.getOutput()-neuron.getTarget(),2)/2;
		}
		return error;
	}	
	
	public String getDefinition(){
		return "errorfunction="+this.getClass().getSimpleName();
	}	
	
	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}

}
