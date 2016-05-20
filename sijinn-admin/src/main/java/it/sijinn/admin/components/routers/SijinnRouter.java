package it.sijinn.admin.components.routers;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import it.classhidra.core.controller.bsController;
import it.classhidra.core.controller.bsFilter;
import it.classhidra.core.init.app_init;
import it.classhidra.core.init.log_init;

@WebFilter(
		filterName="SijinnRouter",
		value={
				"/actions/*",
				"/*"
		},
		initParams={
				@WebInitParam(name="CharacterEncoding",value="UTF-8"),
				@WebInitParam(name="ExcludedUrl",value="/js/;/css/;/images/;"),
				@WebInitParam(name="ExcludedPattern",value="^(?!.*/neohort/).*\\.jsp$"),
				@WebInitParam(name="RestSupport",value="true")
		},
		
		dispatcherTypes={
				DispatcherType.REQUEST,
				DispatcherType.FORWARD,
				DispatcherType.INCLUDE
		}
	)
public class SijinnRouter extends bsFilter{

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws ServletException, IOException {
	

		if(req!=null && req instanceof HttpServletRequest && resp!=null && resp instanceof HttpServletResponse){
		

			
/*			
			final Properties appInitProperty = new Properties();
				appInitProperty.setProperty(app_init.id_path,"sijinn-admin");
				appInitProperty.setProperty(app_init.id_extention_do,"");
				appInitProperty.setProperty(app_init.id_actioncall_separator,"-");
				appInitProperty.setProperty(app_init.id_external_loader,"it.sijinn.admin.loaders.ExternalLoader");
				appInitProperty.setProperty(app_init.id_package_annotated,"it.sijinn.admin.components");
				
			final Properties logInitProperty = new Properties();
				logInitProperty.setProperty(log_init.id_Write2Concole,"true");
			
			final HashMap<String, Properties> otherProperties = new HashMap<String, Properties>();
				otherProperties.put(log_init.id_property, logInitProperty);
			
			bsController.isInitialized(appInitProperty,otherProperties);
*/
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


			super.doFilter(req, resp, chain);
		}else{
            chain.doFilter(req, resp);
            return;			
		}
	}

}
