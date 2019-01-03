package it.sijinn.perceptron.utils;

import it.sijinn.common.Synapse;

public interface ISynapseProperty {
	ISynapseProperty clear();
	ISynapseProperty setSynapse(Synapse synapse);
	Synapse getSynapse();
	float getAggregated();
	void setAggregated(float value);
}
