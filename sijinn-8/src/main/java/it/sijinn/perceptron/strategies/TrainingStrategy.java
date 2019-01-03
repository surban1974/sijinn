package it.sijinn.perceptron.strategies;

import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.Utils;

public abstract class TrainingStrategy implements ITrainingStrategy {

	protected ITrainingAlgorithm algorithm;
	protected IErrorFunctionApplied errorFunction;
	protected IStrategyListener listener;
	protected boolean reversed = false;
	

	public TrainingStrategy(){
		super();
		this.errorFunction = new MSE();
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
	
	public String getDefinition(){
		String result = "";
		result+= 
				Utils.normalXML(
					this.getClass().getSimpleName()+
					((algorithm==null)?"":";"+algorithm.getDefinition()+"")+
					((errorFunction==null)?"":";"+errorFunction.getDefinition()+"")
				,"utf8");
		return result;
	}

	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}

	public boolean isReversed() {
		return reversed;
	}

	public ITrainingStrategy setReversed(boolean reversed) {
		this.reversed = reversed;
		return this;
	}
}
