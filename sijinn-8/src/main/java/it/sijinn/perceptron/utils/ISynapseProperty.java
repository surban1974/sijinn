package it.sijinn.perceptron.utils;

import java.io.Serializable;

import it.sijinn.common.Synapse;

public interface ISynapseProperty extends Serializable{
	ISynapseProperty clear();
	ISynapseProperty setSynapse(Synapse synapse);
	Synapse getSynapse();
	float getAggregated();
	void setAggregated(float value);
}
