package it.sijinn.perceptron.utils.parser;

import it.sijinn.perceptron.Network;

public interface IReadLinesAggregator {
	PairIO getData(Network network, Object[] lines);
	Object getRowData(Network network, Object[] lines);
	Object[] aggregate(Object line, int linenumber);
}
