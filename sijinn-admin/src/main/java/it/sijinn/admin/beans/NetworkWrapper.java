package it.sijinn.admin.beans;

import java.io.Serializable;

import it.sijinn.common.Network;

public class NetworkWrapper implements Serializable{
	private static final long serialVersionUID = 1L;

	private Network instance;
	
	public NetworkWrapper(Network _instance){
		super();
		this.instance = _instance;
	}

	public Network getInstance() {
		return instance;
	}

	public void setInstance(Network instance) {
		this.instance = instance;
	}
	
}
