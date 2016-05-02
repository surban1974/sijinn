package it.sijinn.perceptron.functions.applied;

public class SimpleGaussian implements IFunctionApplied {
	private static final long serialVersionUID = 1L;
	private float sigma=0.5f;
	
	public SimpleGaussian(){
		super();
	}	
	
	public SimpleGaussian(float _sigma){
		super();
		this.sigma = _sigma;
	}
	
	public SimpleGaussian(float _sigma,float _sigma1){
		super();
		this.sigma = _sigma;
	}
	
	@Override
	public float derivative(float delta, float[] param) {
		if(param.length>0)
			return execution(param) * ( -param[0] / (sigma*sigma) );
		return 0;
	}

	@Override
	public float execution(float[] param) {
		if(param.length>0)
			return new Float(Math.exp(-Math.pow(param[0], 2) / (2*Math.pow(sigma, 2)))).floatValue();
		return 0;
	}
	@Override
	public String getDefinition(){
		return this.getClass().getSimpleName()+"{"+sigma+"}";
	}

	@Override
	public float flatspot() {
		return 0;
	}
	
	@Override
	public String getId(){
		return this.getClass().getSimpleName();
	}
}
