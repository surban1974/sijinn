var clpopup = function(prot){
	
	this.type 	= (prot)?prot.type:'info';
	this.message	= (prot)?prot.message:null;
	
	this.open = function(){
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
	}
	
	this.show = function(){
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
	}
	
	this.getClassByType = function(){
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

