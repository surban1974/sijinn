package it.sijinn.perceptron.functions.applied;

public class SimpleHyperbolicTangent implements IFunctionApplied {

	private static final long serialVersionUID = 1L;
	
	public SimpleHyperbolicTangent(){
		super();
	}

	@Override
	public float derivative(float delta, float[] param) {
		if(param.length>0)
			return (1+param[0])*(1-param[0]);
		return 0;
	}

	@Override
	public float execution(float[] param) {
		if(param.length>0)
			return new Float(
						(Math.pow(Math.E, param[0]) - Math.pow(Math.E, -1*param[0]))
						/
						(Math.pow(Math.E, param[0])+Math.pow(Math.E, -1*param[0]))
					).floatValue();
		return 0;
	}
	
	@Override
	public float flatspot() {
		return 0;
	}
	
	@Override
	public String toSaveString(){
		return this.getClass().getName();
	}
	
}
