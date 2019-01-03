package it.sijinn.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.IExtLogger;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.IDataWriter;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.SimpleArrayReader;
import it.sijinn.perceptron.utils.io.SimpleFileReader;
import it.sijinn.perceptron.utils.io.SimpleFileWriter;
import it.sijinn.perceptron.utils.io.SimpleStreamReader;
import it.sijinn.perceptron.utils.io.SimpleStringReader;
import it.sijinn.perceptron.utils.io.SimpleStringWriter;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class Network extends Neuron implements Serializable{
	private static final long serialVersionUID = 1L;
	protected static IExtLogger extLogger;
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
				for(final List<Neuron> list:another.getLayers()){
					layers.add(new ArrayList<Neuron>());
					for(final Neuron neuron:list)
						layers.get(layers.size()-1).add(new Neuron(this,neuron));
				}
				for(final Synapse synapse:another.getSynapses())
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
	public static List<Neuron> createLayer(int neurons, boolean bias){
		return createLayer(neurons, null, true);
	}	
	public static List<Neuron> createLayer(int neurons, IFunctionApplied function){
		return createLayer(neurons, function, false);
	}	
	public static List<Neuron> createLayer(int neurons, IFunctionApplied function, boolean bias){
		if(neurons<=0)
			return null;
		final List<Neuron> layer = new ArrayList<Neuron>();
		if(bias)
			layer.add(new Neuron(null, function, true));
		for(int i=0;i<neurons;i++)
			layer.add(new Neuron(null, function));		
		return layer;
	}
	

	public Network addLayer(int neurons){
		return addLayer(neurons, null, false);
	}
	public Network addLayer(int neurons, IFunctionApplied function){
		return addLayer(neurons, function, false);
	}	
	public Network addLayer(int neurons, IFunctionApplied function, boolean bias){
		if(neurons<=0)
			return this;

		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();

		final List<Neuron> layer = new ArrayList<Neuron>();
		if(bias)
			layer.add(new Neuron(null, function, true));
		for(int i=0;i<neurons;i++)
			layer.add(new Neuron(this, function));
		
		layers.add(layer);
		indexingPositionOfNeurons();
		return this;
	}
	
	public Network insertLayer(int afterLayer, int neurons, IFunctionApplied function, boolean bias, boolean updateSynapses){
		if(neurons<=0)
			return this;

		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();

		final List<Neuron> layer = new ArrayList<Neuron>();
		if(bias){
			final Neuron neuron = new Neuron(null, function, true);
			neuron.setOrder(layer.size()).setLayer(afterLayer+1);
			layer.add(neuron);
		}
		for(int i=0;i<neurons;i++){
			final Neuron neuron = new Neuron(null, function);
			neuron.setOrder(layer.size()).setLayer(afterLayer+1);
			layer.add(neuron);			
		}
		
		
		if(afterLayer>=this.layers.size())
			layers.add(layer);
		else if(afterLayer>=0){
			int current = 0;
			
			final List<List<Neuron>> newLayers = new ArrayList<List<Neuron>>();

			while(current<this.layers.size()){
				if(current==afterLayer){
					newLayers.add(this.layers.get(current));
					newLayers.add(layer);
					if(updateSynapses){
						createSynapsesBetween(this.layers.get(current), layer, 0);
						if(current+1<this.layers.size())
							createSynapsesBetween(layer, this.layers.get(current+1), 0);
					}
					
					
				}else
					newLayers.add(this.layers.get(current));
				current++;
			}
			this.layers = newLayers;
		}
		
		indexingPositionOfNeurons();
		return this;
	}
	
	public Network removeLayer(int index, boolean updateSynapses){
		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();
		
		if(index>=0){
			int current = 0;
			
			final List<List<Neuron>> newLayers = new ArrayList<List<Neuron>>();
			while(current<this.layers.size()){
				if(current!=index)
					newLayers.add(this.layers.get(current));
				else{
					if(updateSynapses){
						removeSynapses(this.layers.get(current));
						if(current-1>=0 && current+1<this.layers.size())
							createSynapsesBetween(this.layers.get(current-1), this.layers.get(current+1), 0);

					}
				}
				current++;
			}
			this.layers = newLayers;
		}
		
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
					final Neuron neuron = layer.get(i);
					if(neuron!=null){
						neuron.setLayer(h);
						neuron.setOrder(i);
						if(neuron.obtainNetwork()==null)
							neuron.setNetwork(this);
					}
				}
			}
		}
		return this;
	}
	
	
	public Synapse findSynapse(Neuron from, Neuron to){
		if(layers==null || layers.size()==0)
			return null;
		
		Neuron neuronFrom = null;
		Neuron neuronTo = null;
		
		for(int i=0;i<this.layers.size();i++){
			if(neuronFrom!=null && neuronTo!=null)
				break;			
			final List<Neuron> currentLayer = layers.get(i);
			for(final Neuron neuron:currentLayer){
				if(neuronFrom!=null && neuronTo!=null)
					break;
				if(neuron!=null){
					if(neuron.getLayer()==from.getLayer() && neuron.getOrder()==from.getOrder())
						neuronFrom = neuron;
					if(neuron.getLayer()==to.getLayer() && neuron.getOrder()==to.getOrder())
						neuronTo = neuron;
				}
			}
		}
		if(neuronFrom!=null && neuronTo!=null){
			if(neuronFrom.obtainChildren()!=null){
				for(final Synapse synapse:neuronFrom.obtainChildren()){
					if(synapse.getTo()!=null && synapse.getTo().getLayer()==neuronTo.getLayer() && synapse.getTo().getOrder()==neuronTo.getOrder())
						return synapse;
				}
			}else if(neuronTo.obtainParents()!=null){
				for(final Synapse synapse:neuronTo.obtainParents()){
					if(synapse.getFrom()!=null && synapse.getFrom().getLayer()==neuronFrom.getLayer() && synapse.getFrom().getOrder()==neuronFrom.getOrder())
						return synapse;
				}
			}			
		}
		return null;
	}
	
	public Network removeSynapse(Neuron from, Neuron to){

		if(layers==null || layers.size()==0)
			return this;
		
		Neuron neuronFrom = null;
		Neuron neuronTo = null;
		
		for(int i=0;i<this.layers.size();i++){
			if(neuronFrom!=null && neuronTo!=null)
				break;			
			final List<Neuron> currentLayer = layers.get(i);
			for(final Neuron neuron:currentLayer){
				if(neuronFrom!=null && neuronTo!=null)
					break;
				if(neuron!=null){
					if(neuron.getLayer()==from.getLayer() && neuron.getOrder()==from.getOrder())
						neuronFrom = neuron;
					if(neuron.getLayer()==to.getLayer() && neuron.getOrder()==to.getOrder())
						neuronTo = neuron;
				}
			}
		}
		
		if(neuronFrom!=null && neuronTo!=null){
			final List<Synapse> forRemove = new ArrayList<Synapse>();
			for(final Synapse synapse:neuronFrom.obtainChildren()){
				if(synapse.getTo()!=null && synapse.getTo().getLayer()==neuronTo.getLayer() && synapse.getTo().getOrder()==neuronTo.getOrder())
					forRemove.add(synapse);
			}
			for(final Synapse synapse:forRemove)
				neuronFrom.obtainChildren().remove(synapse);
			forRemove.clear();
			
			for(final Synapse synapse:neuronTo.obtainParents()){
				if(synapse.getFrom()!=null && synapse.getFrom().getLayer()==neuronFrom.getLayer() && synapse.getFrom().getOrder()==neuronFrom.getOrder())
					forRemove.add(synapse);
			}
			for(final Synapse synapse:forRemove)
				neuronTo.obtainParents().remove(synapse);
			forRemove.clear();			
		}
		
		return this;
	}
	
	public Network removeSynapses(){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0)
			return this;
		
		
		for(int i=0;i<this.layers.size();i++){
			final List<Neuron> currentLayer = layers.get(i);
			for(final Neuron neuron:currentLayer){
				if(neuron!=null){
					final List<Synapse> children=neuron.obtainChildren();
					if(children!=null)
						children.clear();
					final List<Synapse> parents=neuron.obtainParents();
					if(parents!=null)
						parents.clear();					
				}
			}
		}		
		return this;
	}
	
	public Network removeSynapses(List<Neuron> layer){

		if(layers==null || layers.size()==0)
			return this;
		
		

			for(final Neuron neuron:layer){
				if(neuron!=null){
					final List<Synapse> children=neuron.obtainChildren();
					if(children!=null){
						for(final Synapse synapse:children){
							if(synapse.getTo()!=null && synapse.getTo().obtainParents()!=null){
								for(final Synapse synapse_p:synapse.getTo().obtainParents()){
									if(synapse_p.getDirection().equalsIgnoreCase(synapse.getDirection())){
										synapse.getTo().obtainParents().remove(synapse_p);
										break;
									}
								}
							}
						}						
						children.clear();
					}
					final List<Synapse> parents=neuron.obtainParents();
					if(parents!=null){
						for(final Synapse synapse:parents){
							if(synapse.getFrom()!=null && synapse.getFrom().obtainChildren()!=null){
								for(final Synapse synapse_ch:synapse.getFrom().obtainChildren()){
									if(synapse_ch.getDirection().equalsIgnoreCase(synapse.getDirection())){
										synapse.getFrom().obtainChildren().remove(synapse_ch);
										break;
									}
								}
							}
						}
						parents.clear();	
					}
				}
			}

		
		return this;
	}
	
	
	
	
	public Network removeSynapses(Neuron neuron){

		if(layers==null || layers.size()==0)
			return this;
		
		


				if(neuron!=null){
					final List<Synapse> children=neuron.obtainChildren();
					if(children!=null){
						for(final Synapse synapse:children){
							if(synapse.getTo()!=null && synapse.getTo().obtainParents()!=null){
								for(final Synapse synapse_p:synapse.getTo().obtainParents()){
									if(synapse_p.getDirection().equalsIgnoreCase(synapse.getDirection())){
										synapse.getTo().obtainParents().remove(synapse_p);
										break;
									}
								}
							}
						}						
						children.clear();
					}
					final List<Synapse> parents=neuron.obtainParents();
					if(parents!=null){
						for(final Synapse synapse:parents){
							if(synapse.getFrom()!=null && synapse.getFrom().obtainChildren()!=null){
								for(final Synapse synapse_ch:synapse.getFrom().obtainChildren()){
									if(synapse_ch.getDirection().equalsIgnoreCase(synapse.getDirection())){
										synapse.getFrom().obtainChildren().remove(synapse_ch);
										break;
									}
								}
							}
						}
						parents.clear();	
					}
				}


		
		return this;
	}
		
	

	public Network createSynapses(float initalWeight){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0)
			return this;
		
		
		for(int i=0;i<this.layers.size();i++){
			final List<Neuron> currentLayer = layers.get(i);
			List<Neuron> nextLayer = null;
			if(i<layers.size()-1)
				nextLayer = layers.get(i+1);
			if(nextLayer!=null){
				for(final Neuron neuron:currentLayer){
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
			final List<Neuron> currentLayer = layers.get(i);
			List<Neuron> nextLayer = null;
			if(i<layers.size()-1)
				nextLayer = layers.get(i+1);
			if(nextLayer!=null){
				for(final Neuron neuron:currentLayer){
					if(neuron!=null)
						neuron.makeRelation(nextLayer, weightGenerator);
				}
			}
		}
		
		return this;
	}
	
	
	public Network createSynapsesBetween(List<Neuron> currentLayer, List<Neuron> nextLayer, float initalWeight){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0 || currentLayer==null || currentLayer.size()==0 || nextLayer==null || nextLayer.size()==0)
			return this;
		
		for(final Neuron neuron:nextLayer){
			if(neuron!=null && neuron.obtainParents()!=null)
				neuron.obtainParents().clear();
		}
		
		for(final Neuron neuron:currentLayer){
			if(neuron!=null){
				if(neuron.obtainChildren()!=null)
					neuron.obtainChildren().clear();
				neuron.makeRelation(nextLayer, initalWeight);
			}
		}
		
		return this;
	}
	
	public Network createSynapsesBetween(List<Neuron> currentLayer, List<Neuron> nextLayer, IGenerator weightGenerator){
		indexingPositionOfNeurons();

		if(layers==null || layers.size()==0 || currentLayer==null || currentLayer.size()==0 || nextLayer==null || nextLayer.size()==0)
			return this;
		
		for(final Neuron neuron:nextLayer){
			if(neuron!=null && neuron.obtainParents()!=null)
				neuron.obtainParents().clear();
		}
		
		for(final Neuron neuron:currentLayer){
			if(neuron!=null){
				if(neuron.obtainChildren()!=null)
					neuron.obtainChildren().clear();
				neuron.makeRelation(nextLayer, weightGenerator);
			}
		}
		
		return this;
	}	
	
	public Network updateActivationFunctions(IFunctionApplied function){
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.getLayer()>0)
					neuron.setFunction(function);
			}
		}
		return this;
		
	}
	
	
	public Network clearSynapses(float newWeight, boolean clearProperties){
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null){
					neuron.updateWeights(newWeight, clearProperties);
				}
			}
		}
		return this;
	}
	
	public Network clearSynapses(IGenerator weightGenerator, boolean clearProperties){
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null){
					neuron.updateWeights(weightGenerator, clearProperties);
				}
			}
		}
		return this;
	}		

	
	public Neuron findNeuron(Neuron find){
		if(layers==null || layers.size()==0)
			return null;
		for(int i=0;i<this.layers.size();i++){
			final List<Neuron> currentLayer = layers.get(i);
			for(final Neuron neuron:currentLayer){
				if(neuron!=null){
					if(neuron.getLayer()==find.getLayer() && neuron.getOrder()==find.getOrder())
						return neuron;
				}
			}
		}
		return null;
	}
	
	public Network addNeurons(Neuron[] neurons){
		for(final Neuron neuron:neurons)
			this.addNeuron(neuron,false);
		return this;
	}
	
	public Network addNeuron(Neuron neuron, boolean shiftExisted){
		return addNeuron(neuron, shiftExisted, false);
	}

	public Network addNeuron(Neuron neuron, boolean shiftExisted, boolean updateSynapses){
		if(neuron==null || neuron.getLayer()<0 || neuron.getOrder()<0)
			return this;
		
		if(this.layers==null)
			this.layers = new ArrayList<List<Neuron>>();
		
		while(neuron.getLayer()>this.layers.size()-1)
			this.layers.add(new ArrayList<Neuron>());
		
		final List<Neuron> layer = this.layers.get(neuron.getLayer());
		
		while(neuron.getOrder()>layer.size()-1)
			layer.add(null);
		
		final Neuron exist = layer.get(neuron.getOrder());
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
		
		if(updateSynapses){
			List<Neuron> prevLayer = null;
			List<Neuron> nextLayer = null;
			
			if(neuron.getLayer()>0)
				prevLayer = this.layers.get(neuron.getLayer()-1);
			
			if(neuron.getLayer()<this.layers.size()-1)
				nextLayer = this.layers.get(neuron.getLayer()+1);
			
			if(prevLayer!=null ){
				for(Neuron current:prevLayer){
					if(current!=null)
						current.makeRelation(neuron, 0);
				}
			}
			if(nextLayer!=null ){
				for(Neuron current:nextLayer){
					if(current!=null)
						neuron.makeRelation(current, 0);
				}
			}			
		}
		
		return this;
	}
	
	public Network removeNeuron(Neuron neuron, boolean updateSynapses){
		if(neuron==null || neuron.getLayer()<0 || neuron.getOrder()<0 || this.layers==null || neuron.getLayer()>=this.layers.size())
			return this;

		final List<Neuron> layer = this.layers.get(neuron.getLayer());
		if(neuron.getOrder()>=layer.size())
			return this;
		for(final Neuron current:layer){
			if(neuron.getOrder()==current.getOrder()){
				if(updateSynapses)
					removeSynapses(current);
				
				layer.remove(current);
				break;
			}
		}
		
		return this;
	}
	
	public Network addSynapses(Synapse[] synapses, boolean updateIfExist){
		for(final Synapse synapse:synapses)
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
			for(final Neuron neuron:currentLayer){
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
			Network.error(e);
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
			Network.error(e);
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
			Network.error(e);
			throw e;
		}		
		return error;
	}	

	public float test(float[][][] testData, IErrorFunctionApplied errorFunction) throws Exception{
		return test(testData, errorFunction, false);
	}
	public float test(float[][][] testData, IErrorFunctionApplied errorFunction, boolean reversed) throws Exception{
		
		if(testData==null)
			return -1;
		
		if(errorFunction==null)
			return -1;
		
	
		float error=0;

		final IDataReader dataReader = new SimpleArrayReader(testData);
		
		final IReadLinesAggregator dataAggregator = new IReadLinesAggregator() {
			
			@Override
			public PairIO getData(Network network, Object[] lines) {
				if(lines==null || lines.length==0)
					return null;
				final float[][] f = (float[][])lines[0];
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
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput(), reversed);
						error+=errorFunction.compute(this, 0, reversed);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}
		
		return error;
	}

	public float test(File file, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		return test(file, dataAggregator, errorFunction, false);
	}
	public float test(File file, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction, boolean reversed) throws Exception{
		
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
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput(), reversed);
						error+=errorFunction.compute(this, 0, reversed);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}
		
		return error;
	}

	public float test(IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		return test(streamWrapper, dataAggregator, errorFunction, false);
	}
	public float test(IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction, boolean reversed) throws Exception{
		
		if(streamWrapper==null)
			return -1;
		
		if(dataAggregator==null)
			return -1;

		if(errorFunction==null)
			return -1;
		
	
		float error=0;

		final IDataReader dataReader = new SimpleStreamReader(streamWrapper);
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput(), reversed);
						error+=errorFunction.compute(this, 0, reversed);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}
		
		return error;
	}
	
	public float test(IDataReader dataReader, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction) throws Exception{
		return test(dataReader, dataAggregator, errorFunction, false);
	}
	public float test(IDataReader dataReader, IReadLinesAggregator dataAggregator, IErrorFunctionApplied errorFunction, boolean reversed) throws Exception{
		
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
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(this,aggregated);
						compute(param.getInput(), param.getOutput(), reversed);
						error+=errorFunction.compute(this, 0, reversed);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}

		return error;
	}
	

	public float[][] compute(IDataReader dataReader, IReadLinesAggregator dataAggregator) throws Exception{
		return compute(dataReader, dataAggregator, false);
	}
	public float[][] compute(IDataReader dataReader, IReadLinesAggregator dataAggregator, boolean reversed) throws Exception{
		
		if(dataReader==null || dataAggregator==null)
			return new float[0][0];
		
		final List<float[]> result = new ArrayList<float[]>();
		try{
			if(dataReader.open()){
				Object line = null;
				int linenumber=0;
				while ((line = dataReader.readNext()) != null) {
					final Object[] aggregated = dataAggregator.aggregate(line,linenumber);
					if(aggregated!=null){
						final PairIO param = dataAggregator.getData(network,aggregated);
						result.add(
							network.compute(param.getInput(), null, reversed)
						);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}
	
		return (float[][])result.toArray();
	}
	

	public float[][] compute(float[][] _data, boolean reversed){
		if(!reversed)
			return compute(_data);
		else
			return computeReversed(_data);
	}
	
	public float[][] compute(float[][] _data){
		final float[][] result = new float[_data.length][0];
		for(int i=0;i<_data.length;i++){
			result[i] = compute(_data[i], null);
		}		
		return result;
	}
	
	public float[][] computeReversed(float[][] _data){
		final float[][] result = new float[_data.length][0];
		for(int i=0;i<_data.length;i++){
			result[i] = computeReversed(null, _data[i]);
		}		
		return result;
	}	
	
	public float[] compute(float[] input, float[] output){
		if(this.layers==null || this.layers.size()<2)
			return new float[0];
		if(input!=null && input.length>0)
			setInputValues(0, input);
		if(output!=null && output.length>0)
			setOutputValues(0, output);
		
		float[] result = new float[0];
		for(final List<Neuron> currentLayer: this.layers){
			result = new float[currentLayer.size()]; 
			for(final Neuron neuron:currentLayer){
				if(neuron!=null)
					result[neuron.getOrder()] = neuron.calculation();
			}
		}
		return result;
	}
	
	public float[] computeReversed(float[] input, float[] output){
		if(this.layers==null || this.layers.size()<2)
			return new float[0];
		if(input!=null && input.length>0)
			setInputValuesReversed(0, input);
		if(output!=null && output.length>0)
			setOutputValuesReversed(0, output);
		
		float[] result = new float[0];
		for(int i=this.layers.size()-1;i>=0; i--){
			final List<Neuron> currentLayer = this.layers.get(i);
			result = new float[currentLayer.size()]; 
			for(final Neuron neuron:currentLayer){
				if(neuron!=null)
					result[neuron.getOrder()] = neuron.calculationReversed();
			}
		}
		return result;
	}	
	
	public float[] compute(float[] input, float[] output, boolean reversed){
		if(!reversed)
			return compute(input, output);
		else 
			return computeReversed(input, output);
	}
	
	public float[] compute(boolean reversed){
		if(!reversed)
			return compute(new float[0], new float[0]);
		else 
			return computeReversed(new float[0], new float[0]);
	}	
	
	public float[] compute(){
		float[] result = new float[0];
		for(final List<Neuron> currentLayer: this.layers){
			result = new float[currentLayer.size()]; 
			for(final Neuron neuron:currentLayer){
				if(neuron!=null)
					result[neuron.getOrder()] = neuron.calculation();
			}
		}
		return result;
	}
	
	public float[] computeReversed(){
		float[] result = new float[0];
		for(final List<Neuron> currentLayer: this.layers){
			result = new float[currentLayer.size()]; 
			for(final Neuron neuron:currentLayer){
				if(neuron!=null)
					result[neuron.getOrder()] = neuron.calculationReversed();
			}
		}
		return result;
	}	
	
	public int setInputValues(int start, float[] input){
		int result = start;
		if(this.layers==null || this.layers.get(0).size()==0 || input==null)
			return start;
		int biases=0;
		for(;result<input.length && result<this.layers.get(0).size();result++){
			if(this.layers.get(0).get(result) instanceof Network){
				result=((Network)this.layers.get(0).get(result)).setInputValues(result, input);
			}else if(this.layers.get(0).get(result).isBias())
				biases++;
			else	
				this.layers.get(0).get(result).setOutput(input[result]);
			
		}
		return result-biases;
	}
	
	public int setInputValuesReversed(int start, float[] input){
		int result = start;
		if(this.layers==null || this.layers.get(0).size()==0 || input==null)
			return start;
		int biases=0;
		for(;result<input.length && result<this.layers.get(0).size();result++){
			if(this.layers.get(0).get(result) instanceof Network){
				result=((Network)this.layers.get(0).get(result)).setInputValues(result, input);
			}else if(this.layers.get(0).get(result).isBias())
				biases++;
			else
				this.layers.get(0).get(result).setTarget(input[result]);
			
		}
		return result-biases;
	}	
	
	public int setOutputValues(int start, float[] output){
		int result = start;
		int lastLayer = this.layers.size()-1;
		if(this.layers==null || this.layers.get(lastLayer).size()==0 || output==null)
			return start;
		for(;result<output.length && result<this.layers.get(lastLayer).size();result++){
			if(this.layers.get(lastLayer).get(result) instanceof Network){
				result=((Network)this.layers.get(lastLayer).get(result)).setOutputValues(result, output);
			}else
				this.layers.get(lastLayer).get(result).setTarget(output[result]);
		}
		return result;
	}	
	
	public int setOutputValuesReversed(int start, float[] output){
		int result = start;
		int lastLayer = this.layers.size()-1;
		if(this.layers==null || this.layers.get(lastLayer).size()==0 || output==null)
			return start;
		for(;result<output.length && result<this.layers.get(lastLayer).size();result++){
			if(this.layers.get(lastLayer).get(result) instanceof Network){
				result=((Network)this.layers.get(lastLayer).get(result)).setOutputValues(result, output);
			}else
				this.layers.get(lastLayer).get(result).setOutput(output[result]);

		}
		return result;
	}	
	
	public float[] getInputValues(){
		if(this.layers==null || this.layers.size()==0 || this.layers.get(0).size()==0)
			return new float[0];
		final float[] result = new float[this.layers.get(0).size()];
		for(int i=0;i<this.layers.get(0).size();i++)
			result[i] = this.layers.get(0).get(i).getOutput();
		return result;
	}
	
	public float[] getOutputValues(){
		if(this.layers==null || this.layers.size()==0 || this.layers.get(this.layers.size()-1).size()==0)
			return new float[0];
		final float[] result = new float[this.layers.get(this.layers.size()-1).size()];
		for(int i=0;i<this.layers.get(this.layers.size()-1).size();i++)
			result[i] = this.layers.get(this.layers.size()-1).get(i).getOutput();
		return result;
	}
	
	public float[] getWeight(){
		if(this.layers==null || this.layers.size()<2)
			return new float[0];	
		final List<Float> weights = new ArrayList<Float>();
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null){
					for(final Synapse synapse:neuron.obtainChildren())
						weights.add(synapse.getWeight());
				}
			}
		}
		final float[] result = new float[weights.size()];
		for(int i=0;i<weights.size();i++)
			result[i] = weights.get(i).floatValue();
		return result;
	}
	
	public Network setWeight(float[] weights){
		if(this.layers==null || this.layers.size()<2)
			return this;	
		int current=0;
		
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null){
					for(final Synapse synapse:neuron.obtainChildren()){
						if(current<weights.length){
							synapse.setWeight(weights[current]);
							current++;
						}
					}
				}
			}
		}
		return this;
	}	
	
	public Synapse[] getSynapses(){
		if(this.layers==null || this.layers.size()<2)
			return new Synapse[0];	
		final List<Synapse> synapses = new ArrayList<Synapse>();
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null)
					synapses.addAll(neuron.obtainChildren());
			}
		}
		final Synapse[] result = new Synapse[synapses.size()];
		
		return synapses.toArray(result);
	}
	
	public Neuron[] getNeurons(){
		if(this.layers==null || this.layers.size()<2)
			return new Neuron[0];	
		final List<Neuron> neurons = new ArrayList<Neuron>();
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null)
					neurons.add(neuron);
			}
		}
		final Neuron[] result = new Neuron[neurons.size()];
		
		return neurons.toArray(result);
	}	

	
	public Network obtainCloned() throws Exception {
		return Utils.clone(this, Network.class);
	}
	
	public String save(){
		final SimpleStringWriter stringWriter = new SimpleStringWriter();
		try{
			save(stringWriter);
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			return null;
		}
		return stringWriter.getOutput();
	}
	
	public boolean save(String path){
		return save(path,null);
	}
	
	public boolean save(String path, ITrainingStrategy[] strategies){
		try{	
			return save(new SimpleFileWriter(new File(path)),strategies);
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			return false;
		}
	}
	
	public boolean save(final IDataWriter dataWriter) throws Exception{
		return save(dataWriter,null);
	}
	
	public boolean save(IDataWriter dataWriter, ITrainingStrategy[] strategies) throws Exception{
		dataWriter.open();
		dataWriter.writeNext(this.toSaveString("",strategies).getBytes());
		dataWriter.close();
		dataWriter.finalizer();
		return true;
	}	
	
	public Network open(String xml){
		try{	
			return open(new SimpleStringReader(xml,"utf8"));
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			return this;
		}		
	}
	
	public Network open(File path){
		try{	
			return open(new SimpleFileReader(path));
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			return this;
		}		
	}
	
	public Network open(final IStreamWrapper streamWrapper) throws Exception{
		try{
			return open(new SimpleStreamReader(streamWrapper));
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}
	}	
	
	public Network open(IDataReader dataReader) throws Exception{
		try{
			final ByteArrayInputStream xmlSrcStream = new	ByteArrayInputStream(dataReader.readAll());
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			dbf.setValidating(true);
			final Document documentXML = dbf.newDocumentBuilder().parse(xmlSrcStream);
			
			Node node = null;
			int first=0;
			while(node==null && first < documentXML.getChildNodes().getLength()){
				if(documentXML.getChildNodes().item(first).getNodeType()== Node.ELEMENT_NODE)
					node = documentXML.getChildNodes().item(first);
				first++;
			}
			if(node==null){
				Exception e = new Exception("Network's structure is incomplet for initialization.");
				throw e;
			};
			
			return Network.create(this, node, logger);

		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			throw e;
		}

	}	
	
	
	
	public String toSaveString(){
		return toSaveString("",null);
	}
	public String toSaveString(String prefix,ITrainingStrategy[] strategies){
		String result=(prefix!=null)?prefix:"";
		result+=prefix+"<network";
		if(layer>-1)
			result+=" layer=\""+layer+"\"";
		if(order>-1)
			result+=" order=\""+order+"\"";
		if(function!=null)	
			result+=" function=\""+((function==null)?"":function.getDefinition())+"\"";
		if(error!=0)	
			result+=" error=\""+error+"\"";
		result+=">\n";
		

		if(this.layers==null || this.layers.size()==0)
			return result;
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null){
					if(neuron instanceof Network)
						result+=((Network)neuron).toSaveString(prefix+"\t",null);
					else result+=neuron.toSaveString(prefix+"\t");
				}
			}
		}
		for(final List<Neuron> currentLayer: this.layers){
			for(final Neuron neuron:currentLayer){
				if(neuron!=null && neuron.obtainChildren()!=null){
					for(final Synapse synapse:neuron.obtainChildren())
						result+=synapse.toSaveString(prefix+"\t");
				}
			}
		}
		if(strategies!=null){
			for(final ITrainingStrategy strategy:strategies)
				result+=strategy.toSaveString(prefix+"\t");
		}
		result+=prefix+"</network>\n";
		return result;
	}
	
	public String toString(){
		return toSaveString();
	}
	


	public List<List<Neuron>> getLayers() {
		return layers;
	}
	
	public int getLayerBiases(int layer) {
		if(layers==null || layer>=layers.size() || layer<0)
			return 0;
		int bias=0;;
		for(final Neuron neuron:layers.get(layer)){
			if(neuron.isBias())
				bias++;
		}
		return bias;
	}	

	public float getError() {
		return error;
	}

	public void setError(float error) {
		this.error = error;
	}


	public static IExtLogger addExtLogger(IExtLogger logger){
		extLogger = logger;
		return extLogger; 
	}
	
	public static IExtLogger obtainExtLogger(){
		return extLogger; 
	}

	public static void error(String mess){
		if(extLogger!=null)
			extLogger.add(mess, IExtLogger.log_ERROR);
	}	

	public static void error(Throwable th){
		if(extLogger!=null)
			extLogger.add(th, IExtLogger.log_ERROR);
	}	
	
	public static Network create(Network network, Node node, Logger logger){
		if(node==null || !node.getNodeName().equalsIgnoreCase("network")){
			logger.error("Network instance Error: xml node is incomplet for initialization.");
			Network.error("Network instance Error: xml node is incomplet for initialization.");
			return null;
		}

		
		if(network==null)
			network = new Network();
		
		final NamedNodeMap nnm = node.getAttributes();	 		
		if (nnm!=null){
			for (int i=0;i<node.getAttributes().getLength();i++){
				final String paramName = node.getAttributes().item(i).getNodeName();
				final Node node_nnm =	nnm.getNamedItem(paramName);
				String sNodeValue = node_nnm.getNodeValue();
				if(sNodeValue!=null){
					sNodeValue=sNodeValue.replace('\n', ' ').replace('\r', ' ');
					sNodeValue = sNodeValue.replace(" ", "");
				}
				if(paramName.equalsIgnoreCase("layer")){
					try{
						network.setLayer(Integer.valueOf(sNodeValue));
					}catch(Exception e){
					}
				}else if(paramName.equalsIgnoreCase("order")){
					try{
						network.setOrder(Integer.valueOf(sNodeValue));
					}catch(Exception e){
					}
				}else if(paramName.equalsIgnoreCase("function"))
					network.setFunction(Neuron.create(sNodeValue, logger));
				else if(paramName.equalsIgnoreCase("error")){
					try{
						network.setError(Float.valueOf(sNodeValue));
					}catch(Exception e){
					}
				}
				
			}
		}

		
		for(int k=0;k<node.getChildNodes().getLength();k++){
			if(node.getChildNodes().item(k).getNodeType()== Node.ELEMENT_NODE){
				if(node.getChildNodes().item(k).getNodeName().equalsIgnoreCase("neuron"))
					network.addNeuron(Neuron.create(network, node.getChildNodes().item(k), logger), false);
				else if(node.getChildNodes().item(k).getNodeName().equalsIgnoreCase("synapse"))
					network.addSynapse(Synapse.create(node.getChildNodes().item(k), logger), false);
				else if(node.getChildNodes().item(k).getNodeName().equalsIgnoreCase("network")){
					final Network newNetwork = Network.create(new Network(), node.getChildNodes().item(k), logger);
					newNetwork.setNetwork(network);
					network.addNeuron(newNetwork, false);
				}
			}
		}

		return network;
	}
	
	
	public static ITrainingAlgorithm createAlgorithmById(String algorithm, Logger logger){
		ITrainingAlgorithm trainingAlgorithm = null;
		String input = algorithm;
		try{
			if(algorithm!=null && !algorithm.trim().equals("")){
				if(algorithm.indexOf("algorithm=")==0)
					algorithm = algorithm.replace("algorithm=", "");
				if(algorithm.indexOf("{")==-1){
					if(algorithm.indexOf(".")>-1)
						trainingAlgorithm = (ITrainingAlgorithm)Class.forName(algorithm).newInstance();
					else
						trainingAlgorithm = (ITrainingAlgorithm)Class.forName("it.sijinn.perceptron.algorithms."+algorithm).newInstance();
				}else{
					final String parameters = algorithm.substring(algorithm.indexOf("{")+1, algorithm.lastIndexOf("}"));
					algorithm = algorithm.substring(0, algorithm.indexOf("{"));
					StringTokenizer stp = new StringTokenizer(parameters, "|");
					final List<Float> param = new ArrayList<Float>();
					while(stp.hasMoreTokens())
						param.add(Float.valueOf(stp.nextToken()));
					Float[] fParam = new Float[param.size()];
					fParam = param.toArray(fParam);
					
					Class<?> clazz = null;
					if(algorithm.indexOf(".")>-1)
						clazz = (Class<?>)Class.forName(algorithm).asSubclass(ITrainingAlgorithm.class);
					else
						clazz = (Class<?>)Class.forName("it.sijinn.perceptron.algorithms."+algorithm);
					
					Constructor<?> clazzConstructor = null;
					for(final Constructor<?> constructor: (Constructor<?>[])clazz.getConstructors()){
						if(constructor.getParameterTypes().length==fParam.length){
							boolean isCorrect=true;
							for(final Class<?> paramClass: constructor.getParameterTypes())
								isCorrect&=(paramClass.isPrimitive() &&  paramClass.getName().equals("float"));
							if(isCorrect){
								clazzConstructor = constructor;
								break;
							}
						}
					}
					
					if(clazzConstructor!=null)
						trainingAlgorithm = (ITrainingAlgorithm)clazzConstructor.newInstance((Object[])fParam);
					else{
						if(logger!=null){
							logger.error("Training Algorithm instance Error: properties=["+input+"] is incomplet for initialization.");
							Network.error("Training Algorithm instance Error: properties=["+input+"] is incomplet for initialization.");
						}return null;
					}
				}	
			}
		}catch(Exception e){
			if(logger!=null){
				logger.error(e);
				Network.error(e);
			}
			return null;
		}
		return trainingAlgorithm;		
	}

	public static ITrainingStrategy createStrategyById(String strategy, Logger logger){
		ITrainingStrategy trainingStrategy = null;
		try{
			if(strategy!=null && !strategy.trim().equals("")){
				if(strategy.indexOf(";")==0)
					strategy = strategy.substring(0, strategy.indexOf(";"));

				if(strategy.indexOf(".")>-1)
					trainingStrategy = (ITrainingStrategy)Class.forName(strategy).newInstance();
				else
					trainingStrategy = (ITrainingStrategy)Class.forName("it.sijinn.perceptron.strategies."+strategy).newInstance();
			}
		}catch(Exception e){
			if(logger!=null){
				logger.error(e);
				Network.error(e);
			}
			return null;
		}
		return trainingStrategy;		
	}	
	
	
}
