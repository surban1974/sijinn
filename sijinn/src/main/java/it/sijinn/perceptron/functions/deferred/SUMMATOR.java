package it.sijinn.perceptron.functions.deferred;

public class SUMMATOR implements IDAFloatFunction {
	
	@Override
	public float apply(float aggregator, float value) {
		return aggregator+value;
	}

	@Override
	public String toSaveString() {
		return "deferredAggregateFunction="+this.getClass().getSimpleName();
	}

	@Override
	public void init() {
	}

}
