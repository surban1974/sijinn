package it.sijinn.perceptron.utils.io;

import java.io.InputStream;

public interface IStreamWrapper {
	InputStream openStream() throws Exception;
	boolean closeStream() throws Exception;
	IStreamWrapper instance() throws Exception;
}
