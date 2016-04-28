package it.sijinn.perceptron.functions.applied;

public class SimpleLinear implements IFunctionApplied {

	private static final long serialVersionUID = 1L;
	private float slope=0;
	
	public SimpleLinear(float _slope){
		super();
		this.slope = _slope;
	}
	
	@Override
	public float derivative(float delta, float[] param) {
		if(param.length>0)
			return slope;
		return 0;
	}

	@Override
	public float execution(float[] param) {
		if(param.length>0)
			return (slope*param[0]);
		return 0;
	}
	
	@Override
	public float flatspot() {
		return 0;
	}
	
	@Override
	public String getDefinition(){
		return this.getClass().getSimpleName()+"{"+slope+"}";
	}
}
