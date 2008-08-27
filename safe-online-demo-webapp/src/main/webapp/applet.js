var xmlHttp


function GetXmlHttpObject() { 
	if (window.ActiveXObject) {
		try {
			return new ActiveXObject("Msxml2.XMLHTTP")
		}
		catch(e) {
 			try {
				return new ActiveXObject("Microsoft.XMLHTTP")
			}
			catch(e) {
			}
		}
	}
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest()
	}
	return null
}


function prepareCallback() {
	xmlHttp = GetXmlHttpObject()
	if (null == xmlHttp) {
		alert("Browser does not support HTTP Request")
		return
	}
	var url = "callback/"
	xmlHttp.onreadystatechange = stateChanged
	xmlHttp.open("GET", url, true)
	xmlHttp.send(null)
}


function stateChanged() {
	if (xmlHttp.readyState == 4 || xmlHttp.readyState == "complete") {
		//document.demoForm.submit()
	}
}
