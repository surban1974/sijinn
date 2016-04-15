<%@ taglib uri="/WEB-INF/tlds/bsController.tld" prefix="bs" %>
<%@ page import="java.util.*" %> 
<%@ page import="it.classhidra.core.init.*" %>
<%@ page import="it.classhidra.core.controller.*" %>
<%@ page import="it.classhidra.core.tool.exception.*" %>

<!-- view: <%=this.getClass().getName()%> -->
<!-- build <%=bsController.getInfoVersion("it.brandart.version") %> -->
<!-- framework <%=bsController.getInfoVersion("it.classhidra.version") %> -->
<%  
	
	i_action 		formAction 			= (request.getAttribute(bsController.CONST_BEAN_$INSTANCEACTION)==null)?new action():(i_action)request.getAttribute(bsController.CONST_BEAN_$INSTANCEACTION);
	info_action		formInfoAction 		= formAction.get_infoaction();
	i_bean 			formBean 			= formAction.get_bean();
	redirects 		formRedirect 		= formAction.getCurrent_redirect();
	info_navigation	formInfoNavigation	= bsController.getFromInfoNavigation(null, request);
	if(formInfoNavigation==null) formInfoNavigation = new info_navigation();
	formInfoNavigation.decodeMessage(request);
	auth_init 		userInfo			= (auth_init)request.getSession().getAttribute(bsController.CONST_BEAN_$AUTHENTIFICATION);
	int shw=0;
	if(formInfoAction!=null) shw++;
	if(formBean!=null) shw++;
	if(formRedirect!=null) shw++;
	if(formInfoNavigation!=null) shw++;
	if(userInfo!=null) shw++;
	
	new Integer(shw).toString();

	
%>


<head>    
    <title><bs:message code="application_title" defaultValue="Application "/></title>
    <link rel="SHORTCUT ICON" href="images/application.ico">
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="description" content="<bs:message code="application_title" defaultValue="Application "/>">
	

	
	
</head>



<script type="text/javascript" src="Controller?$action=bsLoadFromFramework&src=javascript/&type=text/javascript&cache=10000&vrsn=<%=bsController.getInfoVersion("it.classhidra.version") %>"></script>
<script type="text/javascript" src="javascript/loader.js"></script>


<script>
	dhtmlLoadCss("css/Pagine1024.css?vrsn=<%=bsController.getInfoVersion("it.brandart.version") %>");
</script>





