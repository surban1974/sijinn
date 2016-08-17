package it.sijinn.perceptron.functions.generator;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;

public class DefiniteWeightGenerator implements IGenerator {
	
	private Network network;
	
	public DefiniteWeightGenerator(Network network){
		super();
		this.network = network;
	}

	@Override
	public float generate(Neuron from, Neuron to) {
		if(network!=null){
			Synapse synapse = network.findSynapse(from, to);
			if(synapse!=null)
				return synapse.getWeight();
		}
		return 0;
	}

}
