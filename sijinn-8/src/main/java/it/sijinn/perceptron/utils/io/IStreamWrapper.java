package it.sijinn.perceptron.utils.io;

import java.io.InputStream;
import java.io.Serializable;

public interface IStreamWrapper extends Serializable{
	InputStream openStream() throws Exception;
	boolean closeStream() throws Exception;
	IStreamWrapper instance() throws Exception;
}
