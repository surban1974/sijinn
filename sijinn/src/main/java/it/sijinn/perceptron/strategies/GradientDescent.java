package it.sijinn.perceptron.strategies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IDataReader;
import it.sijinn.perceptron.utils.IReadLinesAggregator;
import it.sijinn.perceptron.utils.IStreamWrapper;
import it.sijinn.perceptron.utils.PairIO;

public class GradientDescent extends TrainingStrategy implements ITrainingStrategy { 
	
	
	public GradientDescent(ITrainingAlgorithm _algorithm, IErrorFunctionApplied _errorFunction){
		super(_algorithm, _errorFunction);
	}	

	public GradientDescent(ITrainingAlgorithm _algorithm){
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
			Object next=null;
			int linenumber=0;
			while((next = dataReader.readNext()) !=null){
				Object[] aggregated = dataAggregator.aggregate(next,linenumber);
				if(aggregated!=null){
					PairIO param = dataAggregator.getData(network,aggregated);
					network.compute(param.getInput(), param.getOutput());
					algorithm.calculate(network, null);		
					algorithm.updateWeights(network);
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
	
	public String toSaveString(){
		return "strategy="+this.getClass().getSimpleName()+
				((algorithm==null)?"":"\n"+algorithm.toSaveString()+"")+
				((errorFunction==null)?"":"\n"+errorFunction.toSaveString()+"");
	}
	
}
