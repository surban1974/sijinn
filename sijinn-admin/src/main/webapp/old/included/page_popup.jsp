<%
	String panel_width = request.getParameter("panel-width");
	String panel_height = request.getParameter("panel_height");
	String panel_div_style = "";
	if(panel_width!=null) panel_div_style+="width :"+panel_width+";";
	if(panel_height!=null) panel_div_style+="height :"+panel_height+";";
	String panel_onclose = request.getParameter("panel-onclose");
	if(panel_onclose==null) panel_onclose="document.getElementById('content_Panel_Popup').style.display='none';";
%><center>

<div id="content_Panel_Popup" 
style="z-index: 9999; position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: none;"
>

<table border="0" width="100%" height="100%" style="z-index:99999">
<tr>

<td align="center" >

<jsp:include page="../included/panel_top.jsp">
	<jsp:param name="panel-show-header" value="true" />
	<jsp:param name="panel-show-close-button" value="true" /> 
	<jsp:param name="panel-description" value="" />	
	<jsp:param name="panel-id" value="panel_Popup" />
	<jsp:param name="panel-onclose" value="<%=panel_onclose%>" />	
</jsp:include>	

<div id="content_body_Panel_Popup" style=" z-index: 999; background-color: white; vertical-align: middle; <%=panel_div_style %>">
</div>

<jsp:include page="../included/panel_bottom.jsp"/>
</td>

</tr>
</table>
<div id="content_Panel_Popup_opacity" 
style="z-index: -1; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-image: url('images/decor/blank.gif'); filter: alpha(opacity=50);opacity: 0.5; "
></div>
</div>
</center>
<script>

ajustPanel();
window.onresize = function() {ajustPanel();}
</script>