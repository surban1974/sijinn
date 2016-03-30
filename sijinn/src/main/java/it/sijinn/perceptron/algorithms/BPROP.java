package it.sijinn.perceptron.algorithms;



import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.Synapse;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;
import it.sijinn.perceptron.utils.ISynapseProperty;

public class BPROP implements ITrainingAlgorithm {
	
	protected float learningRate=0;
	protected float learningMomentum=0;
	
	class BPROPSynapseProperty implements ISynapseProperty{
		private float sigma = 0;
		private float delta = 0;
		private float previousDelta = 0;
		private float aggregatedDelta = 0;
		
		public BPROPSynapseProperty clear(){
			sigma=0;
			delta = 0;
			previousDelta=0;
			aggregatedDelta=0;
			return this;
		}
		
		public float getSigma() {
			return sigma;
		}
		public void setSigma(float sigma) {
			this.sigma = sigma;
		}
		public float getPreviousDelta() {
			return previousDelta;
		}
		public void setPreviousDelta(float previousDelta) {
			this.previousDelta = previousDelta;
		}
		public float getAggregatedDelta() {
			return aggregatedDelta;
		}
		public void setAggregatedDelta(float aggregatedDelta) {
			this.aggregatedDelta = aggregatedDelta;
		}
		public float getDelta() {
			return delta;
		}
		public void setDelta(float delta) {
			this.delta = delta;
		}
		public String toString(){
			return "{"+sigma+","+delta+","+previousDelta+","+aggregatedDelta+"}";
		}		
	}
	

	
	public BPROP(float _learningRate){
		super();
		this.learningRate = _learningRate;
	}
	
	public BPROP(float _learningRate, float _learningMomentum){
		super();
		this.learningRate = _learningRate;
		this.learningMomentum = _learningMomentum;
	}

	public ITrainingAlgorithm calculateAndUpdateWeights(Network network) {		
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
	
	public ITrainingAlgorithm calculate(Network network, IDAFloatFunction aggregatorFunction) {		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;

		if(aggregatorFunction!=null)
			aggregatorFunction.init();
		for(int i=network.getLayers().size()-1;i>0;i--){
			for(Neuron neuron: network.getLayers().get(i)){
				if(neuron!=null)
					updateGradients(neuron, i==network.getLayers().size()-1, aggregatorFunction);
			}
		}
		return this;
	}
	
	public ITrainingAlgorithm updateWeights(Network network) {		
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
	
	public ITrainingAlgorithm clear(Network network){
		for(int i=network.getLayers().size()-1;i>0;i--){
			for(Neuron neuron: network.getLayers().get(i)){
				if(neuron!=null && neuron.getParents()!=null){
					for(Synapse relation:neuron.getParents()){
						if(relation.getProperty()!=null)
							relation.getProperty().clear();
					}
				}
			}
		}
		return this;
	}
	
	public ITrainingAlgorithm sync(Network network1, Network network2, IDAFloatFunction aggregatorFunction, int type) {
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
					relation1.setProperty(new BPROPSynapseProperty());
				if(relation2.getProperty()==null)
					relation2.setProperty(new BPROPSynapseProperty());				

				
				if(aggregatorFunction==null)
					((BPROPSynapseProperty)relation1.getProperty()).setAggregatedDelta(((BPROPSynapseProperty)relation1.getProperty()).getAggregatedDelta()+((BPROPSynapseProperty)relation2.getProperty()).getDelta());
				else
					((BPROPSynapseProperty)relation1.getProperty()).setAggregatedDelta(
							aggregatorFunction.apply(((BPROPSynapseProperty)relation1.getProperty()).getAggregatedDelta(), ((BPROPSynapseProperty)relation2.getProperty()).getDelta())
					);
				
			}
			
			break;
		default:
			break;
		}
		return this;

	}

	
	private void backPropagation(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput())* ((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0);
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getPreviousDelta() +
							(1-learningMomentum) * learningRate  * sigma * relation.getFrom().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setPreviousDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.getChildren()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight()*((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0);
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getPreviousDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setPreviousDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}
		}
	}	
	
	private void updateWeights(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					((BPROPSynapseProperty)relation.getProperty()).setPreviousDelta(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					relation.setWeight(relation.getWeight()+((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					((BPROPSynapseProperty)relation.getProperty()).clear();
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					((BPROPSynapseProperty)relation.getProperty()).setPreviousDelta(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					relation.setWeight(relation.getWeight()+((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					((BPROPSynapseProperty)relation.getProperty()).clear();
				}
			}
		}
	}
	
	private void updateGradients(Neuron neuron, boolean lastLayer, IDAFloatFunction aggregatorFunction){
		
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) * ((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0);
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getPreviousDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					if(aggregatorFunction==null)
						((BPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta()+newDelta);
					else
						((BPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(
								aggregatorFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta(), newDelta)
						);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.getChildren()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight() * ((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0);
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getPreviousDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					if(aggregatorFunction==null)
						((BPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta()+newDelta);
					else
						((BPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(
								aggregatorFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregatedDelta(), newDelta)
						);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}
		}
	}		
	
	public float getLearningRate(){
		return learningRate;
	}

	public float getLearningMomentum() {
		return learningMomentum;
	}

	public String toSaveString(){
		return "algorithm="+this.getClass().getSimpleName()+","+learningRate+","+learningMomentum;
	}
}
