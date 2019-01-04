package it.sijinn.perceptron.algorithms;

import java.util.function.Consumer;
import java.util.stream.Stream;

import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.utils.ISynapseProperty;


public class QPROP extends TrainAlgorithm implements ITrainingAlgorithm {
	
	protected float learningRate=0.1f;
	protected float decay=0.0001f;
	protected float epsilon=0.35f;

	
	protected class QPROPSynapseProperty implements ISynapseProperty{
		private float sigma = 0;
		private float delta = 0;
		private float aggregated = 0;
		private float previousAggregated = 0;
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
			previousAggregated = 0;
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

		public float getPreviousAggregated() {
			return previousAggregated;
		}

		public void setPreviousAggregated(float prevAggregated) {
			this.previousAggregated = prevAggregated;
		}

	
	}
	
	
	public QPROP(){
		super();
	}
	public QPROP(float learningRate, float decay, float epsilon){
		super();
		this.learningRate = learningRate;
		this.decay = decay;
		this.epsilon = epsilon;
	}
	
	@Override
	protected ISynapseProperty instanceProperty() {
		return new QPROPSynapseProperty();
	}	

	
	protected void backPropagation(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			final float sigma0 = (neuron.getTarget() - neuron.getOutput()) * 
					(
						((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
						((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
								
									final float newDelta = learningRate  * sigma0 * relation.getFrom().getOutput();
									
									final float shrink = learningRate/(1f+learningRate);
									final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
									final float sigma = -newDelta+decay*relation.getWeight();
									final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									
									if(delta<0) {
										if (sigma>0)
											weightChange-=epsilon*sigma;
										if (sigma>=(shrink*previousSigma)) 
											weightChange+=learningRate * delta;
										else
											weightChange+=delta*sigma/(previousSigma-sigma);
									}else if(delta>0){
										if (sigma<0)
											weightChange-=epsilon*sigma;
										else if(sigma<=(shrink*previousSigma))
											weightChange+=learningRate * delta; 
										else
											weightChange+=delta*sigma/(previousSigma-sigma); 
									}else 
										weightChange-=epsilon*sigma;
									
									((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
									((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(newDelta);
									relation.setWeight(relation.getWeight()+weightChange);								
							}
						}
				);
			}
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma0=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
						relation.setProperty(new QPROPSynapseProperty());
					sigma0+=relation.getWeight()*((QPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma0*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma0,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma0_ = sigma0;
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
						new Consumer<Synapse>() {
							@Override
							public void accept(Synapse relation) {
								if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
									relation.setProperty(new QPROPSynapseProperty());
							
								final float newDelta = learningRate * sigma0_ * relation.getFrom().getOutput();
								
								final float shrink = learningRate/(1f+learningRate);
								final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
								final float sigma = -newDelta+decay*relation.getWeight();
								final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
								float weightChange=0;
								
								if(delta<0) {
									if (sigma>0)
										weightChange-=epsilon*sigma;
									if (sigma>=(shrink*previousSigma)) 
										weightChange+=learningRate * delta;
									else
										weightChange+=delta*sigma/(previousSigma-sigma);
								}else if(delta>0){
									if (sigma<0)
										weightChange-=epsilon*sigma;
									else if(sigma<=(shrink*previousSigma))
										weightChange+=learningRate * delta; 
									else
										weightChange+=delta*sigma/(previousSigma-sigma); 
								}else 
									weightChange-=epsilon*sigma;
								
								((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
								((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(newDelta);
								
								relation.setWeight(relation.getWeight()+weightChange);
							}
						}
				);
			
			}
		}
	}	
	
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									final float shrink = learningRate/(1f+learningRate);
									final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
									final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+decay*relation.getWeight();
									final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									
									if(delta<0) {
										if (sigma>0)
											weightChange-=epsilon*sigma;
										if (sigma>=(shrink*previousSigma)) 
											weightChange+=learningRate * delta;
										else
											weightChange+=delta*sigma/(previousSigma-sigma);
									}else if(delta>0){
										if (sigma<0)
											weightChange-=epsilon*sigma;
										else if(sigma<=(shrink*previousSigma))
											weightChange+=learningRate * delta; 
										else
											weightChange+=delta*sigma/(previousSigma-sigma); 
									}else 
										weightChange-=epsilon*sigma;
									
									((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
									((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
									relation.setWeight(
											relation.getWeight()+weightChange
											);
	
								}
							}
					);
				}
						
		}else{
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									final float shrink = learningRate/(1f+learningRate);
									final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
									final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+decay*relation.getWeight();
									final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									
									if(delta<0) {
										if (sigma>0)
											weightChange-=epsilon*sigma;
										if (sigma>=(shrink*previousSigma)) 
											weightChange+=learningRate * delta;
										else
											weightChange+=delta*sigma/(previousSigma-sigma);
									}else if(delta>0){
										if (sigma<0)
											weightChange-=epsilon*sigma;
										else if(sigma<=(shrink*previousSigma))
											weightChange+=learningRate * delta; 
										else
											weightChange+=delta*sigma/(previousSigma-sigma); 
									}else 
										weightChange-=epsilon*sigma;
									
									((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
									((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
									relation.setWeight(
											relation.getWeight()+weightChange
											);
								}
							}
					);
				}
			
		}

	}
	
	protected void updateWeightsReversed(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			if(neuron.obtainChildren()!=null){
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									final float shrink = learningRate/(1f+learningRate);
									final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
									final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+decay*relation.getWeight();
									final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									
									if(delta<0) {
										if (sigma>0)
											weightChange-=epsilon*sigma;
										if (sigma>=(shrink*previousSigma)) 
											weightChange+=learningRate * delta;
										else
											weightChange+=delta*sigma/(previousSigma-sigma);
									}else if(delta>0){
										if (sigma<0)
											weightChange-=epsilon*sigma;
										else if(sigma<=(shrink*previousSigma))
											weightChange+=learningRate * delta; 
										else
											weightChange+=delta*sigma/(previousSigma-sigma); 
									}else 
										weightChange-=epsilon*sigma;
				
									
				
									
									((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
									((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
									relation.setWeight(
											relation.getWeight()+weightChange
											);
	
								}
							}
					);
			}			
		}else{
			if(neuron.obtainChildren()!=null){
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									final float shrink = learningRate/(1f+learningRate);
									final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
									final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+decay*relation.getWeight();
									final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									
									if(delta<0) {
										if (sigma>0)
											weightChange-=epsilon*sigma;
										if (sigma>=(shrink*previousSigma)) 
											weightChange+=learningRate * delta;
										else
											weightChange+=delta*sigma/(previousSigma-sigma);
									}else if(delta>0){
										if (sigma<0)
											weightChange-=epsilon*sigma;
										else if(sigma<=(shrink*previousSigma))
											weightChange+=learningRate * delta; 
										else
											weightChange+=delta*sigma/(previousSigma-sigma); 
									}else 
										weightChange-=epsilon*sigma;
				
									
				
									
									((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
									((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
									relation.setWeight(
											relation.getWeight()+weightChange
											);
								}
							}
					);
			}
		}

	}	

	
	protected void updateGradients(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									final float newDelta = sigma * relation.getFrom().getOutput();
									if(deferredAgregateFunction==null)
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
						relation.setProperty(new QPROPSynapseProperty());					
					sigma+=relation.getWeight()*((QPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
									
									final float newDelta = sigma_ * relation.getFrom().getOutput();					
									if(deferredAgregateFunction==null)
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);
			}
		}
	}
	
	protected void updateGradientsReversed(Neuron neuron, boolean lastLayer){
		
		if(lastLayer){
			final float sigma = (neuron.getTarget() - neuron.getOutput()) * 
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainChildren()!=null){
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									final float newDelta = sigma * relation.getTo().getOutput();
									if(deferredAgregateFunction==null)
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);		
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
						relation.setProperty(new QPROPSynapseProperty());					
					sigma+=relation.getWeight()*((QPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
										relation.setProperty(new QPROPSynapseProperty());
									((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
									
									final float newDelta = sigma_ * relation.getTo().getOutput();					
									if(deferredAgregateFunction==null)
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((QPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);
			}
		}
	}
	
	
	@Override
	public String getDefinition() {
//		return "algorithm="+this.getClass().getSimpleName()+","+learningRate+","+decay+","+epsilon+
//				((deferredAgregateFunction==null)?"":","+deferredAgregateFunction.getDefinition()+"");
		return "algorithm="+this.getClass().getSimpleName()+"{"+learningRate+"|"+decay+"|"+epsilon+"}";		
	}

	public float getLearningRate() {
		return learningRate;
	}

	public QPROP setLearningRate(float learningRate) {
		this.learningRate = learningRate;
		return this;
	}

	public float getDecay() {
		return decay;
	}

	public QPROP setDecay(float decay) {
		this.decay = decay;
		return this;
	}

	public float getEpsilon() {
		return epsilon;
	}

	public QPROP setEpsilon(float epsilon) {
		this.epsilon = epsilon;
		return this;
	}



}
