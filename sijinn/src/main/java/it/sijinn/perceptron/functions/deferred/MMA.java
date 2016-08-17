package it.sijinn.perceptron.functions.deferred;

public class MMA implements IDAFloatFunction {

	private long counter=0;
	
	public MMA(){
		super();
	}
	
	@Override
	public float apply(float aggregator, float value) {
		counter++;
		if(counter==1)
			return value;
		else 
			return (value+(counter-1)*aggregator)/counter;
	}

	@Override
	public String getDefinition() {
		return "deferredAggregateFunction="+this.getClass().getSimpleName();
	}

	@Override
	public void init() {
		counter=0;
	}

	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}	
}
