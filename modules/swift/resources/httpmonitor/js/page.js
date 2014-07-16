if (typeof Array.isArray === 'undefined') {
	Array.isArray = function(obj) {
		return Object.toString.call(obj) === '[object Array]';
	}
};

function replace(src, dst, attrs) {
	$(src).each(function(index) {
		var self = this;
		var newElement = $("<" + dst + "></" + dst + ">");
		$.each(this.attributes, function(index) {
			$(newElement).attr(self.attributes[index].name, self.attributes[index].value);
		});
		$.each(attrs, function(name, value){
			$(newElement).attr(name, value);
		});
		$(newElement).html($(this).html());
		$(this).after(newElement).remove();
	});
}

function create(tag) {
	return $("<" + tag + "></" + tag + ">");
}

function installTemplate(dst, id, dstid, fn) {
	if ($(dstid).length == 0) {
		console.log($(dstid).length);
		console.log(dstid + " not found");
		$(dst).loadTemplate(id, {}, {append: true});
		var el = $(dst + ">:last-child");
		el.attr("id", dstid.substring(1));
		fn(el);
	}
}

function createOrUpdateRow(table, rowIndex, key, values, matchesFn, addFn) {
	var rows = $(table + " tr");
	var tr;
	if (rows.length <= rowIndex) {
		tr = create("tr");
		$(table).append(tr);
	} 
	else if (!matchesFn(rows.eq(rowIndex), key)) {
		tr = create("tr");
		$(table).children().eq(rowIndex).before(tr);
	}
	else {
		tr = $(table).children().eq(rowIndex);
	}
	for (var i = 0; i < values.length; i++) {
		addFn(tr, key, i, values[i]);
	}
}

function parseXML(str) {
	if (window.DOMParser) {
		return new DOMParser().parseFromString(str, "text/xml");
	}
	else {
		xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = false;
		xmlDoc.loadXML(str);
		return xmlDoc;
	}
}

function display(dstId, srcId, pvalues) {
	var template = getElement(srcId);
	var src = getAttr(template, "src");
	if (src != null) {
		console.log("src: " + src)
		console.log("text: " + template.textContent)
		asyncRequest("getTemplate", src, function(id, data, error) {
			if (error != null) {
				noty({text: error});
				return;
			}
			display_(destId, srcId, template, data, pvalues);
		});
	}
	else {
		display_(dstId, srcId, template, template.textContent, pvalues);
	}
}

function display_(dstId, srcId, template, text, pvalues) {
	var xml = parseXML(text);
	var dest = getElement(dstId);
	params = getAttr(template, "params", "").split("(,\\s*)|(\\s+)");
	mode = getAttr(template, "mode", "replace");
	
	checkParams(params, pvalues);
	
	if (mode == "replace") {
		displayReplace(dest, xml, dstId, pvalues);
	}
	else {
		throw new Error("Unknown mode: '" + mode + "'");
	}
}

function displayReplace(dst, src, id, pvalues) {
	pvalues._procs = {};
	pvalues._runAfterReplace = [];
	var old = document.getElementById(id + "-s");
	var n = document.createElement("div");
	n.setAttribute("class", "template-expansion");
	n.id = id + "-s";
	expandChildren(n, src, pvalues);
	if (old == null) {
		dst.appendChild(n);
	}
	else {
		dst.appendChild(n);
		dst.removeChild(old);
	}
	
	for (var i in pvalues._runAfterReplace) {
		//console.log(pvalues._runAfterReplace[i]);
		pvalues._runAfterReplace[i]();
	}
}

function expandChildren(dst, src, params) {
	for (var i = 0; i < src.childNodes.length; i++) {
		var child = src.childNodes[i];
		expandChild(dst, child, params);
	}
}

function expandChild(dst, child, params) {
	if (isOp(child)) {
		return runOp(dst, child, params);
	}
	else {
		if (child.nodeType == 3) {
			// copy text
			dst.appendChild(expandTextNode(child, params));
		}
		else if (child.nodeType == 1) {
			dst.appendChild(expandNode(child, params));
		}
		else {
			console.log("Ignoring node: " + child);
		}
	}
}

function expandTextNode(src, params) {
	return document.createTextNode(expandString(src.textContent, params));
}

