package it.sijinn.perceptron.functions.generator;

import it.sijinn.common.Neuron;

public class RandomPositiveWeightGenerator implements IGenerator {

	@Override
	public float generate(Neuron from, Neuron to) {
		return new Float(Math.random()).floatValue();
	}

}
