package it.sijinn.perceptron.utils.parser;

import it.sijinn.common.Network;

public class SimpleArrayDataAggregator implements IReadLinesAggregator{
	@Override
	public PairIO getData(Network network, Object[] lines) {
		if(lines==null || lines.length==0)
			return null;
		float[][] f = (float[][])lines[0];
		return new PairIO(f[0], f[1]);
	}
	
	@Override
	public Object[] aggregate(Object line, int linenumber) {
		if(line==null)
			return null;
		else 
			return new Object[]{line};
	}
	
	@Override
	public Object getRowData(Network network, Object[] objs) {
		if(objs==null || objs.length==0)
			return null;
		return objs[0];
	}			
}
