package it.sijinn.perceptron.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class BatchGradientDescent extends OnlineGradientDescent implements ITrainingStrategy { 
	
	private int parallelLimit=0; 
	
	public BatchGradientDescent(){
		super();
		parallelLimit=0;
	}

	public BatchGradientDescent(ITrainingAlgorithm _algorithm){
		super(_algorithm);
		parallelLimit=0;
	}	
	
	
	@Override
	public float apply(Network network, final IDataReader dataReader, final IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null || dataReader==null)
			return -1;
		float error = 0;
		if(algorithm.getDeferredAgregateFunction()==null)
			algorithm.setDeferredAgregateFunction(new SUMMATOR());

		Object next=null;
		int linenumber=0;
		if(dataReader.open()){

			linenumber=0;
			if(parallelLimit<=1){
				while((next = dataReader.readNext()) !=null){
					final Object[] aggregated = dataAggregator.aggregate(next,linenumber);
					if(listener!=null) 
						listener.onAfterLinePrepared(network,linenumber,aggregated);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(network,aggregated);
						if(listener!=null) 
							listener.onAfterDataPrepared(network,linenumber,param);
						network.compute(param.getInput(), param.getOutput(), reversed);							
						if(listener!=null) 
							listener.onAfterDataComputed(network,linenumber,param,reversed);
						algorithm.calculate(network,reversed);	
						if(listener!=null) 
							listener.onAfterAlgorithmCalculated(network,algorithm,linenumber,param, reversed);
					}
					linenumber++;
				}
			}else{				
				final List<PairIO> accumulator = new ArrayList<>();
				next = dataReader.readNext();
				while(next!=null || (next==null && !accumulator.isEmpty())){
					
					if((next!=null && accumulator.size()==parallelLimit) || (next==null && !accumulator.isEmpty())){
						

						final Stream<PairIO> stream = accumulator.parallelStream();
						
						stream.forEachOrdered(pair ->
							{
								try {
									final Network cNetwork = new Network(network);
									final PairIO cPair = new PairIO(pair.getInput(), pair.getOutput(), pair.getLinenumber());
									cNetwork.compute(cPair.getInput(), cPair.getOutput(), reversed);
									if(listener!=null) 
										listener.onAfterDataComputed(cNetwork,cPair.getLinenumber(),cPair,reversed);
									algorithm.calculate(cNetwork,reversed);
									if(listener!=null) 
										listener.onAfterAlgorithmCalculated(cNetwork,algorithm,cPair.getLinenumber(),cPair, reversed);
									algorithm.sync(
											network, 
											cNetwork,
											ITrainingAlgorithm.SYNC_WEIGHT_DELTA,
											reversed
									);
								}catch (Exception e) {
									network.obtainLogger().error(e);
								}
								
						});
				
						accumulator.clear();
						
					}
					
					if(next!=null){
						final Object[] aggregated = dataAggregator.aggregate(next,linenumber);
						if(listener!=null) 
							listener.onAfterLinePrepared(network,linenumber,aggregated);
						if(aggregated!=null){
							final PairIO param = dataAggregator.getData(network,aggregated);
							param.setLinenumber(linenumber);
							if(listener!=null) 
								listener.onAfterDataPrepared(network,linenumber,param);
							accumulator.add(param);
						}
						linenumber++;
						next = dataReader.readNext();
					}
				}
			}			
 
			
			dataReader.close();
			dataReader.finalizer();
			
			algorithm.updateWeights(network,reversed);
			if(listener!=null) 
				listener.onAfterAlgorithmUpdated(network,algorithm,linenumber,null, reversed);
		}
			
		
		
		if(dataReader.open()){
				if(listener!=null) listener.onAfterReaderOpen(network,dataReader);
			linenumber=0;
			while((next = dataReader.readNext()) !=null){
				final Object[] aggregated = dataAggregator.aggregate(next,linenumber);
				if(listener!=null) 
					listener.onAfterLinePrepared(network,linenumber,aggregated);
				if(aggregated!=null){
					final PairIO param = dataAggregator.getData(network,aggregated);
					if(listener!=null) 
						listener.onAfterDataPrepared(network,linenumber,param);
					network.compute(param.getInput(), param.getOutput(), reversed);
					if(listener!=null) 
						listener.onAfterDataComputed(network,linenumber,param,reversed);
					error+=errorFunction.compute(network, 0, reversed);
					if(listener!=null) 
						listener.onAfterErrorComputed(network,error,linenumber,param,reversed);
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
		return error;
	}

	public int getThreads() {
		return parallelLimit;
	}

	
	public String toSaveString(){
		return "strategy="+this.getClass().getSimpleName()+","+parallelLimit+
				((algorithm==null)?"":"\n"+algorithm.getDefinition()+"")+
				((errorFunction==null)?"":"\n"+errorFunction.getDefinition()+"");
	}
	

	public BatchGradientDescent setParallelLimit(int parallelLimit) {
		this.parallelLimit = parallelLimit;
		return this;
	}


	public BatchGradientDescent setParallelTimeout(long parallelTimeout) {
		return this;
	}
	
	public BatchGradientDescent setTrainingAlgorithm(final ITrainingAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public BatchGradientDescent setErrorFunction(final IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}
	
	public BatchGradientDescent setListener(IStrategyListener _listener){
		this.listener = _listener;
		if(this.listener!=null)
			this.listener.setTrainingStrategy(this);
		return this;
	}	
	
	@Override
	public BatchGradientDescent setReversed(boolean reversed) {
		this.reversed = reversed;
		return this;
	}	
	

}
