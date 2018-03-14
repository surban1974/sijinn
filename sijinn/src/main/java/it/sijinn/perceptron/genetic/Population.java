package it.sijinn.perceptron.genetic;

import java.io.Serializable;
import java.util.Arrays;

public class Population implements Serializable{

	private static final long serialVersionUID = 1L;
	private Species[] species;

    public Population(int populationSize, float[] initialWeights, INeuralBreeding iBreading) {
    	species = new Species[populationSize];
        if (initialWeights!=null) {
            for (int i = 0; i < size(); i++) {
                Species newspecies = new Species(initialWeights.length);
                newspecies.generate(initialWeights, iBreading);
                setSpecies(i, newspecies);
            }
        }
    }

    public Species getSpecies(int index) {
        return species[index];
    }

    public Species getFittest(INeuralBreeding iBreading) {
    	if(iBreading!=null)
    		return iBreading.fittest(this);
    	else{
	        Species fittest = species[0];
	        for (int i = 0; i < size(); i++) {
	            if (fittest.getFitness() >= getSpecies(i).getFitness()) {
	                fittest = getSpecies(i);
	            }
	        }
	        return fittest;
    	}
    }

    public int size() {
        return species.length;
    }


    public void setSpecies(int index, Species indiv) {
    	species[index] = indiv;
    }

	public Species[] getSpecies() {
		return species;
	}
	
	public Iterable<Species> getSpeciesIterable() {
		if(species!=null)
			return Arrays.asList(species);
		else 
			return Arrays.asList(new Species[0]);
	}
}
