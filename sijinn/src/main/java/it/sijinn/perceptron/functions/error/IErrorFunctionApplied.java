package it.sijinn.perceptron.functions.error;

import it.sijinn.common.Network;

public interface IErrorFunctionApplied {
	float compute(Network network, float initialError);
	String getDefinition();
}
