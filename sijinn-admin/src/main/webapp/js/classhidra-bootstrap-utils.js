(function(factory){

	// CommonJS/RequireJS and "native" compatibility
	if(typeof module !== "undefined" && typeof exports == "object") {
		// A commonJS/RequireJS environment
		if(typeof window != "undefined") {
			// Window and document exist, so return the factory's return value.
			module.exports = factory();
		} else {
			// Let the user give the factory a Window and Document.
			module.exports = factory;
		}
	} else {
		// Assume a traditional browser.
		window.clpopup = factory();
	}

})(function(root){

	var clpopup = function(prot){
	
		this.type 	= (prot)?prot.type:'info';
		this.message	= (prot)?prot.message:null;
	
	}
	
	clpopup.prototype = {
	
		open : function(){
			if(document.getElementById('modalpopup')){
				this.show();
			}else{
				
				var tmp_container = document.createElement('div');
				tmp_container.id='tmp_container';
				document.body.appendChild(tmp_container);
				var self = this;
				new clajax({
					url:'pages/included/modal.html',
					target: document.getElementById('tmp_container'),
					success: function(){ 
						self.show();		
					}
				}).request('GET');
			}	
		},
	
		show : function(){
			var modal = new Modal(
					document.getElementById('modalpopup'), 
					{
						content: '<div class="modal-header">'
								+'<div id="modalpopup.body" class="'+this.getClassByType()+'" ></div>'
								+'<div class="modal-footer">'
								+'<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>'
								+'</div>'
					}
				);
				modal.open();
				if(this.message)
					document.getElementById('modalpopup.body').innerHTML = this.message;
		},
	
		getClassByType : function(){
				if(this.type){
					if(this.type=='info')
						return 'alert alert-info';
					if(this.type=='success')
						return 'alert alert-success';
					if(this.type=='warning')
						return 'alert alert-warning';
					if(this.type=='danger')
						return 'alert alert-danger';			
				}
				return '';
			
		}

	}



	return clpopup;

});
	

function showException(e){
	new clpopup({
		type: 'danger',
		message: e.toString()
	}).open();	
}

function json2model(source,json){
	for(var property in source){
		if (typeof json[property] === "undefined") {
		}else{
			if (typeof source[property] === 'function')
				source[property](json[property]);		
			else if(typeof source[property] === 'object')
				json2model(source[property],json[property]);
			else	
				source[property] = json[property];
		}
	}
}

function dirtyModelElements(newModel,oldModel, array, prefix){
	for(var property in newModel){
	
			if(typeof newModel[property] === 'object')
				dirtyModelElements(newModel[property],oldModel[property],array,((!prefix || prefix=='')?'':prefix+'.')+property);
			else{	
				if(newModel[property] != oldModel[property]){
					var data = {};
					data[((!prefix || prefix=='')?'':prefix+'.')+property] = newModel[property];
					array.push(data);
				}
			}
	}
}
