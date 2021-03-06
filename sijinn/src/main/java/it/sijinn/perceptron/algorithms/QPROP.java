package it.sijinn.perceptron.algorithms;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
	
						final float newDelta = learningRate  * sigma0 * relation.getFrom().getOutput();
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -newDelta+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(newDelta);
						relation.setWeight(relation.getWeight()+weightChange);	
					}
				}
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
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						
						final float newDelta = learningRate * sigma0 * relation.getFrom().getOutput();
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -newDelta+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(newDelta);
						
						relation.setWeight(relation.getWeight()+weightChange);	
					}
				}
			
			}
		}
	}	
	
	protected void updateWeights(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			if(neuron.obtainParents()!=null){
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{				
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
						((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
						relation.setWeight(
								relation.getWeight()+weightChange
								);
					}
				}
			}			
		}else{
			if(neuron.obtainParents()!=null){
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{	
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
						((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
						relation.setWeight(
								relation.getWeight()+weightChange
								);
					}
				}
			}
		}

	}
	
	protected void updateWeightsReversed(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			if(neuron.obtainChildren()!=null){
				if(neuron.obtainChildren().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainChildren().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{					
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
	
						
	
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
						((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
						relation.setWeight(
								relation.getWeight()+weightChange
								);
					}
				}
			}			
		}else{
			if(neuron.obtainChildren()!=null){
				if(neuron.obtainChildren().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainChildren().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{					
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						
						final float shrink = this.learningRate/(1f+this.learningRate);
						final float delta = ((QPROPSynapseProperty)relation.getProperty()).getDelta();
						final float sigma = -((QPROPSynapseProperty)relation.getProperty()).getAggregated()+this.decay*relation.getWeight();
						final float previousSigma = -((QPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
						float weightChange=0;
						
						if(delta<0) {
							if (sigma>0)
								weightChange-=this.epsilon*sigma;
							if (sigma>=(shrink*previousSigma)) 
								weightChange+=this.learningRate * delta;
							else
								weightChange+=delta*sigma/(previousSigma-sigma);
						}else if(delta>0){
							if (sigma<0)
								weightChange-=this.epsilon*sigma;
							else if(sigma<=(shrink*previousSigma))
								weightChange+=this.learningRate * delta; 
							else
								weightChange+=delta*sigma/(previousSigma-sigma); 
						}else 
							weightChange-=this.epsilon*sigma;
	
						
	
						
						((QPROPSynapseProperty)relation.getProperty()).setDelta(weightChange);
						((QPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated());
						((QPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
						relation.setWeight(
								relation.getWeight()+weightChange
								);
					}
				}
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
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
								((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
								final float newDelta = sigma * relation.getFrom().getOutput();
								if(deferredAgregateFunction==null)
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
								else
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(
										deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
									);
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{	
					for(final Synapse relation:neuron.obtainParents()){
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
				if(neuron.obtainParents().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainParents().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainParents()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
								((QPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
								
								final float newDelta = sigma_ * relation.getFrom().getOutput();					
								if(deferredAgregateFunction==null)
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(((QPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
								else
									((QPROPSynapseProperty)relation.getProperty()).setAggregated(
										deferredAgregateFunction.apply(((QPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
									);
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{	
					for(final Synapse relation:neuron.obtainParents()){
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
				if(neuron.obtainChildren().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainChildren().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{				
					for(final Synapse relation:neuron.obtainChildren()){
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
				if(neuron.obtainChildren().size()>1 && isParallel()) {
					final ExecutorService executorService = Executors.newFixedThreadPool((getParallelLimit()==0)?neuron.obtainChildren().size():getParallelLimit());	
					final List<Future<Synapse>> futures = new LinkedList<>();
					for(final Synapse relation:neuron.obtainChildren()){
						if(relation.getProperty()==null || !(relation.getProperty() instanceof QPROPSynapseProperty))
							relation.setProperty(new QPROPSynapseProperty());
						final Callable<Synapse> callable = new Callable<Synapse>() {
							public Synapse call() throws Exception {
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
								return relation;
							}
						};
						futures.add(executorService.submit(callable));
					}					
					executorService.shutdown();
					for(final Future<Synapse> future: futures) {
						try {
							future.get();
						}catch (Exception e) {
						}
					}
				}else{				
					for(final Synapse relation:neuron.obtainChildren()){
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
