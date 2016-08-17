package it.sijinn.perceptron.utils.io;

public interface IDataWriter {
	boolean open() throws Exception;
	boolean close() throws Exception;
	boolean writeNext(byte[] obj) throws Exception;
	boolean finalizer() throws Exception;
}
