package it.sijinn.common;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.utils.Utils;

public class Neuron implements Serializable{
	private static final long serialVersionUID = 1L;
	protected Network network;
	protected int layer=-1;
	protected int order=-1;
	protected IFunctionApplied function;
	protected List<Synapse> children;
	protected List<Synapse> parents;
	protected float output=0;
	protected float target=0;
	protected boolean bias=false;
	
	final protected Logger logger = LogManager.getLogger(this.getClass());
	
	
	public Neuron(){
		super();
	}
	
	public Neuron(Network _network, Neuron another){
		super();		
		this.network = _network;
		if(another!=null){
			this.layer = another.getLayer();
			this.order = another.getOrder();
			this.function = another.getFunction();
			this.bias = another.bias;
		}
	}
	
	public Neuron(Network _network, IFunctionApplied _function){
		super();
		this.network = _network;
		this.function = _function;
	}
	
	public Neuron(Network _network, IFunctionApplied _function, boolean _bias){
		super();
		this.network = _network;
		this.function = _function;
		this.bias = _bias;
	}
	
	public Neuron(Network _network, IFunctionApplied _function, int _layer, int _order){
		super();
		this.network = _network;
		this.function = _function;
		this.layer = _layer;
		this.order = _order;
	}	
	
	public Neuron(Network _network, IFunctionApplied _function, int _layer, int _order, boolean _bias){
		super();
		this.network = _network;
		this.function = _function;
		this.layer = _layer;
		this.order = _order;
		this.bias = _bias;
	}
	

	
	
	
	public boolean makeRelation(List<Neuron> layer, float initialWeight){
		if(layer==null)
			return false;
		for(Neuron neuron:layer){
			Synapse relation = new Synapse(this, neuron, initialWeight);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			if(!neuron.isBias())
				neuron.addParentRelation(relation);
		}
		return true;
	}
	
	public boolean makeRelation(List<Neuron> layer, IGenerator weightGenerator){
		if(layer==null)
			return false;
		for(Neuron neuron:layer){
			Synapse relation = new Synapse(this, neuron, weightGenerator);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			if(!neuron.isBias())
				neuron.addParentRelation(relation);
		}
		return true;
	}
	
	
	public boolean makeRelation(List<Neuron> layer){
		if(layer==null)
			return false;
		for(int i=0;i<layer.size();i++){
			Neuron neuron = layer.get(i);
			
			Synapse relation = null;
			relation = new Synapse(this, neuron, 0);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			if(!neuron.isBias())
				neuron.addParentRelation(relation);
		}
		return true;
	}
	
	public boolean makeRelation(Neuron neuron, float initialWeight){
		if(neuron==null)
			return false;

			Synapse relation = new Synapse(this, neuron, initialWeight);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			if(!neuron.isBias())
				neuron.addParentRelation(relation);
		return true;
	}
	
	public boolean makeRelation(Neuron neuron, IGenerator weightGenerator){
		if(neuron==null)
			return false;

			Synapse relation = new Synapse(this, neuron, weightGenerator);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			if(!neuron.isBias())
				neuron.addParentRelation(relation);

		return true;
	}	
	
	public boolean updateWeights(boolean clearProperties){
		if(children!=null){
			for(Synapse synapse:children){
				synapse.setWeight(0f);
				if(clearProperties && synapse.getProperty()!=null)
//					synapse.getProperty().clear();
					synapse.setProperty(null);
			}
		}
		return true;
	}
	
	public boolean updateWeights(float weight, boolean clearProperties){
		if(children!=null){
			for(Synapse synapse:children){
				synapse.setWeight(weight);
				if(clearProperties && synapse.getProperty()!=null)
//					synapse.getProperty().clear();
					synapse.setProperty(null);
			}
		}
		return true;
	}
	
	public boolean updateWeights(IGenerator weightGenerator, boolean clearProperties){
		if(weightGenerator==null)
			return false;
		if(children!=null){
			for(Synapse synapse:children){
				synapse.setWeight(weightGenerator.generate(synapse.getFrom(), synapse.getTo()));
				if(clearProperties && synapse.getProperty()!=null)
//					synapse.getProperty().clear();
					synapse.setProperty(null);
			}
		}
		return true;
	}	
	
	
	public boolean addParentRelation(Synapse relation){
		if(relation==null)
			return false;
		if(isBias())
			return false;
		if(parents==null)
			parents = new ArrayList<Synapse>();
		parents.add(relation);
		return true;
	}
	
