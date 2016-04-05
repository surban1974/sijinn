package it.sijinn.perceptron.strategies;

import java.io.File;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.error.IErrorFunctionApplied;
import it.sijinn.perceptron.utils.IStrategyListener;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.IStreamWrapper;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;

public interface ITrainingStrategy {
	float apply(Network network, float[][][] data) throws Exception;
	float apply(Network network, File file, IReadLinesAggregator dataAggregator) throws Exception;
	float apply(Network network, IStreamWrapper streamWrapper, IReadLinesAggregator dataAggregator) throws Exception;
	float apply(Network network, IDataReader dataReader, IReadLinesAggregator dataAggregator) throws Exception;
	ITrainingAlgorithm getTrainingAlgorithm();
	ITrainingStrategy setTrainingAlgorithm(ITrainingAlgorithm trainingAlgorithm);
	ITrainingStrategy setListener(IStrategyListener listener);
	IErrorFunctionApplied getErrorFunction();
	String toSaveString(String prefix);
}
