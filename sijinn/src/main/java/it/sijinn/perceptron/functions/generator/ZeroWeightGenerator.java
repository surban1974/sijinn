package it.sijinn.perceptron.functions.generator;

import it.sijinn.common.Neuron;

public class ZeroWeightGenerator implements IGenerator {

	@Override
	public float generate(Neuron from, Neuron to) {
		return 0;
	}

}
