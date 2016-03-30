package it.sijinn.perceptron.strategies;

import java.io.File;


import it.sijinn.perceptron.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IReadLinesAggregator;
import it.sijinn.perceptron.utils.IDataReader;
import it.sijinn.perceptron.utils.IStreamWrapper;

public interface ITrainingStrategy {
	float apply(Network network, float[][][] data) throws Exception;
	float apply(Network network, File file, IReadLinesAggregator dataAggregator) throws Exception;
	float apply(Network network, IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator) throws Exception;
	float apply(Network network, IDataReader dataReader, IReadLinesAggregator dataAggregator) throws Exception;
	ITrainingAlgorithm getTrainingAlgorithm();
	IErrorFunctionApplied getErrorFunction();
	String toSaveString();
}
