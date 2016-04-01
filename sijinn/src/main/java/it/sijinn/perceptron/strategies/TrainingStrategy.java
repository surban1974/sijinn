package it.sijinn.perceptron.strategies;

import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.utils.IStrategyListener;

public abstract class TrainingStrategy implements ITrainingStrategy {

	protected ITrainingAlgorithm algorithm;
	protected IErrorFunctionApplied errorFunction;
	protected IStrategyListener listener;
	


	public TrainingStrategy(ITrainingAlgorithm _algorithm){
		super();
		this.algorithm = _algorithm;
		this.errorFunction = new MSE();
	}	
	

	public ITrainingAlgorithm getTrainingAlgorithm() {
		return algorithm;
	}


	public IErrorFunctionApplied getErrorFunction() {
		return errorFunction;
	}

	public ITrainingStrategy setTrainingAlgorithm(final ITrainingAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public ITrainingStrategy setErrorFunction(final IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}
	
	public ITrainingStrategy setListener(IStrategyListener _listener){
		if(this.listener!=null)
			this.listener.setTrainingStrategy(this);
		return this;
	}

}
