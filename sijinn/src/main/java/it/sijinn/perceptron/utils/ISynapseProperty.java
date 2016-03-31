package it.sijinn.perceptron.utils;

import it.sijinn.perceptron.Synapse;

public interface ISynapseProperty {
	ISynapseProperty clear();
	ISynapseProperty setSynapse(Synapse synapse);
	Synapse getSynapse();
}
