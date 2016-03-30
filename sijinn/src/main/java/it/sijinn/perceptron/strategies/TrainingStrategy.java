package it.sijinn.perceptron.strategies;

import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;

public abstract class TrainingStrategy implements ITrainingStrategy {

	protected ITrainingAlgorithm algorithm;
	protected IErrorFunctionApplied errorFunction;
	
	
	public TrainingStrategy(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction){
		super();
		this.algorithm = _algorithm;
		this.errorFunction = _errorFunction;
	}	

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

}
