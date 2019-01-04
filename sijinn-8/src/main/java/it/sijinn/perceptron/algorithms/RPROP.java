package it.sijinn.perceptron.algorithms;



import java.util.function.Consumer;
import java.util.stream.Stream;

import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.utils.ISynapseProperty;

public class RPROP extends TrainAlgorithm implements ITrainingAlgorithm {
	
	protected float etaPositive=1.2f;
	protected float etaNegative=0.5f;
	protected float maxDelta=50f;
	protected float minDelta=0.000001f;
	protected IGenerator initialDeltaGenarator=null;
	private float sigmaBP;

	
	protected class RPROPSynapseProperty implements ISynapseProperty{
		private float sigma = 0;
		private float delta = 0;
		private float weightChange = 0;
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
			weightChange = 0;
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
			if(delta==0){
				if(initialDeltaGenarator==null)
					return 0.1f;
				else 
					return initialDeltaGenarator.generate((relation==null)?null:relation.getFrom(), (relation==null)?null:relation.getTo());
			}else
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

		public float getWeightChange() {
			return weightChange;
		}

		public void setWeightChange(float previousWeightChange) {
			this.weightChange = previousWeightChange;
		}

		public float getPreviousAggregated() {
			return previousAggregated;
		}

		public void setPreviousAggregated(float prevAggregated) {
			this.previousAggregated = prevAggregated;
		}

	
	}
	
	public RPROP(){
		super();
	}
	public RPROP(float etaNegative, float etaPositive, float minDelta, float maxDelta){
		super();
		this.etaNegative = etaNegative;
		this.etaPositive = etaPositive;
		this.minDelta = minDelta;
		this.maxDelta = maxDelta;
	}
	
