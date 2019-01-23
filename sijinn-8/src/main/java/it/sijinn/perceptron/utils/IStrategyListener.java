package it.sijinn.perceptron.utils;

import java.io.Serializable;

import it.sijinn.common.Network;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.parser.PairIO;

public interface IStrategyListener extends Serializable{
	IStrategyListener setTrainingStrategy(ITrainingStrategy strategy);
	void onAfterReaderOpen(Network network,IDataReader reader) throws Exception;
	void onAfterReaderClose(Network network,IDataReader reader) throws Exception;
	void onAfterReaderFinalize(Network network,IDataReader reader) throws Exception;
	void onAfterLinePrepared(Network network,int linenumber, Object[] aggregated) throws Exception;
	void onAfterDataPrepared(Network network,int linenumber, PairIO param) throws Exception;
	void onAfterDataComputed(Network network,int linenumber, PairIO param, boolean reversed) throws Exception;
	void onAfterAlgorithmCalculated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param, boolean reversed) throws Exception;
	void onAfterAlgorithmUpdated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param, boolean reversed) throws Exception;
	void onAfterAlgorithmCalculatedAndUpdated(Network network, ITrainingAlgorithm algorithm, int linenumber, PairIO param, boolean reversed) throws Exception;
	void onAfterErrorComputed(Network network, float error, int linenumber, PairIO param, boolean reversed) throws Exception;
}
