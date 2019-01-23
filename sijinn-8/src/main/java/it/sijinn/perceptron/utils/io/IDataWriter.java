package it.sijinn.perceptron.utils.io;

import java.io.Serializable;

public interface IDataWriter extends Serializable{
	boolean open() throws Exception;
	boolean close() throws Exception;
	boolean writeNext(byte[] obj) throws Exception;
	boolean finalizer() throws Exception;
}
