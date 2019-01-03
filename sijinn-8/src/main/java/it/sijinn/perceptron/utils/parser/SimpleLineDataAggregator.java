package it.sijinn.perceptron.utils.parser;

import java.util.StringTokenizer;

import it.sijinn.common.Network;

public class SimpleLineDataAggregator implements IReadLinesAggregator {
	
	protected String separator = " ";
	protected int start = 0;
	protected int finish = -1;
	
	public SimpleLineDataAggregator(String _separator){
		super();
		this.separator = _separator;
	}
	
	public SimpleLineDataAggregator(String _separator, int startLine){
		super();
		this.separator = _separator;
		this.start = startLine;
	}
	
	public SimpleLineDataAggregator(String _separator, int startLine, int finishLine){
		super();
		this.separator = _separator;
		this.start = startLine;
		this.finish = finishLine;
	}

	@Override
	public PairIO getData(Network network, Object[] objs) {
		if(objs==null && !(objs instanceof String[]))
			return new PairIO(new float[0],new float[0]);
		final String[] lines = (String[])objs;
		if(lines!=null && lines.length>0){

			final float[] input = new float[network.getLayers().get(0).size() - network.getLayerBiases(0)];
			final float[] target = new float[network.getLayers().get(network.getLayers().size()-1).size()];
			final PairIO pairIO = new PairIO(input,target);
			final StringTokenizer st = new StringTokenizer(lines[0], separator);
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
	public Object[] aggregate(Object obj, int linenumber) {
		if(linenumber<start || (finish!=-1 && linenumber>finish) || obj==null || !(obj instanceof String) || ((String)obj).equals(""))					
			return null;
		else
			return new String[]{(String)obj};
	}

	@Override
	public Object getRowData(Network network, Object[] objs) {
		if(objs==null && !(objs instanceof String[]))
			return null;
		final String[] lines = (String[])objs;
		if(lines!=null && lines.length>0)
			return lines[0];
		return null;
	}


}
