package it.sijinn.admin.components.controllers; 




import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionCall;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Expose;
import it.classhidra.annotation.elements.NavigatedDirective;
import it.classhidra.annotation.elements.Parameter;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.core.controller.redirects;
import it.classhidra.core.tool.exception.bsControllerException;
import it.classhidra.scheduler.common.i_batch;
import it.classhidra.scheduler.scheduling.db.db_batch;
import it.classhidra.scheduler.scheduling.db.db_batch_log;
import it.classhidra.scheduler.scheduling.thread.singleThreadEvent;
import it.classhidra.serialize.JsonWriter;
import it.classhidra.serialize.Serialized;
import it.sijinn.admin.beans.ExtLogger;
import it.sijinn.admin.beans.NetworkWrapper;
import it.sijinn.admin.workers.Worker;
import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.common.Synapse;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.algorithms.GENE;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.DefiniteWeightGenerator;
import it.sijinn.perceptron.functions.generator.IGenerator;
import it.sijinn.perceptron.functions.generator.RandomPositiveWeightGenerator;
import it.sijinn.perceptron.functions.generator.RandomWeightGenerator;
import it.sijinn.perceptron.functions.generator.ZeroWeightGenerator;
import it.sijinn.perceptron.genetic.NeuralBreeding;
import it.sijinn.perceptron.strategies.BatchGradientDescent;
import it.sijinn.perceptron.strategies.GeneticBreeding;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.sijinn.perceptron.strategies.StochasticGradientDescent;
import it.sijinn.perceptron.utils.IExtLogger;
import it.sijinn.perceptron.utils.io.IDataReader;
import it.sijinn.perceptron.utils.io.ResourceStreamWrapper;
import it.sijinn.perceptron.utils.io.SimpleStreamReader;
import it.sijinn.perceptron.utils.io.SimpleStringReader;
import it.sijinn.perceptron.utils.parser.IReadLinesAggregator;
import it.sijinn.perceptron.utils.parser.PairIO;
import it.sijinn.perceptron.utils.parser.SimpleLineDataAggregator;


@Action (
	path="network",
	name="model",
	redirect="/pages/network.html",
	reloadAfterAction="true",
	entity=@Entity(
		property="allway:public"
	)
)



@NavigatedDirective
public class ControllerNetwork extends AbstractBase implements i_action, i_bean, Serializable{
	private static final long serialVersionUID = 1L;
	
	final private String resource_training = "examples/resources/interpolation_training.txt";
	final private String resource_xor_net = "examples/resources/XOR_init.net";

	private IDataReader dataReader;
	private IReadLinesAggregator readLinesAggregator = new SimpleLineDataAggregator(";");	
	
	private singleThreadEvent event;
	
	private i_batch worker;	
	private NetworkWrapper wrapper;	
	private ITrainingStrategy strategy;	
	private ITrainingAlgorithm algorithm;

	
	private float learningRate = 0.5f;
	private float learningMomentum = 0.01f;
	private String activationFunctions = "SimpleSigmoidFermi";
	private String defaultNetwork = "Interpolation";
	private String initWeight = "RP";
	
