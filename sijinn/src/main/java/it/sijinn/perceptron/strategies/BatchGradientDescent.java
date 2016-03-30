package it.sijinn.perceptron.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.deferredaggregatefunctions.IDAFloatFunction;
import it.sijinn.perceptron.deferredaggregatefunctions.MMA;
import it.sijinn.perceptron.errorfunctions.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IDataReader;
import it.sijinn.perceptron.utils.IReadLinesAggregator;
import it.sijinn.perceptron.utils.PairIO;

public class BatchGradientDescent extends GradientDescent implements ITrainingStrategy { 
	
	private int parallelLimit=0; 
	private long parallelTimeout=1000*60;
	private IDAFloatFunction deferredAggregateFunction;
	
	public BatchGradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction, IDAFloatFunction _defferedAggregationFunction, int _parallelLimit, long _parallelTimeout){
		super(_algorithm, _errorFunction);
		parallelLimit=_parallelLimit;
		parallelTimeout=_parallelTimeout;
		deferredAggregateFunction=_defferedAggregationFunction;
	}	
	
	public BatchGradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction, IDAFloatFunction _defferedAggregationFunction){
		super(_algorithm, _errorFunction);
		parallelLimit=0;
		deferredAggregateFunction=_defferedAggregationFunction;
	}	
	
	public BatchGradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction, int _parallelLimit, long _parallelTimeout){
		super(_algorithm, _errorFunction);
		parallelLimit=_parallelLimit;
		parallelTimeout=_parallelTimeout;
	}	

	public BatchGradientDescent(ITrainingAlgorithm _algorithm, int _parallelLimit, long _parallelTimeout){
		super(_algorithm);
		parallelLimit=_parallelLimit;
		parallelTimeout=_parallelTimeout;
	}
	
	public BatchGradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction){
		super(_algorithm, _errorFunction);
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
		final IDAFloatFunction daFunction = new MMA();


		Object next=null;
		int linenumber=0;
		if(dataReader.open()){
			next=null;
			linenumber=0;
			if(parallelLimit<=1){
				while((next = dataReader.readNext()) !=null){
					Object[] aggregated = dataAggregator.aggregate(next,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(network,aggregated);
						network.compute(param.getInput(), param.getOutput());
						if(deferredAggregateFunction!=null)
							algorithm.calculate(network, deferredAggregateFunction);		
						else
							algorithm.calculate(network, daFunction);
					}
					linenumber++;
				}
			}else{

				List<PairIO> accumulator = new ArrayList<PairIO>();
				while((next = dataReader.readNext()) !=null){
					if(accumulator.size()==parallelLimit){
						ExecutorService taskExecutor = Executors.newFixedThreadPool(parallelLimit);
						List<Future<ApplyExecutor>> list = new ArrayList<Future<ApplyExecutor>>();
						for(PairIO pair:accumulator)
							list.add(taskExecutor.submit(new ApplyExecutor(new Network(network), pair.getInput(), pair.getOutput(), daFunction)));
						taskExecutor.shutdown();
						try {
							taskExecutor.awaitTermination(parallelTimeout, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {				
						}
						for(Future<ApplyExecutor> future:list)
							algorithm.sync(network, future.get().getNetwork(), (deferredAggregateFunction!=null)?deferredAggregateFunction:daFunction, ITrainingAlgorithm.SYNC_WEIGHT_DELTA);
						
						accumulator.clear();
					}
					Object[] aggregated = dataAggregator.aggregate(next,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(network,aggregated);
						accumulator.add(param);
					}
					linenumber++;
				}
				if(accumulator.size()>0){
					ExecutorService taskExecutor = Executors.newFixedThreadPool(parallelLimit);
					List<Future<ApplyExecutor>> list = new ArrayList<Future<ApplyExecutor>>();
					for(PairIO pair:accumulator)
						list.add(taskExecutor.submit(new ApplyExecutor(new Network(network), pair.getInput(), pair.getOutput(), daFunction)));
					taskExecutor.shutdown();
					try {
						taskExecutor.awaitTermination(parallelTimeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {				
					}
					for(Future<ApplyExecutor> future:list)
						algorithm.sync(network, future.get().getNetwork(), (deferredAggregateFunction!=null)?deferredAggregateFunction:daFunction, ITrainingAlgorithm.SYNC_WEIGHT_DELTA);

					accumulator.clear();
				}
			}
			
 
			
			dataReader.close();
			dataReader.finalizer();
			
			algorithm.updateWeights(network);
			algorithm.clear(network);
		}
			
		
		
		if(dataReader.open()){
			next=null;
			linenumber=0;
			while((next = dataReader.readNext()) !=null){
				Object[] aggregated = dataAggregator.aggregate(next,linenumber);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
					network.compute(param.getInput(), param.getOutput());
					error+=errorFunction.compute(network, 0);
				}
				linenumber++;
			}
			dataReader.close();
			dataReader.finalizer();
		}
		return error;
	}

	public int getThreads() {
		return parallelLimit;
	}

	public IDAFloatFunction getDefferedAggregationFunction() {
		return deferredAggregateFunction;
	}
	
	public String toSaveString(){
		return "strategy="+this.getClass().getSimpleName()+","+parallelLimit+
				((algorithm==null)?"":"\n"+algorithm.toSaveString()+"")+
				((errorFunction==null)?"":"\n"+errorFunction.toSaveString()+"")+
				((deferredAggregateFunction==null)?"":"\n"+deferredAggregateFunction.toSaveString()+"");
	}
	
	
	
	class ApplyExecutor implements Callable<ApplyExecutor>{
		private Network network;
		private float[] input;
		private float[] output;
		private IDAFloatFunction daFunction;
		
		ApplyExecutor(Network _network, float[] _input, float[] _output, final IDAFloatFunction _daFunction){
			super();
			this.network = _network;
			this.input = _input;
			this.output = _output;
			this.daFunction = _daFunction;
		}

		@Override
		public ApplyExecutor call() throws Exception {
			
			network.compute(input, output);
			if(deferredAggregateFunction!=null)
				algorithm.calculate(network, deferredAggregateFunction);		
			else
				algorithm.calculate(network, daFunction);
			return this;
		}

		public Network getNetwork() {
			return network;
		}
		
	}
}
