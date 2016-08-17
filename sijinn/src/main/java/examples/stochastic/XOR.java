package examples.stochastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import examples.functions.XORSigmoid;
import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;







public class XOR {
	
	final static float[][][] trainingData = 
							new float[][][] {						
								{{1, 1}, {0}},
						        {{0, 1}, {1}},
						        {{1, 0}, {1}},
						        {{0, 0}, {0}},
							};

	public static void main(String[] args) {
		
		float approximation = 0.000001f;
		final float learningRate = (1/3f);
		final float learningMomentum = 0;
		final int maxSteps = 50000;


		final IFunctionApplied Fermi4XOR = new XORSigmoid();

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2),
						Network.createLayer(2, Fermi4XOR),
						Network.createLayer(1, Fermi4XOR)
						))
			).addSynapses(
					new Synapse[]
							{
									new Synapse(0,0,1,0,-0.082843f),
									new Synapse(0,0,1,1,-0.011006f),
									new Synapse(0,1,1,0,0.018629f),
									new Synapse(0,1,1,1,-0.071407f),
									new Synapse(1,0,2,0,0.032680f),
									new Synapse(1,1,2,0,0.020701f),
							},
							true
						);
			;
				

			

	
		
		
		

		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new BPROP().setLearningRate(learningRate).setLearningMomentum(learningMomentum)).setErrorFunction(new MSE());
		
		try{
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
			network.save("c:/tmp/xor.net");
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {					
						{1, 1},
			            {0, 1},
			            {1, 0},
			            {0, 0},
			            {0.8f,0.9f},
			            {0.1f,0.2f}
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
