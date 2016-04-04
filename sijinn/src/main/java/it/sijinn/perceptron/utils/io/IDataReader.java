package it.sijinn.perceptron.utils.io;

public interface IDataReader {
	boolean open() throws Exception;
	boolean close() throws Exception;
	Object readNext() throws Exception;
	byte[] readAll() throws Exception;
	boolean finalizer() throws Exception;
}