function expandNode(src, params) {
	var dst = document.createElement(src.tagName);
	for (var i = 0; i < src.attributes.length; i++) {
		var sattr = src.attributes[i];
		var name = sattr.name;
		if (name == "tref") {
			name = "href";
		}
		var dattr = document.createAttribute(name);
		dst.attributes.setNamedItem(dattr);
		if (sattr.name == "tref") {
			dattr.value = expandCGIParams(sattr.value, params);
		}
		else {
			dattr.value = expandString(sattr.value, params);
		}
	}
	expandChildren(dst, src, params);
	return dst;
}

function isOp(node) {
	if (node.nodeType != 1) {
		return false;
	}
	else {
		return node.tagName == "for" || node.tagName == "if" || node.tagName == "then" || node.tagName == "else" ||
			node.tagName == "pager" || node.tagName == "js" || node.tagName == "proc" || node.tagName == "call";
	}
}

function runOp(dst, src, params) {
	var op = src.tagName;
	if (op == "for") {
		var name = src.getAttribute("name");
		var values = expandParam(src.getAttribute("in"), params);
		runFor(dst, src, params, name, values);
	}
	else if (op == "then" || op == "else") {
		expandChildren(dst, src, params);
	}
	else if (op == "if") {
		var test = src.getAttribute("test");
		runIf(dst, src, params, test);
	}
	else if (op == "pager") {
		var id = src.getAttribute("id");
		var maxPage = expandString(src.getAttribute("maxPage"), params);
		var crtPage = expandString(src.getAttribute("crtPage"), params);
		var target = expandString(src.getAttribute("target"), params);
		runPager(dst, id, maxPage, crtPage, target, params);
	}
	else if (op == "js") {
		runJS(dst, expandString(src.textContent, params), params);
	}
	else if (op == "proc") {
		var name = src.getAttribute("name");
		var procParams = src.getAttribute("params");
		runProc(src, name, procParams, params);
	}
	else if (op == "call") {
		var name = src.getAttribute("name");
		runCall(dst, src, name, params);
	}
	else {
		throw new Error("Unknown operator: " + op);
	}
}

function runProc(src, name, procParams, params) {
	proc = {
		params: procParams.split(/[, ]+/),
		src: src
	}
	params._procs[name] = proc;
}

function runCall(dst, src, name, params) {
	var proc = params._procs[name];
	if (proc == null) {
		throw new Error("No such procedure: " + name);
	}
	
	var cparams = $.extend({}, params);
	for (var i = 0; i < proc.params.length; i++) {
		var param = proc.params[i];
		var actual = src.getAttribute(param);
		if (actual == null) {
			throw new Error("Missing parameter '" + param + "' for procedure '" + name + "'");
		}
		var value = expandString(actual, params);
		//console.log(param + ":");
		//console.log(value);
		cparams[param] = value;
	}
		
	expandChildren(dst, proc.src, cparams);
}

function expandParam(str, p) {
	if (str.indexOf("{") == 0) {
		var name = str.substring(1, str.length - 1);
		//console.log(name);
		name = name.replace(/\$/g, "p.");
		//console.log(name);
		var value = eval(name);
		//console.log("eval(" + name + "): " + value);
		return value;
	}
	else {
		return str;
	}
}

function expandString(str, p) {
	var index = str.indexOf("{");
	
	if (index == 0 && str.indexOf("{", 1) == -1 && str.indexOf("}") == str.length - 1) {
		return expandParam(str, p);
	}
	var last = 0;
	var r = "";
	while (index >= 0) {
		if (index == 0 && str.charAt(index - 1) == "\\") {
			index = str.indexOf("{", index + 1);
			continue;
		}
		var end = str.indexOf("}", index);
		r = r + str.substring(last, index);
		last = end + 1;
		var name = str.substring(index + 1, end);
		//console.log(name);
		name = name.replace(/\$/g, "p.");
		//console.log(name);
		var value = eval(name);
		//console.log("eval(" + name + "): " + value);
		r = r + value;
		index = str.indexOf("{", last);
	}
	r = r + str.substring(last);
	return r;
}

