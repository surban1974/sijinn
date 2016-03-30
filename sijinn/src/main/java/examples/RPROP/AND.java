package examples.RPROP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.errorfunctions.MSE;
import it.sijinn.perceptron.functions.SimpleThereshold;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.strategies.ITrainingStrategy;

public class AND {

	public static void main(String[] args) {
		float approximation = 0.000001f;
		final float learningRate = 0.1f;
		final float learningMomentum = 0f;
		final int maxSteps = 50000;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2),
						Network.createLayer(1, new SimpleThereshold(1))
						)),
				0
			)
			;
				
		float[][][] trainingData = 	 
					new float[][][] {						
						{{0, 0}, {0}},
			            {{0, 1}, {0}},
			            {{1, 0}, {0}},
			            {{1, 1}, {1}},
					};
		

	
		
		
		

		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new RPROP(),  new MSE());
		
		try{
			float delta = network.training(trainingData, trainingStrategy);
			int step=0;
			
			for(step=0;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(trainingData, trainingStrategy);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}else
					break;
			}
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {					
						{0, 0},
			            {0, 1},
			            {1, 0},
			            {1, 1},
			            {0.8f,0.9f}
			          }
			);
			System.out.println(Utils.print(test, new String[]{" ","\n"}));
		}catch(Exception e){
			
		}
		

	}

}
