package it.sijinn.admin.workers;

import java.io.Serializable;
import java.util.Date;

import it.classhidra.scheduler.common.generic_batch;
import it.classhidra.scheduler.common.i_batch;
import it.classhidra.serialize.Serialized;
import it.sijinn.common.Network;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;

public class Worker_INTER extends generic_batch implements i_batch, Serializable{

	private static final long serialVersionUID = 1L;
	
	final private String resource_training = "examples/resources/interpolation_training.txt";
	final private String resource_test = "examples/resources/interpolation_test.txt";
	

	private float approximation = 0.001f;
	private int maxSteps = 500000;

	private Network network;
	
	private ITrainingStrategy trainingStrategy;
	
	private IStreamWrapper streamWrapper = new ResourceStreamWrapper(resource_training);
	private IStreamWrapper streamWrapperTest = new ResourceStreamWrapper(resource_test);
	private IReadLinesAggregator readLinesAggregator = new SimpleLineDataAggregator(";");	
	
	private float delta=0;
	private int step=0;
	private boolean forcedStop = false;

	@Override
	public String execute() throws Exception {
		long startTime = 0;


		try{	
			startTime = new Date().getTime();
			
			delta = network.training(
					streamWrapper,
					trainingStrategy,
					readLinesAggregator
					);
			System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
			
			while(step<maxSteps && delta>approximation && !forcedStop){
				if(delta>approximation){
					delta = network.training(
							streamWrapper,
							trainingStrategy,
							readLinesAggregator);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}else
					break;
				step++;
			}
			if(forcedStop)
				forcedStop=false;
			
			System.out.println("Time: " + (new Date().getTime()-startTime)/1000+"s");
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			
			
			
			final float error_test = network.test(streamWrapperTest, readLinesAggregator, new MSE());
			System.out.println("MSE Test: " + error_test);
			



		}catch(Exception e){
			
		}
		
		return null;
		

	}



	@Override
	public String getId() {		
		return "EXAMPLE_NETWORK";
	}






	public float getApproximation() {
		return approximation;
	}



	public Worker_INTER setApproximation(float approximation) {
		this.approximation = approximation;
		return this;
	}



	public int getMaxSteps() {
		return maxSteps;
	}



	public Worker_INTER setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
		return this;
	}


	@Serialized(children=true,depth=2)
	public ITrainingStrategy getTrainingStrategy() {
		return trainingStrategy;
	}


	public Worker_INTER setTrainingStrategy(ITrainingStrategy trainingStrategy) {
		this.trainingStrategy = trainingStrategy;
		return this;
	}



	public IStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}


	public Worker_INTER setStreamWrapper(IStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
		return this;
	}


	public IStreamWrapper getStreamWrapperTest() {
		return streamWrapperTest;
	}


	public Worker_INTER setStreamWrapperTest(IStreamWrapper streamWrapperTest) {
		this.streamWrapperTest = streamWrapperTest;
		return this;
	}


	public float getDelta() {
		return delta;
	}


	public int getStep() {
		return step;
	}



	public Network getNetwork() {
		return network;
	}	
	
	public Worker_INTER setNetwork(Network network) {
		this.network = network;
		return this;
	}



	public Worker_INTER setReadLinesAggregator(IReadLinesAggregator readLinesAggregator) {
		this.readLinesAggregator = readLinesAggregator;
		return this;
	}



	public void setForcedStop(boolean forcedStop) {
		this.forcedStop = forcedStop;
	}
	
	@Serialized(children=true)
	public int getExecutionState(){
		if(db==null)
			return i_batch.STATE_SCHEDULED;
		else
			return db.getState();
	}



}
