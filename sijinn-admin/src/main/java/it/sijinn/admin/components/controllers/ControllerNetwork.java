package it.sijinn.admin.components.controllers; 




import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionCall;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Expose;
import it.classhidra.annotation.elements.NavigatedDirective;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.core.controller.redirects;
import it.classhidra.core.tool.exception.bsControllerException;

import it.classhidra.scheduler.scheduling.db.db_batch;
import it.classhidra.scheduler.scheduling.db.db_batch_log;
import it.classhidra.scheduler.scheduling.thread.singleThreadEvent;
import it.sijinn.admin.beans.NetworkWrapper;
import it.sijinn.admin.workers.Worker_INTER;
import it.sijinn.common.Network;
import it.sijinn.common.Neuron;
import it.sijinn.perceptron.algorithms.BPROP;
import it.sijinn.perceptron.algorithms.ITrainingAlgorithm;
import it.sijinn.perceptron.functions.applied.SimpleSigmoidFermi;
import it.sijinn.perceptron.functions.deferred.SUMMATOR;
import it.sijinn.perceptron.functions.error.MSE;
import it.sijinn.perceptron.functions.generator.RandomPositiveWeightGenerator;
import it.sijinn.perceptron.strategies.BatchGradientDescent;
import it.sijinn.perceptron.strategies.ITrainingStrategy;
import it.classhidra.scheduler.common.i_batch;
import it.classhidra.serialize.JsonWriter;
import it.classhidra.serialize.Serialized;

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
	
	private singleThreadEvent event;
	
	private i_batch worker;
	
	private NetworkWrapper wrapper;
	
	private ITrainingStrategy strategy;
	
	private ITrainingAlgorithm algorithm;
	
	private float learningRate = 0.5f;
	private float learningMomentum = 0.01f;

	
public ControllerNetwork(){
	super();
}

@Override
public redirects actionservice(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, UnavailableException, bsControllerException {
	
	
	if(wrapper==null){
		wrapper = new NetworkWrapper(new Network(
			new ArrayList<List<Neuron>>(Arrays.asList(
					Network.createLayer(2),
					Network.createLayer(4, new SimpleSigmoidFermi()),
					Network.createLayer(3, new SimpleSigmoidFermi()),
					Network.createLayer(1, new SimpleSigmoidFermi())
					)),
			new RandomPositiveWeightGenerator()
		));
	}
	
	if(algorithm==null){
		algorithm = new BPROP()
				.setLearningRate(learningRate)
				.setLearningMomentum(learningMomentum)
				.setDeferredAgregateFunction(new SUMMATOR());
	}	
	
	if(strategy==null){
		strategy = new BatchGradientDescent(algorithm)
				.setErrorFunction(new MSE());
	}
	

	
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
		name="stop",
		navigated="false",
		Redirect=@Redirect(contentType="application/json"),
		Expose=@Expose(methods = {Expose.POST,Expose.GET}))
public String stop(){
	if(event!=null && event.getWorker()!=null){
		((Worker_INTER)event.getWorker()).setForcedStop(true);
		event.interrupt();
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
	worker = new Worker_INTER()
			.setNetwork(wrapper.obtainInstance())
			.setTrainingStrategy(strategy)
			.setApproximation(0.000001f);
	event = new singleThreadEvent(new db_batch(), new db_batch_log(), worker);
	event.start();	
	return JsonWriter.object2json(this.get_bean(), "model");
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

@Override
public void reimposta() {
	super.reimposta();
	super.clear();
}



}
