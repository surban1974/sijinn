package it.sijinn.perceptron.utils;

import it.sijinn.perceptron.Neuron;

public class RandomPositiveWeightGenerator implements IWeightGenerator {

	@Override
	public float generate(Neuron from, Neuron to) {
		return new Float(Math.random()).floatValue();
	}

}
