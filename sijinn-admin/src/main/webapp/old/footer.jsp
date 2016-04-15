<%@ taglib uri="/WEB-INF/tlds/bsController.tld" prefix="bs" %>
<%@ page import="it.classhidra.core.tool.exception.message"%><%@ page import="java.util.*" %>
<%@ page import="it.classhidra.core.tool.log.stubs.iStub" %>
<%@ page import="it.classhidra.core.controller.bsController" %>
<%@ page import="it.classhidra.core.tool.exception.bsControllerException" %><oldjsp:include page="../included/page_popup.jsp"/><oldjsp:include page="../included/iframe_popup.jsp"/>


<div id="content_Panel_runSubmit" name="cp_popup"
style="z-index: 9999; position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: none; background-image: url('images/decor/blank.gif'); filter: alpha(opacity=50);opacity: 0.5;  "
>
<table border="0" width="100%" height="100%" style="z-index:99999">
<tr>
<td align="center"><img src="images/wait.gif" border="0"></td>
</tr>
</table>
</div>

<bs:switch>
<bs:notIsNull bean="MessageAsPopup">
	<bs:more bean="$listmessages" name="size" method_prefix="" value="0">
	<script>
		showAsPopup("messages?middleAction=view&img_size=big",600,200);
	</script>
	</bs:more>
</bs:notIsNull>
<bs:otherwise>
	<bs:more bean="$listmessages" name="size" method_prefix="" value="0">
	<script>
	if(isIE()==8){
		showAsPopup("messages?middleAction=view&img_size=big",600,200);
	}else{
		dhtmlLoadScript('jsp/ajax/jscript_Messages2.jsp','text/javascript');
	}
	</script>
	</bs:more>
</bs:otherwise>
</bs:switch>



<script>

window.onresize = function() { 
	ajustPanel();
	<bs:equal bean="$authentication" name="_language" value="AR">
		ajustMenuPanel_ar();
	</bs:equal>
	<bs:notEqual bean="$authentication" name="_language" value="AR">
		ajustMenuPanel();
	</bs:notEqual>
	try{
		if(document.getElementById('current_div_width').value!=''){
			
		}else{
			readyPage();
		}
	}catch(e){		
	}
}
document.onreadystatechange = function () {
	  var state = document.readyState;
	  if (state == 'interactive') {

	  } else if (state == 'complete') {
		  ajustPanel();
		  ajustPageInHeader();
		  try{
		  	readyPage();
		  }catch(e){		
		  }
		  try{
		  	if(document.getElementById('current_div_width').value!=''){
		  		goViewExtend();
		  	}
		  }catch(e){}
	  }
}


</script>

<bs:equal bean="login" name="menu_pin" value="true">
<script>
	<bs:equal bean="$authentication" name="_language" value="AR">
		rePositionForMenu_ar();
	</bs:equal>
</script>	
</bs:equal>

<div id="footer_fixedbox">

</div>
