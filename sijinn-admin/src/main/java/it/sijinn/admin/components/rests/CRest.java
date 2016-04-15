package it.sijinn.admin.components.rests; 




import java.io.Serializable;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionMapping;
import it.classhidra.annotation.elements.Expose;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.annotation.elements.Rest;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.action;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
@ActionMapping (
		actions={
				@Action (
						path="restnetworks",
						name="networks",
						Redirect=@Redirect(contentType="application/json"),
						Expose =@Expose(
									method="GET",
									restmapping={
											@Rest(path="/rest/networks/")
									})
						
				)
		}
)



@SessionDirective
public class CRest extends action implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;

public CRest(){
	super();
}




}
