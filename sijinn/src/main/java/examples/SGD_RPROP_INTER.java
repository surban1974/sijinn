package examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;

public class SGD_RPROP_INTER {

	public static void main(String[] args) {
		
		final String resource = "examples/resources/interpolation_training.txt";
		final float approximation = 0.001f;
		final int maxSteps = 5000;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2),
						Network.createLayer(4,	new SimpleSigmoidFermi()),
						Network.createLayer(1, new SimpleSigmoidFermi())
						)),0
//				new RandomWeightGenerator()
			)
			;
				

		

	

		
		
		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new RPROP()).setErrorFunction(new MSE());

		final IStreamWrapper streamWrapper = new ResourceStreamWrapper(resource);
		final IReadLinesAggregator readLinesAggregator = new SimpleLineDataAggregator(";",0,0);

		


		try{		
			float delta = network.training(
					streamWrapper,
					trainingStrategy,
					readLinesAggregator
					);
			int step=0;
			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
			
			for(step=1;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(
							streamWrapper,
							trainingStrategy,
							readLinesAggregator);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}else
					break;
			}
			
			network.save("c:/tmp/SGD_RPROP_INTER.net", new ITrainingStrategy[]{trainingStrategy});
			
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {					
						{0.823593752f,0.176406248f},	//0,842274203
						{0.453583164f,0.546416836f},  	//0,7101472
			          }
			);

			System.out.print(Utils.print(test, new String[]{" ","\n"}));

		}catch(Exception e){
			
		}
		

	}

}
