package it.sijinn.perceptron.strategies;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class AutoBalancedGradientDescent extends OnlineGradientDescent implements ITrainingStrategy { 
	
	private boolean initialised=false;
	private PairIO parameters=null;
	
	public AutoBalancedGradientDescent(){
		super();
	}
	
	public AutoBalancedGradientDescent(ITrainingAlgorithm _algorithm){
		super(_algorithm);
	}

	@Override
	public float apply(Network network, final IDataReader dataReader, final IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null || dataReader==null)
			return -1;

		if(!initialised){
			if(dataReader.open()){
					if(listener!=null) listener.onAfterReaderOpen(network,dataReader);
				Object line = null;
				int linenumber=0;
				if ((line = dataReader.readNext()) != null) {
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(listener!=null) 
						listener.onAfterLinePrepared(network,linenumber,aggregated);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(network,aggregated);
						if(listener!=null) 
							listener.onAfterDataPrepared(network,linenumber,param);
							
						parameters = param;	
							
						network.setInputValues(0, param.getInput());
						network.setInputValuesReversed(0, param.getInput());
						network.setOutputValues(0, param.getOutput());
						network.setOutputValuesReversed(0, param.getOutput());
					}
					linenumber++;
				}
				dataReader.close();
				if(listener!=null) 
					listener.onAfterReaderClose(network,dataReader);
				dataReader.finalizer();
				if(listener!=null) 
					listener.onAfterReaderFinalize(network,dataReader);
			}
			initialised=true;
		}
		
		float errorDirect = 0;
		float errorReverse = 0;
		
		network.compute(false);
		if(listener!=null) 
			listener.onAfterDataComputed(network,0,parameters, false);
		algorithm.calculateAndUpdateWeights(network, false);
		if(listener!=null) 
			listener.onAfterAlgorithmCalculatedAndUpdated(network,algorithm,0,parameters, false);						
		errorDirect=errorFunction.compute(network, 0, false);
		if(listener!=null) 
			listener.onAfterErrorComputed(network,errorDirect,0,parameters, false);	
				
		network.compute(true);
		if(listener!=null) 
			listener.onAfterDataComputed(network,0,parameters, true);
		algorithm.calculateAndUpdateWeights(network, true);
		if(listener!=null) 
			listener.onAfterAlgorithmCalculatedAndUpdated(network,algorithm,0,parameters, true);						
		errorReverse=errorFunction.compute(network, 0, true);
		if(listener!=null) 
			listener.onAfterErrorComputed(network,errorReverse,0,parameters, true);				
		
		
		return Math.min(errorDirect, errorReverse);
	}
	
	public AutoBalancedGradientDescent setTrainingAlgorithm(final ITrainingAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public AutoBalancedGradientDescent setErrorFunction(final IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}
	
	public AutoBalancedGradientDescent setListener(IStrategyListener _listener){
		if(this.listener!=null)
			this.listener.setTrainingStrategy(this);
		return this;
	}

	@Override
	public AutoBalancedGradientDescent setReversed(boolean reversed) {
		this.reversed = reversed;
		return this;
	}	
	

}
