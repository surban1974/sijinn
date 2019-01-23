package it.sijinn.perceptron.genetic;

public class NeuralBreeding implements INeuralBreeding {

	private static final long serialVersionUID = 1L;
	private float uniformRate = 0.5f;
    private float mutationRate = 0.015f;
    private int tournamentSize = 5;
    private boolean elitism = true;	

	@Override
	public Population evolve(Population population) {

		final Population newPopulation = new Population(population.size(), null, this);
        if(elitism) 
        	newPopulation.setSpecies(0, population.getFittest(this));
        int elitismOffset;
        if(elitism)
            elitismOffset = 1;
        else
            elitismOffset = 0;

        for (int i = elitismOffset; i < population.size(); i++) {
        	final Species single1 = tournament(population);
        	final Species single2 = tournament(population);
        	final Species newSingle = crossover(single1, single2);
	        newPopulation.setSpecies(i, newSingle);
	    }

	        // Mutate population
	        for (int i = elitismOffset; i < newPopulation.size(); i++) {
	        	mutate(newPopulation.getSpecies(i));
	        }

	        return newPopulation;

	}

	@Override
	public Species crossover(Species species1, Species species2) {
		final Species newSol = new Species(species1.size());
        for (int i = 0; i < species1.size(); i++) {
            if(Math.random() <= uniformRate)
                newSol.setWeight(i, species1.getWeight(i));
            else
                newSol.setWeight(i, species2.getWeight(i));
        }
        return newSol;
	}

	@Override
	public Species mutate(Species species) {
        for (int i = 0; i < species.size(); i++) {
            if (Math.random() <= mutationRate) 
            	species.setWeight(i, species.getWeight(i)+(float)(Math.random()-0.5));
        }
        return species;
	}

	@Override
	public Species tournament(Population population) {
		final Population tournament = new Population(tournamentSize, null, this);
        int min = Math.min(tournamentSize, population.size());
        for (int i = 0; i < min; i++) {
        	final int randomId = (int) (Math.random() * population.size());
            if(randomId<population.size())
            	tournament.setSpecies(i, population.getSpecies(randomId));
        }
        return tournament.getFittest(this);
	}
	
	@Override
	public float mutateInitial(float first, float second) {
		return first + second + (float)(Math.random()-0.5);
	}
	
	@Override
    public Species fittest(Population population) {
		if(population!=null && population.size()>0){
	        Species fittest = population.getSpecies(0);
	        for (int i = 0; i < population.size(); i++) {
	            if (fittest.getFitness() >= population.getSpecies(i).getFitness()) {
	                fittest =population.getSpecies(i);
	            }
	        }
	        return fittest;
		}else
			return null;
    }

	public float getUniformRate() {
		return uniformRate;
	}

	public NeuralBreeding setUniformRate(float uniformRate) {
		this.uniformRate = uniformRate;
		return this;
	}

	public float getMutationRate() {
		return mutationRate;
	}

	public NeuralBreeding setMutationRate(float mutationRate) {
		this.mutationRate = mutationRate;
		return this;
	}

	public int getTournamentSize() {
		return tournamentSize;
	}

	public NeuralBreeding setTournamentSize(int tournamentSize) {
		this.tournamentSize = tournamentSize;
		return this;
	}

	public boolean isElitism() {
		return elitism;
	}

	public NeuralBreeding setElitism(boolean elitism) {
		this.elitism = elitism;
		return this;
	}



}
