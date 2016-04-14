package it.sijinn.admin.components.controllers; 




import java.io.Serializable;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionMapping;
import it.classhidra.annotation.elements.Entity;
import it.classhidra.annotation.elements.Redirect;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
@ActionMapping (
		redirects={
				@Redirect(	
						path="/pages/content.html",
						descr="Content",
						mess_id="title_fw_Content"
				)
				
			},
		actions={
				@Action (
						path="content",
						name="formContent",
						redirect="/pages/content.html",
						navigated="true",
						memoryInSession="false",
						reloadAfterAction="true",
						entity=@Entity(
								property="allway:public"
						)
				)
		}
)




public class Content extends CBase implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;

public Content(){
	super();
}

//
//
//public redirects actionservice(HttpServletRequest request, HttpServletResponse response) throws ServletException, UnavailableException, bsControllerException {
//	return new redirects(get_infoaction().getRedirect());			
//
//}


}
