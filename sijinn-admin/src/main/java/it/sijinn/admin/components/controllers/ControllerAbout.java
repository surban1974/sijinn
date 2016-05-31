package it.sijinn.admin.components.controllers; 




import java.io.Serializable;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.SessionDirective;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;

@Action (
	path="about",
	name="model",
	redirect="/pages/about.html",
	entity=@Entity(
			property="allway:public"
	) 
)

@SessionDirective
public class ControllerAbout extends AbstractBase implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;

public ControllerAbout(){
	super();
}




}
