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

	
	public BPROP(){
		super();
	}
	public BPROP(float learningRate, float learningMomentum){
		super();
		this.learningRate = learningRate;
		this.learningMomentum = learningMomentum;
	}
	
	@Override
	protected ISynapseProperty instanceProperty() {
		return new BPROPSynapseProperty();
	}
	

	
	protected void backPropagation(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate  * sigma * relation.getFrom().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight()*((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
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
	
	protected void backPropagationReversed(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainChildren()!=null){
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate  * sigma * relation.getTo().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.obtainChildren()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight()*((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getTo().getOutput();
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			
			}
		}
	}		
	
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			if(neuron.obtainParents()!=null){
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();

					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);

				}
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();
					
					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);
				}
			}
		}
	}
	
	protected void updateWeightsReversed(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			if(neuron.obtainChildren()!=null){
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					
					float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();

					relation.setWeight(relation.getWeight()+newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
					((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);

				}
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
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
			if(neuron.obtainParents()!=null){
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
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
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight() * ((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
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
	
	protected void updateGradientsReversed(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput()) *
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainChildren()!=null){
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getTo().getOutput();
					if(deferredAgregateFunction==null)
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
					else
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(
							deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
						);

				}
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight() * ((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				
				for(Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());

					((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
							(1-learningMomentum) * learningRate * sigma * relation.getTo().getOutput();					
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

	public String getDefinition(){
//		return "algorithm="+this.getClass().getSimpleName()+","+learningRate+","+learningMomentum+
//				((deferredAgregateFunction==null)?"":","+deferredAgregateFunction.getDefinition()+"");
		return "algorithm="+this.getClass().getSimpleName()+"{"+learningRate+"|"+learningMomentum+"}";

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
