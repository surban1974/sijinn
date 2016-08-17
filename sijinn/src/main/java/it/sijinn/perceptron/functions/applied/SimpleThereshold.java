package it.sijinn.perceptron.functions.applied;

public class SimpleThereshold implements IFunctionApplied {

	private static final long serialVersionUID = 1L;
	private float threshold = 1;

	public SimpleThereshold(float _threshold){
		super();
		this.threshold = _threshold;
	}
	
	@Override
	public float derivative(float delta, float[] o) {
		if(o.length>0)
			return 1;
		return 0;
	}
	@Override
	public float execution(float[] param) {
		if(param.length>0){
			if(param[0]>threshold)
				return 1;
			else return 0;
		}
		return 0;
	}	
	@Override
	public float flatspot() {
		return 0;
	}	
	@Override
	public String getDefinition(){
		return this.getClass().getSimpleName()+"{"+threshold+"}";
	}
	
	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}
}
