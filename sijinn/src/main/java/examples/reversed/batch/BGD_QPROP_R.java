package examples.reversed.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.perceptron.algorithms.QPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.RandomPositiveWeightGenerator;
import it.sijinn.perceptron.strategies.BatchGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;

public class BGD_QPROP_R {

	public static void main(String[] args) {
		
		final String resource_training = "examples/resources/rtest.txt";

		final float approximation = 0.001f;
		final float learningRate = 0.5f;
		final int maxSteps = 50000;
		long startTime = 0;


		

		
		final Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2, new SimpleSigmoidFermi()),
						Network.createLayer(4, new SimpleSigmoidFermi()),
						Network.createLayer(2, new SimpleSigmoidFermi())
						)),
				new RandomPositiveWeightGenerator()
//				new ZeroWeightGenerator()

			)
			;
				
		
		
		final ITrainingStrategy trainingStrategy = new BatchGradientDescent(
				new QPROP()
				.setLearningRate(learningRate)
				.setDeferredAgregateFunction(new SUMMATOR())
				)
			.setReversed(true)	
			.setErrorFunction(new MSE());

		final IStreamWrapper streamWrapper = new ResourceStreamWrapper(resource_training);
		final IReadLinesAggregator readLinesAggregator = new SimpleLineDataAggregator(";");

		


		try{	
			startTime = new Date().getTime();
			
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
			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
			System.out.println("Time: " + (new Date().getTime()-startTime)/1000+"s");
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			
			network.save("c:/tmp/R.net", new ITrainingStrategy[]{trainingStrategy});
			
			
			float[][] test = network.compute(
					new float[][] {					
						{0,1},	//{1,0}
						{1,0},  //{0,1}
			          },
					true
			);

			System.out.print(Utils.print(test, new String[]{" ","\n"}));

		}catch(Exception e){
			
		}
		

	}

}
