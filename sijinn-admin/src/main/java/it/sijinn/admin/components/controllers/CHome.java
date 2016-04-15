package it.sijinn.admin.components.controllers; 




import java.io.Serializable;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionMapping;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
@ActionMapping (
		redirects={
				@Redirect(	
						path="/pages/home.html",
						descr="Home",
						mess_id="title_fw_Home"
				)
				
			},
		actions={
				@Action (
						path="home",
						name="formHome",
						redirect="/pages/home.html",
//						navigated="true",
//						memoryInSession="false",
						reloadAfterAction="true",
						entity=@Entity(
								property="allway:public"
						)
				)
		}
)



@SessionDirective
public class CHome extends CBase implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;

public CHome(){
	super();
}




}
