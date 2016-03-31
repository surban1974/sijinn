package it.sijinn.perceptron.algorithms;

import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;

public abstract class TrainAlgorithm implements ITrainingAlgorithm {

	protected IDAFloatFunction deferredAgregateFunction;
	
	public IDAFloatFunction getDeferredAgregateFunction() {
		return deferredAgregateFunction;
	}

	public ITrainingAlgorithm setDeferredAgregateFunction(IDAFloatFunction deferredAgregateFunction) {
		this.deferredAgregateFunction = deferredAgregateFunction;
		return this;
	}
}
