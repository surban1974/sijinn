package it.sijinn.admin.components.controllers; 





import java.io.OutputStream;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionCall;
import it.classhidra.annotation.elements.Async;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Expose;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.annotation.elements.ResponseHeader;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.bsController;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.core.controller.redirects;
import it.classhidra.core.tool.exception.bsControllerException;
import it.classhidra.serialize.JsonWriter;
import it.sijinn.admin.workers.Worker;

@Action (
	path="async",
	name="model",
	redirect="/pages/async.html",
	entity=@Entity(
			property="allway:public"
	),
	Async=@Async(
		value=true
	)
)


@SessionDirective
public class ControllerAsync extends AbstractBase implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;
	private boolean stop = false;

	public ControllerAsync(){
		super();
	}

	@Override
	public redirects actionservice(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, UnavailableException, bsControllerException {
		return super.actionservice(request, response);
	}

	@ActionCall(
			name="check",
			navigated="false",
			Redirect=@Redirect(contentType="text/event-stream"),
			Expose=@Expose(methods = {Expose.POST,Expose.GET}),
			Async=@Async(
					value=true,
					flushBuffer=true,
					timeout=0,
					loopEvery=5000,
					headers = {
							@ResponseHeader(name="Cache-Control",value="no-cache"),
							@ResponseHeader(name="Connection",value="keep-alive")
							}
					)
			)
	public String check(HttpServletRequest request){
		
		i_bean bean = null;
		try{
			bean = bsController.getFromInfoNavigation("network", request).get_content();
		}catch(Exception e){			
		}
		if(bean!=null){
			ControllerNetwork network = (ControllerNetwork)bean;
			return JsonWriter.object2json(((Worker)network.getWorker()).getStep(), "step");
			
		}
		
		return "{step:0}";
	}	
	
	@ActionCall(
			name="thread",
			navigated="false",
			Expose=@Expose(methods = {Expose.POST,Expose.GET}))
	public void thread(HttpServletRequest request, HttpServletResponse response){
		
		while(!stop){
			i_bean bean = null;
			try{
				bean = bsController.getFromInfoNavigation("network", request).get_content();
			}catch(Exception e){			
			}
			if(bean!=null){
				ControllerNetwork network = (ControllerNetwork)bean;
				
				try {
					OutputStream os = response.getOutputStream();
					os = response.getOutputStream();
					os.write(JsonWriter.object2json(((Worker)network.getWorker()).getStep(), "step").getBytes());
					os.flush();
					
				} catch (Exception e) {

				}
				

				
			}
			try{
				Thread.sleep(10000);
			}catch(Exception e){
				
			}
		
		}
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}		

}
