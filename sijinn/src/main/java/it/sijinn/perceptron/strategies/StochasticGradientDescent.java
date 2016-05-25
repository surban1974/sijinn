package it.sijinn.perceptron.strategies;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class StochasticGradientDescent extends OnlineGradientDescent implements ITrainingStrategy { 
	
	public StochasticGradientDescent(){
		super();
	}
	
	public StochasticGradientDescent(ITrainingAlgorithm _algorithm){
		super(_algorithm);
	}

	@Override
	public float apply(Network network, final IDataReader dataReader, final IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null || dataReader==null)
			return -1;

		float error = 0;
		

		if(dataReader.open()){
				if(listener!=null) listener.onAfterReaderOpen(network,dataReader);
			Object line = null;
			int linenumber=0;
			while ((line = dataReader.readNext()) != null) {
				Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
						if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
						
					network.compute(param.getInput(), param.getOutput(), reversed);
						if(listener!=null) listener.onAfterDataComputed(network,linenumber,param, reversed);
					algorithm.calculateAndUpdateWeights(network, reversed);
						if(listener!=null) listener.onAfterAlgorithmCalculatedAndUpdated(network,algorithm,linenumber,param, reversed);						
					error+=errorFunction.compute(network, 0, reversed);
						if(listener!=null) listener.onAfterErrorComputed(network,error,linenumber,param, reversed);
	
						
						
				}
				linenumber++;
			}
			dataReader.close();
				if(listener!=null) listener.onAfterReaderClose(network,dataReader);
			dataReader.finalizer();
				if(listener!=null) listener.onAfterReaderFinalize(network,dataReader);
		}
		
		return error;
	}
	
	public StochasticGradientDescent setTrainingAlgorithm(final ITrainingAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public StochasticGradientDescent setErrorFunction(final IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}
	
	public StochasticGradientDescent setListener(IStrategyListener _listener){
		if(this.listener!=null)
			this.listener.setTrainingStrategy(this);
		return this;
	}

	@Override
	public StochasticGradientDescent setReversed(boolean reversed) {
		this.reversed = reversed;
		return this;
	}	
	

}
