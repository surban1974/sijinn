package it.sijinn.admin.beans;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Properties;



import it.classhidra.serialize.Format; 
import it.classhidra.serialize.Serialized;
import it.classhidra.core.tool.util.util_classes;
import it.classhidra.core.tool.util.util_file;

public class SettingProperties  implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String CONST_CONFIGNAME = "sijinn-admin.properties";

	@Serialized
	private String config;
	
	private String config_path;
	
	@Serialized
	private String root;
	
	@Serialized
	private boolean restAuth=false;
	
	@Serialized
	private String restUser;
	
	private String restPassword;
	
	@Serialized(input=@Format(format="dd/MM/yyyy HH:mm"),output=@Format(format="dd/MM/yyyy HH:mm"))
	private Date date;
	

	
	public SettingProperties(){
		super();
		Properties property = null;
		config_path = System.getProperty("config-path");
		if(config_path!=null){
			File path = new File(config_path);
			if(!path.exists()){
				if(!path.mkdirs())
					config_path=null;
			}
			
		}
		try{
			if(config_path==null){
				URL rootUrl = this.getClass().getClassLoader().getResource(".");
	   			String path = util_classes.convertUrl2File(rootUrl).getAbsolutePath();
	   			path=path.replace('\\', '/');
	   			if(path.lastIndexOf("/")!=path.length())
	   				path+="/";
	   			config = path+CONST_CONFIGNAME;
			}else
				config = config_path+CONST_CONFIGNAME;
			
   			property = util_file.loadProperty(config);
   			

		}catch(Exception e){			
		}
		if(property==null){
			this.root="...?";
			this.restAuth=true;
			this.restUser="";
			this.restPassword="";
		}else{
			this.root = property.getProperty("root", "...");
			try{
				this.restAuth=new Boolean(property.getProperty("restAuth", ""));
			}catch(Exception e){
				this.restAuth=false;
			}
			this.restUser=property.getProperty("restUser", "");
			this.restPassword=property.getProperty("restPassword", "");
		}
		this.date=new Date();
	}
	
	public boolean save() throws Exception{
		return util_file.writeByteToFile(toProperties().getBytes(), getConfig());
	}
	
	public String toProperties(){
		String result="";
		result+="root="+getRoot()+System.getProperty("line.separator");
		result+="restAuth="+isRestAuth()+System.getProperty("line.separator");
		result+="restUser="+getRestUser()+System.getProperty("line.separator");
		result+="restPassword="+getRestPassword()+System.getProperty("line.separator");
		return result;
	}
	
	
	public String getRoot() {
		return root;
	}
	

	public void setRoot(String root) {
		this.root = root;
	}
	
	public boolean isRestAuth() {
		return restAuth;
	}
	
	public void setRestAuth(boolean restAuth) {
		this.restAuth = restAuth;
	}
	

	public String getRestUser() {
		return restUser;
	}
	
	public void setRestUser(String restUser) {
		this.restUser = restUser;
	}
	public String getRestPassword() {
		return restPassword;
	}
	
	@Serialized(input=@Format)
	public void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}

	public Date getDate() {
		return date;
	}

	
	public void setDate(Date date) {
		this.date = date;
	}


	public String getConfig() {
		return config;
	}


	public void setConfig(String config) {
		this.config = config;
	}

	
	
}
