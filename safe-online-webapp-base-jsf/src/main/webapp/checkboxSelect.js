function selectBox(id, checkbox) {
	var id_parts = checkbox.name.split(':');
	var row = id_parts[id_parts.length - 2];
	
	var boxes = checkbox.form.elements;
	for(var i=0; i < boxes.length; i++) {
		var boxids = boxes[i].name.split(':');
		if(boxids[id_parts.length-1] == id) {
			if(boxids[id_parts.length-2] == row) {
				if(checkbox.checked) {
					boxes[i].checked = true;
				}
			}
		}
	}
}