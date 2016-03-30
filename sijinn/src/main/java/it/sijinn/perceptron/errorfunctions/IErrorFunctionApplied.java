package it.sijinn.perceptron.errorfunctions;

import it.sijinn.perceptron.Network;

public interface IErrorFunctionApplied {
	float compute(Network network, float initialError);
	String toSaveString();
}
