package it.sijinn.admin.components.controllers; 




import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionCall;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Expose;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.bsController;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.core.controller.redirects;
import it.classhidra.serialize.JsonReader2Map;
import it.classhidra.serialize.JsonWriter;
import it.classhidra.serialize.Serialized;
import it.sijinn.admin.beans.SettingProperties;




@Action (
	path="settings",
	name="model",
	memoryInSession="true",
	entity=@Entity(
		property="allway:public"
	),
	redirects={
		@Redirect(auth_id="k-settings",path="/pages/k-settings.html",avoidPermissionCheck="true"),
		@Redirect(auth_id="a-settings",path="/pages/a-settings.html",avoidPermissionCheck="true")
	}
)

@SessionDirective
public class ControllerSettings extends AbstractBase implements i_action, i_bean, Serializable{
	private static final long serialVersionUID = 1L;
	
	protected SettingProperties settings;


	public ControllerSettings(){
		super();
	}
	
	@ActionCall(name="k",navigated="false",Expose=@Expose(method = Expose.GET))
	public redirects enterpoint_k(){
		return new redirects(get_infoaction().getRedirect("k-settings"));
	}
	
	@ActionCall(name="a",navigated="false",Expose=@Expose(method = Expose.GET))
	public redirects enterpoint_a(){
		return new redirects(get_infoaction().getRedirect("a-settings"));
	}	

	@ActionCall(
			name="save",
			navigated="false",
			Redirect=@Redirect(contentType="application/json"),
			Expose=@Expose(method = Expose.POST)
	)
	public String save(HttpServletRequest request, HttpServletResponse response){
		clear();
		if(getSettings()!=null){
			try{
				if(getSettings().save())
					setSuccess("The 'Settings property' saved successfully.");
				else
					setError("There were problems with 'Save' operation.");
			}catch(Exception e){
				setError("There were problems with operation: "+e.toString());
			}
		}
		String json = super.modelAsJson(request, response);
		clear();
		return json;
		
	}
	
	@ActionCall(
			name="load",
			navigated="false",
			Redirect=@Redirect(contentType="application/json"),
			Expose=@Expose(method = Expose.POST)
	)
	public String load(HttpServletRequest request, HttpServletResponse response){
		clear();
		setSettings(new SettingProperties());
		setSuccess("The 'Settings property' loaded successfully.");
		String json = super.modelAsJson(request, response);
		clear();
		return json;
		
	}	
	
	@ActionCall(
			name="diff",
			navigated="false",
			Redirect=@Redirect(contentType="application/json"),
			Expose=@Expose(method = Expose.POST))
	public String diffAsJson(HttpServletRequest request, HttpServletResponse response){
		
		String modelName = getString("outputserializedname");
		if(modelName==null || modelName.equals(""))
			modelName = request.getParameter("outputserializedname");
		
		String outputappliedfor = getOutputappliedfor();
		if(outputappliedfor==null || outputappliedfor.equals(""))
			outputappliedfor = request.getParameter("outputappliedfor");
		if(outputappliedfor!=null && !outputappliedfor.trim().equals(""))
			return JsonWriter.object2json(get_bean().asBean().get(outputappliedfor), (modelName==null || modelName.equals(""))?outputappliedfor:modelName);
		

		if(modelName==null || modelName.equals("")){
			if(get_infobean()!=null && get_infobean().getName()!=null && !get_infobean().getName().equals(""))
				modelName = get_infobean().getName();
			else if(get_infoaction()!=null && get_infoaction().getName()!=null && !get_infoaction().getName().equals(""))
				modelName = get_infoaction().getName();
			else
				modelName = "model";
		}
		
		byte[] datas = (byte[])request.getAttribute(bsController.CONST_RECOVERED_REQUEST_CONTENT);
		if(datas!=null){
			String json = new String(datas);
			Map<String,?> mapped = new JsonReader2Map().mapping(null, json, null);
			List<String> parameters = null;
			if(mapped!=null && mapped.size()>0){
				parameters = new ArrayList<String>();
				Iterator<String> it = mapped.keySet().iterator();
				while(it.hasNext())
					parameters.add(it.next().toString());
			}
			
			return 
					JsonWriter.object2json(
							this.get_bean(),
							modelName,
							parameters);
		}else
			return 
					JsonWriter.object2json(
							this.get_bean(),
							modelName
							);
	}	
	
	
	@Serialized
	public SettingProperties getSettings() {
		return settings;
	}
	
	public void setSettings(SettingProperties settings) {
		this.settings = settings;
	}

	@Override
	public void reimposta() {
		super.reimposta();
		
		if(this.settings==null)
			this.settings = new SettingProperties();

	}








}
