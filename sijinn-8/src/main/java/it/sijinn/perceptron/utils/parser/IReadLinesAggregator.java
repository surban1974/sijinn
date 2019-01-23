package it.sijinn.perceptron.utils.parser;

import java.io.Serializable;

import it.sijinn.common.Network;

public interface IReadLinesAggregator extends Serializable{
	PairIO getData(Network network, Object[] lines);
	Object getRowData(Network network, Object[] lines);
	Object[] aggregate(Object line, int linenumber);
}
