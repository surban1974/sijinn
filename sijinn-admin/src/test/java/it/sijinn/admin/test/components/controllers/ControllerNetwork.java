package it.sijinn.admin.test.components.controllers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;

import it.classhidra.core.controller.bsController;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.init.app_init;
import it.classhidra.core.init.log_init;


public class ControllerNetwork {

	@Before
	public void setUp() throws Exception {
		bsController.isInitialized(
				new Properties(){
					private static final long serialVersionUID = 1L;

				{
					put(app_init.id_path,"sijinn-admin");
					put(app_init.id_extention_do,"");
					put(app_init.id_actioncall_separator,"-");
					put(app_init.id_external_loader,"it.sijinn.admin.loaders.ExternalLoader");
					put(app_init.id_package_annotated,"it.sijinn.admin.components");
				}},
				new HashMap<String, Properties>(){
					private static final long serialVersionUID = 1L;

				{
					put(log_init.id_property,
						new Properties(){
							private static final long serialVersionUID = 1L;

						{
							put(log_init.id_Write2Concole,"true");
						}}						
					);
				}}
		);
	  } 
	
	@Test
	public void testNetworkInstance() {
		i_action action = null;
		try{
			action = bsController.getActionInstance("network", null, null);
		}catch(Exception e){
			
		}
		
		Assert.assertNotNull(action);
		Assert.assertNotNull(action.getCurrent_redirect());
		Assert.assertEquals(action.getCurrent_redirect().get_uri(),"/pages/network.html");
		
		assertThat(action, instanceOf(it.sijinn.admin.components.controllers.ControllerNetwork.class));
		
		it.sijinn.admin.components.controllers.ControllerNetwork network = (it.sijinn.admin.components.controllers.ControllerNetwork)action;
		
		Assert.assertNotNull(network.getNetwork());
		Assert.assertNotNull(network.getNetwork().obtainInstance());
		Assert.assertEquals(network.getDefaultNetwork(),"Interpolation");
		
		Assert.assertEquals(network.getNetwork().obtainInstance().getNeurons().length,10);
		Assert.assertEquals(network.getNetwork().obtainInstance().getSynapses().length,23);
		
		network.change("type", "XOR");
		Assert.assertEquals(network.getNetwork().obtainInstance().getNeurons().length,5);
		Assert.assertEquals(network.getNetwork().obtainInstance().getSynapses().length,6);	
		
		network.change("type", "CUST");
		Assert.assertEquals(network.getNetwork().obtainInstance().getNeurons().length,2);
		Assert.assertEquals(network.getNetwork().obtainInstance().getSynapses().length,1);		
		
	}
	
	
	@Test
	public void testNetworkCallInstance() {
		i_action action = null;
		try{
			action = bsController.getActionInstance("network","json", null, null);
		}catch(Exception e){
			
		}
		Assert.assertNotNull(action);
		Assert.assertNotNull(action.getCurrent_redirect());
		Assert.assertNotNull(action.getCurrent_redirect().getWrapper());
		Assert.assertNotNull(action.getCurrent_redirect().getWrapper().getContent());
		
	}
	
	@After
	public void clean() throws Exception {
		bsController.clean();
	}

}
