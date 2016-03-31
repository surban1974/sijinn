package examples.RPROP;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.Neuron;
import it.sijinn.perceptron.algorithms.RPROP;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.strategies.BatchGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.Utils;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;

public class Animals {

	public static void main(String[] args) {
		
		final String fileName = "examples/resources/animals.txt";
		final float approximation = 0.0000001f;
		final int maxSteps = 50000;


		

		
		Network network = new Network(
				new ArrayList<List<Neuron>>(Arrays.asList(
						Network.createLayer(20,	new SimpleSigmoidFermi()),
						Network.createLayer(7, new SimpleSigmoidFermi())
						)),0
			)
			;
				

		

	

		
		
		final ITrainingStrategy trainingStrategy = new BatchGradientDescent(new RPROP(), new MSE());
		
		final IReadLinesAggregator fileDataReader = new IReadLinesAggregator() {
			
			@Override
			public PairIO getData(Network network, Object[] objs) {
				if(objs==null && !(objs instanceof String[]))
					return new PairIO(new float[0],new float[0]);
				String[] lines = (String[])objs;
				if(lines!=null && lines.length>0){
					float[] input = new float[network.getLayers().get(0).size()];
					float[] target = new float[network.getLayers().get(network.getLayers().size()-1).size()];
					PairIO pairIO = new PairIO(input,target);
					StringTokenizer st = new StringTokenizer(lines[0], "\t");
					int currentIndex=0;

					while(st.hasMoreTokens()){
						try{
							if(currentIndex<pairIO.getInput().length)
								pairIO.getInput()[currentIndex] = Float.valueOf(st.nextToken()).floatValue();
							else
								pairIO.getOutput()[currentIndex-pairIO.getInput().length] = Float.valueOf(st.nextToken()).floatValue();
						}catch(Exception e){
						}
						currentIndex++;
					}
					return pairIO;
				}

				return new PairIO(new float[0],new float[0]);
			}
			
			@Override
			public Object[] aggregate(Object line, int linenumber) {
				if(linenumber==0 || line==null || line.equals(""))					
					return null;
				else
					return new String[]{(String)line};
			}

			@Override
			public Object getRowData(Network network, Object[] lines) {
				return null;
			}
		};
		
		final IStreamWrapper streamWrapper = new IStreamWrapper() {
			
			@Override
			public InputStream openStream() throws Exception {
				return Animals.class.getClassLoader().getResourceAsStream(fileName);
			}
			
			@Override
			public boolean closeStream() throws Exception {
				return true;
			}

			@Override
			public IStreamWrapper instance() throws Exception {
				// TODO Auto-generated method stub
				return this;
			}
		};

		try{		
			float delta = network.training(
					streamWrapper,
					trainingStrategy,
					fileDataReader
					);
			int step=0;
			
			for(step=0;step<maxSteps;step++){
				if(delta>approximation){
					delta = network.training(
							streamWrapper,
							trainingStrategy,
							fileDataReader);
					if(step % 1000 == 0)
						System.out.println("Step: " + step + " MSE: " + delta+ " Weights: "+Utils.print(network.getWeight()," "));
				}else
					break;
			}
			
			network.save("c:/tmp/animals.net");
			
			System.out.println("Steps: " + step);
			System.out.println("MSE: " + delta);
			float[][] test = network.compute(
					new float[][] {					
						{1,	0,	0,	1,	0,	0,	1,	1,	1,	1,	0,	0,	0,	0,	1,		0,	0,	0,	0,		1},
						{1,	0,	0,	1,	0,	0,	1,	1,	1,	1,	0,	0,	0,	0,	0.8f,	0,	0,	0,	0.1f,	1},
						{0,	0,	1,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,		0,	1,	0,	0,		0}
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
