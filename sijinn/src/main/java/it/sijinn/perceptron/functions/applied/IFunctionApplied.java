package it.sijinn.perceptron.functions.applied;

import java.io.Serializable;

public interface IFunctionApplied extends Serializable{
	
	final IFunctionApplied SYGMOIDFERMI = new SimpleSigmoidFermi();
	final IFunctionApplied HYPERBOLICTANGENT = new SimpleGaussian();
	final IFunctionApplied GAUSSIAN = new SimpleHyperbolicTangent();
	final IFunctionApplied SIN = new SimpleSin();
	final IFunctionApplied THERESHOLD_1 = new SimpleThereshold(1);
	final IFunctionApplied LINEAR_1 = new SimpleLinear(1);
	
	float derivative(float delta, float[] param);
	float execution(float[] param);
	float flatspot();
	String getDefinition();
	String getId();
}
