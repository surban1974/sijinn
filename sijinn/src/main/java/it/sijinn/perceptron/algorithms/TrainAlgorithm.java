package it.sijinn.perceptron.algorithms;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.algorithms.RPROP.RPROPSynapseProperty;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;
import it.sijinn.perceptron.utils.ISynapseProperty;

public abstract class TrainAlgorithm implements ITrainingAlgorithm {

	protected IDAFloatFunction deferredAgregateFunction;
	
	public ITrainingAlgorithm calculateAndUpdateWeights(Network network) throws Exception{		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;

		for(int i=network.getLayers().size()-1;i>0;i--){
			for(Neuron neuron: network.getLayers().get(i)){
				if(neuron!=null)
					backPropagation(neuron, i==network.getLayers().size()-1);
			}
		}
		return this;
	}
	
	public ITrainingAlgorithm calculate(Network network) throws Exception{		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;
		if(deferredAgregateFunction!=null)
			deferredAgregateFunction.init();
		for(int i=network.getLayers().size()-1;i>0;i--){
			for(Neuron neuron: network.getLayers().get(i)){
				if(neuron!=null)
					updateGradients(neuron, i==network.getLayers().size()-1);
			}
		}
		return this;
	}
	
	public ITrainingAlgorithm updateWeights(Network network) throws Exception{		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;

		for(int i=network.getLayers().size()-1;i>0;i--){
			for(Neuron neuron: network.getLayers().get(i)){
				if(neuron!=null){
					updateWeights(neuron, i==network.getLayers().size()-1);
				}
			}
		}
		return this;
	}	
	
	public ITrainingAlgorithm sync(Network network1, Network network2, int type) throws Exception{	
		switch (type) {
		case SYNC_WEIGHT_DELTA:
			if(network1==null || network1.getLayers()==null || network1.getLayers().size()==0)
				return this;
			if(network2==null || network2.getLayers()==null || network2.getLayers().size()==0)
				return this;
			
			Synapse[] synapses1 = network1.getSynapses();
			Synapse[] synapses2 = network2.getSynapses();
			
			if(synapses1==null || synapses2==null || synapses1.length!=synapses2.length)
				return this;
			
			for(int i=0;i<synapses1.length;i++){
				Synapse relation1 = synapses1[i];
				Synapse relation2 = synapses2[i];
				if(relation1.getProperty()==null)
					relation1.setProperty(instanceProperty());
				if(relation2.getProperty()==null)
					relation2.setProperty(instanceProperty());				

				if(deferredAgregateFunction==null)
					((RPROPSynapseProperty)relation1.getProperty()).setAggregated(relation1.getProperty().getAggregated()+relation2.getProperty().getAggregated());
				else
					((RPROPSynapseProperty)relation1.getProperty()).setAggregated(
						deferredAgregateFunction.apply(relation1.getProperty().getAggregated(), relation2.getProperty().getAggregated())
					);
			}
			
			break;
		default:
			break;
		}
		return this;

	}	
	
	protected abstract ISynapseProperty instanceProperty();

	protected void backPropagation(Neuron neuron, boolean lastLayer){
		
	}
	
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		
	}
	
	protected void updateGradients(Neuron neuron, boolean lastLayer){
		
	}
	
	public IDAFloatFunction getDeferredAgregateFunction() {
		return deferredAgregateFunction;
	}

	public ITrainingAlgorithm setDeferredAgregateFunction(IDAFloatFunction deferredAgregateFunction) {
		this.deferredAgregateFunction = deferredAgregateFunction;
		return this;
	}
	
	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}
}
