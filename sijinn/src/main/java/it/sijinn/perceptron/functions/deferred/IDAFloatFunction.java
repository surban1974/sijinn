package it.sijinn.perceptron.functions.deferred;

public interface IDAFloatFunction {
	float apply(float first, float second);
	void init();
	String toSaveString();
}
