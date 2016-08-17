package examples.functions;

import it.sijinn.perceptron.functions.applied.IFunctionApplied;

public class XORSigmoid implements IFunctionApplied {
	private static final long serialVersionUID = 1L;
	
	public XORSigmoid(){
		super();
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
	public String getDefinition(){
		return this.getClass().getName();
	}

	@Override
	public float flatspot() {
		return 0;
	}
	
	@Override
	public String getId(){
		return this.getClass().getName();
	}
}
