package it.sijinn.perceptron.algorithms;



import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.utils.ISynapseProperty;

public class BPROP extends TrainAlgorithm implements ITrainingAlgorithm {
	
	protected float learningRate=0.1f;
	protected float learningMomentum=0;
	
	protected class BPROPSynapseProperty implements ISynapseProperty{
		private float sigma = 0;
		private float delta = 0;
		private float aggregated = 0;
		private Synapse relation = null;

		public ISynapseProperty setSynapse(Synapse synapse){
			this.relation = synapse;
			return this;
		}
		
		public Synapse getSynapse(){
			return relation;
		}
		

		public ISynapseProperty clear() {
			sigma = 0;
			delta = 0;
			aggregated = 0;
			return this;
		}			

		
		public float getSigma() {
			return sigma;
		}
		public void setSigma(float sigma) {
			this.sigma = sigma;
		}
		public float getDelta() {
			return delta;
		}
		public void setDelta(float previousDelta) {
			this.delta = previousDelta;
		}
		public float getAggregated() {
			return aggregated;
		}
		public void setAggregated(float aggregatedDelta) {
			this.aggregated = aggregatedDelta;
		}
		public String toString(){
			return "{"+sigma+","+delta+","+aggregated+"}";
		}		
	}
	
	@Override
	protected ISynapseProperty instanceProperty() {
		return new BPROPSynapseProperty();
	}
	
/*
	public ITrainingAlgorithm calculateAndUpdateWeights(Network network)  throws Exception{			
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
	
	public ITrainingAlgorithm updateWeights(Network network)  throws Exception{		
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
					relation1.setProperty(new BPROPSynapseProperty());
				if(relation2.getProperty()==null)
					relation2.setProperty(new BPROPSynapseProperty());				

				
				if(deferredAgregateFunction==null)
					((BPROPSynapseProperty)relation1.getProperty()).setAggregated(((BPROPSynapseProperty)relation1.getProperty()).getAggregated()+((BPROPSynapseProperty)relation2.getProperty()).getAggregated());
				else
					((BPROPSynapseProperty)relation1.getProperty()).setAggregated(
						deferredAgregateFunction.apply(((BPROPSynapseProperty)relation1.getProperty()).getAggregated(), ((BPROPSynapseProperty)relation2.getProperty()).getAggregated())
					);
				
			}
			
			break;
		default:
			break;
		}
		return this;

	}
*/
	
	protected void backPropagation(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate  * sigma * relation.getFrom().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
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
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			
			}
		}
	}	
	
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();

					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);

				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();
					
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);
				}
			}
		}
	}
	
	protected void updateGradients(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) *
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
					if(deferredAgregateFunction==null)
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
					else
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(
							deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
						);

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
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new BPROPSynapseProperty());

					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();					
					if(deferredAgregateFunction==null)
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
					else
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(
							deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
						);

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
		return "algorithm="+this.getClass().getSimpleName()+","+learningRate+","+learningMomentum+
				((deferredAgregateFunction==null)?"":","+deferredAgregateFunction.toSaveString()+"");
	}

	public BPROP setLearningRate(float learningRate) {
		this.learningRate = learningRate;
		return this;
	}

	public BPROP setLearningMomentum(float learningMomentum) {
		this.learningMomentum = learningMomentum;
		return this;
	}
}
