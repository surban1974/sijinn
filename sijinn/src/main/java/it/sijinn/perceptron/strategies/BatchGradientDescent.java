package it.sijinn.perceptron.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class BatchGradientDescent extends OnlineGradientDescent implements ITrainingStrategy { 
	
	private int parallelLimit=0; 
	private long parallelTimeout=1000;
	

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
			next=null;
			linenumber=0;
			if(parallelLimit<=1){
				while((next = dataReader.readNext()) !=null){
					Object[] aggregated = dataAggregator.aggregate(next,linenumber);
						if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(network,aggregated);
							if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
						float out[] = network.compute(param.getInput(), param.getOutput());
						Utils.print(out, " ");
							if(listener!=null) listener.onAfterDataComputed(network,linenumber,param);
						algorithm.calculate(network);	
							if(listener!=null) listener.onAfterAlgorithmCalculated(network,algorithm,linenumber,param);
					}
					linenumber++;
				}
			}else{

				
				final List<PairIO> accumulator = new ArrayList<PairIO>();
				next = dataReader.readNext();
				while(next!=null || (next==null && accumulator.size()>0)){
					
					if((next!=null && accumulator.size()==parallelLimit) || (next==null && accumulator.size()>0)){
						
						final ExecutorService executorService = Executors.newFixedThreadPool(
								parallelLimit,
								new ThreadFactory() {					
									@Override
									 public Thread newThread(Runnable r) {
									     return new Thread(r, "BGD-"+algorithm.getClass().getSimpleName());
									   }
								}
							);

						
						final List<Future<ApplyExecutor>> list = new ArrayList<Future<ApplyExecutor>>();
						for(PairIO pair:accumulator)
							list.add(executorService.submit(new ApplyExecutor(new Network(network), pair.getInput(), pair.getOutput(), pair.getLinenumber())));
						executorService.shutdown();
						try {
							final boolean done = executorService.awaitTermination(parallelTimeout, TimeUnit.MILLISECONDS);
							if(!done)
								executorService.shutdownNow();
							
						} catch (InterruptedException e) {	
							e.toString();
						}
						for(Future<ApplyExecutor> future:list){
							algorithm.sync(network, future.get().getNetwork(), ITrainingAlgorithm.SYNC_WEIGHT_DELTA);
							future.get().getNetwork().release();
						}
						
						accumulator.clear();
						
					}
					
					if(next!=null){
						Object[] aggregated = dataAggregator.aggregate(next,linenumber);
							if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
						if(aggregated!=null){
							PairIO param = dataAggregator.getData(network,aggregated);
							param.setLinenumber(linenumber);
								if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
							accumulator.add(param);
						}
						linenumber++;
						next = dataReader.readNext();
					}

				}
				
/*				
				List<PairIO> accumulator = new ArrayList<PairIO>();
				while((next = dataReader.readNext()) !=null ){
					if(accumulator.size()==parallelLimit){
						ExecutorService taskExecutor = Executors.newFixedThreadPool(parallelLimit);
						List<Future<ApplyExecutor>> list = new ArrayList<Future<ApplyExecutor>>();
						for(PairIO pair:accumulator)
							list.add(taskExecutor.submit(new ApplyExecutor(new Network(network), pair.getInput(), pair.getOutput(), linenumber)));
						taskExecutor.shutdown();
						try {
							final boolean done = taskExecutor.awaitTermination(parallelTimeout, TimeUnit.MILLISECONDS);
							if(!done)
								taskExecutor.shutdownNow();
						} catch (InterruptedException e) {	
							e.toString();
						}
						for(Future<ApplyExecutor> future:list)
							algorithm.sync(network, future.get().getNetwork(), ITrainingAlgorithm.SYNC_WEIGHT_DELTA);
						
						accumulator.clear();
					}
					Object[] aggregated = dataAggregator.aggregate(next,linenumber);
						if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(network,aggregated);
							if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
						accumulator.add(param);
					}
					linenumber++;
				}
				if(accumulator.size()>0){
					ExecutorService taskExecutor = Executors.newFixedThreadPool(parallelLimit);
					List<Future<ApplyExecutor>> list = new ArrayList<Future<ApplyExecutor>>();
					for(PairIO pair:accumulator)
						list.add(taskExecutor.submit(new ApplyExecutor(new Network(network), pair.getInput(), pair.getOutput(), linenumber)));
					taskExecutor.shutdown();
					try {
						taskExecutor.awaitTermination(parallelTimeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {				
					}
					for(Future<ApplyExecutor> future:list)
						algorithm.sync(network, future.get().getNetwork(), ITrainingAlgorithm.SYNC_WEIGHT_DELTA);

					accumulator.clear();
				}
*/				
			}
			
 
			
			dataReader.close();
			dataReader.finalizer();
			
			algorithm.updateWeights(network);
				if(listener!=null) listener.onAfterAlgorithmUpdated(network,algorithm,linenumber,null);
			algorithm.clear(network);
		}
			
		
		
		if(dataReader.open()){
				if(listener!=null) listener.onAfterReaderOpen(network,dataReader);
			next=null;
			linenumber=0;
			while((next = dataReader.readNext()) !=null){
				Object[] aggregated = dataAggregator.aggregate(next,linenumber);
					if(listener!=null) listener.onAfterLinePrepared(network,linenumber,aggregated);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
						if(listener!=null) listener.onAfterDataPrepared(network,linenumber,param);
					network.compute(param.getInput(), param.getOutput());
						if(listener!=null) listener.onAfterDataComputed(network,linenumber,param);
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
		return error;
	}

	public int getThreads() {
		return parallelLimit;
	}

	
	public String toSaveString(){
		return "strategy="+this.getClass().getSimpleName()+","+parallelLimit+
				((algorithm==null)?"":"\n"+algorithm.toSaveString()+"")+
				((errorFunction==null)?"":"\n"+errorFunction.toSaveString()+"");
	}
	
	
	
	class ApplyExecutor implements Callable<ApplyExecutor>{
		private Network network;
		private float[] input;
		private float[] output;
		private int linenumber;
		
		ApplyExecutor(Network _network, float[] _input, float[] _output, int _linenumber){
			super();
			this.network = _network;
			this.input = _input;
			this.output = _output;
			this.linenumber = _linenumber;
		}

		@Override
		public ApplyExecutor call() throws Exception {
			
			network.compute(input, output);
				if(listener!=null) listener.onAfterDataComputed(network,linenumber,new PairIO(input, output));
			algorithm.calculate(network);
				if(listener!=null) listener.onAfterAlgorithmCalculated(network,algorithm,linenumber,new PairIO(input, output));
			return this;
		}

		public Network getNetwork() {
			return network;
		}
		
	}



	public BatchGradientDescent setParallelLimit(int parallelLimit) {
		this.parallelLimit = parallelLimit;
		return this;
	}


	public BatchGradientDescent setParallelTimeout(long parallelTimeout) {
		this.parallelTimeout = parallelTimeout;
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
}
