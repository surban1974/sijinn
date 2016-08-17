package it.sijinn.perceptron.genetic;

public class Algorithm {


    private static final float uniformRate = 0.5f;
    private static final float mutationRate = 0.015f;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;


    public static Population evolvePopulation(Population pop) {
        Population newPopulation = new Population(pop.size(), null, new NeuralBreeding());

        if (elitism) 
            newPopulation.setSpecies(0, pop.getFittest(new NeuralBreeding()));
        


        int elitismOffset;
        if(elitism)
            elitismOffset = 1;
        else
            elitismOffset = 0;
        

        // Tournament
        for (int i = elitismOffset; i < pop.size(); i++) {
            Species single1 = tournamentSelection(pop);
            Species single2 = tournamentSelection(pop);
            Species newSingle = crossover(single1, single2);
            newPopulation.setSpecies(i, newSingle);
        }

        // Mutate population
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getSpecies(i));
        }

        return newPopulation;
    }

    // Crossover single
    private static Species crossover(Species single1, Species single2) {
    	Species newSol = new Species(single1.size());
        for (int i = 0; i < single1.size(); i++) {
            // Crossover
            if (Math.random() <= uniformRate) {
                newSol.setWeight(i, single1.getWeight(i));
            } else {
                newSol.setWeight(i, single2.getWeight(i));
            }
        }
        return newSol;
    }

    // Mutate single
    private static Species mutate(Species single) {
        for (int i = 0; i < single.size(); i++) {
            if (Math.random() <= mutationRate) 
                // Create random weight
                single.setWeight(i, single.getWeight(i)+(float)(Math.random()-0.5));
            
        }
        return single;
    }

    // Select single for crossover
    private static Species tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = new Population(tournamentSize, null, new NeuralBreeding());
        // For each place in the tournament get a random single
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            if(randomId<pop.size())
            	tournament.setSpecies(i, pop.getSpecies(randomId));
        }
        // Get the fittest
        Species fittest = tournament.getFittest(new NeuralBreeding());
        return fittest;
    }
}
