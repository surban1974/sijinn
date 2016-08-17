<%
	String panel_id = (request.getParameter("panel-id")==null)?"":request.getParameter("panel-id");
	String panel_description = (request.getParameter("panel-description")==null)?"":request.getParameter("panel-description");
	String panel_position = request.getParameter("panel-position");
	String panel_top = request.getParameter("panel-top");
	String panel_left = request.getParameter("panel-left");
	String panel_width = request.getParameter("panel-width");
	String panel_height = request.getParameter("panel-height");
	String panel_div_style = request.getParameter("panel-style");
	String panel_table_style = request.getParameter("panel-table-style");
	
	String panel_visibility = request.getParameter("panel-visibility");
	String panel_display = request.getParameter("panel-display");
	
	
	boolean show_header = (request.getParameter("panel-show-header")==null)?false:true;
	if(show_header && request.getParameter("panel-show-header").equals("false")) show_header=false;
	boolean show_close_button = (request.getParameter("panel-show-close-button")==null)?false:true;
	if(show_close_button && request.getParameter("panel-show-close-button").equals("false")) show_close_button=false;

	boolean block_close_button = (request.getParameter("panel-block-close-button")==null)?false:true;
	if(block_close_button && request.getParameter("panel-block-close-button").equals("false")) block_close_button=false;
	
	
	boolean show_max_button = (request.getParameter("panel-show-max-button")==null)?false:true;
	if(show_max_button && request.getParameter("panel-show-max-button").equals("false")) show_max_button=false;

	if(panel_div_style == null) panel_div_style="";
	if(panel_description == null || panel_description.equals("null")) panel_description=" ";
	
	if(panel_position!=null){
		panel_div_style+="position: "+panel_position+";";
		if(panel_top!=null) panel_div_style+="top :"+panel_top+";";
		if(panel_left!=null) panel_div_style+="left :"+panel_left+";";
		if(panel_width!=null) panel_div_style+="width :"+panel_width+";";
		if(panel_height!=null) panel_div_style+="height :"+panel_height+";";
	}
	if(panel_visibility!=null) panel_div_style+="visibility :"+panel_visibility+";";
	else panel_div_style+="visibility :visible;";
	if(panel_display!=null) panel_div_style+="display :"+panel_display+";";
	else panel_div_style+="display :block;";
	
	String panel_onclose = (request.getParameter("panel-onclose")==null)?"document.getElementById('"+panel_id+"').style.visibility='hidden';":request.getParameter("panel-onclose");
	panel_onclose="try{JSBeforeClose_popup();}catch(e){};try{closePanel('"+panel_id+"')}catch(e){};"+panel_onclose;
%>


<div id="<%=panel_id%>"  style="z-index:10000;  <%=panel_div_style %>">
<table cellspacing="0" cellpadding="0"  style="padding: 1px; border: solid 1px silver;<%=panel_table_style %>" class="asPanel">
<%if(show_header){%>	
	<tr >						
		<td  style="background-image:url('images/corners/panel_t.gif')"> 
		
			<table width="100%" cellspacing="0" cellpadding="0" >
			<tr >
			<td width="1%" style="font-family: monospace; font-size: 15px; color: gray;background-image:url('images/corners/panel_t.gif');">&nbsp;</td>
			<td align="left" width="100%"
				style="font-family: monospace; font-size: 15px; color: rgb(51,51,51);text-shadow: 1px 1px 1px silver; background-image:url('images/corners/panel_t.gif'); " 
			>
			<span style="filter: alpha(opacity=100);opacity: 1;"><nobr id="content_desc_<%=panel_id%>" ><b><%=panel_description %></b></nobr></span>
			</td>
		
<%if(show_close_button){%>	
			<td id="<%=panel_id%>_img_minimize" class="buttonAsJsSmall" align="center" valign="top"  width="22px" style="width:22px;display:none;">
				<img src="images/corners/minimize.gif" border="0" 
				onclick="showAsPanelNormal('<%=panel_id%>');try{afterShowAsPanelNormal('<%=panel_id%>');}catch(e){}">
			</td>
			<td id="<%=panel_id%>_img_maximize" class="buttonAsJsSmall" align="center" valign="top" width="22px" style="width:22px">
				<img src="images/corners/maximize.gif" border="0" 
				onclick="showAsPanelMax('<%=panel_id%>');try{afterShowAsPanelMax('<%=panel_id%>');}catch(e){}">
			</td>
<%if(!block_close_button){%>				
		
			<td id="<%=panel_id%>_img_close" class="buttonAsJsSmall" align="center" valign="top"  width="22px" style="width:22px">
				<img src="images/corners/close.gif" border="0" 
				onclick="<%=panel_onclose %>">
			</td>
<%} %>
<%} %>
			</tr>
			</table>
		</td>
	</tr>				
<%} %>			
	<tr >						
		<td>
