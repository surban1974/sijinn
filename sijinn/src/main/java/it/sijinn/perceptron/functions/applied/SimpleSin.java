package it.sijinn.perceptron.functions.applied;

public class SimpleSin implements IFunctionApplied {

	private static final long serialVersionUID = 1L;

	public SimpleSin(){
		super();
	}
	
	@Override
	public float derivative(float delta, float[] o) {
		if(o.length>0)
			return new Float(Math.cos(o[0])).floatValue();
		return 0;
	}
	@Override
	public float execution(float[] param) {
		if(param.length>0)
			 return new Float(Math.sin(param[0])).floatValue();
		
		return 0;
	}	
	@Override
	public String toSaveString(){
		return this.getClass().getName();
	}
}
