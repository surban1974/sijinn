package examples.RPROP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.Synapse;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.OnlineGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.Utils;

public class Classic_WeightPresent {

	public static void main(String[] args) {
		

		final int maxSteps = 100;

		
		Network network = new Network(
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
		

		float approximation = 0.000001f;
		
		float[][][] trainingData = 					
				new float[][][] {						
					{{0.35f, 0.9f}, {0.5f}}
				};

		

//		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new RPROP(), new MSE());
		final ITrainingStrategy trainingStrategy = new OnlineGradientDescent(new RPROP(), new MSE());
		
		try{
			int step=0;
			float delta = network.training(trainingData, trainingStrategy);

			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));

			for(step=1;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(trainingData, trainingStrategy);
//					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}
				else
					break;
			}
			
			network.save("c:/tmp/test.net");
			
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

}
