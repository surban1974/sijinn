package it.sijinn.perceptron.functions.generator;

import it.sijinn.common.Neuron;

public class RandomPositiveWeightGenerator implements IGenerator {

	private static final long serialVersionUID = 1L;

	@Override
	public float generate(Neuron from, Neuron to) {
		return new Float(Math.random()).floatValue();
	}

}
