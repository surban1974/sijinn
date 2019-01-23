package it.sijinn.perceptron.functions.generator;

import java.io.Serializable;

import it.sijinn.common.Neuron;

public interface IGenerator extends Serializable{
	float generate(Neuron from, Neuron to);
}
