package it.sijinn.perceptron.algorithms;

import it.sijinn.perceptron.genetic.Population;

public interface IGeneticAlgorithm extends ITrainingAlgorithm{
	Population getPopulation();
}
