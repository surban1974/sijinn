package it.sijinn.perceptron.functions.deferred;

public class MMA implements IDAFloatFunction {

	private long counter=0;
	
	@Override
	public float apply(float aggregator, float value) {
		counter++;
		if(counter==1)
			return value;
		else 
			return (value+(counter-1)*aggregator)/counter;
	}

	@Override
	public String toSaveString() {
		return "deferredAggregateFunction="+this.getClass().getSimpleName();
	}

	@Override
	public void init() {
		counter=0;
	}

}
