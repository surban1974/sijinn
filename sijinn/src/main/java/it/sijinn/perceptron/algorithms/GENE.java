package it.sijinn.perceptron.algorithms;

import it.sijinn.common.Network;
import it.sijinn.perceptron.functions.deferred.IDAFloatFunction;
import it.sijinn.perceptron.genetic.INeuralBreeding;
import it.sijinn.perceptron.genetic.Population;
import it.sijinn.perceptron.genetic.Species;

public class GENE implements IGeneticAlgorithm{

	protected IDAFloatFunction deferredAgregateFunction;
	protected INeuralBreeding neuralBreeding;
	protected int populationSize=50;
	protected Population population;
	
    public GENE(INeuralBreeding neuralBreeding){
    	super();
    	this.neuralBreeding = neuralBreeding;
    }
    
	public ITrainingAlgorithm calculateAndUpdateWeights(Network network, boolean reversed) throws Exception{		
		return this;
	}

	public ITrainingAlgorithm calculate(Network network, boolean reversed) throws Exception{		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;

		if(neuralBreeding!=null){
			if(population==null)
				population = new Population(populationSize, network.getWeight(), neuralBreeding);
			else
				population = neuralBreeding.evolve(population);
		}
		return this;
	}
	
	
	public ITrainingAlgorithm updateWeights(Network network, boolean reversed) throws Exception{		
		if(network==null || network.getLayers()==null || network.getLayers().size()==0)
			return this;

		Species fittest = population.getFittest(neuralBreeding);
		
		if(fittest!=null){
			network.setWeight(fittest.getWeights());
			network.setError(fittest.getFitness());
		}		

		return this;
	}	
	
	public ITrainingAlgorithm sync(Network network1, Network network2, int type, boolean reversed) throws Exception{
		return this;
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

	@Override
	public String getDefinition(){
		return "algorithm="+this.getClass().getSimpleName()+"{"+((neuralBreeding==null)?0:neuralBreeding.getUniformRate())+"|"+((neuralBreeding==null)?0:neuralBreeding.getMutationRate())+"|"+((neuralBreeding==null)?0:neuralBreeding.getTournamentSize())+"|"+((neuralBreeding==null)?0:neuralBreeding.isElitism())+"}";
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public GENE setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
		return this;
	}

	@Override
	public Population getPopulation() {
		return population;
	}

}
