package examples.BPROP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.RandomWeightGenerator;
import it.sijinn.perceptron.strategies.OnlineGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;

public class Iris_array {

	public static void main(String[] args) {
		
		final float learningRate = 0.1f;
		final float learningMomentum = 0;
		final float approximation = 0.001f;
		final int maxSteps = 50000;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(4,	new SimpleSigmoidFermi()),
						Network.createLayer(3, new SimpleSigmoidFermi())
						)),
				new RandomWeightGenerator()
			)
			;
				

		

	

		
		

		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new BPROP(learningRate,learningMomentum), new MSE());
//		final ITrainingStrategy trainingStrategy = new GradientDescent(new BPROP(learningRate,learningMomentum), new MSE());

		


		try{		
			float delta = network.training(trainingData,trainingStrategy);
			int step=0;
			
			for(step=0;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(trainingData,trainingStrategy);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta);
				}else
					break;
			}
			
			network.save("c:/tmp/iris_array.net");
			
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {	
//						{4.3f,3.0f,1.1f,0.1f},
//						{6.2f,2.2f,4.5f,1.5f},
						{6.2f,3.4f,5.4f,2.3f},
						{5.9f,3.0f,5.1f,1.8f},

			          }
			);
			for(float[] current:test ){
				System.out.print("[ ");
				for(float f:current)
					System.out.print(f+" ");
				System.out.print("]");
				System.out.println("");
				
			}
		}catch(Exception e){
			
		}
		

	}

	
	final static float[][][] trainingData = 	 
			new float[][][] {						
			{{5.1f,3.5f,1.4f,0.2f},{0f,0f,1f}},
			{{4.9f,3.0f,1.4f,0.2f},{0f,0f,1f}},
			{{4.7f,3.2f,1.3f,0.2f},{0f,0f,1f}},
			{{4.6f,3.1f,1.5f,0.2f},{0f,0f,1f}},
			{{5.0f,3.6f,1.4f,0.2f},{0f,0f,1f}},
			{{5.4f,3.9f,1.7f,0.4f},{0f,0f,1f}},
			{{4.6f,3.4f,1.4f,0.3f},{0f,0f,1f}},
			{{5.0f,3.4f,1.5f,0.2f},{0f,0f,1f}},
			{{4.4f,2.9f,1.4f,0.2f},{0f,0f,1f}},
			{{4.9f,3.1f,1.5f,0.1f},{0f,0f,1f}},
			{{5.4f,3.7f,1.5f,0.2f},{0f,0f,1f}},
			{{4.8f,3.4f,1.6f,0.2f},{0f,0f,1f}},
			{{4.8f,3.0f,1.4f,0.1f},{0f,0f,1f}},
			{{4.3f,3.0f,1.1f,0.1f},{0f,0f,1f}},
			{{5.8f,4.0f,1.2f,0.2f},{0f,0f,1f}},
			{{5.7f,4.4f,1.5f,0.4f},{0f,0f,1f}},
			{{5.4f,3.9f,1.3f,0.4f},{0f,0f,1f}},
			{{5.1f,3.5f,1.4f,0.3f},{0f,0f,1f}},
			{{5.7f,3.8f,1.7f,0.3f},{0f,0f,1f}},
			{{5.1f,3.8f,1.5f,0.3f},{0f,0f,1f}},
			{{5.4f,3.4f,1.7f,0.2f},{0f,0f,1f}},
			{{5.1f,3.7f,1.5f,0.4f},{0f,0f,1f}},
			{{4.6f,3.6f,1.0f,0.2f},{0f,0f,1f}},
			{{5.1f,3.3f,1.7f,0.5f},{0f,0f,1f}},
			{{4.8f,3.4f,1.9f,0.2f},{0f,0f,1f}},
			{{5.0f,3.0f,1.6f,0.2f},{0f,0f,1f}},
			{{5.0f,3.4f,1.6f,0.4f},{0f,0f,1f}},
			{{5.2f,3.5f,1.5f,0.2f},{0f,0f,1f}},
			{{5.2f,3.4f,1.4f,0.2f},{0f,0f,1f}},
			{{4.7f,3.2f,1.6f,0.2f},{0f,0f,1f}},
			{{4.8f,3.1f,1.6f,0.2f},{0f,0f,1f}},
			{{5.4f,3.4f,1.5f,0.4f},{0f,0f,1f}},
			{{5.2f,4.1f,1.5f,0.1f},{0f,0f,1f}},
			{{5.5f,4.2f,1.4f,0.2f},{0f,0f,1f}},
			{{4.9f,3.1f,1.5f,0.1f},{0f,0f,1f}},
			{{5.0f,3.2f,1.2f,0.2f},{0f,0f,1f}},
			{{5.5f,3.5f,1.3f,0.2f},{0f,0f,1f}},
			{{4.9f,3.1f,1.5f,0.1f},{0f,0f,1f}},
			{{4.4f,3.0f,1.3f,0.2f},{0f,0f,1f}},
			{{5.1f,3.4f,1.5f,0.2f},{0f,0f,1f}},
			{{5.0f,3.5f,1.3f,0.3f},{0f,0f,1f}},
			{{4.5f,2.3f,1.3f,0.3f},{0f,0f,1f}},
			{{4.4f,3.2f,1.3f,0.2f},{0f,0f,1f}},
			{{5.0f,3.5f,1.6f,0.6f},{0f,0f,1f}},
			{{5.1f,3.8f,1.9f,0.4f},{0f,0f,1f}},
			{{4.8f,3.0f,1.4f,0.3f},{0f,0f,1f}},
			{{5.1f,3.8f,1.6f,0.2f},{0f,0f,1f}},
			{{4.6f,3.2f,1.4f,0.2f},{0f,0f,1f}},
			{{5.3f,3.7f,1.5f,0.2f},{0f,0f,1f}},
			{{5.0f,3.3f,1.4f,0.2f},{0f,0f,1f}},
			{{7.0f,3.2f,4.7f,1.4f},{0f,1f,0f}},
			{{6.4f,3.2f,4.5f,1.5f},{0f,1f,0f}},
			{{6.9f,3.1f,4.9f,1.5f},{0f,1f,0f}},
			{{5.5f,2.3f,4.0f,1.3f},{0f,1f,0f}},
			{{6.5f,2.8f,4.6f,1.5f},{0f,1f,0f}},
			{{5.7f,2.8f,4.5f,1.3f},{0f,1f,0f}},
			{{6.3f,3.3f,4.7f,1.6f},{0f,1f,0f}},
			{{4.9f,2.4f,3.3f,1.0f},{0f,1f,0f}},
			{{6.6f,2.9f,4.6f,1.3f},{0f,1f,0f}},
			{{5.2f,2.7f,3.9f,1.4f},{0f,1f,0f}},
			{{5.0f,2.0f,3.5f,1.0f},{0f,1f,0f}},
			{{5.9f,3.0f,4.2f,1.5f},{0f,1f,0f}},
			{{6.0f,2.2f,4.0f,1.0f},{0f,1f,0f}},
			{{6.1f,2.9f,4.7f,1.4f},{0f,1f,0f}},
			{{5.6f,2.9f,3.6f,1.3f},{0f,1f,0f}},
			{{6.7f,3.1f,4.4f,1.4f},{0f,1f,0f}},
			{{5.6f,3.0f,4.5f,1.5f},{0f,1f,0f}},
			{{5.8f,2.7f,4.1f,1.0f},{0f,1f,0f}},
			{{6.2f,2.2f,4.5f,1.5f},{0f,1f,0f}},
			{{5.6f,2.5f,3.9f,1.1f},{0f,1f,0f}},
			{{5.9f,3.2f,4.8f,1.8f},{0f,1f,0f}},
			{{6.1f,2.8f,4.0f,1.3f},{0f,1f,0f}},
			{{6.3f,2.5f,4.9f,1.5f},{0f,1f,0f}},
			{{6.1f,2.8f,4.7f,1.2f},{0f,1f,0f}},
			{{6.4f,2.9f,4.3f,1.3f},{0f,1f,0f}},
			{{6.6f,3.0f,4.4f,1.4f},{0f,1f,0f}},
			{{6.8f,2.8f,4.8f,1.4f},{0f,1f,0f}},
			{{6.7f,3.0f,5.0f,1.7f},{0f,1f,0f}},
			{{6.0f,2.9f,4.5f,1.5f},{0f,1f,0f}},
			{{5.7f,2.6f,3.5f,1.0f},{0f,1f,0f}},
			{{5.5f,2.4f,3.8f,1.1f},{0f,1f,0f}},
			{{5.5f,2.4f,3.7f,1.0f},{0f,1f,0f}},
			{{5.8f,2.7f,3.9f,1.2f},{0f,1f,0f}},
			{{6.0f,2.7f,5.1f,1.6f},{0f,1f,0f}},
			{{5.4f,3.0f,4.5f,1.5f},{0f,1f,0f}},
			{{6.0f,3.4f,4.5f,1.6f},{0f,1f,0f}},
			{{6.7f,3.1f,4.7f,1.5f},{0f,1f,0f}},
			{{6.3f,2.3f,4.4f,1.3f},{0f,1f,0f}},
			{{5.6f,3.0f,4.1f,1.3f},{0f,1f,0f}},
			{{5.5f,2.5f,4.0f,1.3f},{0f,1f,0f}},
			{{5.5f,2.6f,4.4f,1.2f},{0f,1f,0f}},
			{{6.1f,3.0f,4.6f,1.4f},{0f,1f,0f}},
			{{5.8f,2.6f,4.0f,1.2f},{0f,1f,0f}},
			{{5.0f,2.3f,3.3f,1.0f},{0f,1f,0f}},
			{{5.6f,2.7f,4.2f,1.3f},{0f,1f,0f}},
			{{5.7f,3.0f,4.2f,1.2f},{0f,1f,0f}},
			{{5.7f,2.9f,4.2f,1.3f},{0f,1f,0f}},
			{{6.2f,2.9f,4.3f,1.3f},{0f,1f,0f}},
			{{5.1f,2.5f,3.0f,1.1f},{0f,1f,0f}},
			{{5.7f,2.8f,4.1f,1.3f},{0f,1f,0f}},
			{{6.3f,3.3f,6.0f,2.5f},{1f,0f,0f}},
			{{5.8f,2.7f,5.1f,1.9f},{1f,0f,0f}},
			{{7.1f,3.0f,5.9f,2.1f},{1f,0f,0f}},
			{{6.3f,2.9f,5.6f,1.8f},{1f,0f,0f}},
			{{6.5f,3.0f,5.8f,2.2f},{1f,0f,0f}},
			{{7.6f,3.0f,6.6f,2.1f},{1f,0f,0f}},
			{{4.9f,2.5f,4.5f,1.7f},{1f,0f,0f}},
			{{7.3f,2.9f,6.3f,1.8f},{1f,0f,0f}},
			{{6.7f,2.5f,5.8f,1.8f},{1f,0f,0f}},
			{{7.2f,3.6f,6.1f,2.5f},{1f,0f,0f}},
			{{6.5f,3.2f,5.1f,2.0f},{1f,0f,0f}},
			{{6.4f,2.7f,5.3f,1.9f},{1f,0f,0f}},
			{{6.8f,3.0f,5.5f,2.1f},{1f,0f,0f}},
			{{5.7f,2.5f,5.0f,2.0f},{1f,0f,0f}},
			{{5.8f,2.8f,5.1f,2.4f},{1f,0f,0f}},
			{{6.4f,3.2f,5.3f,2.3f},{1f,0f,0f}},
			{{6.5f,3.0f,5.5f,1.8f},{1f,0f,0f}},
			{{7.7f,3.8f,6.7f,2.2f},{1f,0f,0f}},
			{{7.7f,2.6f,6.9f,2.3f},{1f,0f,0f}},
			{{6.0f,2.2f,5.0f,1.5f},{1f,0f,0f}},
			{{6.9f,3.2f,5.7f,2.3f},{1f,0f,0f}},
			{{5.6f,2.8f,4.9f,2.0f},{1f,0f,0f}},
			{{7.7f,2.8f,6.7f,2.0f},{1f,0f,0f}},
			{{6.3f,2.7f,4.9f,1.8f},{1f,0f,0f}},
			{{6.7f,3.3f,5.7f,2.1f},{1f,0f,0f}},
			{{7.2f,3.2f,6.0f,1.8f},{1f,0f,0f}},
			{{6.2f,2.8f,4.8f,1.8f},{1f,0f,0f}},
			{{6.1f,3.0f,4.9f,1.8f},{1f,0f,0f}},
			{{6.4f,2.8f,5.6f,2.1f},{1f,0f,0f}},
			{{7.2f,3.0f,5.8f,1.6f},{1f,0f,0f}},
			{{7.4f,2.8f,6.1f,1.9f},{1f,0f,0f}},
			{{7.9f,3.8f,6.4f,2.0f},{1f,0f,0f}},
			{{6.4f,2.8f,5.6f,2.2f},{1f,0f,0f}},
			{{6.3f,2.8f,5.1f,1.5f},{1f,0f,0f}},
			{{6.1f,2.6f,5.6f,1.4f},{1f,0f,0f}},
			{{7.7f,3.0f,6.1f,2.3f},{1f,0f,0f}},
			{{6.3f,3.4f,5.6f,2.4f},{1f,0f,0f}},
			{{6.4f,3.1f,5.5f,1.8f},{1f,0f,0f}},
			{{6.0f,3.0f,4.8f,1.8f},{1f,0f,0f}},
			{{6.9f,3.1f,5.4f,2.1f},{1f,0f,0f}},
			{{6.7f,3.1f,5.6f,2.4f},{1f,0f,0f}},
			{{6.9f,3.1f,5.1f,2.3f},{1f,0f,0f}},
			{{5.8f,2.7f,5.1f,1.9f},{1f,0f,0f}},
			{{6.8f,3.2f,5.9f,2.3f},{1f,0f,0f}},
			{{6.7f,3.3f,5.7f,2.5f},{1f,0f,0f}},
			{{6.7f,3.0f,5.2f,2.3f},{1f,0f,0f}},
			{{6.3f,2.5f,5.0f,1.9f},{1f,0f,0f}},
			{{6.5f,3.0f,5.2f,2.0f},{1f,0f,0f}},
			{{6.2f,3.4f,5.4f,2.3f},{1f,0f,0f}},
			{{5.9f,3.0f,5.1f,1.8f},{1f,0f,0f}}
			};
	
}
