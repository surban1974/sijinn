package examples.BPROP;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.OnlineGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;

public class Iris {

	public static void main(String[] args) {
		
		final String resourceName = "examples/resources/iris.txt";
		final float learningRate = 0.1f;
		final float learningMomentum = 0;
		final float approximation = 0.001f;
		final int maxSteps = 50000;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(4,	new SimpleSigmoidFermi()),
//						Network.createLayer(4,	new SigmoidFermi()),
						Network.createLayer(3, new SimpleSigmoidFermi())
						)),0
			)
			;
				

		

	

		
		

		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new BPROP(learningRate,learningMomentum), new MSE());
//		final ITrainingStrategy trainingStrategy = new GradientDescent(new BPROP(learningRate,learningMomentum), new MSE());

		
		final IReadLinesAggregator resourceDataAggregator = new SimpleLineDataAggregator(",");
		
		final IStreamWrapper streamReader = new ResourceStreamWrapper(resourceName);

		try{		
			float delta = network.training(
					streamReader,
					trainingStrategy,
					resourceDataAggregator
					);
			int step=0;
			
			for(step=0;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(
							streamReader,
							trainingStrategy,
							resourceDataAggregator);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta);
				}else
					break;
			}
			
			network.save("c:/tmp/iris.net");
			
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {		
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

}
