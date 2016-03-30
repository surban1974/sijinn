package it.sijinn.perceptron.test.SGD.RPROP;




import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.ResourceStreamWrapper;



public class XOR {

	private static Network network;

	private final int maxSteps = 50000;
	private float approximation = 0.000001f;
	

  
  @Test
  public void backPropagation() throws Exception {
	  
	  	network = new Network().open(new ResourceStreamWrapper("it/sijinn/perceptron/test/resources/XOR_init.net"));
	  	

		final float[][][] trainingData = 					
				new float[][][] {						
					{{1, 1}, {0}},
			        {{0, 1}, {1}},
			        {{1, 0}, {1}},
			        {{0, 0}, {0}},
				};
				
		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new RPROP(), new MSE());

		Assert.assertArrayEquals(network.getWeight(), new float[]{-0.082843f, -0.011006f, 0.018629f, -0.071407f, 0.03268f, 0.020701f}, approximation);

		
	  	float delta = network.training(trainingData, trainingStrategy);		
	  	
//	  	Assert.assertArrayEquals(network.getWeight(), new float[]{-0.07902719f, -0.0071963067f, 0.020662606f, -0.06946719f, 0.029533438f, 0.017504819f}, approximation);
//	  	Assert.assertEquals(delta, 0.5153747f, approximation);

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
		
	  	Assert.assertEquals(step, 50000);
	  	Assert.assertArrayEquals(network.getWeight(), new float[]{8.644926f, 0.958956f, 8.475787f, 0.9586877f, 36.75325f, -45.854008f}, approximation);
	  	Assert.assertEquals(delta, 0.0015931221f, approximation);
	  	Assert.assertArrayEquals(execution[0], new float[]{0}, 0.1f);
	  	Assert.assertArrayEquals(execution[1], new float[]{1}, 0.1f);
	  	Assert.assertArrayEquals(execution[2], new float[]{1}, 0.1f);
	  	Assert.assertArrayEquals(execution[3], new float[]{0}, 0.1f);
	  	


  }
  


  
  
  @After
  public void releaseNetwork() throws Exception {
	  if(network!=null)
		  network.release();
  }


 
}
