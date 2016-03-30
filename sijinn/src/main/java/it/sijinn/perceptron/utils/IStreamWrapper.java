package it.sijinn.perceptron.utils;

import java.io.InputStream;

public interface IStreamWrapper {
	InputStream openStream() throws Exception;
	boolean closeStream() throws Exception;
	IStreamWrapper instance() throws Exception;
}
