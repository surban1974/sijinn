package it.sijinn.perceptron.functions.generator;

import it.sijinn.perceptron.Neuron;

public interface IGenerator {
	float generate(Neuron from, Neuron to);
}
