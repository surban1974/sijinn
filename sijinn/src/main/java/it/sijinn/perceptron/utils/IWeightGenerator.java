package it.sijinn.perceptron.utils;

import it.sijinn.perceptron.Neuron;

public interface IWeightGenerator {
	float generate(Neuron from, Neuron to);
}
