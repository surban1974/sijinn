package it.sijinn.perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.IDataWriter;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.SimpleArrayReader;
import it.sijinn.perceptron.utils.io.SimpleFileReader;
import it.sijinn.perceptron.utils.io.SimpleFileWriter;
import it.sijinn.perceptron.utils.io.SimpleStreamReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class Network extends Neuron implements Serializable{
	private static final long serialVersionUID = 1L;
	protected List<List<Neuron>> layers;	
	protected float error=-1;
	

	
	public Network(){
		super();
	}
	
	public Network(Network another){
		super();
		if(another!=null){
			error=-1;
			if(another.getLayers()!=null){
				layers = new ArrayList<List<Neuron>>();
				for(List<Neuron> list:another.getLayers()){
					layers.add(new ArrayList<Neuron>());
					for(Neuron neuron:list)
						layers.get(layers.size()-1).add(new Neuron(this,neuron));
				}
				for(Synapse synapse:another.getSynapses())
					this.addSynapse(
						new Synapse(
								synapse.getFrom().getLayer(),
								synapse.getFrom().getOrder(),
								synapse.getFrom().getFunction(),
								synapse.getTo().getLayer(),
								synapse.getTo().getOrder(),
								synapse.getTo().getFunction(),
								synapse.getWeight()),
						false
					);
			}
		}
	}
	
	public Network(List<List<Neuron>> _layers){
		super();
		if(_layers!=null && _layers.size()>0)
			this.layers = _layers;
		indexingPositionOfNeurons();
	}
	
	public Network(List<List<Neuron>> _layers, float ititialWeight){
		super();
		if(_layers!=null && _layers.size()>0)
			this.layers = _layers;
		indexingPositionOfNeurons();
		createSynapses(ititialWeight);
	}
	
	public Network(List<List<Neuron>> _layers, IGenerator weightGenerator){
		super();
		if(_layers!=null && _layers.size()>0)
			this.layers = _layers;
		indexingPositionOfNeurons();
		createSynapses(weightGenerator);
	}	
	
	
	public static List<Neuron> createLayer(int neurons){
		return createLayer(neurons, null);
	}	
	public static List<Neuron> createLayer(int neurons, IFunctionApplied function){
		if(neurons<=0)
			return null;
		List<Neuron> layer = new ArrayList<Neuron>();
		for(int i=0;i<neurons;i++)
			layer.add(new Neuron(null, function));
		return layer;
	}
	

	public Network addLayer(int neurons){
		return addLayer(neurons, null);
	}
	public Network addLayer(int neurons, IFunctionApplied function){
		if(neurons<=0)
			return this;

		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();

		List<Neuron> layer = new ArrayList<Neuron>();

		for(int i=0;i<neurons;i++)
			layer.add(new Neuron(this, function));
		
		layers.add(layer);
		indexingPositionOfNeurons();
		return this;
	}
	
	
	public Network release(){
		if(layers!=null)
			layers.clear();
		return this;
	}

	
	public Network indexingPositionOfNeurons(){

		if((layers!=null && layers.size()>0)){
			for(int h=0;h<layers.size();h++){
				List<Neuron> layer = layers.get(h);
				for(int i=0; i<layer.size();i++){
					Neuron neuron = layer.get(i);
					if(neuron!=null){
						neuron.setLayer(h);
						neuron.setOrder(i);
						if(neuron.getNetwork()==null)
							neuron.setNetwork(this);
					}
				}
			}
		}
		return this;
	}
	

	public Network createSynapses(float initalWeight){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0)
			return this;
		
		
		for(int i=0;i<this.layers.size();i++){
			List<Neuron> currentLayer = layers.get(i);
			List<Neuron> nextLayer = null;
			if(i<layers.size()-1)
				nextLayer = layers.get(i+1);
			if(nextLayer!=null){
				for(Neuron neuron:currentLayer){
					if(neuron!=null)
						neuron.makeRelation(nextLayer, initalWeight);
				}
			}
		}
		
		return this;
	}
	
	public Network createSynapses(IGenerator weightGenerator){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0)
			return this;
		
		
		for(int i=0;i<this.layers.size();i++){
			List<Neuron> currentLayer = layers.get(i);
			List<Neuron> nextLayer = null;
			if(i<layers.size()-1)
				nextLayer = layers.get(i+1);
			if(nextLayer!=null){
				for(Neuron neuron:currentLayer){
					if(neuron!=null)
						neuron.makeRelation(nextLayer, weightGenerator);
				}
			}
		}
		
		return this;
	}
	
	
	public Network clearSynapses(float newWeight, boolean clearProperties){
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getChildren()!=null){
					neuron.updateWeights(newWeight, clearProperties);
				}
			}
		}
		return this;
	}
	
	public Network clearSynapses(IGenerator weightGenerator, boolean clearProperties){
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getChildren()!=null){
					neuron.updateWeights(weightGenerator, clearProperties);
				}
			}
		}
		return this;
	}		

	
	public Network addNeurons(Neuron[] neurons){
		for(Neuron neuron:neurons)
			this.addNeuron(neuron,false);
		return this;
	}

	public Network addNeuron(Neuron neuron, boolean shiftExisted){
		if(neuron==null || neuron.getLayer()<0 || neuron.getOrder()<0)
			return this;
		
		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();
		
		while(neuron.getLayer()>this.layers.size()-1)
			this.layers.add(new ArrayList<Neuron>());
		
		List<Neuron> layer = this.layers.get(neuron.getLayer());
		
		while(neuron.getOrder()>layer.size()-1)
			layer.add(null);
		
		Neuron exist = layer.get(neuron.getOrder());
		if(exist==null)
			layer.set(neuron.getOrder(), neuron);
		else if(shiftExisted){
			layer.add(null);
			for(int i=layer.size()-1;i>=neuron.getOrder();i--){
				layer.set(i, layer.get(i-1));
				layer.get(i).setOrder(i);
			}
			layer.set(neuron.getOrder(), neuron);
		} else
			layer.set(neuron.getOrder(), neuron);
		
		return this;
	}
	
	
	public Network addSynapses(Synapse[] synapses, boolean updateIfExist){
		for(Synapse synapse:synapses)
			this.addSynapse(synapse, updateIfExist);
		return this;
	}
	
	public Network addSynapse(Synapse synapse, boolean updateIfExist){
		
		if(synapse==null || synapse.getFrom()==null || synapse.getTo()==null)
			return this;

		if(this.layers==null || this.layers.size()==0)
			return this;
		
		
		Neuron from = null;
		Neuron to = null;
		
		
		
		List<Neuron> currentLayer = this.layers.get(0);
		int nextLayerPosition=1;

		while(currentLayer!=null && (from==null || to==null)){
			for(Neuron neuron:currentLayer){
				if(neuron!=null){
					if(from==null){
						if(	neuron.getLayer()==synapse.getFrom().getLayer() &&
							neuron.getOrder()==synapse.getFrom().getOrder())
							from = neuron;
					}
					if(to==null){
						if(	neuron.getLayer()==synapse.getTo().getLayer() &&
							neuron.getOrder()==synapse.getTo().getOrder())
							to = neuron;
					}				
				}
			}
			
			if(nextLayerPosition<this.layers.size()){
				currentLayer = this.layers.get(nextLayerPosition);
				nextLayerPosition++;
			}else
				currentLayer = null;
		}		
		
		if(from!=null && to!=null){
			if(from.getFunction()==null && synapse.getFrom().getFunction()!=null)
				from.setFunction(synapse.getFrom().getFunction());
			if(to.getFunction()==null && synapse.getTo().getFunction()!=null)
				to.setFunction(synapse.getTo().getFunction());			
			synapse.setFrom(from);
			synapse.setTo(to);
			from.setChildSynapse(synapse,updateIfExist);
			to.setParentSynapse(synapse,updateIfExist);
		}
		
		
		return this;
	}
	

	
	
	public float training(float[][][] trainingData, ITrainingStrategy strategy) throws Exception{
		
		if(trainingData==null)
			return -1;
		
		if(strategy==null)
			return -1;
		
		if(strategy.getErrorFunction()==null)
			return -1;
		
		if(strategy.getTrainingAlgorithm()==null)
			return -1;		

		if(this.layers==null || this.layers.size()==0)
			return -1;
		
		error = strategy.apply(this, trainingData);
				
		return error;
	}
	
	public float training(File trainingData, ITrainingStrategy strategy, IReadLinesAggregator dataAggregater) throws Exception{
		
		if(trainingData==null)
			return -1;
		
		if(strategy==null)
			return -1;
		
		if(strategy.getErrorFunction()==null)
			return -1;
		
		if(strategy.getTrainingAlgorithm()==null)
			return -1;		

		if(this.layers==null || this.layers.size()==0)
			return -1;
		
		try{
			error = strategy.apply(this, trainingData, dataAggregater);
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
				
		return error;
	}
	
	public float training(IStreamWrapper streamWrapper, ITrainingStrategy strategy, IReadLinesAggregator dataAggregator) throws Exception{
		
		if(streamWrapper==null)
			return -1;
		
		if(strategy==null)
			return -1;
		
		if(strategy.getErrorFunction()==null)
			return -1;
		
		if(strategy.getTrainingAlgorithm()==null)
			return -1;		

		if(this.layers==null || this.layers.size()==0)
			return -1;
		
		try{
			error = strategy.apply(this, streamWrapper, dataAggregator);
		}catch(Exception e){
			logger.error(e);
			throw e;
		}		
		return error;
	}
	
	public float training(IDataReader dataReader, ITrainingStrategy strategy, IReadLinesAggregator dataAggregator) throws Exception{
		
		if(dataReader==null)
			return -1;
		
		if(strategy==null)
			return -1;
		
		if(strategy.getErrorFunction()==null)
			return -1;
		
		if(strategy.getTrainingAlgorithm()==null)
			return -1;		

		if(this.layers==null || this.layers.size()==0)
			return -1;
		
		try{
			error = strategy.apply(this, dataReader, dataAggregator);
		}catch(Exception e){
			logger.error(e);
			throw e;
		}		
		return error;
	}	

	
	public float test(float[][][] testData, IErrorFunctionApplied errorFunction) throws Exception{
		
		if(testData==null)
			return -1;
		
		if(errorFunction==null)
			return -1;
		
	
		float error=0;

		IDataReader dataReader = new SimpleArrayReader(testData);
		
		final IReadLinesAggregator dataAggregator = new IReadLinesAggregator() {
			
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

		
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput());
						error+=errorFunction.compute(this, 0);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
		
		return error;
	}

	public float test(File file, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		
		if(file==null)
			return -1;
		
		if(dataAggregator==null)
			return -1;

		if(errorFunction==null)
			return -1;
		
	
		float error=0;

		IDataReader dataReader = new SimpleFileReader(file);
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput());
						error+=errorFunction.compute(this, 0);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
		
		return error;
	}

	
	public float test(IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		
		if(streamWrapper==null)
			return -1;
		
		if(dataAggregator==null)
			return -1;

		if(errorFunction==null)
			return -1;
		
	
		float error=0;

		IDataReader dataReader = new SimpleStreamReader(streamWrapper);
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput());
						error+=errorFunction.compute(this, 0);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
		
		return error;
	}
	
	
	public float test(IDataReader dataReader, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		
		if(dataReader==null)
			return -1;

		if(dataAggregator==null)
			return -1;
		
		if(errorFunction==null)
			return -1;

		float error=0;
		
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput());
						error+=errorFunction.compute(this, 0);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}

		return error;
	}
	

	
	public float[][] compute(IDataReader dataReader, IReadLinesAggregator dataAggregator) throws Exception{
		
		if(dataReader==null || dataAggregator==null)
			return new float[0][0];
		
		List<float[]> result = new ArrayList<float[]>();
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						PairIO param = dataAggregator.getData(network,aggregated);
						result.add(
							network.compute(param.getInput(), null)
						);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
	
		return (float[][])result.toArray();
	}
	
	public float[][] compute(float[][] _data){
		float[][] result = new float[_data.length][0];
		for(int i=0;i<_data.length;i++){
			result[i] = compute(_data[i], null);
		}		
		return result;
	}
	
	public float[] compute(float[] input, float[] output){
		if(this.layers==null || this.layers.size()<2)
			return new float[0];
		
		for(int i=0;i<input.length;i++){
			if(this.layers.get(0).size()>i)
				this.layers.get(0).get(i).setOutput(input[i]);
		}
		
		if(output!=null){
			for(int i=0;i<output.length;i++){
				if(this.layers.get(this.layers.size()-1).size()>i)
					this.layers.get(this.layers.size()-1).get(i).setTarget(output[i]);
			}
		}
		
		float[] result = new float[0];
		for(List<Neuron> currentLayer: this.layers){
			result = new float[currentLayer.size()]; 
			for(Neuron neuron:currentLayer){
				if(neuron!=null)
					result[neuron.getOrder()] = neuron.calculation();
			}
		}
		return result;
	}
	
	public float[] getWeight(){
		if(this.layers==null || this.layers.size()<2)
			return new float[0];	
		List<Float> weights = new ArrayList<Float>();
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getChildren()!=null){
					for(Synapse synapse:neuron.getChildren())
						weights.add(synapse.getWeight());
				}
			}
		}
		float[] result = new float[weights.size()];
		for(int i=0;i<weights.size();i++)
			result[i] = weights.get(i).floatValue();
		return result;
	}
	
	public Synapse[] getSynapses(){
		if(this.layers==null || this.layers.size()<2)
			return new Synapse[0];	
		List<Synapse> synapses = new ArrayList<Synapse>();
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getChildren()!=null)
					synapses.addAll(neuron.getChildren());
			}
		}
		Synapse[] result = new Synapse[synapses.size()];
		
		return synapses.toArray(result);
	}

	
	public Network getCloned() throws Exception {
		return Utils.clone(this, Network.class);
	}
	
	
	public boolean save(String path){
		return save(path,null);
	}
	
	public boolean save(String path, ITrainingStrategy[] strategies){
		try{	
			return save(new SimpleFileWriter(new File(path)),strategies);
		}catch(Exception e){
			logger.error(e);
			return false;
		}
	}
	
	public boolean save(IDataWriter dataWriter) throws Exception{
		return save(dataWriter,null);
	}
	
	public boolean save(IDataWriter dataWriter, ITrainingStrategy[] strategies) throws Exception{
		dataWriter.open();
		dataWriter.writeNext(this.toSaveString(strategies).getBytes());
		dataWriter.close();
		dataWriter.finalizer();
		return true;
	}	
	
	public Network open(String path){
		try{	
			return open(new SimpleFileReader(new File(path)));
		}catch(Exception e){
			logger.error(e);
			return this;
		}		
	}
	
