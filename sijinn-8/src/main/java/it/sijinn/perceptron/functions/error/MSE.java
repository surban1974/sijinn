package it.sijinn.perceptron.functions.error;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;

public class MSE implements IErrorFunctionApplied {

	private static final long serialVersionUID = 1L;
	protected boolean regularizationL1=false;
	protected float regularizationL1Alfa=1;
	protected boolean regularizationL2=false;
	protected float regularizationL2Alfa=1;
	
	@Override
	public float compute(Network network, float initialError, boolean reversed) {
		float wl1=0;
		float wl2=0;
		if(regularizationL1 || regularizationL2){
			if(!(network==null || network.getLayers()==null || network.getLayers().size()==0)){
				final float[] weights = network.getWeight();
				for(float weight : weights){
					if(regularizationL1)
						wl1+=Math.abs(weight);
					if(regularizationL2)
						wl2+=weight*weight;
				}
			}
		}
		if(!reversed)
			return compute(network, initialError) + regularizationL1Alfa*wl1 + regularizationL2Alfa*wl2;
		else
			return computeReversed(network, initialError) + regularizationL1Alfa*wl1 + regularizationL2Alfa*wl2;
	}

	public float compute(Network network, float initialError) {
		float error = initialError;
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return initialError;
		for(final Neuron neuron: network.getLayers().get(network.getLayers().size()-1)){
			if(neuron!=null)
				error+= Math.pow(neuron.getOutput()-neuron.getTarget(),2)/2;
		}
		return error;
	}
	
	public float computeReversed(Network network, float initialError) {
		float error = initialError;
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return initialError;
		for(final Neuron neuron: network.getLayers().get(0)){
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

	@Override
	public IErrorFunctionApplied setRegularizationL1(boolean l1, float alfa1) {
		regularizationL1 = l1;
		regularizationL1Alfa = alfa1;
		return this;
	}

	@Override
	public IErrorFunctionApplied setRegularizationL2(boolean l2, float alfa2) {
		regularizationL2 = l2;
		regularizationL2Alfa = alfa2;
		return this;
	}

}
