package it.sijinn.common;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.utils.ISynapseProperty;
import it.sijinn.perceptron.utils.Utils;


public class Synapse implements Serializable{

	private static final long serialVersionUID = 1L;
	protected Neuron from;
	protected Neuron to;
	protected float weight = 0;
	protected ISynapseProperty property;
	
	public Synapse(Neuron _from, Neuron _to, float _weight){
		super();
		this.from = _from;
		this.to = _to;
		this.weight = _weight;
	}
	
	public Synapse(Neuron _from, Neuron _to, IGenerator weightGenerator){
		super();
		this.from = _from;
		this.to = _to;
		if(weightGenerator!=null)
			this.weight = weightGenerator.generate(from, to);
	}
	
	
	public Synapse(int fromLayer, int fromOrder, int toLayer, int toOrder, float _weight){
		super();
		this.from = new Neuron();
		this.from.setLayer(fromLayer);
		this.from.setOrder(fromOrder);
		this.to = new Neuron();
		this.to.setLayer(toLayer);
		this.to.setOrder(toOrder);
		this.weight = _weight;
	}
	
	public Synapse(int fromLayer, int fromOrder, int toLayer, int toOrder, IGenerator weightGenerator){
		super();
		this.from = new Neuron();
		this.from.setLayer(fromLayer);
		this.from.setOrder(fromOrder);
		this.to = new Neuron();
		this.to.setLayer(toLayer);
		this.to.setOrder(toOrder);
		if(weightGenerator!=null)
			this.weight = weightGenerator.generate(from, to);
	}	
	
	public Synapse(int fromLayer, int fromOrder, IFunctionApplied fromFunction, int toLayer, int toOrder, IFunctionApplied toFunction, float _weight){
		super();
		this.from = new Neuron();
		this.from.setLayer(fromLayer);
		this.from.setOrder(fromOrder);
		this.from.setFunction(fromFunction);
		if(toLayer>-1 && toOrder>-1 && toFunction!=null){
			this.to = new Neuron();
			this.to.setLayer(toLayer);
			this.to.setOrder(toOrder);
			this.to.setFunction(toFunction);
		}
		this.weight = _weight;
	}
	

	public String getDirection(){
		return ""+
				((from!=null)?from.getLayer():"-1")+","+
				((from!=null)?from.getOrder():"-1")+"|"+
				((to!=null)?to.getLayer():"-1")+","+
				((to!=null)?to.getOrder():"-1");
		
	}
	
	public String toSaveString(String prefix){
		String result=(prefix!=null)?prefix:"";
		result+="<synapse>"+
		Utils.normalXML(((from!=null)?from.getLayer():"-1")+","+((from!=null)?from.getOrder():"-1")+","+((to!=null)?to.getLayer():"-1")+","+((to!=null)?to.getOrder():"-1")+","+weight+ "," + ((property!=null)?property.toString():"{}"),"utf8")+
		"</synapse>\n";
		return result;
	}	
	
	public String toString(){
		return toSaveString(null);
	}
	
	public float getWeight() {
		return weight;
	}
	public Synapse setWeight(float weight) {
		this.weight = weight;
		return this;
	}
	public Neuron getFrom() {
		return from;
	}
	public Synapse setFrom(Neuron from) {
		this.from = from;
		return this;
	}
	
	public Neuron getTo() {
		return to;
	}
	public Synapse setTo(Neuron to) {
		this.to = to;
		return this;
	}
	
	public ISynapseProperty getProperty() {
		return property;
	}

	public Synapse setProperty(ISynapseProperty property) {
		this.property = property;
		return this;
	}
	
	
	public static Synapse create(Node node, Logger logger){
		if(node==null)
			return null;
		if(node.getNodeName().equalsIgnoreCase("synapse")){
			String properties = null;
			try{
				properties = ((Text)node.getFirstChild()).getData();
			}catch(Exception e){			
			}
			if(properties!=null)
				return create(properties, logger);
			else{
				logger.error("Synapse instance Error: xml node is incomplet for initialization.");
				Network.error("Synapse instance Error: xml node is incomplet for initialization.");
			}
		}
		return null;
	}
	
	public static Synapse create(String properties, Logger logger){
		
		if(properties==null || properties.length()==0)
			return null;
		try{
			final StringTokenizer st = new StringTokenizer(properties, ",");
			int layerFrom = -1;
			int orderFrom = -1;
			int layerTo = -1;
			int orderTo = -1;
			float weight = 0;

			if(st.hasMoreTokens())
				layerFrom = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				orderFrom = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				layerTo = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				orderTo = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				weight = Float.valueOf(st.nextToken()).floatValue();
			
			if(layerFrom>-1 && orderFrom>-1 && layerTo>-1 && orderTo>-1)
				return new Synapse(layerFrom,orderFrom,layerTo,orderTo, weight);
			else{ 
				logger.error("Synapse instance Error: properties=["+properties+"] is incomplet for initialization.");
				Network.error("Synapse instance Error: properties=["+properties+"] is incomplet for initialization.");
				return null;
			}
		}catch(Exception e){
			logger.error(e);
			Network.error(e);
			return null;
		}
		
		
	}	
}
