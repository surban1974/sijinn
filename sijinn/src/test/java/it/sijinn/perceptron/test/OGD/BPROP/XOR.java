package it.sijinn.perceptron.test.OGD.BPROP;




import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.OnlineGradientDescent;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;



public class XOR {

	private static Network network;
	private final float learningRate = 1f;
	private final float learningMomentum = 0f;
	private final int maxSteps = 100000;
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
				
		final ITrainingStrategy trainingStrategy = new OnlineGradientDescent(new BPROP().setLearningRate(learningRate).setLearningMomentum(learningMomentum)).setErrorFunction(new MSE());

		Assert.assertArrayEquals(network.getWeight(), new float[]{-0.082843f, -0.011006f, 0.018629f, -0.071407f, 0.03268f, 0.020701f}, approximation);

		
	  	float delta = network.training(trainingData, trainingStrategy);		
	  	
	  	Assert.assertArrayEquals(network.getWeight(), new float[]{-0.08278386f, -0.011004937f, 0.01668793f, -0.07332962f, 0.029417388f, 0.017390102f}, approximation);
	  	Assert.assertEquals(delta, 0.51536596f, approximation);

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
	  	Assert.assertArrayEquals(network.getWeight(), new float[]{-5.6371593f, 2.0234356f, -12.117193f, -12.860654f, -16.378328f, 6.033961f}, approximation);
	  	Assert.assertEquals(delta, 0.250053, approximation);
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
