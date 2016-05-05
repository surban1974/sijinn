package it.sijinn.admin.components.routers;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import it.classhidra.core.controller.bsFilter;

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
		super.doFilter(req, resp, chain);
	}

}
