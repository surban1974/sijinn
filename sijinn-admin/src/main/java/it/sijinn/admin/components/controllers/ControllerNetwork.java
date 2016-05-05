package it.sijinn.admin.components.controllers; 




import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.applied.IFunctionApplied;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.RandomPositiveWeightGenerator;
import it.sijinn.perceptron.strategies.BatchGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
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
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String check(){
	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="change",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String change(@Parameter(name="type") String type, @Parameter(name="value") String value ){
	clear();
	if(type==null){
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
			ITrainingStrategy cur_strategy = Network.createStrategyById(value, null);
			if(cur_strategy!=null){
				cur_strategy.setErrorFunction(new MSE());
				this.strategy = cur_strategy;
			}
			if(algorithm!=null)
				this.strategy.setTrainingAlgorithm(algorithm);
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
		navigated="false",
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
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String restart(){
	if(event!=null)
		event.interrupt();	
	wrapper.obtainInstance().clearSynapses(new RandomPositiveWeightGenerator(),true);
	
	if(worker==null)
		worker = new Worker();
	
	if(strategy!=null)
		strategy
			.setTrainingAlgorithm(algorithm)
			.setErrorFunction(new MSE());
		
	((Worker)worker)
		.setStep(0)
		.setDelta(0)
		
		.setNetwork(wrapper.obtainInstance().clearSynapses(new RandomPositiveWeightGenerator(), true))
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
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String exec(){
	if(wrapper!=null){
		wrapper.obtainInstance().calculation();
	}

	return JsonWriter.object2json(this.get_bean(), "model");
}


@ActionCall(
		name="addlayer",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String addlayer(){
	
	if(wrapper!=null)
		wrapper.obtainInstance()
//		.removeSynapses()
		.insertLayer(wrapper.obtainInstance().getLayers().size()-2, 1, Neuron.create(activationFunctions, null), false,true)
//		.createSynapses(0)
		;
	

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removelayer",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removelayer(@Parameter(name="layer") int layer){
	
	if(wrapper!=null)
		wrapper.obtainInstance()
//		.removeSynapses()
		.removeLayer(layer,true)
//		.createSynapses(0)
		;
	

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="addneuron",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String addneuron(@Parameter(name="layer") int layer){
	
	if(wrapper!=null){
		if(layer>=0 && layer<wrapper.obtainInstance().getLayers().size()){
			List<Neuron> neurons = wrapper.obtainInstance().getLayers().get(layer);
			Neuron neuron = new Neuron(wrapper.obtainInstance(),  Neuron.create(activationFunctions, null), layer, neurons.size(), false) ;
			wrapper.obtainInstance()
//			.removeSynapses()
			.addNeuron(neuron, true, true)
//			.createSynapses(0)
			;
		}
	}
	

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="removeneuron",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String removeneuron(@Parameter(name="layer") int layer, @Parameter(name="order") int order){
	
	if(wrapper!=null){
		if(layer>=0 && layer<wrapper.obtainInstance().getLayers().size()){
			List<Neuron> neurons = wrapper.obtainInstance().getLayers().get(layer);
			Neuron neuron = new Neuron(wrapper.obtainInstance(),  Neuron.create(activationFunctions, null), layer, neurons.size()-1, false) ;
			wrapper.obtainInstance()
//			.removeSynapses()
			.removeNeuron(neuron, true)
//			.createSynapses(0)
			;
		}
	}
	

	return JsonWriter.object2json(this.get_bean(), "model");
}


@ActionCall(
		name="diff",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String diffAsJson(){
	return "";
}	


@ActionCall(
		name="viewdata",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.GET))
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
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.GET))
public void setdata(@Parameter(name="data") String data){
	
	if(data!=null){
		dataReader = new SimpleStringReader(data, null);
	}

}


@ActionCall(
		name="reset",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String reset(){
	

		readLinesAggregator = new SimpleLineDataAggregator(";");	
		
		
		learningRate = 0.5f;
		learningMomentum = 0.01f;
		activationFunctions = "SimpleSigmoidFermi";
	
	
		wrapper = new NetworkWrapper(new Network(
			new ArrayList<List<Neuron>>(Arrays.asList(
					Network.createLayer(2),
					Network.createLayer(4, Neuron.create(activationFunctions, null)),
					Network.createLayer(3, Neuron.create(activationFunctions, null)),
					Network.createLayer(1, Neuron.create(activationFunctions, null))
					)),
			new RandomPositiveWeightGenerator()
		));
	
	
		algorithm = new BPROP()
				.setDeferredAgregateFunction(new SUMMATOR());
		
	
		strategy = new BatchGradientDescent()
				.setTrainingAlgorithm(algorithm)
				.setErrorFunction(new MSE());
	
	
		worker = new Worker();
	
		dataReader = new SimpleStreamReader(new ResourceStreamWrapper(resource_training));

	

	return JsonWriter.object2json(this.get_bean(), "model");
}

@ActionCall(
		name="viewnetwork",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.GET))
public String viewnetwork(){
	
	String result = ""; 
	if(wrapper!=null && wrapper.obtainInstance()!=null)
		result = wrapper.obtainInstance().save();
	
	result = result.replace("\n", "\\\\n").replace("\r", "\\\\r").replace("\t", "\\\\t").replace("\"", "\\\"");
	
	return JsonWriter.object2json(result, "network", null,true,1);
}	

@ActionCall(
		name="updatenetwork",
		navigated="false",
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
		}
	}
	
	return JsonWriter.object2json(this.get_bean(), "model");
}	

@ActionCall(
		name="updatedata",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(method = Expose.POST))
public String updatedata(@Parameter(name="data") String updatedata){

	if(updatedata!=null)
		dataReader = new SimpleStringReader(updatedata,null);
	
	return JsonWriter.object2json(this.get_bean(), "model");
}


@Override
public String modelAsJson(HttpServletRequest request, HttpServletResponse response) {
	initController();
	return super.modelAsJson(request, response);
}

public void initController(){
	if(wrapper==null)
		wrapper = new NetworkWrapper(new Network(
			new ArrayList<List<Neuron>>(Arrays.asList(
					Network.createLayer(2),
					Network.createLayer(4, Neuron.create(activationFunctions, null)),
					Network.createLayer(3, Neuron.create(activationFunctions, null)),
					Network.createLayer(1, Neuron.create(activationFunctions, null))
					)),
			new RandomPositiveWeightGenerator()
		));
	
	
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
	return (strategy!=null)?strategy.getId():"";
}

@Serialized
public String getAlgorithm() {
	return (algorithm!=null)?algorithm.getId():"";
}




}
