package it.sijinn.perceptron;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.generator.IGenerator;

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
		}
	}
	
	public Neuron(Network _network, IFunctionApplied _function){
		super();
		this.network = _network;
		this.function = _function;
	}
	
	public Neuron(Network _network, IFunctionApplied _function, int _layer, int _order){
		super();
		this.network = _network;
		this.function = _function;
		this.layer = _layer;
		this.order = _order;
	}
	

	
	
	
	public boolean makeRelation(List<Neuron> layer, float initialWeight){
		if(layer==null)
			return false;
		for(Neuron neuron:layer){
			Synapse relation = new Synapse(this, neuron, initialWeight);
			if(children==null)
				children = new ArrayList<Synapse>();
			children.add(relation);
			neuron.addParenRelation(relation);
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
			neuron.addParenRelation(relation);
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
			neuron.addParenRelation(relation);
		}
		return true;
	}
	
	public boolean updateWeights(boolean clearProperties){
		if(children!=null){
			for(Synapse synapse:children){
				synapse.setWeight(0f);
				if(clearProperties && synapse.getProperty()!=null)
					synapse.getProperty().clear();
			}
		}
		return true;
	}
	
	public boolean updateWeights(float weight, boolean clearProperties){
		if(children!=null){
			for(Synapse synapse:children){
				synapse.setWeight(weight);
				if(clearProperties && synapse.getProperty()!=null)
					synapse.getProperty().clear();
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
					synapse.getProperty().clear();
			}
		}
		return true;
	}	
	
	
	public boolean addParenRelation(Synapse relation){
		if(relation==null)
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
			
			output = ((getFunction()!=null)?getFunction().execution(new float[]{output}):0);
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
	
	
	public String toSaveString(){
		String result = "";
		result+="neuron="+getLayer()+","+getOrder()+","+((getFunction()!=null)?getFunction().getClass().getSimpleName():"")+"\n";
		return result;
	}
	
	public String toString(){
		return toSaveString();
	}
	
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public List<Synapse> getChildren() {
		return children;
	}
	public void setChildren(List<Synapse> children) {
		this.children = children;
	}
	public List<Synapse> getParents() {
		return parents;
	}
	public void setParents(List<Synapse> parent) {
		this.parents = parent;
	}
	public float getOutput() {
		return output;
	}
	public void setOutput(float output) {
		this.output = output;
	}
	public IFunctionApplied getFunction() {
		return function;
	}
	public void setFunction(IFunctionApplied function) {
		this.function = function;
	}

	public float getTarget() {
		return target;
	}

	public void setTarget(float target) {
		this.target = target;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}
	
	
	public static Neuron create(Network _network, String properties, Logger logger){
		
		if(properties==null || properties.length()==0)
			return null;
		try{
			StringTokenizer st = new StringTokenizer(properties, ",");
			int layer = -1;
			int order = -1;
			IFunctionApplied functionApplied = null;
			if(st.hasMoreTokens())
				layer = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens())
				order = Integer.valueOf(st.nextToken()).intValue();
			if(st.hasMoreTokens()){
				String function = st.nextToken();
				if(!function.trim().equals("")){
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
							logger.error("Neuron instance Error: FunctionApplied into properties=["+properties+"] is incomplet for initialization.");
							return null;
						}
					}	
				}
			}
			if(layer>-1 && order>-1)
				return new Neuron(_network, functionApplied,layer,order);
			else{
				logger.error("Neuron instance Error: properties=["+properties+"] is incomplet for initialization.");
				return null;
			}
			
		}catch(Exception e){
			logger.error(e);
			return null;
		}
		
		
	}	
	
}