function expandCGIParams(str, params) {
	var qmi = str.indexOf("?");
	if (qmi == -1) {
		return expandString(str, params);
	}
	var r = str.substring(0, qmi + 1);
	var p = str.substring(qmi + 1);
	
	var first = true;
	var s = p.split("&");
	for (var i = 0; i < s.length; i++) {
		var kv = s[i].split("=", 2);
		var v = expandString(kv[1], params);
		if (v != null) {
			if (!first) {
				r = r + "&";
			}
			first = false;
			r = r + kv[0] + "=" + v;
		}
	}
	return r;
}

function runFor(dst, src, params, name, values) {
	var cparams = $.extend({}, params);
	var isArray = Array.isArray(values);
	//console.log(values);
	for (var v in values) {
		if (isArray) {
			cparams[name] = values[v];
		}
		else {
			cparams[name] = v;
		}
		for (var i = 0; i < src.childNodes.length; i++) {
			expandChild(dst, src.childNodes[i], cparams);
		}
	}
}

function runIf(dst, src, params, test) {
	var c = expandParam(test, params);
	if (c) {
		expandChild(dst, getNonTextNode(src, 0), params);
	}
	else {
		var els = getNonTextNode(src, 1);
		if (els != null) {
			expandChild(dst, els, params);
		}
	}
}

function getNonTextNode(src, index) {
	for (var i = 0; i < src.childNodes.length; i++) {
		var c = src.childNodes[i];
		if (c.nodeType == 1) {
			if (index == 0) {
				return c;
			}
			else {
				index--;
			}
		}
	}
	return null;
}


function runPager(dst, id, maxPage, crtPage, target, params) {
	$(dst).append('\
			<div id="' + id + '" class="pagination">\
				<a href="#" class="first" data-action="first">&laquo;</a>\
				<a href="#" class="previous" data-action="previous">&lsaquo;</a>\
				<input type="text" readonly="readonly" data-max-page="40" />\
				<a href="#" class="next" data-action="next">&rsaquo;</a>\
				<a href="#" class="last" data-action="last">&raquo;</a>\
			</div>');
	target = expandCGIParams(target, params);
	params._runAfterReplace.push(function() {
		$("#" + id).jqPagination({
			current_page: crtPage,
			max_page: maxPage,
			paged: function(page) {
				console.log("Switching page to " + page);
				browserSetAddr(target.replace("%", page));	
			}
		});
	});
}

function runJS(dst, text, _p) {
	_p._runAfterReplace.push(function() {
		eval(text);
	});
}

function getElement(id) {
	var el = document.getElementById(id);
	if (el == null) {
		throw new Error("No such element: '" + id + "'");
	}
	return el;
}

function getAttr(el, name, defVal) {
	var val = el.getAttribute(name);
	if (val == null) {
		return defVal;
	}
	else {
		return val;
	}
}

function checkParams(names, map) {
	for (var name in names) {
		if (!name in map) {
			throw new Error("Missing parameter '" + name + "'")
		}
	}
}

function uify(id) {
	$(id + " .uify.button").button();
}

function resizeTabs(id) {
	var winw = $(window).width() - 40;
	var winh = $(window).height() - $("#tabs-heading").height() - 16;
	var tcw = $(id).width();
	var tch = $(id).height() + $("#tabs-heading").height() + 24;
	
	
	var targetw = Math.max(winw, tcw).toFixed(0);
	var targeth = Math.max(winh, tch).toFixed(0);
	//noty({text: "id: " + id + "; win: " + winw + ", " + winh + "; tc: " + tcw + ", " + tch + "; target: " + targetw + ", " + targeth});
			
	$("#tabs").css("width", targetw + "px");
	$("#tabs").css("height", targeth + "px");
}

setupUpdates();

function setActiveTabFromURL() {
	var url = window.location.href;
	
	var tabid = null;
	var hash = url.indexOf("#");
	if (hash != -1) {
		var end = url.indexOf("?", hash);
		if (end != -1) {
			tabid = url.substring(hash, end);
		}
		else {
			tabid = url.substring(hash);
		}
	}
	
	if (tabid == null) {
		return;
	}
	
	var tabindex = getTabIndex("#tabs", tabid);
	$("#tabs").tabs({active: tabindex});
	if (tabid == "#browser") {
		browserSetAddr(url.substring(hash));
	}
}

function getTabIndex(idp, idt) {
	var i = 0;
	var index = -1;
	$(idp + " #tabs-heading li a").each(function() {
		if ($(this).attr("href") == idt) {
			index = i;
		}
		i = i + 1;
	});
	return index;
}