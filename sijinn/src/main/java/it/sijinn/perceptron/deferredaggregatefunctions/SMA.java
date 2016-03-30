package it.sijinn.perceptron.deferredaggregatefunctions;

public class SMA implements IDAFloatFunction {

	private Float _first=null;
	private long counter=0;
	
	@Override
	public float apply(float aggregator, float value) {
		if(_first==null || counter==0)
			_first=value;
		counter++;
		if(counter==1)
			return value;
		else 
			return aggregator-_first/counter+value/counter;
	}

	@Override
	public String toSaveString() {
		return "deferredAggregateFunction="+this.getClass().getSimpleName();
	}

	@Override
	public void init() {
		counter=0;
		_first=null;
		
	}

}