	private boolean parallel = false;
	private Map<String, Neuron> selectedn = new HashMap<String, Neuron>();
	private boolean initialized = false;
	
	

	
public ControllerNetwork(){
	super();
}

@Override
public redirects actionservice(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, UnavailableException, bsControllerException {
	initController();
	return super.actionservice(request, response);
}

@ActionCall(
		name="check",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String check(){
	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="change",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String change(@Parameter(name="type") String type, @Parameter(name="value") String value ){
	clear();
	if(type==null){
	}else if(type.equals("type")){
		setDefaultNetwork(value);
		
		String json = JsonWriter.object2json(this.get_bean(), "model");
		clear();
		return json;
	}else if(type.equals("initWeight")){	
		initWeight = value;
		if(wrapper!=null && wrapper.obtainInstance()!=null)
			wrapper.obtainInstance().clearSynapses(createWeightGenerator(), true);
		
		return JsonWriter.object2json(this.get_bean(), "model");
	}else if(type.equals("activationFunctions")){
		if(wrapper!=null && wrapper.obtainInstance()!=null){

			try{
				IFunctionApplied function = Neuron.create(value, null);
				if(function==null)
					setError("Activation Function "+value+" is not exsist.");
				wrapper.obtainInstance().updateActivationFunctions(function);
				setSuccess("Activation Function "+value+" updated successfully.");
				this.activationFunctions = value;
			}catch(Exception e){
				setError("Error instance Activation Function: "+e.toString());
			}
				
			String json = JsonWriter.object2json(this.get_bean(), "model");
			clear();
			return json;

		}
	}else if(type.equals("trainingStrategy")){
		try{
			String second=null;
			if(value!=null && value.indexOf("-")>-1){
				try{					
					second = value.split("-")[1];
					value = value.split("-")[0];
				}catch(Exception e){
					
				}
			}
			
			if(second!=null)
				setParallel(true);
			else
				setParallel(false);
			
			ITrainingStrategy cur_strategy = Network.createStrategyById(value, null);
			if(cur_strategy!=null){
				cur_strategy.setErrorFunction(new MSE());
				this.strategy = cur_strategy;
			}
			if(this.strategy instanceof GeneticBreeding){
				this.algorithm = new GENE(
										new NeuralBreeding().setElitism(false)
									).setPopulationSize(25);
			}else{
				if(this.algorithm instanceof GENE)
					this.algorithm = new BPROP().setDeferredAgregateFunction(new SUMMATOR());
				else if(this.strategy instanceof StochasticGradientDescent && !(this.algorithm instanceof BPROP))
					this.algorithm = new BPROP().setDeferredAgregateFunction(new SUMMATOR());
			}
			if(this.algorithm!=null)
				this.strategy.setTrainingAlgorithm(algorithm);
			
			if(this.strategy instanceof GeneticBreeding)
				if(isParallel()) 
					((GeneticBreeding)this.strategy).setParallelLimit(10);
				else
					((GeneticBreeding)this.strategy).setParallelLimit(0);
			
			

			setSuccess("Training Strategy "+value+" updated successfully.");
		}catch(Exception e){
			setError("Error instance Training Strategy: "+e.toString());
		}
		String json = JsonWriter.object2json(this.get_bean(), "model");
		clear();
		return json;
	}else if(type.equals("trainingAlgorithm")){
		try{
			ITrainingAlgorithm cur_algorithm = Network.createAlgorithmById(value, null);
			if(cur_algorithm!=null){
				cur_algorithm.setDeferredAgregateFunction(new SUMMATOR());
				this.algorithm = cur_algorithm;
			}
			if(strategy!=null)
				this.strategy.setTrainingAlgorithm(algorithm);
			setSuccess("Training Algorithm "+value+" updated successfully.");
		}catch(Exception e){
			setError("Error instance Training Algorithm: "+e.toString());
		}
		String json = JsonWriter.object2json(this.get_bean(), "model");
		clear();
		return json;
	}

	setError("Generic error - insufficient parameters");
	String json = JsonWriter.object2json(this.get_bean(), "model");
	clear();
	return json;
}

@ActionCall(
		name="stop",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String stop(){
	if(event!=null && event.getWorker()!=null){
		((Worker)event.getWorker()).setForcedStop(true);
		event.interrupt();
		try{
			SECONDS.sleep(3);
		}catch(Exception e){			
		}
	}
	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="restart",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String restart(){
	if(event!=null)
		event.interrupt();	
	
	if(worker==null)
		worker = new Worker();
	
	if(strategy!=null)
		strategy
			.setTrainingAlgorithm(algorithm)
			.setErrorFunction(new MSE());
		
	((Worker)worker)
		.setStep(0)
		.setDelta(0)
		
		.setNetwork(wrapper.obtainInstance().clearSynapses(createWeightGenerator(), true))
		.setTrainingStrategy(strategy)
		.setDataReader(dataReader)
		.setReadLinesAggregator(readLinesAggregator)
		;
	
	event = new singleThreadEvent(new db_batch(), new db_batch_log(), worker);
	event.start();
	try{
		SECONDS.sleep(3);
	}catch(Exception e){			
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="exec",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String exec(){
	if(wrapper!=null){
		wrapper.obtainInstance().calculation();
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removesynapses",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removesynapses(){
	if(wrapper!=null){
		wrapper.obtainInstance().removeSynapses();
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removesynapse",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removesynapse(@Parameter(name="from") Neuron from, @Parameter(name="to") Neuron to){
	if(wrapper!=null){
		wrapper.obtainInstance().removeSynapse(from, to);
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="addsynapse",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String addsynapse(@Parameter(name="from") Neuron from, @Parameter(name="to") Neuron to){
	if(wrapper!=null){
		wrapper.obtainInstance().addSynapse(new Synapse(from, to, 0), false);
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}


@ActionCall(
		name="addlayer",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String addlayer(){
	
	if(wrapper!=null)
		wrapper.obtainInstance()
		.insertLayer(wrapper.obtainInstance().getLayers().size()-2, 1, Neuron.create(activationFunctions, null), false,true)
		;

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removelayer",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removelayer(@Parameter(name="layer") int layer){
	
	if(wrapper!=null){
		try{
			for(Neuron neuron: wrapper.obtainInstance().getLayers().get(layer))
				selectedn.remove("n"+neuron.getLayer()+","+neuron.getOrder());
			for (Map.Entry<String, Neuron> entry : selectedn.entrySet()) {
				List<String> forRemove = new ArrayList<String>();
				if(entry.getValue().getLayer()>layer)
					forRemove.add(entry.getKey());
				for(String key:forRemove)
					selectedn.remove(key);
			}

				
		}catch(Exception e){			
		}
		wrapper.obtainInstance()
		.removeLayer(layer,true)
		;
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="addneuron",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String addneuron(@Parameter(name="layer") int layer){
	
	if(wrapper!=null){
		if(layer>=0 && layer<wrapper.obtainInstance().getLayers().size()){
			List<Neuron> neurons = wrapper.obtainInstance().getLayers().get(layer);
			Neuron neuron = new Neuron(wrapper.obtainInstance(),  Neuron.create(activationFunctions, null), layer, neurons.size(), false) ;
			wrapper.obtainInstance()
			.addNeuron(neuron, true, true)
			;
		}
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removeneuron",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removeneuron(@Parameter(name="layer") int layer, @Parameter(name="order") int order){
	
	if(wrapper!=null){
		if(layer>=0 && layer<wrapper.obtainInstance().getLayers().size()){
			List<Neuron> neurons = wrapper.obtainInstance().getLayers().get(layer);
			Neuron neuron = new Neuron(wrapper.obtainInstance(),  Neuron.create(activationFunctions, null), layer, neurons.size()-1, false) ;
			wrapper.obtainInstance()
			.removeNeuron(neuron, true)
			;
			selectedn.remove("n"+neuron.getLayer()+","+neuron.getOrder());
		}
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}


@ActionCall(
		name="diff",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String diffAsJson(){
	return "";
//	return JsonWriter.object2json(this.get_bean(), "model");

}	


@ActionCall(
		name="viewdata",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String viewdata(){
	
	List<float[]> aggr = new ArrayList<float[]>(); 
	if(dataReader!=null && readLinesAggregator!=null){
		try{

			if(dataReader.open()){
				Object next=null;
				int linenumber=0;
				while((next = dataReader.readNext()) !=null){
					Object[] aggregated = readLinesAggregator.aggregate(next,linenumber);
					if(aggregated!=null){
						PairIO param = readLinesAggregator.getData(wrapper.obtainInstance(),aggregated);
						float[] current = new float[param.getInput().length+param.getOutput().length];
						System.arraycopy(param.getInput(), 0, current, 0, param.getInput().length );
						System.arraycopy(param.getOutput(), 0, current, param.getInput().length, param.getOutput().length );
						aggr.add(current);
					}
					linenumber++;
				}
				dataReader.close();
				dataReader.finalizer();
			}
		}catch(Exception e){
			
		}
	}
	return JsonWriter.object2json(aggr, "data", null,true,3);
}	

@ActionCall(
		name="setdata",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public void setdata(@Parameter(name="data") String data){
	
	if(data!=null){
		dataReader = new SimpleStringReader(data, null);
	}

}


@ActionCall(
		name="reset",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String reset(){
	

		readLinesAggregator = new SimpleLineDataAggregator(";");	
		
		
		learningRate = 0.5f;
		learningMomentum = 0.01f;
		activationFunctions = "SimpleSigmoidFermi";
	
	
		wrapper = new NetworkWrapper(createDefaultNetwork());
	
/*	
		algorithm = new BPROP()
				.setDeferredAgregateFunction(new SUMMATOR());
		
	
		strategy = new BatchGradientDescent()
				.setTrainingAlgorithm(algorithm)
				.setErrorFunction(new MSE());
	
*/	
		worker = new Worker();
	
		if(dataReader==null)
			dataReader = new SimpleStreamReader(new ResourceStreamWrapper(resource_training));
		selectedn.clear();
	

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="viewnetwork",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String viewnetwork(){
	
	String result = ""; 
	if(wrapper!=null && wrapper.obtainInstance()!=null)
		result = wrapper.obtainInstance().save();
	
	result = result.replace("\n", "\\\\n").replace("\r", "\\\\r").replace("\t", "\\\\t").replace("\"", "\\\"");
	
	return JsonWriter.object2json(result, "network", null,true,1);
}	

@ActionCall(
		name="updatenetwork",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String updatenetwork(@Parameter(name="networksource") String networksource){


	
	Network newnetwork = new Network().open(networksource);
	
	if(Network.obtainExtLogger()!=null){
		Map<String, List<String>> log = (Map<String, List<String>>)Network.obtainExtLogger().export();
		String err="";
		if(log!=null && log.get(IExtLogger.log_ERROR)!=null){
			for(String mess:log.get(IExtLogger.log_ERROR))
				err+=mess+" ";
		}
		if(!err.equals("")){
			clear();
			setError(err);
			String json = JsonWriter.object2json(this.get_bean(), "model");
			clear();
			return json;
		}else{
			if(networksource!=null && wrapper!=null && wrapper.obtainInstance()!=null)
				wrapper.setInstance(newnetwork);			
			selectedn.clear();
		}
	}
	
	return JsonWriter.object2json(this.get_bean(), "model");
}	

@ActionCall(
		name="updatedata",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String updatedata(@Parameter(name="data") String updatedata){

	if(updatedata!=null)
		dataReader = new SimpleStringReader(updatedata,null);
	
	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="selectneuron",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String selectneuron(@Parameter(name="neuron") Neuron neuron, @Parameter(name="sel") boolean sel){
	if(neuron!=null && neuron.getLayer()>-1 && neuron.getOrder()>-1){
		if(sel)
			selectedn.put("n"+neuron.getLayer()+","+neuron.getOrder(), neuron);
		else
			selectedn.remove("n"+neuron.getLayer()+","+neuron.getOrder());
	}
	return JsonWriter.object2json(this.getSelectedn(), "selectedn",null,true,1);
}

@ActionCall(
		name="ainfo",
		
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String ainfo(){
	
	return JsonWriter.object2json(algorithm, "algorithm",null,true,1);
}


@Override
public String modelAsJson(HttpServletRequest request, HttpServletResponse response) {
	initController();
	return super.modelAsJson(request, response);
}

public void initController(){
	if(wrapper==null)
		wrapper = new NetworkWrapper(createDefaultNetwork());	
	
	if(algorithm==null)
		algorithm = new BPROP()
				.setDeferredAgregateFunction(new SUMMATOR());		
	
	if(strategy==null)
		strategy = new BatchGradientDescent()
				.setTrainingAlgorithm(algorithm)
				.setErrorFunction(new MSE());	
	
	if(worker==null)
		worker = new Worker();
	
	if(dataReader==null)
		dataReader = new SimpleStreamReader(new ResourceStreamWrapper(resource_training));
	
	Network.addExtLogger(new ExtLogger());	
	initialized = true;

}

private Network createDefaultNetwork(){
	try{
		if(getDefaultNetwork().equals("Interpolation")){
			activationFunctions = "SimpleSigmoidFermi";
			dataReader = new SimpleStreamReader(new ResourceStreamWrapper(resource_training));
			return 
					new Network(
							new ArrayList<List<Neuron>>(Arrays.asList(
									Network.createLayer(2),
									Network.createLayer(4, Neuron.create(activationFunctions, null)),
									Network.createLayer(3, Neuron.create(activationFunctions, null)),
									Network.createLayer(1, Neuron.create(activationFunctions, null))
									)),
							createWeightGenerator()
						);
		}else if(getDefaultNetwork().equals("XOR")){
			activationFunctions = "examples.functions.XORSigmoid";
			dataReader = new SimpleStringReader(						
				"1;1;0;\n"+
		        "0;1;1\n"+
		        "1;0;1\n"+
		        "0;0;0\n",
		        null
			);		
			return new Network().open(new ResourceStreamWrapper(resource_xor_net));
		}else if(getDefaultNetwork().equals("CUST")){
			activationFunctions = "SimpleSigmoidFermi";
			dataReader = new SimpleStringReader(						
				"",
		        null
			);		
			return new Network().open("<network><neuron>0,0, ,false</neuron><neuron>1,0,SimpleSigmoidFermi{0.1},false</neuron><synapse>0,0,1,0,-0.5,{}</synapse></network>");
		}else
			return null;
	}catch(Exception e){
		return null;
	}
}

private IGenerator createWeightGenerator(){
	try{
		if(this.initWeight.equals("R"))
			return new RandomWeightGenerator();
		else if(this.initWeight.equals("RP"))
			return new RandomPositiveWeightGenerator();
		else if(this.initWeight.equals("Z"))
			return new ZeroWeightGenerator();
		else if(this.initWeight.equals("FR")){
			if(getDefaultNetwork().equals("XOR"))
				return new DefiniteWeightGenerator(new Network().open(new ResourceStreamWrapper(resource_xor_net)));
			else 
				return new ZeroWeightGenerator();
		}
	}catch(Exception e){		
	}
	return null;
}

@Override
public void reimposta() {
	super.reimposta();
	super.clear();	
}

public singleThreadEvent getEvent() {
	return event;
}

public void setEvent(singleThreadEvent event) {
	this.event = event;
}

@Serialized(children=true)
public i_batch getWorker() {
	return worker;
}

public void setWorker(i_batch worker) {
	this.worker = worker;
}

@Serialized(children=true)
public NetworkWrapper getNetwork() {
	return wrapper;
}

public void setNetwork(NetworkWrapper wrapper) {
	this.wrapper = wrapper;
}



public float getLearningRate() {
	return learningRate;
}

public void setLearningRate(float learningRate) {
	this.learningRate = learningRate;
}

public float getLearningMomentum() {
	return learningMomentum;
}

public void setLearningMomentum(float learningMomentum) {
	this.learningMomentum = learningMomentum;
}

@Serialized
public String getActivationFunctions() {
	return activationFunctions;
}

public void setActivationFunctions(String activationFunctions) {
	this.activationFunctions = activationFunctions;
}

@Serialized
public String getStrategy() {
	return (strategy!=null)?strategy.getId()+((isParallel())?"-true":""):"";
}

@Serialized
public String getAlgorithm() {
	return (algorithm!=null)?algorithm.getId():"";
}

@Serialized(children=true)
public List<String> getSelectedn() {
	return new ArrayList<String>(selectedn.keySet());
}

@Serialized
public String getDefaultNetwork() {
	return defaultNetwork;
}

public void setDefaultNetwork(String defaultNetwork) {
	if(defaultNetwork!=null && this.defaultNetwork!=null && !this.defaultNetwork.equals(defaultNetwork)){
		this.defaultNetwork = defaultNetwork;
		this.wrapper = null;
		initController();
	}
}

@Serialized
public boolean isParallel() {
	return parallel;
}

public void setParallel(boolean parallel) {
	this.parallel = parallel;
	if(this.strategy instanceof GeneticBreeding)
		if(isParallel()) 
			((GeneticBreeding)this.strategy).setParallelLimit(10);
		else
			((GeneticBreeding)this.strategy).setParallelLimit(0);

}

@Serialized
public String getInitWeight() {
	return initWeight;
}

public void setInitWeight(String initWeight) {
	this.initWeight = initWeight;
}

@Override
public void init(HttpServletRequest request) throws bsControllerException {
	super.init(request);
	if(!initialized)
		initController();
}






}
