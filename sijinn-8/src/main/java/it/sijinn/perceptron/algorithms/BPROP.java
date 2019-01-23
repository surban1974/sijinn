package it.sijinn.perceptron.algorithms;


import java.util.stream.Stream;

import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.utils.ISynapseProperty;

public class BPROP extends TrainAlgorithm implements ITrainingAlgorithm {
	private static final long serialVersionUID = 1L;
	protected float learningRate=0.1f;
	protected float learningMomentum=0;
	
	protected class BPROPSynapseProperty implements ISynapseProperty{

		private static final long serialVersionUID = -5832214517012665237L;
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
	

	@Override
	protected void backPropagation(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate  * sigma * relation.getFrom().getOutput();
							relation.setWeight(relation.getWeight()+newDelta);								
							((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);								
						}
				);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(!(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight()*((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
					
				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma_ * relation.getFrom().getOutput();
							relation.setWeight(relation.getWeight()+newDelta);	
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
							((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
						}
				);
					
			
			}
		}
	}	
	
	@Override
	protected void backPropagationReversed(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainChildren()!=null){
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate  * sigma * relation.getFrom().getOutput();
							relation.setWeight(relation.getWeight()+newDelta);								
							((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
							
						}
				);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainParents()){
					if(!(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight()*((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
					
				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma_ * relation.getFrom().getOutput();
							relation.setWeight(relation.getWeight()+newDelta);	
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
							((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);								
						}
				);
			
			}
		}
	}		
	
	@Override
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		if(neuron.obtainParents()!=null ){
			final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();

			stream.forEach(relation -> {
						if(!(relation.getProperty() instanceof BPROPSynapseProperty))
							relation.setProperty(new BPROPSynapseProperty());								
						final float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();								
						relation.setWeight(relation.getWeight()+newDelta);
						((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);									
					}
			);
		}
	}
	
	@Override
	protected void updateWeightsReversed(Neuron neuron, boolean lastLayer){
		if(neuron.obtainChildren()!=null){
			final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
	
			stream.forEach(relation -> {
						if(!(relation.getProperty() instanceof BPROPSynapseProperty))
							relation.setProperty(new BPROPSynapseProperty());								
						final float newDelta = ((BPROPSynapseProperty)relation.getProperty()).getAggregated();								
						relation.setWeight(relation.getWeight()+newDelta);
						((BPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
						((BPROPSynapseProperty)relation.getProperty()).setAggregated(0);						
					}
			);
		}			
	}	
	
	@Override
	protected void updateGradients(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) *
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();

				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
							if(deferredAgregateFunction==null)
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
							else
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(
									deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
								);
						}
				);

			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(!(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight() * ((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();

				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma_ * relation.getFrom().getOutput();					
							if(deferredAgregateFunction==null)
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
							else
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(
									deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
								);								
							}
				);				
			}
		}
	}		
	
	@Override
	protected void updateGradientsReversed(Neuron neuron, boolean lastLayer){		
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) *
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainChildren()!=null){
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();

				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma * relation.getFrom().getOutput();
							if(deferredAgregateFunction==null)
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
							else
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(
									deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
								);
						}
				);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainParents()){
					if(!(relation.getProperty() instanceof BPROPSynapseProperty))
						relation.setProperty(new BPROPSynapseProperty());
					sigma+=relation.getWeight() * ((BPROPSynapseProperty)relation.getProperty()).getSigma();
				}				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();

				stream.forEach(relation -> {
							if(!(relation.getProperty() instanceof BPROPSynapseProperty))
								relation.setProperty(new BPROPSynapseProperty());								
							((BPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
							final float newDelta = learningMomentum * ((BPROPSynapseProperty)relation.getProperty()).getDelta() +
									(1-learningMomentum) * learningRate * sigma_ * relation.getFrom().getOutput();					
							if(deferredAgregateFunction==null)
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(((BPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
							else
								((BPROPSynapseProperty)relation.getProperty()).setAggregated(
									deferredAgregateFunction.apply(((BPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
								);								
							}
				);					
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
