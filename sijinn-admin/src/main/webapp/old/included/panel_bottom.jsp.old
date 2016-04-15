<%
String mess_id = request.getParameter("mess-id");
if(mess_id==null) mess_id = (String)request.getAttribute("mess-id");
String mess_description = request.getParameter("mess-description");
if(mess_description==null) mess_description = (String)request.getAttribute("mess-description");

if(mess_description==null && mess_id!=null){
	mess_description = it.classhidra.core.controller.bsController.writeLabel(request,mess_id,mess_id,null);
}
%></td>
		<td style="background-image:url('images/corners/normal/corner_r.gif');filter: alpha(opacity=65);opacity: 0.65;" width="6"  height="4"><img src="images/decor/blank_trans.gif" border="0" width="6"></td>
	</tr>
			
	<tr >						
		<td style="background-image:url('images/corners/normal/corner_b_l.gif');filter: alpha(opacity=65);opacity: 0.65;background-repeat: no-repeat" width="4"  height="6"></td>
		<td style="background-image:url('images/corners/normal/corner_b.gif');filter: alpha(opacity=65);opacity: 0.65; background-repeat:repeat-x;"  height="6"><%if(mess_description!=null){ %>		
		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td  width="50%">&nbsp;</td>
				<td  width="0%"  height="20"><img src="images/corners/vb_left.gif" border="0"/></td>
				<td style="background-image:url('images/corners/vb_center.gif')" align="center" width="0%" ><nobr><span class="title_section"><%=mess_description%></span></nobr></td>
				<td  width="0%"  height="20"><img src="images/corners/vb_right.gif" border="0"/></td>
				<td  width="50%">&nbsp;</td>
			</tr>
		</table>
<%} %></td> 
		 
		 
		<td style="background-image:url('images/corners/normal/corner_b_r.gif');filter: alpha(opacity=65);opacity: 0.65;background-repeat: no-repeat"  width="6" height="6"></td>
	</tr>		
				
</table>
</div><%
request.setAttribute("mess-id",null);
request.setAttribute("mess-description",null);
%>


