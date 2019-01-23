package it.sijinn.perceptron.functions.deferred;

import java.io.Serializable;

public interface IDAFloatFunction extends Serializable{
	float apply(float first, float second);
	void init();
	String getDefinition();
	String getId();
}
