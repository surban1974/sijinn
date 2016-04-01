package it.sijinn.perceptron.strategies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class OnlineGradientDescent extends TrainingStrategy implements ITrainingStrategy { 
	
	public OnlineGradientDescent(ITrainingAlgorithm _algorithm){
		super(_algorithm);
	}
	
	@Override
	public float apply(Network network, final float[][][] data) throws Exception{
		if(network==null)
			return -1;

		
		final IDataReader reader = new IDataReader() {
			private int current = 0;
			private int finish = 0;
			@Override
			public boolean open() throws Exception {
				finish = data.length;
				return true;
			}
			
			@Override
			public Object readNext() throws Exception {
				float[][] result = null;
				if(current<finish){
					result = data[current];
					current++;
				}
				return result;
			}
			
			@Override
			public boolean finalizer() throws Exception {
				return true;
			}
			
			@Override
			public boolean close() throws Exception {
				current=0;
				return true;
			}
		};
		
		final IReadLinesAggregator aggregator = new IReadLinesAggregator() {
			
			@Override
			public PairIO getData(Network network, Object[] lines) {
				if(lines==null || lines.length==0)
					return null;
				float[][] f = (float[][])lines[0];
				return new PairIO(f[0], f[1]);
			}
			
			@Override
			public Object[] aggregate(Object line, int linenumber) {
				if(line==null)
					return null;
				else 
					return new Object[]{line};
			}
			
			@Override
			public Object getRowData(Network network, Object[] objs) {
				if(objs==null || objs.length==0)
					return null;
				return objs[0];
			}			
		};
		
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


		final IDataReader reader = new IDataReader() {
			
			private BufferedReader breader = null;
			
			@Override
			public boolean open() throws Exception {
				if(file!=null && file.exists()){
					breader = new BufferedReader(new FileReader(file));	
					return true;
				}else
					return false;
				
			}
			
			@Override
			public Object readNext() throws Exception {
				if(breader==null)
					return null;
				else
					return breader.readLine();
			}
			
			@Override
			public boolean finalizer() throws Exception {
				breader = null;
				return true;
			}
			
			@Override
			public boolean close() throws Exception {
				if(breader!=null)
					breader.close();
				return true;
			}
		};
		
		return apply(network, reader, dataAggregator);
	}
	
	@Override
	public float apply(Network network, final IStreamWrapper streamWrapper, final IReadLinesAggregator dataAggregator) throws Exception{
		if(network==null)
			return -1;
	
		
		final IDataReader reader = new IDataReader() {
			
			private InputStream stream = null;
			private BufferedReader breader = null;
			private IStreamWrapper wrapper = streamWrapper.instance();
			
			@Override
			public boolean open() throws Exception {
				if(streamWrapper!=null){
					stream = wrapper.openStream();
					breader = new BufferedReader(new InputStreamReader(stream));	
					return true;
				}else
					return false;
				
			}
			
			@Override
			public Object readNext() throws Exception {
				if(breader==null)
					return null;
				else
					return breader.readLine();
			}
			
			@Override
			public boolean finalizer() throws Exception {
				breader = null;
				stream = null;				
				return true;
			}
			
			@Override
			public boolean close() throws Exception {
				if(breader!=null)
					breader.close();
				if(stream!=null)
					stream.close();
				if(wrapper!=null)
					wrapper.closeStream();
				return true;
			}
		};
		
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
		algorithm.clear(network);
		
		return error;
	}
	
	public String toSaveString(){
		return "strategy="+this.getClass().getSimpleName()+
				((algorithm==null)?"":"\n"+algorithm.toSaveString()+"")+
				((errorFunction==null)?"":"\n"+errorFunction.toSaveString()+"");
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
