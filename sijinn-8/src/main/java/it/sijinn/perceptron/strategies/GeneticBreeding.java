package it.sijinn.perceptron.strategies;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.IGeneticAlgorithm;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.genetic.Species;
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

public class GeneticBreeding implements ITrainingStrategy {

	private static final long serialVersionUID = 1L;
	protected ITrainingAlgorithm algorithm;
	protected IErrorFunctionApplied errorFunction;
	protected IStrategyListener listener;
	protected boolean reversed = false;
	private int parallelLimit=0;

	public GeneticBreeding(){
		super();
	}
	
	public GeneticBreeding(ITrainingAlgorithm _algorithm){
		super();
		this.algorithm = _algorithm;
		this.errorFunction = new MSE();
	}	

	@Override
	public float apply(Network network, float[][][] data) throws Exception {
		if(network==null)
			return -1;

		
		final IDataReader reader = new SimpleArrayReader(data);
		final IReadLinesAggregator aggregator = new SimpleArrayDataAggregator();
		return apply(network, reader, aggregator);
	}

	@Override
	public float apply(Network network, File file, IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null)
			return -1;

		final IDataReader reader = new SimpleFileReader(file);
		return apply(network, reader, dataAggregator);
	}

	@Override
	public float apply(Network network, IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator)
			throws Exception {
		if(network==null)
			return -1;
	
		
		final IDataReader reader = new SimpleStreamReader(streamWrapper);		
		return apply(network, reader, dataAggregator);
	}

	
	
	@Override
	public float apply(Network network, final IDataReader dataReader, final IReadLinesAggregator dataAggregator) throws Exception {
		if(network==null || dataReader==null || algorithm==null || !(algorithm instanceof IGeneticAlgorithm))
			return -1;
	
		algorithm.calculate(network, reversed);	
		if(listener!=null) 
			listener.onAfterAlgorithmCalculated(network,algorithm,-1,null, reversed);		
		
			
		if(parallelLimit<=1){	
			for(final Species entity: ((IGeneticAlgorithm)algorithm).getPopulation().getSpecies())
				calculateFitness(network, dataReader, dataAggregator, entity);
		}else{
			final List<Species> accumulator = new ArrayList<>();
			Species next = null;
			final Iterator<Species> it = ((IGeneticAlgorithm)algorithm).getPopulation().getSpeciesIterable().iterator();


			while((next= (it.hasNext())?it.next():null)!=null || !accumulator.isEmpty()){
				if((next!=null && accumulator.size()==parallelLimit) || (next==null && !accumulator.isEmpty())){

					final Stream<Species> stream = accumulator.parallelStream();
					
					stream.forEachOrdered(species-> {
							try {
								calculateFitness(new Network(network), dataReader, dataAggregator, species);								
							}catch (Exception e) {
								network.obtainLogger().error(e);
							}
							
					});
					accumulator.clear();
				}
					
				if(next!=null)
					accumulator.add(next);
			}
		}
		
		algorithm.updateWeights(network, reversed);
		if(listener!=null) 
			listener.onAfterAlgorithmUpdated(network,algorithm,-1,null, reversed);		
		
		return network.getError();
	}	
	
	private float calculateFitness(Network network, IDataReader dataReader, IReadLinesAggregator dataAggregator, Species entity) throws Exception{
		float error = 0;
		
		network.setWeight(entity.getWeights());	
		if(dataReader.open()){
			if(listener!=null) 
				listener.onAfterReaderOpen(network,dataReader);
			Object next=null;
			int linenumber=0;

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
							listener.onAfterDataComputed(network,linenumber,param, reversed);
					error+=errorFunction.compute(network, 0, reversed);
						if(listener!=null) 
							listener.onAfterErrorComputed(network,error,linenumber,param, reversed);					
				}
				linenumber++;
				
			}
			dataReader.close();
				if(listener!=null) listener.onAfterReaderClose(network,dataReader);
			dataReader.finalizer();
				if(listener!=null) listener.onAfterReaderFinalize(network,dataReader);
		}
		entity.setFitness(error);
		return error;	
	}

	@Override
	public ITrainingAlgorithm getTrainingAlgorithm() {
		return algorithm;
	}

	@Override
	public ITrainingStrategy setTrainingAlgorithm(ITrainingAlgorithm trainingAlgorithm) {
		this.algorithm = trainingAlgorithm;
		return this;
	}

	@Override
	public ITrainingStrategy setListener(IStrategyListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	public IErrorFunctionApplied getErrorFunction() {
		return errorFunction;
	}

	@Override
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

	@Override
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
	public ITrainingStrategy setErrorFunction(IErrorFunctionApplied errorFunction) {
		this.errorFunction = errorFunction;
		return this;
	}

	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}

	public int getParallelLimit() {
		return parallelLimit;
	}

	public GeneticBreeding setParallelLimit(int parallelLimit) {
		this.parallelLimit = parallelLimit;
		return this;
	}

	public GeneticBreeding setParallelTimeout(long parallelTimeout) {
		return this;
	}

	public boolean isReversed() {
		return reversed;
	}

	public GeneticBreeding setReversed(boolean reversed) {
		this.reversed = reversed;
		return this;
	}	

}
