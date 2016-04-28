package it.sijinn.perceptron.functions.applied;

public class SimpleSigmoidFermi implements IFunctionApplied {
	private static final long serialVersionUID = 1L;
	private float flatspot = 0.1f;
	
	public SimpleSigmoidFermi(){
		super();
	}
	
	public SimpleSigmoidFermi(float _flatspot){
		super();
		this.flatspot = _flatspot;
	}	

	@Override
	public float derivative(float delta, float[] param) {
		if(param.length>0)
			return param[0]*(1-param[0]);
		return 0;
	}

	@Override
	public float execution(float[] param) {
		if(param.length>0)
			return new Float(1/(1+Math.pow(Math.E, -1*param[0]))).floatValue();
		return 0;
	}
	
	@Override
	public float flatspot() {
		return flatspot;
	}
	
	@Override
	public String getDefinition(){
		return this.getClass().getSimpleName()+"{"+flatspot+"}";
	}
}
