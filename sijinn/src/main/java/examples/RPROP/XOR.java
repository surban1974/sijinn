package examples.RPROP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.Synapse;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.errorfunctions.MSE;
import it.sijinn.perceptron.functions.IFunctionApplied;
import it.sijinn.perceptron.strategies.GradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.Utils;

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
		final int maxSteps = 50000;


		final IFunctionApplied Fermi4XOR = new IFunctionApplied() {

			private static final long serialVersionUID = 1L;

			@Override
			public float derivative(float delta, float[] param) {
				if(param.length>0)
					return 4*param[0]*(1-param[0]);
				return 0;
			}

			@Override
			public float execution(float[] param) {
				if(param.length>0)
					return new Float(1/(1+Math.pow(Math.E, -4*(param[0]-0.5f)))).floatValue();
				return 0;
			}
			
			@Override
			public String toSaveString(){
				return this.getClass().getName();
			}
		};

		
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
			)
			;
				

			

	
		
		
		

		final ITrainingStrategy trainingStrategy = new StochasticGradientDescent(new RPROP(), new MSE());
//		final ITrainingStrategy trainingStrategy = new GradientDescent(new RPROP(), new MSE());
		
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
			System.out.println("Steps: " + step + " MSE: " + delta);
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
			
			System.out.println(Utils.print(test, new String[]{" ","\n"}));
/*			
			for(float[] current:test ){
				System.out.print("[ ");
				for(float f:current)
					System.out.print(f+" ");
				System.out.print("]");
				System.out.println("");
				
			}
*/			
		}catch(Exception e){
			
		}
		

	}

}
