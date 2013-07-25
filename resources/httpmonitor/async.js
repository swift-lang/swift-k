var INITIAL = 1000;
var DELAY = 2000;
var TIMEOUT = 10000;

function AsyncRequest(url, callback) {
	self.browser = navigator.appName;
	self.url = url;
	self.rcb = callback;
	self.responseReceived = false;

	if(browser == "Microsoft Internet Explorer") {
		self.ro = new ActiveXObject("Microsoft.XMLHTTP");
	}
	else {
		self.ro = new XMLHttpRequest();
	}
	
	self.handleResponse = function() {
		if (self.ro.readyState == 4) {
			self.responseReceived = true;
			try {
				var response = self.ro.responseText;
				/*alert("status: " + self.ro.status + "\nstatusText: " + self.ro.statusText + 
						"\nresponseType: " + self.ro.responseType + "\nresponseText: " + self.ro.responseText);*/
				var i;
				for(i = 0; i < response.length; i++) {
					var c = response.charAt(i);
					if (c != '\n' && c != '\t' && c != '\r' && c != ' ' && c != '\f') {
						break;
					}
				}
				response = response.substring(i);
				var update = new Array();
				if(response.indexOf('\n' != -1)) {
					values = response.split('\n');
					for (v in values) {
						var value = values[v];
						var i = value.indexOf(":");
						if (i == -1) {
							update[value] = "";
						}
						else {
							update[value.substr(0, i)] = value.substr(i + 1);
						}
					}
				}
			}
			catch(err) {
				self.rcb(null, "Error processing response from server: " + err);
			}
			try {				
				self.rcb(update, null);
			}
			catch(err) {
				self.callbackErr(err);
			}
		}
	}
	
	self.callbackErr = function(err) {
		try {
			self.rcb(null, "Update failed: " + err);
		}
		catch(err2) {
			//Probably should write something out to the page indicating that updates
			//Are no longer being monitored
			//window.alert("Callback failed to process error message\n" + err + "\n" + err2);
			self.stopUpdates(); 
		}
	}
	
	self.replyTimeout = function() {
		if (!self.responseReceived) {
			self.callbackErr("Reply timeout");
		}
	}
}

AsyncRequest.prototype.send = function() {
	self.ro.open('get', self.url);
	self.ro.onreadystatechange = self.handleResponse;
	self.ro.send(null);
	self.setTimeout(replyTimeout, TIMEOUT);
}
	
	
function registerUpdate(url, callback, initial, delay) {
	if (initial === undefined) { initial = INITIAL; }
	if (delay === undefined) { delay = DELAY; }
	self.setTimeout(tick, initial);
	self.tcb = callback;
	self.done = false;
	
	function tick() {
		var request = new AsyncRequest(url, reply);
		request.send();
	}
	
	function reply(stuff, error) {
		self.tcb(stuff, error);
		if (!self.done) {
			self.setTimeout(tick, delay);
		}
	}
}

function stopUpdates() {
	self.done = true;
}
