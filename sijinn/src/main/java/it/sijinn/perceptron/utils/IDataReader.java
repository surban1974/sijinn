package it.sijinn.perceptron.utils;

public interface IDataReader {
	boolean open() throws Exception;
	boolean close() throws Exception;
	Object readNext() throws Exception;
	boolean finalizer() throws Exception;
}
