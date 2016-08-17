<%
String mess_id = request.getParameter("mess-id");
if(mess_id==null) mess_id = (String)request.getAttribute("mess-id");
String mess_description = request.getParameter("mess-description");
if(mess_description==null) mess_description = (String)request.getAttribute("mess-description");

if(mess_description==null && mess_id!=null){
	mess_description = it.classhidra.core.controller.bsController.writeLabel(request,mess_id,mess_id,null);
}
%>		</td>
	</tr>
			
				
</table>
</div><%
request.setAttribute("mess-id",null);
request.setAttribute("mess-description",null);
%>


