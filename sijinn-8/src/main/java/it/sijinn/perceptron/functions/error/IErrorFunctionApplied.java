package it.sijinn.perceptron.functions.error;

import it.sijinn.common.Network;

public interface IErrorFunctionApplied {
	float compute(Network network, float initialError, boolean reversed);
	String getDefinition();
	String getId();
	IErrorFunctionApplied setRegularizationL1(boolean l1, float alfa1);
	IErrorFunctionApplied setRegularizationL2(boolean l2, float alfa2);
}
