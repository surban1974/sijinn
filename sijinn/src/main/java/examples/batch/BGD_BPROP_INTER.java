package examples.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
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

public class BGD_BPROP_INTER {

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
				

		

	

		final ITrainingStrategy trainingStrategy = new BatchGradientDescent(
				new BPROP()
				.setLearningRate(learningRate)
				.setLearningMomentum(learningMomentum)
				.setDeferredAgregateFunction(new SUMMATOR())
				)
//				.setParallelLimit(10)
				.setErrorFunction(new MSE())
				;

/*		
		final ITrainingStrategy trainingStrategy = new BatchGradientDescent(
				new BPROP().setLearningRate(learningRate).setLearningMomentum(learningMomentum)
				)
				.setErrorFunction(new MSE())
				.setParallelLimit(5)
				.setParallelTimeout(1000)
				.setListener(new IStrategyListener() {
					public void onAfterLinePrepared(Network network, int linenumber, Object[] aggregated) throws Exception {
					}
					public void onAfterErrorComputed(Network network, float error, int linenumber, PairIO param) throws Exception {
					}
					public void onAfterDataPrepared(Network network, int linenumber, PairIO param) throws Exception {
					}
					public void onAfterDataComputed(Network network, int linenumber, PairIO param) throws Exception {
					}
					public void onAfterAlgorithmUpdated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param) throws Exception {
					}
					public void onAfterAlgorithmCalculatedAndUpdated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param) throws Exception {
					}
					public void onAfterAlgorithmCalculated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param) throws Exception {
					}
					public IStrategyListener setTrainingStrategy(ITrainingStrategy _strategy) {
						return this;
					}
					public void onAfterReaderOpen(Network network, IDataReader reader) throws Exception {
					}
					public void onAfterReaderClose(Network network, IDataReader reader) throws Exception {
					}
					public void onAfterReaderFinalize(Network network, IDataReader reader) throws Exception {
						System.out.print(".");
					}
				});
*/				

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
			
			network.save("c:/tmp/BGD_BPROP_INTER.net", new ITrainingStrategy[]{trainingStrategy});
			
			final IStreamWrapper streamWrapperTest = new ResourceStreamWrapper(resource_test);
			
			final float error_test = network.test(streamWrapperTest, readLinesAggregator, new MSE());
			System.out.println("MSE Test: " + error_test);
			
			float[][] test = network.compute(
					new float[][] {					
						{0.823593752f,0.176406248f},	//0.842274203
						{0.453583164f,0.546416836f},  	//0.7101472
						{0,0},							//0
						{-0.453583164f,-0.546416836f},  //?
			          }
			);
			System.out.print(Utils.print(test, new String[]{" ","\n"}));

		}catch(Exception e){
			
		}
		

	}

}