public Network open(final IStreamWrapper streamWrapper) throws Exception{
		
		try{
			
			final IReadLinesAggregator dataAggregator = new IReadLinesAggregator() {
				
				@Override
				public PairIO getData(Network network, Object[] lines) {
					return null;
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
					String line = (String)objs[0];
					StringTokenizer st = new StringTokenizer(line, "=");
					String[] result = new String[2];
					if(st.hasMoreTokens()){
						result[0]=st.nextToken();
						if(st.hasMoreTokens())
							result[1]=st.nextToken();
						else result[1]="";
							
					}else{
						result[0]="";
						result[1]="";
					}
					return result;
				}			
			};
			
			final IDataReader dataReader = new IDataReader() {
				
				private InputStream stream = null;
				private BufferedReader breader = null;
				
				@Override
				public boolean open() throws Exception {
					if(streamWrapper!=null){
						stream = streamWrapper.openStream();
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
					return true;
				}
			};
			
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						String[] pair = (String[])dataAggregator.getRowData(network,aggregated);
						if(pair[0].equals("neuron"))
							this.addNeuron(Neuron.create(this,pair[1], logger), false);
						if(pair[0].equals("synapse"))
							this.addSynapse(Synapse.create(pair[1], logger), false);
						if(pair[0].equals("error"))
							this.setError(new Float(pair[1]));
						
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
		
		
		return this;
	}	
	
	public Network open(IDataReader dataReader) throws Exception{
		
		try{
			
			final IReadLinesAggregator dataAggregator = new IReadLinesAggregator() {
				
				@Override
				public PairIO getData(Network network, Object[] lines) {
					return null;
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
					String line = (String)objs[0];
					StringTokenizer st = new StringTokenizer(line, "=");
					String[] result = new String[2];
					if(st.hasMoreTokens()){
						result[0]=st.nextToken();
						if(st.hasMoreTokens())
							result[1]=st.nextToken();
						else result[1]="";
							
					}else{
						result[0]="";
						result[1]="";
					}
					return result;
				}			
			};
			
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						String[] pair = (String[])dataAggregator.getRowData(network,aggregated);
						if(pair[0].equals("neuron"))
							this.addNeuron(Neuron.create(this,pair[1], logger), false);
						if(pair[0].equals("synapse"))
							this.addSynapse(Synapse.create(pair[1], logger), false);
						if(pair[0].equals("error"))
							this.setError(new Float(pair[1]));
						
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
		
		
		return this;
	}	
	
	public String toSaveString(){
		return toSaveString(null);
	}
	public String toSaveString(ITrainingStrategy[] strategies){
		String result="error="+getError()+"\n";
		if(this.layers==null || this.layers.size()==0)
			return result;
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null)
					result+=neuron.toSaveString();
			}
		}
		for(List<Neuron> currentLayer: this.layers){
			for(Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getChildren()!=null){
					for(Synapse synapse:neuron.getChildren())
						result+=synapse.toSaveString();
				}
			}
		}
		if(strategies!=null){
			for(ITrainingStrategy strategy:strategies)
				result+=strategy.toSaveString();
		}
		return result;
	}
	
	public String toString(){
		return toSaveString();
	}
	


	public List<List<Neuron>> getLayers() {
		return layers;
	}

	public float getError() {
		return error;
	}

	public void setError(float error) {
		this.error = error;
	}


	
	
	
}
