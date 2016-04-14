package it.sijinn.admin.components.controllers;

import it.classhidra.annotation.elements.Action;
import it.classhidra.annotation.elements.ActionMapping;


@ActionMapping (
		redirects={},
			actions={
					@Action (
							path="operator",
							redirect = "/menuCreator"
					)					
			}
		
		)



public class Mapper_redirects {

}
