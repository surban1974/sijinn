package it.sijinn.perceptron.strategies;

import java.io.File;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.SimpleArrayReader;
import it.sijinn.perceptron.utils.io.SimpleFileReader;
import it.sijinn.perceptron.utils.io.SimpleStreamReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;
import it.sijinn.perceptron.utils.parser.SimpleArrayDataAggregator;

public class OnlineGradientDescent extends TrainingStrategy implements ITrainingStrategy { 
	
	public OnlineGradientDescent(){
		super();
	}
	
	public OnlineGradientDescent(ITrainingAlgorithm _algorithm){
		super(_algorithm);
	}
	
	@Override
	public float apply(Network network, final float[][][] data) throws Exception{
		if(network==null)
			return -1;

		
		final IDataReader reader = new SimpleArrayReader(data);
		final IReadLinesAggregator aggregator = new SimpleArrayDataAggregator();
		return apply(network, reader, aggregator);
		
		
		
/*		
		for(int i=0;i<data.length;i++){
			network.compute(data[i][0], data[i][1]);		
			algorithm.updateGradients(network);	
			algorithm.updateWeights(_network);	
			error+=errorFunction.compute(network, 0);	
		}		
	
		return error;
*/
		
		

	}
	
	public float apply(Network network, final File file, final IReadLinesAggregator dataAggregator) throws Exception{
		if(network==null)
			return -1;

		final IDataReader reader = new SimpleFileReader(file);
		return apply(network, reader, dataAggregator);
	}
	
	@Override
	public float apply(Network network, final IStreamWrapper streamWrapper, final IReadLinesAggregator dataAggregator) throws Exception{
		if(network==null)
			return -1;
	
		
		final IDataReader reader = new SimpleStreamReader(streamWrapper);		
		return apply(network, reader, dataAggregator);
		

	}
	
	@Override
	public float apply(Network network, final IDataReader dataReader, final IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null || dataReader==null)
			return -1;

		float error = 0;
		
	
		if(dataReader.open()){
				if(listener!=null) listener.onAfterReaderOpen(network,dataReader);
			Object next=null;
			int linenumber=0;
			while((next = dataReader.readNext()) !=null){
				Object[] aggregated = dataAggregator.aggregate(next,linenumber);
					if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
						if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
					network.compute(param.getInput(), param.getOutput());
						if(listener!=null) listener.onAfterDataComputed(network,linenumber,param);
					algorithm.calculate(network);	
						if(listener!=null) listener.onAfterAlgorithmCalculated(network,algorithm,linenumber,param);
					algorithm.updateWeights(network);
						if(listener!=null) listener.onAfterAlgorithmUpdated(network,algorithm,linenumber,param);
					error+=errorFunction.compute(network, 0);
						if(listener!=null) listener.onAfterErrorComputed(network,error,linenumber,param);					
				}
				linenumber++;
			}
			dataReader.close();
				if(listener!=null) listener.onAfterReaderClose(network,dataReader);
			dataReader.finalizer();
				if(listener!=null) listener.onAfterReaderFinalize(network,dataReader);
		}
//		algorithm.clear(network);
		
		return error;
	}
	
	public String toSaveString(String prefix){
		String result = (prefix!=null)?prefix:"";
		result+= "<strategy>"+
				Utils.normalXML(
					this.getClass().getSimpleName()+
					((algorithm==null)?"":";"+algorithm.getDefinition()+"")+
					((errorFunction==null)?"":";"+errorFunction.getDefinition()+"")
				,"utf8")+
				"</strategy>\n";
		return result;
	}
	
	public OnlineGradientDescent setTrainingAlgorithm(final ITrainingAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public OnlineGradientDescent setErrorFunction(final IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}
	
	public OnlineGradientDescent setListener(IStrategyListener _listener){
		if(this.listener!=null)
			this.listener.setTrainingStrategy(this);
		return this;
	}	
	

	
}
