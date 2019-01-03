package it.sijinn.perceptron.utils;

public interface IExtLogger {
	public static String log_DEBUG = 				"DEBUG";
	public static String log_INFO = 				"INFO";
	public static String log_WARN = 				"WARN";
	public static String log_ERROR = 				"ERROR";
	public static String log_FATAL = 				"FATAL";
	
	IExtLogger add(String mess,String type);
	IExtLogger add(Throwable th,String type);
	Object export();
	
}
