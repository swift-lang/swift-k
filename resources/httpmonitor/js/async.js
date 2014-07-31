var INITIAL = 1000;
var DELAY = 2000;
var TIMEOUT = 10000;

AsyncRequest = function(context) {
	this.context = context;
	var browser = navigator.appName;
	if (context.paramFn) {
		this.url = context.url + "?" + encodeParams(context.paramFn());
	}
	else {
		this.url = context.url;
	}
	this.responseReceived = false;

	if (browser == "Microsoft Internet Explorer") {
		this.ro = new ActiveXObject("Microsoft.XMLHTTP");
	}
	else {
		this.ro = new XMLHttpRequest();
	}
}

AsyncRequest.prototype.handleResponse = function() {
	if (this.ro.readyState == 4) {
		this.responseReceived = true;
		var data;
		if (this.ro.status != 200) {
			stopUpdates(this.context);
			if (this.ro.responseText != "") {
				this.callbackWrapper(null, this.ro.responseText);
			}
			else {
				this.callbackWrapper(null, "Error: " + this.ro.statusText);
			}
			return;
		}
		try {
			var response = this.ro.responseText;
			var i;
			for(i = 0; i < response.length; i++) {
				var c = response.charAt(i);
				if (c != '\n' && c != '\t' && c != '\r' && c != ' ' && c != '\f') {
					break;
				}
			}
			response = response.substring(i);
			try {
				data = JSON.parse(response);
			}
			catch (err) {
				console.log("Failed to parse response: " + err + ". Response was: " + response);
				this.callbackWrapper(null, "Error processing response from server: " + err);
			}
		}
		catch(err) {
			console.log(err.stack);
			this.callbackWrapper(null, "Error processing response from server: " + err);
			return;
		}
		
		this.callbackWrapper(data, null);			
	}
}
	
AsyncRequest.prototype.callbackWrapper = function(stuff, error) {
	try {
		if (error) {
			console.log(this.url + " error: " + error);
			this.context.callback(this.context.id, null, "Update failed: " + error);
			stopUpdates(this.context);
		}
		else {
			this.context.callback(this.context.id, stuff, null);
		}
	}
	catch(err2) {
		//Probably should write something out to the page indicating that updates
		//Are no longer being monitored
		//window.alert("Callback failed to process error message\n" + err + "\n" + err2);
		console.log(this.url + " error: " + err2);
		console.log(err2.stack);
		noty({text: this.url + " error: " + err2});
		stopUpdates(this.context); 
	}
}

AsyncRequest.prototype.replyTimeout = function() {
	if (!this.responseReceived) {
		this.callbackWrapper(this.context, null, "Reply timeout");
	}
}

AsyncRequest.prototype.send = function() {
	try {
		var index = this.url.indexOf("#");
		if (index != -1) {
			var index2 = this.url.indexOf("?", index);
			if (index2 == -1) {
				this.url = this.url.substring(0, index);
			}
			else {
				this.url = this.url.substring(0, index) + this.url.substring(index2);
			}
		}
		this.ro.open('get', this.url);
		var ar = this;
		this.ro.onreadystatechange = function() {
			ar.handleResponse();
		};
		this.ro.send(null);
		window.setTimeout(function() {
			ar.replyTimeout();
		}, TIMEOUT);
	}
	catch(err) {
		this.callbackWrapper(this.context, null, err);
		stopUpdates(this.context);
	}
}

function encodeParams(map) {
	var s = "";
	for (var k in map) {
		var v = map[k];
		if (s != "") {
			s = s + "&";
		}
		s = s + k + "=" + v;
	}
	return s;
}

function encodeParam(v) {
	return v;
}
	
function setupUpdates(initial, delay) {
	if (initial === undefined) { initial = INITIAL; }
	if (delay === undefined) { delay = DELAY; }
	
	self.delay = delay;
	self.setTimeout(tick, initial);
	self.dynamicUpdates = new Array();
	self.tabUpdates = {};
}

function tick() {
	if (self.dynamicUpdates) {
		for (var i = 0; i < self.dynamicUpdates.length;) {
			var context = self.dynamicUpdates[i];
			if (context.done) {
				self.dynamicUpdates.splice(i, 1);
			}
			else if (context.paused) {
				i++;
			}
			else {
				var request = new AsyncRequest(context);
				
				request.send();
				if (context.once) {
					self.dynamicUpdates.splice(i, 1);
				}
				i++;
			}
		}
	}
	self.setTimeout(tick, self.delay);
}

function registerUpdate(id, url, callback, paramFn) {
	var context = {id: id, url: url, callback: callback, once: false, done: false, paused: false, paramFn: paramFn};
	var request = new AsyncRequest(context);
	request.send();
	self.dynamicUpdates.push(context);
}

function asyncRequest(id, url, callback) {
	var context = {id: id, url: url, callback: callback, once: true, done: false, paused: false};
	var request = new AsyncRequest(context);
	request.send();
}

function pauseUpdates(id) {
	setUpdatesPaused(id, true);
}

function resumeUpdates(id) {
	setUpdatesPaused(id, false);
}

function setUpdatesPaused(id, value) {
	for (var i in self.dynamicUpdates) {
		if (self.dynamicUpdates[i].id == id) {
			self.dynamicUpdates[i].paused = value;
		}
	}
}

function stopUpdates(context) {
	console.log("Stopping updates for " + context.id);
	context.done = true;
}

function stopUpdatesByID(id) {
	for (var i in self.dynamicUpdates) {
		if (self.dynamicUpdates[i].id == id) {
			self.dynamicUpdates[i].done = true;
			console.log("Stopping updates for " + id);
		}
	}
}
