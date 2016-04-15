<%@ taglib uri="/WEB-INF/tlds/bsController.tld" prefix="bs" %>
<%
String go_action = request.getParameter("go-action");
if(go_action==null) go_action = "public";

%>
<bs:input type="hidden" name="current_section"/>
					<table>
						<tr>
<bs:notEqual  name="elements_sections.size" value="0">
<bs:notEqual  name="current_section" value="1">						
							<td style=" cursor: pointer; "
								onclick="goAction('<%=go_action%>?middleAction=change_section&current_section='+(<bs:formelement name="current_section"/>-1))">
								<img src="images/decor_ws/16_leftarrow.gif">
							</td>
</bs:notEqual>	
</bs:notEqual>					
							<bs:sequence name="elements_sections">	
							<bs:bean name="current_s" property="elements_sections" index="sequence"/>
<bs:equal bean="current_s" name="desc" value="true">							
							<td style="border-style: solid;border-width: 1px;"><bs:formelement bean="current_s" name="code"/>
							</td>
</bs:equal>	
<bs:equal bean="current_s" name="desc" value="false">							
							<td class="colTab" style=" cursor: pointer; border-style: solid;border-width: 1px; "
								onclick="goAction('<%=go_action%>?middleAction=change_section&current_section=<bs:formelement bean="current_s" name="code"/>')">
								<font color="white"><bs:formelement bean="current_s" name="code"/></font>
							</td>
</bs:equal>							
							</bs:sequence>
<bs:notEqual  name="elements_sections.size" value="0">		
<bs:notEqual  name="elements_sections.size"  field="current_section">							
					<td style=" cursor: pointer; "
								onclick="goAction('<%=go_action%>?middleAction=change_section&current_section='+(<bs:formelement name="current_section"/>+1))">
								<img src="images/decor_ws/16_rightarrow.gif">
							</td>
</bs:notEqual>	
</bs:notEqual>								
						</tr>
					</table>