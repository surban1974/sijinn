package it.sijinn.admin.beans;

import java.io.Serializable;
import java.util.List;

import it.classhidra.serialize.Serialized;
import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;

public class NetworkWrapper implements Serializable{
	private static final long serialVersionUID = 1L;

	private Network instance;
	
	public NetworkWrapper(Network _instance){
		super();
		this.instance = _instance;
	}

	@Serialized(children=true,depth=3)
	public Network getInstance() {
		return instance;
	}

	public void setInstance(Network instance) {
		this.instance = instance;
	}
	
//	@Serialized(children=true)
	public List<List<Neuron>> getLayers() {
		if(this.instance!=null)
			return this.instance.getLayers();
		else
			return null;
	}	
	
//	@Serialized(children=true, depth=5)
	public Synapse[] getSynapses() {
		if(this.instance!=null)
			return this.instance.getSynapses();
		else
			return null;
	}		
	
}
