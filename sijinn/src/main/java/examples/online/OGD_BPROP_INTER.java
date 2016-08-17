package examples.online;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.RandomPositiveWeightGenerator;
import it.sijinn.perceptron.strategies.OnlineGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;

public class OGD_BPROP_INTER {

	public static void main(String[] args) {
		
		final String resource_training = "examples/resources/interpolation_training.txt";
		final String resource_test = "examples/resources/interpolation_test.txt";

		final float learningRate = 0.5f;
		final float learningMomentum = 0.01f;
		final float approximation = 0.001f;
		final int maxSteps = 50000;
		long startTime = 0;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(2),
						Network.createLayer(4, new SimpleSigmoidFermi()),
						Network.createLayer(1, new SimpleSigmoidFermi())
						)),
				new RandomPositiveWeightGenerator()
			)
			;
				

		

	

		
		
		final ITrainingStrategy trainingStrategy = new OnlineGradientDescent(new BPROP().setLearningRate(learningRate).setLearningMomentum(learningMomentum)).setErrorFunction(new MSE());

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
			System.out.println("Time: " + (new Date().getTime()-startTime)/1000+"s");
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			
			network.save("c:/tmp/OGD_BPROP_INTER.net", new ITrainingStrategy[]{trainingStrategy});
			
			final IStreamWrapper streamWrapperTest = new ResourceStreamWrapper(resource_test);
			
			final float error_test = network.test(streamWrapperTest, readLinesAggregator, new MSE());
			System.out.println("MSE Test: " + error_test);
			
			float[][] test = network.compute(
					new float[][] {					
						{0.823593752f,0.176406248f},	//0.842274203
						{0.453583164f,0.546416836f},  	//0.7101472
			          }
			);

			System.out.print(Utils.print(test, new String[]{" ","\n"}));

		}catch(Exception e){
			
		}
		

	}

}
