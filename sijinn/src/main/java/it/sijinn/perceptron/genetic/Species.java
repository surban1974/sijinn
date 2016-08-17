package it.sijinn.perceptron.genetic;

public class Species {

    private float[] weights;
    private float fitness = 0;
    
    public Species(int size){
    	super();
    	this.weights = new float[size];
    }

    public void generate(float[] initialWeights, INeuralBreeding iBreading) {
    	this.weights = new float[initialWeights.length];
        for (int i = 0; i < size(); i++){ 
        	if(iBreading!=null)
        		this.weights[i] = iBreading.mutateInitial(this.weights[i], initialWeights[i]);
        	else
        		this.weights[i]+=initialWeights[i]+ (Math.random()-0.5);
        }
        
    }
    
    public float getWeight(int index) {
        return weights[index];
    }

    public void setWeight(int index, float value) {
    	if(index<weights.length && index>-1)
    		weights[index] = value;
        fitness = 0;
    }

    public int size() {
        return weights.length;
    }

    public float getFitness() {
        return fitness;
    }
    
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}    

    @Override
    public String toString() {
        String geneString = fitness+",{";
        for (float weight:weights) 
            geneString += weight+",";
        geneString+="}";
        
        return geneString;
    }

	public float[] getWeights() {
		return weights;
	}




}
