package it.sijinn.perceptron.algorithms;

import it.sijinn.common.Network;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;

public interface ITrainingAlgorithm {
	final int SYNC_WEIGHT_DELTA = 1;
	ITrainingAlgorithm updateWeights(Network network) throws Exception;
	ITrainingAlgorithm calculate(Network network) throws Exception;
	ITrainingAlgorithm calculateAndUpdateWeights(Network network) throws Exception;
	ITrainingAlgorithm sync(Network network1, Network network2, int type) throws Exception;
	IDAFloatFunction getDeferredAgregateFunction();
	ITrainingAlgorithm setDeferredAgregateFunction(IDAFloatFunction deferredAgregateFunction);
	String getDefinition();
	String getId();
}