	@Override
	protected ISynapseProperty instanceProperty() {
		return new RPROPSynapseProperty();
	}
	
	
	protected void backPropagation(Neuron neuron, boolean lastLayer){

		if(lastLayer){
			sigmaBP = (neuron.getTarget() - neuron.getOutput()) * 
					(
							((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0)+
							((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0)
					);
			if(neuron.obtainParents()!=null){
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEachOrdered(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigmaBP>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigmaBP)*delta;
									}else if(previousSigma*sigmaBP<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigmaBP=0;
									}else if(previousSigma*sigmaBP==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigmaBP)*delta;
									}
								
									relation.setWeight(
											relation.getWeight()+weightChange
											);
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigmaBP);					
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
								}
							}
					);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				sigmaBP=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
						relation.setProperty(new RPROPSynapseProperty());
	
					sigmaBP+=relation.getWeight()*((RPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigmaBP*=((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigmaBP,new float[]{neuron.getOutput()}):0);
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEachOrdered(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigmaBP>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigmaBP)*delta;
									}else if(previousSigma*sigmaBP<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigmaBP=0;
									}else if(previousSigma*sigmaBP==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigmaBP)*delta;
									}
									
									relation.setWeight(
											relation.getWeight()+weightChange
											);
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigmaBP);					
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									
									float sigma = ((RPROPSynapseProperty)relation.getProperty()).getAggregated();
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigma>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigma)*delta;
									}else if(previousSigma*sigma<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigma=0;
									}else if(previousSigma*sigma==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigma)*delta;
									}
									
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigma);
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
									((RPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									
									float sigma = ((RPROPSynapseProperty)relation.getProperty()).getAggregated();
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigma>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigma)*delta;
									}else if(previousSigma*sigma<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigma=0;
										((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
										((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									}else if(previousSigma*sigma==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigma)*delta;
									}
								
					
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigma);
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
									((RPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
					
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									float sigma = ((RPROPSynapseProperty)relation.getProperty()).getAggregated();
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigma>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigma)*delta;
									}else if(previousSigma*sigma<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigma=0;
									}else if(previousSigma*sigma==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigma)*delta;
									}
									
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigma);
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
									((RPROPSynapseProperty)relation.getProperty()).setAggregated(0);
								
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									float sigma = ((RPROPSynapseProperty)relation.getProperty()).getAggregated();
									float delta = 0;
									final float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getPreviousAggregated();
									float weightChange=0;
									if(previousSigma*sigma>0){
										delta = etaPositive*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.min(delta,maxDelta);
										weightChange= Math.signum(sigma)*delta;
									}else if(previousSigma*sigma<0){
										delta = etaNegative*((RPROPSynapseProperty)relation.getProperty()).getDelta();
										delta=Math.max(delta,minDelta);
										weightChange=-((RPROPSynapseProperty)relation.getProperty()).getWeightChange();
										sigma=0;
										((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
										((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									}else if(previousSigma*sigma==0){
										delta = ((RPROPSynapseProperty)relation.getProperty()).getDelta();
										weightChange= Math.signum(sigma)*delta;
									}
								
					
									((RPROPSynapseProperty)relation.getProperty()).setDelta(delta);
									((RPROPSynapseProperty)relation.getProperty()).setPreviousAggregated(sigma);
									((RPROPSynapseProperty)relation.getProperty()).setWeightChange(weightChange);
									((RPROPSynapseProperty)relation.getProperty()).setAggregated(0);
					
					
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									final float newDelta = sigma * relation.getFrom().getOutput();
									if(deferredAgregateFunction==null)
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(((RPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainChildren()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
						relation.setProperty(new RPROPSynapseProperty());					
					sigma+=relation.getWeight()*((RPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainParents().size()>1 && isParallel())?neuron.obtainParents().parallelStream():neuron.obtainParents().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);								
									final float newDelta = sigma_ * relation.getFrom().getOutput();					
									if(deferredAgregateFunction==null)
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(((RPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
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
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
									final float newDelta = sigma * relation.getTo().getOutput();
									if(deferredAgregateFunction==null)
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(((RPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
					);
			}			
		}else{
			if(neuron.obtainParents()!=null && neuron.obtainChildren()!=null){
				float sigma=0;
				for(final Synapse relation:neuron.obtainParents()){
					if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
						relation.setProperty(new RPROPSynapseProperty());					
					sigma+=relation.getWeight()*((RPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=(((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0)+((neuron.getFunction()!=null)?neuron.getFunction().flatspot():0));
				final float sigma_ = sigma;
				final Stream<Synapse> stream = (neuron.obtainChildren().size()>1 && isParallel())?neuron.obtainChildren().parallelStream():neuron.obtainChildren().stream();
				stream.forEach(
							new Consumer<Synapse>() {
								@Override
								public void accept(Synapse relation) {
									if(relation.getProperty()==null || !(relation.getProperty() instanceof RPROPSynapseProperty))
										relation.setProperty(new RPROPSynapseProperty());
									((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma_);
									
									final float newDelta = sigma_ * relation.getTo().getOutput();					
									if(deferredAgregateFunction==null)
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(((RPROPSynapseProperty)relation.getProperty()).getAggregated()+newDelta);
									else
										((RPROPSynapseProperty)relation.getProperty()).setAggregated(
											deferredAgregateFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregated(), newDelta)
										);
								}
							}
						);
			}
		}
	}

	

	public String getDefinition(){
//		return "algorithm="+this.getClass().getSimpleName()+","+etaNegative+","+etaPositive+","+minDelta+","+maxDelta+
//				((deferredAgregateFunction==null)?"":","+deferredAgregateFunction.getDefinition()+"");
		return "algorithm="+this.getClass().getSimpleName()+"{"+etaNegative+"|"+etaPositive+"|"+minDelta+"|"+maxDelta+"}";

	}


	public RPROP setEtaPositive(float etaPositive) {
		this.etaPositive = etaPositive;
		return this;
	}


	public RPROP setEtaNegative(float etaNegative) {
		this.etaNegative = etaNegative;
		return this;
	}


	public RPROP setMaxDelta(float maxDelta) {
		this.maxDelta = maxDelta;
		return this;
	}


	public RPROP setMinDelta(float minDelta) {
		this.minDelta = minDelta;
		return this;
	}


	public RPROP setInitialDeltaGenarator(IGenerator initialDeltaGenarator) {
		this.initialDeltaGenarator = initialDeltaGenarator;
		return this;
	}
	public float getEtaPositive() {
		return etaPositive;
	}
	public float getEtaNegative() {
		return etaNegative;
	}
	public float getMaxDelta() {
		return maxDelta;
	}
	public float getMinDelta() {
		return minDelta;
	}



}
