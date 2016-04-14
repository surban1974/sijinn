package it.sijinn.admin.beans;

import java.io.Serializable;
import java.util.Date;

import it.classhidra.serialize.Format; 
import it.classhidra.serialize.Serialized;

public class SettingProperties  implements Serializable {

	private static final long serialVersionUID = 1L;

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
		this.root="...?";
		this.restAuth=true;
		this.restUser="";
		this.restPassword="";
		this.date=new Date();
	}
	
	public SettingProperties(String _root){
		super();
		this.root=_root;
		this.restAuth=false;
		this.restUser="";
		this.restPassword="";
		this.date=new Date();		
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

	
	
}
