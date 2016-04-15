<%@ taglib uri="/WEB-INF/tlds/bsController.tld" prefix="bs" %> 
<bs:bean name="navigationLast" source="$navigation" property="lastIRedirect"/>
<bs:bean name="navigationAll" source="navigationLast" property="allChildIRedirect"/>

	<bs:sequence bean="navigationAll">
		<bs:bean name="navigationEntity" source="navigationAll" index="SEQUENCE"/>
<bs:more bean="bs:sequence" name="index" value="0">&#8594;</bs:more>
<span style="cursor:pointer; border:outset 1px;" class="page_section" onclick="goAction('<bs:formelement bean="navigationEntity" name="iAction.path"/>?middleAction=reload')">
	<bs:formelement bean="navigationEntity" name="iRedirect.descr"/>
</span>	
			

	</bs:sequence>
