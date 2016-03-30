package it.sijinn.perceptron.deferredaggregatefunctions;

public interface IDAFloatFunction {
	float apply(float first, float second);
	void init();
	String toSaveString();
}
