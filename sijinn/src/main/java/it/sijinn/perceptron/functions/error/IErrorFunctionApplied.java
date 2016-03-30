package it.sijinn.perceptron.functions.error;

import it.sijinn.perceptron.Network;

public interface IErrorFunctionApplied {
	float compute(Network network, float initialError);
	String toSaveString();
}
