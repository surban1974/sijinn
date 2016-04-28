package it.sijinn.admin.components.controllers; 




import java.io.Serializable;

import it.classhidra.core.controller.action;
import it.classhidra.core.controller.i_action;
import it.classhidra.core.controller.i_bean;
import it.classhidra.serialize.Serialized;


public abstract class AbstractBase extends action implements i_action, i_bean, Serializable{

	private static final long serialVersionUID = 1L;
	
	@Serialized
	private String error;
	
	@Serialized
	private String warning;
	
	@Serialized
	private String info;
	
	@Serialized
	private String success;

public AbstractBase(){
	super();
}
/*
@Override
public void init(HttpServletRequest request) throws bsControllerException {
	if(request.getContentType()!=null && request.getContentType().toLowerCase().indexOf("application/json")>-1){
		if(initJsonPart(request, new JsonMapper() {

			
			@Override
			public Map mapping(i_bean bean, String json, Map table) {
				try{
					JsonReader jsonReader = Json.createReader(new StringReader(json));
					JsonObject object = jsonReader.readObject();
					jsonReader.close();
					
					Map<String, Object> mapped = JsonMapperUtils.jsonToMap(object);
					if(mapped!=null && mapped.size()>0){
						Map<String, Object> root = null;
						
						String modelname = (String)mapped.get("modelname");
						if(modelname!=null)
							root = (mapped.get(modelname)!=null && mapped.get(modelname) instanceof Map)?(Map)mapped.get(modelname):null;
						else if(get_bean()!=null && get_bean().get_infobean()!=null && get_bean().get_infobean().getName()!=null && !get_bean().get_infobean().getName().equals(""))
							root = (mapped.get(get_bean().get_infobean().getName())!=null && mapped.get(get_bean().get_infobean().getName()) instanceof Map)
									?
									(Map)mapped.get(get_bean().get_infobean().getName())
									:
									null;
						else if(get_bean()!=null && get_bean().get_infoaction()!=null && get_bean().get_infoaction().getName()!=null && !get_bean().get_infoaction().getName().equals(""))
							root = (mapped.get(get_bean().get_infoaction().getName())!=null && mapped.get(get_bean().get_infoaction().getName()) instanceof Map)
									?
									(Map)mapped.get(get_bean().get_infoaction().getName())
									:
									null;
						
						if(root==null)
							root = mapped;
						
						Map<String, Object> cl_parameters = new HashMap<String, Object>();
						
						for (Object elem : root.keySet()) {
							String key = (String)elem;
							cl_parameters = recursive(key, root, cl_parameters, "");
						}
						
						table.put(JsonMapper.CONST_ID_CHECKFORDESERIALIZE, "true");
						table.putAll(cl_parameters);
					}
				}catch(Exception e){
					new bsControllerException(e, iStub.log_ERROR);
				}
				return table;
			}
			
			private Map<String, Object> recursive(String key, Map<String, Object> original, Map<String, Object> result, String prefix){
				if(original.get(key) instanceof Map){
					Map<String, Object> sub = (Map<String, Object>)original.get(key);
					for(Object elem : sub.keySet())
						result = recursive((String)elem, sub, result, ((prefix.equals(""))?"":prefix+".")+key);
				}else
					result.put(((prefix.equals(""))?"":prefix+".")+key, original.get(key).toString());
					
				return result;
			}
			
			
		})) return;
	}else
		super.init(request);
}
*/
public void clear(){
	error="";
	warning="";
	info="";
	success="";
}

public String getError() {
	return error;
}

public void setError(String error) {
	this.error = error;
}



public String getInfo() {
	return info;
}

public void setInfo(String info) {
	this.info = info;
}


public String getWarning() {
	return warning;
}

public void setWarning(String warning) {
	this.warning = warning;
}

public String getSuccess() {
	return success;
}

public void setSuccess(String success) {
	this.success = success;
}



}
