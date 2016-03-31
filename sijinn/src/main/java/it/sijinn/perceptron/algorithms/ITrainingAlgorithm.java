package it.sijinn.perceptron.algorithms;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;

public interface ITrainingAlgorithm {
	final int SYNC_WEIGHT_DELTA = 1;
	ITrainingAlgorithm updateWeights(Network network);
	ITrainingAlgorithm calculate(Network network);
	ITrainingAlgorithm calculateAndUpdateWeights(Network network);
	ITrainingAlgorithm clear(Network network);
	ITrainingAlgorithm sync(Network network1, Network network2, int type);
	IDAFloatFunction getDeferredAgregateFunction();
	ITrainingAlgorithm setDeferredAgregateFunction(IDAFloatFunction deferredAgregateFunction);
	String toSaveString();
}
