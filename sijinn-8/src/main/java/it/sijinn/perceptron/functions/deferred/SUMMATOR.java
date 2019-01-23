package it.sijinn.perceptron.functions.deferred;

public class SUMMATOR implements IDAFloatFunction {

	private static final long serialVersionUID = 1L;

	public SUMMATOR() {
		super();
	}
	
	@Override
	public float apply(float aggregator, float value) {
		return aggregator+value;
	}

	@Override
	public String getDefinition() {
		return "deferredAggregateFunction="+this.getClass().getSimpleName();
	}

	@Override
	public void init() {
	}
	
	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}

}
