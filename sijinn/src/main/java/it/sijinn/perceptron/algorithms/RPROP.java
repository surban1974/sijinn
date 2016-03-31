package it.sijinn.perceptron.algorithms;



import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.Synapse;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.utils.ISynapseProperty;

public class RPROP implements ITrainingAlgorithm {
	
	protected float etaPositive=1.2f;
	protected float etaNegative=0.5f;
	protected float maxDelta=50f;
	protected float minDelta=0.000001f;
	protected IGenerator initialDeltaGenarator=null;

	
	class RPROPSynapseProperty implements ISynapseProperty{
		private float sigma = 0;
		private float delta = 0;
		private float previousDelta = 0;
		private float aggregatedDelta = 0;
		private Synapse relation = null;
		
		public ISynapseProperty setSynapse(Synapse synapse){
			this.relation = synapse;
			return this;
		}
		
		public Synapse getSynapse(){
			return relation;
		}
		
		public RPROPSynapseProperty clear(){
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
			if(previousDelta==0){
				if(initialDeltaGenarator==null)
					return 0.1f;
				else 
					return initialDeltaGenarator.generate((relation==null)?null:relation.getFrom(), (relation==null)?null:relation.getTo());
			}else
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
	
	public RPROP(){
		super();
	}
	
	public RPROP(float _etaPositive, float _etaNegative){
		super();
		this.etaPositive = _etaPositive;
		this.etaNegative = _etaNegative;
	}
	
	public RPROP(float _etaPositive, float _etaNegative, float _minDelta, float _maxDelta, IGenerator _initialDeltaGenarator){
		super();
		this.minDelta = _minDelta;
		this.maxDelta = _maxDelta;
		this.etaPositive = _etaPositive;
		this.etaNegative = _etaNegative;
		this.initialDeltaGenarator = _initialDeltaGenarator;
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
					updateGradients(neuron, i==network.getLayers().size()-1,aggregatorFunction);
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
							((RPROPSynapseProperty)relation.getProperty()).clear();
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
					relation1.setProperty(new RPROPSynapseProperty());
				if(relation2.getProperty()==null)
					relation2.setProperty(new RPROPSynapseProperty());				

				if(aggregatorFunction==null)
					((RPROPSynapseProperty)relation1.getProperty()).setAggregatedDelta(((RPROPSynapseProperty)relation1.getProperty()).getAggregatedDelta()+((RPROPSynapseProperty)relation2.getProperty()).getDelta());
				else
					((RPROPSynapseProperty)relation1.getProperty()).setAggregatedDelta(
							aggregatorFunction.apply(((RPROPSynapseProperty)relation1.getProperty()).getAggregatedDelta(), ((RPROPSynapseProperty)relation2.getProperty()).getDelta())
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
						relation.setProperty(new RPROPSynapseProperty());
					
					float newDelta = 0;
					float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getSigma();
					
					if(previousSigma*sigma>0)
						newDelta = this.etaPositive*
							((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
					else if(previousSigma*sigma<0)
						newDelta = this.etaNegative*
								((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
					else if(previousSigma*sigma==0)
						newDelta = ((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();

					newDelta=Math.min(newDelta,this.maxDelta);
					newDelta=Math.max(newDelta,this.minDelta);
					
					newDelta*= Math.signum(previousSigma*sigma);
				
					relation.setWeight(
							relation.getWeight()+newDelta
							);
					((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((RPROPSynapseProperty)relation.getProperty()).setPreviousDelta(newDelta);
					((RPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.getChildren()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
	
					sigma+=relation.getWeight()*((RPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0);
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					
					
					float newDelta = 0;
					float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getSigma();

					if(previousSigma*sigma>0)
						newDelta = this.etaPositive*
							((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
					else if(previousSigma*sigma<0)
						newDelta = this.etaNegative*
								((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
					else if(previousSigma*sigma==0)
						newDelta = ((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
					
					newDelta=Math.min(newDelta,this.maxDelta);
					newDelta=Math.max(newDelta,this.minDelta);
					
					newDelta*= Math.signum(previousSigma*sigma);
					
					relation.setWeight(relation.getWeight()+ newDelta);
					((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					((RPROPSynapseProperty)relation.getProperty()).setPreviousDelta(newDelta);
					((RPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}
		}
	}	
	
	private void updateWeights(Neuron neuron, boolean lastLayer){
		if(lastLayer){
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					((RPROPSynapseProperty)relation.getProperty()).setPreviousDelta(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					relation.setWeight(relation.getWeight()+((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					((RPROPSynapseProperty)relation.getProperty()).clear();
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					((RPROPSynapseProperty)relation.getProperty()).setPreviousDelta(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					relation.setWeight(relation.getWeight()+((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta());
					((RPROPSynapseProperty)relation.getProperty()).clear();
				}
			}
		}
			
			
	}
	
	private void updateGradients(Neuron neuron, boolean lastLayer, IDAFloatFunction aggregatorFunction){
		
		if(lastLayer){
			float sigma = (neuron.getTarget() - neuron.getOutput())* ((neuron.getFunction()!=null)?neuron.getFunction().derivative((neuron.getTarget() - neuron.getOutput()),new float[]{neuron.getOutput()}):0);
			if(neuron.getParents()!=null){
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					
					float newDelta = 0;
					float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getSigma();
						if(previousSigma*sigma>0)
							newDelta = this.etaPositive*
								((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
						else if(previousSigma*sigma<0)
							newDelta = this.etaNegative*
									((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
						else if(previousSigma*sigma==0)
							newDelta = ((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();

					newDelta=Math.min(newDelta,this.maxDelta);
					newDelta=Math.max(newDelta,this.minDelta);
					
					newDelta*= Math.signum(sigma);

						
					((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					if(aggregatorFunction==null)
						((RPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta()+newDelta);
					else 
						((RPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(
								aggregatorFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta(), newDelta)
						);
					((RPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}			
		}else{
			if(neuron.getParents()!=null && neuron.getChildren()!=null){
				float sigma=0;
				for(Synapse relation:neuron.getChildren()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					
					sigma+=relation.getWeight()*((RPROPSynapseProperty)relation.getProperty()).getSigma();
				}
				
				sigma*=((neuron.getFunction()!=null)?neuron.getFunction().derivative(sigma,new float[]{neuron.getOutput()}):0);
				
				for(Synapse relation:neuron.getParents()){
					if(relation.getProperty()==null)
						relation.setProperty(new RPROPSynapseProperty());
					
					float newDelta = 0;
					float previousSigma = ((RPROPSynapseProperty)relation.getProperty()).getSigma();
						if(previousSigma*sigma>0)
							newDelta = this.etaPositive*
								((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
						else if(previousSigma*sigma<0)
							newDelta = this.etaNegative*
									((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();
						else if(previousSigma*sigma==0)
							newDelta = ((RPROPSynapseProperty)relation.getProperty()).getPreviousDelta();

					newDelta=Math.min(newDelta,this.maxDelta);
					newDelta=Math.max(newDelta,this.minDelta);
					
					newDelta*= Math.signum(sigma);
						
					((RPROPSynapseProperty)relation.getProperty()).setSigma(sigma);
					if(aggregatorFunction==null)
						((RPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta()+newDelta);
					else 
						((RPROPSynapseProperty)relation.getProperty()).setAggregatedDelta(
								aggregatorFunction.apply(((RPROPSynapseProperty)relation.getProperty()).getAggregatedDelta(),newDelta)
						);
					((RPROPSynapseProperty)relation.getProperty()).setDelta(newDelta);
				}
			}
		}
	}

	public String toSaveString(){
		return "algorithm="+this.getClass().getSimpleName()+","+etaNegative+","+etaPositive+","+minDelta+","+maxDelta;
	}

}
