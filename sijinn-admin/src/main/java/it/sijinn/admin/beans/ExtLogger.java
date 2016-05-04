package it.sijinn.admin.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sijinn.perceptron.utils.IExtLogger;

public class ExtLogger implements IExtLogger {

	private Map<String, List<String>> container;
	
	public ExtLogger(){
		super();
		container = new HashMap<String, List<String>>();
		container.put(IExtLogger.log_DEBUG, new ArrayList<String>());
		container.put(IExtLogger.log_ERROR, new ArrayList<String>());
		container.put(IExtLogger.log_FATAL, new ArrayList<String>());
		container.put(IExtLogger.log_INFO, new ArrayList<String>());
		container.put(IExtLogger.log_WARN, new ArrayList<String>());
	}
	
	@Override
	public IExtLogger add(String mess, String type) {
		if(mess!=null && type!=null && container!=null)
			container.get(type).add(mess);
		return this;
	}

	@Override
	public IExtLogger add(Throwable th, String type) {
		if(th!=null && type!=null && container!=null)
			container.get(type).add(th.toString());
		return this;
	}

	@Override
	public Object export() {
		Map<String, List<String>> ret = new HashMap<String, List<String>>(container);
		container = new HashMap<String, List<String>>();
		container.put(IExtLogger.log_DEBUG, new ArrayList<String>());
		container.put(IExtLogger.log_ERROR, new ArrayList<String>());
		container.put(IExtLogger.log_FATAL, new ArrayList<String>());
		container.put(IExtLogger.log_INFO, new ArrayList<String>());
		container.put(IExtLogger.log_WARN, new ArrayList<String>());
		return ret;
	}

}
