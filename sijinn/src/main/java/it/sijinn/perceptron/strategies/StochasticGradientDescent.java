package it.sijinn.perceptron.strategies;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.errorfunctions.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IReadLinesAggregator;
import it.sijinn.perceptron.utils.IDataReader;
import it.sijinn.perceptron.utils.PairIO;

public class StochasticGradientDescent extends GradientDescent implements ITrainingStrategy { 
	
	
	public StochasticGradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction){
		super(_algorithm, _errorFunction);
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
			Object line = null;
			int linenumber=0;
			while ((line = dataReader.readNext()) != null) {
				Object[] aggregated = dataAggregator.aggregate(line,linenumber);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
					network.compute(param.getInput(), param.getOutput());
					algorithm.calculateAndUpdateWeights(network);
					error+=errorFunction.compute(network, 0);
				}
				linenumber++;
			}
			dataReader.close();
			dataReader.finalizer();
		}
		algorithm.clear(network);
		
		return error;
	}
}