	public float calculation(){
		if(parents!=null){
			output=0;
			for(Synapse relation: parents)
				output+= relation.getFrom().getOutput()*relation.getWeight();
			
			if(this instanceof Network){
				((Network)this).setInputValues(0, new float[]{output});
				float[] outputs =((Network)this).compute();
				if(outputs!=null && outputs.length>0)
					output = outputs[0];
			}
			output = ((getFunction()!=null)?getFunction().execution(new float[]{output}):0);
		}else if(parents==null && this instanceof Network && !isBias()){
			float[] outputs = ((Network)this).compute();
			if(outputs!=null && outputs.length>0)
				output = outputs[0];
		}
		return output;
	}
	
	public float calculationReversed(){
		if(children!=null){
			output=0;
			for(Synapse relation: children)
				output+= relation.getTo().getOutput()*relation.getWeight();
			
			if(this instanceof Network){
				((Network)this).setInputValues(0, new float[]{output});
				float[] outputs =((Network)this).compute();
				if(outputs!=null && outputs.length>0)
					output = outputs[0];
			}
			output = ((getFunction()!=null)?getFunction().execution(new float[]{output}):0);
		}else if(parents==null && this instanceof Network && !isBias()){
			float[] outputs = ((Network)this).computeReversed();
			if(outputs!=null && outputs.length>0)
				output = outputs[0];
		}
		return output;
	}	
	
	
	public boolean setChildSynapse(Synapse synapse, boolean updateIfExist){
		if(synapse==null)
			return false;
		if(children==null)
			children = new ArrayList<Synapse>();
		if(updateIfExist){
			boolean found=false;
			for(Synapse present:children){
				if(	present.getFrom()!=null && synapse.getFrom()!=null && present.getFrom().equals(synapse.getFrom()) &&
					present.getTo()!=null && synapse.getTo()!=null && present.getTo().equals(synapse.getTo())){
					present.setWeight(synapse.getWeight());
					found=true;
					break;
				}
			}
			if(!found)
				children.add(synapse);
		}else
			children.add(synapse);
		return true;
	}
	
	public boolean setParentSynapse(Synapse synapse, boolean updateIfExist){
		if(synapse==null)
			return false;
		if(parents==null)
			parents = new ArrayList<Synapse>();
		if(updateIfExist){
			boolean found=false;
			for(Synapse present:parents){
				if(	present.getFrom()!=null && synapse.getFrom()!=null && present.getFrom().equals(synapse.getFrom()) &&
					present.getTo()!=null && synapse.getTo()!=null && present.getTo().equals(synapse.getTo())){
					present.setWeight(synapse.getWeight());
					found=true;
					break;
				}
			}
			if(!found)
				parents.add(synapse);
		}else
			parents.add(synapse);
		return true;
	}
	
	
	public String toSaveString(String prefix){
		String result = (prefix!=null)?prefix:"";
		result+="<neuron>"+
				Utils.normalXML(getLayer()+","+getOrder()+","+((getFunction()!=null)?getFunction().getDefinition():" "),"utf8")+","+isBias()+
		"</neuron>\n";
		return result;
	}
	
	public String getPosition(){
		return layer+","+order;
	}
	
	public String toString(){
		return toSaveString(null);
	}
	
	public int getLayer() {
		return layer;
	}
	public Neuron setLayer(int layer) {
		this.layer = layer;
		return this;
	}
	public int getOrder() {
		return order;
	}
	public Neuron setOrder(int order) {
		this.order = order;
		return this;
	}
	public List<Synapse> obtainChildren() {
		return children;
	}
	public Neuron setChildren(List<Synapse> children) {
		this.children = children;
		return this;
	}
	public List<Synapse> obtainParents() {
		return parents;
	}
	public Neuron setParents(List<Synapse> parent) {
		this.parents = parent;
		return this;
	}
	public float getOutput() {
		if(isBias())
			return 1;
		return output;
	}
	public Neuron setOutput(float output) {
		if(!isBias())
			this.output = output;
		return this;
	}
	public IFunctionApplied getFunction() {
		return function;
	}
	public Neuron setFunction(IFunctionApplied function) {
		this.function = function;
		return this;
	}
	
	public float getTarget() {
		return target;
	}

	public Neuron setTarget(float target) {
		this.target = target;
		return this;
	}

