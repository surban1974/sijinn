package examples.reversed.autobalanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.ZeroWeightGenerator;
import it.sijinn.perceptron.strategies.AutoBalancedGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;

import it.sijinn.perceptron.utils.Utils;

public class AUTO_BPROP {

	public static void main(String[] args) {
		


		final float learningRate = 0.5f;
		final float learningMomentum = 0.01f;
		final float approximation = 0.00001f;
		final int maxSteps = 50000;
		long startTime = 0;


		

		
		final Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(11, new SimpleSigmoidFermi()),
						Network.createLayer(44, new SimpleSigmoidFermi()),
						Network.createLayer(11, new SimpleSigmoidFermi())
						)),
//				new RandomPositiveWeightGenerator()
				new ZeroWeightGenerator()

			)
			;
				
		
/*		
		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(
				new BPROP().
				setLearningRate(learningRate).
				setLearningMomentum(learningMomentum)
			)
			.setErrorFunction(new MSE());

*/		
		final ITrainingStrategy trainingStrategy = new AutoBalancedGradientDescent(
				new BPROP().
				setLearningRate(learningRate).
				setLearningMomentum(learningMomentum)
			)
			.setErrorFunction(new MSE());		

		final float[][][] trainingData = 	 
				new float[][][] {						
					{{0,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1}, {0,0.01f,0.04f,0.09f,0.16f,0.25f,0.36f,0.49f,0.64f,0.81f,1}}
				};

		


		try{	
			startTime = new Date().getTime();
			
			float delta = network.training(
					trainingData,
					trainingStrategy
					);
			int step=0;
			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
			
			for(step=1;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(
							trainingData,
							trainingStrategy
							);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}else
					break;
			}
			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
			System.out.println("Time: " + (new Date().getTime()-startTime)/1000+"s");
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			
			network.save("c:/tmp/AUTO.net", new ITrainingStrategy[]{trainingStrategy});
			
			
			float[][] test = network.compute(
					new float[][] {					
						{0,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1}
			          },
					false
			);

			System.out.print(Utils.print(test, new String[]{" ","\n"}));

		}catch(Exception e){
			
		}
		

	}

}
