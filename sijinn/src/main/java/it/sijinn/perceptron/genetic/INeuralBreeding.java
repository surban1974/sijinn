package it.sijinn.perceptron.genetic;

public interface INeuralBreeding {
	Population evolve(Population population);
	Species crossover(Species single1, Species single2);
	Species mutate(Species single);
	Species tournament(Population population);
	float mutateInitial(float first, float second);
	Species fittest(Population population);
	
	float getUniformRate();
	INeuralBreeding setUniformRate(float uniformRate);
	float getMutationRate();
	INeuralBreeding setMutationRate(float mutationRate);
	int getTournamentSize();
	INeuralBreeding setTournamentSize(int tournamentSize);
	boolean isElitism();
	INeuralBreeding setElitism(boolean elitism);
}
