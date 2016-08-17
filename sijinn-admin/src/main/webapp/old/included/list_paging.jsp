<%@ taglib uri="/WEB-INF/tlds/bsController.tld" prefix="bs" %>
<span>
<table>
		<tr>
			<td align="center" class="page_section">
				<table >
					<tr>
			<bs:notEqual name="current_page" value="1">
				<td><script>ObjectDraw("page1","button",'',"&laquo;","goPageN(1)","page_section","","images/menu/","",true,24);</script></td>
				<td><script>ObjectDraw("page1","button",'',"&lt;","goPage(-1)","page_section","","images/menu/","",true,24);</script></td>
			</bs:notEqual>				
				<td><script>ObjectDraw("page1","button",'',"<bs:message code='page_navigation_1' defaultValue='Page'/>","","page_section","","images/menu/","",true,24);</script></td>
				<td><bs:input name="current_page" size="1" styleClass="inputPage"/></td>
				<td><script>ObjectDraw("page1","button",'',"<bs:message code='page_navigation_2' defaultValue='from'/> <bs:formelement name='page'/>&nbsp;","","page_section","","images/menu/","",true,24);</script></td>

			<bs:notEqual name="current_page" field="page">
				<td><script>ObjectDraw("page1","button",'',"&gt;","goPage(1)","page_section","","images/menu/","",true,24);</script></td>
				<td><script>ObjectDraw("page1","button",'',"&raquo;","goPageN(<bs:formelement name='page'/>)","page_section","","images/menu/","",true,24);</script></td>
			</bs:notEqual>								
								
					</tr>
				</table>
			</td>
		</tr>
		
</table>	
</span>	