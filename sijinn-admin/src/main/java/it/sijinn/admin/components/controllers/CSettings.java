package it.sijinn.admin.components.controllers; 




import java.io.Serializable;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.serialize.Serialized;
import it.sijinn.admin.beans.SettingProperties;

@Action (
	path="settings",
	name="model",
	redirect="/pages/settings.html",
	memoryInSession="true",
	entity=@Entity(
			property="allway:public"
	)
)
@SessionDirective
public class CSettings extends CBase implements i_action, i_bean, Serializable{
	private static final long serialVersionUID = 1L;
	
	private SettingProperties settings;


	public CSettings(){
		super();
	}

/*	
	@ActionCall(
			path="serialized",
			name="jsonlocal",
			navigated="false",
			Redirect=@Redirect(contentType="application/json"),
			Expose=@Expose(method = Expose.POST)
	)
	public String modelAsJsonPost(HttpServletRequest request, HttpServletResponse response){
		return super.modelAsJson(request, response);
	}
	
	@ActionCall(
			path="serialized",
			name="jsonlocal",
			navigated="false",
			Redirect=@Redirect(contentType="application/json"),
			Expose=@Expose(
					method = Expose.GET,
					restmapping = {
							@Rest(path="/settings/model/json/",parametermapping="/modelname/"),
							@Rest(path="/settings/model/json/",parametermapping="/"),
							@Rest(path="/settings/model/json/",parametermapping="/modelname/branch/"),
					}
			)
	)
	public String modelAsJsonGet(HttpServletRequest request, HttpServletResponse response){
		return super.modelAsJson(request, response);
	}	
*/
	
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
