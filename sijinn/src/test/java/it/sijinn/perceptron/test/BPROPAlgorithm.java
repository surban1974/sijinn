package it.sijinn.perceptron.test;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;


public class BPROPAlgorithm {

	private static Network network;
	private final float learningRate = 1f;
	private final float learningMomentum = 0f;
	private final int maxSteps = 10000;
	private float approximation = 0.000001f;
	
  @Before
  public void createNetwork() throws Exception {
	  network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2, IFunctionApplied.SYGMOIDFERMI),
						Network.createLayer(2, IFunctionApplied.SYGMOIDFERMI),
						Network.createLayer(1, IFunctionApplied.SYGMOIDFERMI)
						)),0
			).addSynapses(
				new Synapse[]
					{
						new Synapse(0,0,1,0,0.1f),
						new Synapse(0,0,1,1,0.4f),
						new Synapse(0,1,1,0,0.8f),
						new Synapse(0,1,1,1,0.6f),
						new Synapse(1,0,2,0,0.3f),
						new Synapse(1,1,2,0,0.9f),
					},
					true
				);
  }
  
  @Test
  public void backPropagation() throws Exception {
	  
	  

		float[][][] trainingData = 					
				new float[][][] {						
					{{0.35f, 0.9f}, {0.5f}}
				};
		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new BPROP().setLearningRate(learningRate).setLearningMomentum(learningMomentum)).setErrorFunction(new MSE());

		Assert.assertArrayEquals(network.getWeight(), new float[]{0.1f, 0.4f, 0.8f, 0.6f, 0.3f, 0.9f}, approximation);

		
	  	float delta = network.training(trainingData, trainingStrategy);		
	  	
	  	Assert.assertArrayEquals(network.getWeight(), new float[]{0.098278925f, 0.39418897f, 0.79557437f, 0.5850574f, 0.25938162f, 0.8603685f}, approximation);
	  	Assert.assertEquals(delta, 0.018103901f, approximation);

	  	int step=0;
		for(step=0;step<maxSteps;step++){
			if(delta>approximation)
				delta = network.training(trainingData, trainingStrategy);
			else
				break;
		}	  	
	  	
		float[][] execution = network.compute(
				new float[][] {					
					{0, 0}
				});
		
	  	Assert.assertEquals(step, 63);
	  	Assert.assertEquals(delta, 9.978461E-7f, approximation);
	  	Assert.assertArrayEquals(execution[0], new float[]{0.5042761f}, approximation);
	  	


  }
  


  
  
  @After
  public void releaseNetwork() throws Exception {
	  if(network!=null)
		  network.release();
  }


 
}
