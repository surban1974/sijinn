package it.sijinn.perceptron.functions.generator;

import it.sijinn.common.Neuron;

public class RandomWeightGenerator implements IGenerator {

	@Override
	public float generate(Neuron from, Neuron to) {
		return new Float(Math.random()*2-1).floatValue();
	}

}
