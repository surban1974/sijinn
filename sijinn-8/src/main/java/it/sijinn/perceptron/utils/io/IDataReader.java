package it.sijinn.perceptron.utils.io;

import java.io.Serializable;

public interface IDataReader extends Serializable{
	boolean open() throws Exception;
	boolean close() throws Exception;
	Object readNext() throws Exception;
	byte[] readAll() throws Exception;
	boolean finalizer() throws Exception;
}