	public Network obtainNetwork() {
		return network;
	}

	public Neuron setNetwork(Network network) {
		this.network = network;
		return this;
	}
	
	public boolean isBias() {
		return bias;
	}

	public Neuron setBias(boolean bias) {
		this.bias = bias;
		return this;
	}
	
	
	public static Neuron create(Network _network, Node node, Logger logger){
		if(node==null)
			return null;
		if(node.getNodeName().equalsIgnoreCase("neuron")){
			String properties = null;
			try{
				properties = ((Text)node.getFirstChild()).getData();
			}catch(Exception e){			
			}
			if(properties!=null)
				return create(_network, properties, logger);
			else{
				if(logger!=null){
					logger.error("Neuron instance Error: xml node is incomplet for initialization.");
					Network.error("Neuron instance Error: xml node is incomplet for initialization.");
				}
			}
		}
		return null;
	}
	
	public static Neuron create(Network _network, String properties, Logger logger){
		
		if(properties==null || properties.trim().length()==0)
			return null;
		try{
			StringTokenizer st = new StringTokenizer(properties, ",");
			int layer = -1;
			int order = -1;
			boolean bias = false;
			IFunctionApplied functionApplied = null;
			if(st.hasMoreTokens())
				layer = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				order = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				functionApplied = create(st.nextToken(), logger);
			if(st.hasMoreTokens()){
				try{
					bias = Boolean.valueOf(st.nextToken()).booleanValue();
				}catch(Exception e){
				}
			}
			if(layer>-1 && order>-1)
				return new Neuron(_network, functionApplied,layer,order,bias);
			else{
				if(logger!=null){
					logger.error("Neuron instance Error: properties=["+properties+"] is incomplet for initialization.");
					Network.error("Neuron instance Error: properties=["+properties+"] is incomplet for initialization.");
				}
				return null;
			}
			
		}catch(Exception e){
			if(logger!=null){
				logger.error(e);
				Network.error(e);
			}
			return null;
		}
	}	
	
	public static IFunctionApplied create(String function, Logger logger){
		IFunctionApplied functionApplied = null;
		String input = function;
		try{
			if(function!=null && !function.trim().equals("")){
				if(function.indexOf("{")==-1){
					if(function.indexOf(".")>-1)
						functionApplied = (IFunctionApplied)Class.forName(function).newInstance();
					else
						functionApplied = (IFunctionApplied)Class.forName("it.sijinn.perceptron.functions.applied."+function).newInstance();
				}else{
					String parameters = function.substring(function.indexOf("{")+1, function.lastIndexOf("}"));
					function = function.substring(0, function.indexOf("{"));
					StringTokenizer stp = new StringTokenizer(parameters, "|");
					List<Float> param = new ArrayList<Float>();
					while(stp.hasMoreTokens())
						param.add(Float.valueOf(stp.nextToken()));
					Float[] fParam = new Float[param.size()];
					fParam = param.toArray(fParam);
	
					
					Class<?> clazz = null;
					if(function.indexOf(".")>-1)
						clazz = (Class<?>)Class.forName(function).asSubclass(IFunctionApplied.class);
					else
						clazz = (Class<?>)Class.forName("it.sijinn.perceptron.functions.applied."+function);
					
					Constructor<?> clazzConstructor = null;
					for(Constructor<?> constructor: (Constructor<?>[])clazz.getConstructors()){
						if(constructor.getParameterTypes().length==fParam.length){
							boolean isCorrect=true;
							for(Class<?> paramClass: constructor.getParameterTypes())
								isCorrect&=(paramClass.isPrimitive() &&  paramClass.getName().equals("float"));
							if(isCorrect){
								clazzConstructor = constructor;
								break;
							}
						}
					}
					
					if(clazzConstructor!=null)
						functionApplied = (IFunctionApplied)clazzConstructor.newInstance((Object[])fParam);
					else{
						if(logger!=null){
							logger.error("FunctionApplied instance Error: properties=["+input+"] is incomplet for initialization.");
							Network.error("FunctionApplied instance Error: properties=["+input+"] is incomplet for initialization.");
						}
						return null;
					}
				}	
			}
		}catch(Exception e){
			if(logger!=null){
				logger.error(e);
				Network.error(e);
			}
			return null;
		}
		return functionApplied;
	}

	public Logger obtainLogger() {
		return logger;
	}


	
}
