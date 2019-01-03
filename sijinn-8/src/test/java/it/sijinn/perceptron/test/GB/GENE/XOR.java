package it.sijinn.perceptron.test.GB.GENE;




import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.GENE;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.genetic.NeuralBreeding;
import it.sijinn.perceptron.strategies.GeneticBreeding;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;



public class XOR {

	private static Network network;
	private final int maxSteps = 100000;
	private float approximation = 0.000001f;
	

  
  @Test
  public void beeding() throws Exception {
	  
	  	network = new Network().open(new ResourceStreamWrapper("it/sijinn/perceptron/test/resources/XOR_init.net"));
	  	  

		final float[][][] trainingData = 					
				new float[][][] {						
					{{1, 1}, {0}},
			        {{0, 1}, {1}},
			        {{1, 0}, {1}},
			        {{0, 0}, {0}},
				};
				
		final ITrainingStrategy trainingStrategy = new GeneticBreeding(
				new GENE(
					new NeuralBreeding()
					.setElitism(true)
					.setTournamentSize(50)					
				)
				.setPopulationSize(1000)				
			)		
//			.setParallelLimit(10)	
			.setErrorFunction(new MSE());
				
		Assert.assertArrayEquals(network.getWeight(), new float[]{-0.082843f, -0.011006f, 0.018629f, -0.071407f, 0.03268f, 0.020701f}, approximation);

		
	  	float delta = network.training(trainingData, trainingStrategy);		
	  	

	  	int step=0;
		for(step=0;step<maxSteps;step++){
			if(delta>approximation){
				delta = network.training(trainingData, trainingStrategy);
				if(step % 1000 == 0)
					System.out.println("Step: " + step + " MSE: " + delta);
			}else
				break;
		}	  	
		
	  	
		float[][] execution = network.compute(
				new float[][] {					
					{0, 0},
					{0, 1},
					{1, 0},
					{0, 0}
				});
		
	  	Assert.assertEquals(step, 100000);

//	  	Assert.assertEquals(delta, 0.12, approximation);
	  	Assert.assertArrayEquals(execution[0], new float[]{0}, 0.1f);
	  	Assert.assertArrayEquals(execution[1], new float[]{1}, 1f);
	  	Assert.assertArrayEquals(execution[2], new float[]{1}, 0.1f);
	  	Assert.assertArrayEquals(execution[3], new float[]{0}, 0.1f);
	  	


  }
  


  
  
  @After
  public void releaseNetwork() throws Exception {
	  if(network!=null)
		  network.release();
  }


 
}
