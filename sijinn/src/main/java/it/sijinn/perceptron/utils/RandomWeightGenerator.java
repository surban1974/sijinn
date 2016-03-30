package it.sijinn.perceptron.utils;

import it.sijinn.perceptron.Neuron;

public class RandomWeightGenerator implements IWeightGenerator {

	@Override
	public float generate(Neuron from, Neuron to) {
		return new Float(Math.random()*2-1).floatValue();
	}

}
