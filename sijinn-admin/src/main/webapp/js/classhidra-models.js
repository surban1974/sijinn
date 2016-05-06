var GenericModel = {
	extend: function(props) {
	    for(var prop in props) {
	        if(props.hasOwnProperty(prop)) {
	            this[prop] = props[prop];
	        }
	    }
	    return this;
	}	
}

var GenericServerModel = Object.create(GenericModel);
GenericServerModel.extend(
{
	error : '',
	warning : '',
	info : '',
	success : '',
	
		
	init: function(model){
		json2model(this,model);
		return this;
	},

	
	message : function(){
		
		if(this.error && this.error!=''){
			alert("ERROR "+this.error);					 
		}
		if(this.success && this.success!=''){
			alert(this.success);	
		}	
		if(this.info && this.info!=''){
			alert("INFO: "+this.info);
		}
		if(this.warning && this.warning!=''){
			alert("WARNING: "+this.warning);
		}
			
		return this;
	}					
}
);


var GenericViewModel = Object.create(GenericModel);
GenericViewModel.extend(
{
		initialisation : true,
		model : null,
			
		set : function(serverModel){
			this.model = serverModel;
			return this;
		},
		
		init : function(parsed){
			this.initialisation = true;
			try{
				this.model.init(parsed.model);
			}catch(e){
				showException(e);
			}
			this.initialisation = false;	
			return this;
		},
		
		post : function($http,newValue){
		},

		postDirty : function($http,newValue){
		},		
		
		getDirty : function(oldModel,exclude){
			var result = [];
			dirtyModelElements(this,oldModel,result,null,exclude);
			return result;
		},
		
		message : function(){
			this.model.message();
			return this;
		}		
}
);


function json2model(source,json){
	if(isEmpty(source))
		source = json;
	else{
		for(var property in source){
//			if(source.hasOwnProperty(property)){
				if (typeof json[property] === "undefined") {
				}else{
					if (typeof source[property] === 'function')
						source[property](json[property]);		
					else if(typeof source[property] === 'object'){
						if(isEmpty(source[property]))
							source[property] = json[property];
						else if(Array === source[property].constructor){
							if(source[property].length != json[property].length)
								source[property] = json[property];
							else
								json2model(source[property],json[property]);
						}else
							json2model(source[property],json[property]);
					}
					else	
						source[property] = json[property];
				}
//			}
		}
	}
}

function isEmpty(obj) {
    for(var prop in obj) {
        if(obj.hasOwnProperty(prop))
            return false;
    }

    return true && JSON.stringify(obj) === JSON.stringify({});
}

function dirtyModelElements(newModel, oldModel, array, prefix, exclude){
	for(var property in newModel){
	
			if(typeof newModel[property] === 'object'){
				if(newModel[property] && oldModel[property])
					dirtyModelElements(newModel[property],oldModel[property],array,((!prefix || prefix=='')?'':prefix+'.')+property,exclude);
			}else{	
				var exc=false;
				if(exclude && typeof exclude === 'function')
					exc = exclude(property);
				
				if(!exc){
					if(newModel[property] && oldModel[property]){
						if(newModel[property] != oldModel[property]){
							var data = {};
							data[((!prefix || prefix=='')?'':prefix+'.')+property] = newModel[property];
							array.push(data);
						}
					}
				}
			}
	}
}

