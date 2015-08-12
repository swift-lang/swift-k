var visibleSection = null;

function getFirstSectionId() {
	var content = document.getElementById("content");
	return content.children[0].children[0].id;
}

function getCurrentSection() {
	var loc = window.location.href;
	var s = loc.split("#", 2);
	if (s.length == 1) {
		return getFirstSectionId();
	}
	else {
		var t = s[1].split("?");
		return t[0];
	}
}

function setUrl(section) {
	var loc = window.location.href;
	var s = loc.split("#", 2);
	window.history.pushState({}, document.title, s[0] + "#" + section + "?q");
}

function getSectionDiv(id) {
	var title = document.getElementById(id);
	var el = title.parentNode;
	// find sect1 div
	while (el != null) {
		if (el.className == "sect1") {
			return el;
		}
		el = el.parentNode;
	}
	return null;
}

function getSectionId(div) {
	return div.children[0].id;
}

function findElementWithAttr2(parent, attrName, attrValue) {
	for (var i = 0; i < parent.children.length; i++) {
		var el = parent.children[i];
		if (el.hasAttribute(attrName) && el.getAttribute(attrName) == attrValue) {
			return el;
		}
		var childFound = findElementWithAttr2(el, attrName, attrValue);
		if (childFound != null) {
			return childFound;
		}
	}
	return null;
}

function findElementWithAttr(start, attrName, attrValue) {
	var el = document.getElementById(start);
	return findElementWithAttr2(el, attrName, attrValue);
}

function hideSection(section) {
	var div = getSectionDiv(section);
	if (div != null) {
		div.style.display = "none";
		div.style.visibility = "hidden";
	}
	var sect1Id = getSectionId(div);
	var sect1ToC = findElementWithAttr("toc", "href", "#" + sect1Id);
	var sect2ToC = findElementWithAttr("toc", "href", "#" + section);
	sect1ToC.className = "";
	sect2ToC.className = "";
}

function displaySection(section, scroll) {
	if (scroll == null) {
		scroll = true;
	}
	console.log("Displaying " + section);
	var div = getSectionDiv(section);
	if (div != null) {
		div.style.display = "block";
		div.style.visibility = "visible";
	}
	// scroll back a bit to reveal the part covered by the header
	var title = document.getElementById(section);
	if (scroll) {
		var header = document.getElementById("header");
		var offset = header.clientHeight + 4;
		var body = getScrollingContainer();
		body.scrollTop -= offset;
	}
	var sect1Id = getSectionId(div);
	var sect1ToC = findElementWithAttr("toc", "href", "#" + sect1Id);
	var sect2ToC = findElementWithAttr("toc", "href", "#" + section);
	sect1ToC.className = "active";
	sect2ToC.className = "active";
	//sect2ToC.scrollIntoView();
}

function displayCurrentSection() {
	var section = getCurrentSection();
	//console.log("displayCurrentSection(" + section + ")");
	if (visibleSection != null) {
		hideSection(visibleSection);
	}
	displaySection(section);
	visibleSection = section;
}

function hashHandler() {
    this.oldHash = window.location.hash;
    this.Check;

    var that = this;
    var detect = function() {
        if (that.oldHash != window.location.hash){
            that.oldHash = window.location.hash;
            if (window.location.hash.indexOf("?") == -1) {
            	disableNextScrollTracking();
	            displayCurrentSection();
	        }
        }
    };
    this.Check = setInterval(function() {detect()}, 100);
}

function selectPreviousSection(id) {
	console.log("selectPreviousSection(" + id + ")");
	var title = document.getElementById(id);
	var div = title.parentNode;
	var prevDiv = div.previousSibling;
	while (prevDiv != null && prevDiv.nodeType != 1) {
		prevDiv = prevDiv.previousSibling;
	}
	if (prevDiv == null || prevDiv.className != "sect2") {
		return false;
	}
	var prevId = prevDiv.children[0].id;
	visibleSection = prevId;
	hideSection(id);
	displaySection(prevId, false);
	setUrl(prevId);
	return true;
}

function selectNextSection(id) {
	console.log("selectNextSection(" + id + ")");
	var title = document.getElementById(id);
	var div = title.parentNode;
	var nextDiv = null;
	if (div.className == "sect1") {
		var sectionbody = div.children[1];
		for (var i = 0; i < sectionbody.children.length; i++) {
			if (sectionbody.children[i].className == "sect2") {
				nextDiv = sectionbody.children[i];
				break;
			}
		}
	}
	if (nextDiv == null) {
		nextDiv = div.nextSibling;
	}
	while (nextDiv != null && nextDiv.nodeType != 1) {
		nextDiv = nextDiv.nextSibling;
	}
	if (nextDiv == null || nextDiv.className != "sect2") {
		return false;
	}
	var nextId = nextDiv.children[0].id;
	visibleSection = nextId;
	hideSection(id);
	displaySection(nextId, false);
	setUrl(nextId);
	return true;
}

function getUniqueTag(name) {
	return document.getElementsByTagName(name)[0];
}

function getScrollingContainer() {
	if (IS_FIREFOX) {
		return getUniqueTag("html");
	}
	else {
		return getUniqueTag("body");
	}
}

function getBody() {
	return getUniqueTag("body");
}

function getDeltas() {
	var section = getCurrentSection();
	//console.log("getDeltas(" + section + ")");
	var title = document.getElementById(section);
	var el = title.parentNode;
	var body = getScrollingContainer();
	var topDelta = body.scrollTop - el.offsetTop;
	var bottomDelta = null;
	if (el.className == "sect1") {
		var sectionbody = el.children[1];
		for (var i = 0; i < sectionbody.children.length; i++) {
			if (sectionbody.children[i].className == "sect2") {
				bottomDelta = body.scrollTop - sectionbody.children[i].offsetTop;
				break;
			}
		}
	}
	if (bottomDelta == null) {
		bottomDelta = body.scrollTop - el.offsetTop - el.offsetHeight;
	}
	return {top: topDelta, bottom: bottomDelta, section: section};
}

function checkVisibleSection() {
	if (!nextScrollTrackingEnabled) {
		nextScrollTrackingEnabled = true;
		return;
	}
	var deltas = getDeltas();
	var header = document.getElementById("header");
	var offset = header.clientHeight + 4;
	//console.log("deltas.top: " + deltas.top + ", deltas.bottom: " + deltas.bottom + ", offset: " + offset);
	while (deltas.top < -offset && selectPreviousSection(deltas.section)) {
		deltas = getDeltas();
	}
	while (deltas.bottom > 0 && selectNextSection(deltas.section)) {
		deltas = getDeltas();
	}
}

function onClick(cls, fn) {
	els = document.getElementsByClassName(cls);
	
	for (var i = 0; i < els.length; i++) {
		els[i].children[0].onclick = fn;
	}
}

var nextScrollTrackingEnabled = true;

function disableNextScrollTracking() {
	nextScrollTrackingEnabled = false;
}

function setUpScrollTracker() {
	onClick("toclevel1", disableNextScrollTracking);
	onClick("toclevel2", disableNextScrollTracking);
	var content = getBody();
	content.onscroll = checkVisibleSection;
}

function makeNavButton(iconOnRight, iconHTML, className, label, target) {
	var div = document.createElement("div");
	div.className = className;
	var a = document.createElement("a");
	a.setAttribute("href", target);
	if (iconOnRight) {
		a.innerHTML = label + "&nbsp;" + iconHTML;
	}
	else {
		a.innerHTML = iconHTML + "&nbsp;" + label;
	}
	div.appendChild(a);
	return div;
}

function getSectionTitle(sectionDiv) {
	return sectionDiv.children[0].innerHTML;
}

function getSectionTarget(sectionDiv) {
	return "#" + sectionDiv.children[0].getAttribute("id");
}

function makePrevButton(label, target) {
	return makeNavButton(false, "&ltrif;", "nav-prev", label, target);
}

function makeNextButton(label, target) {
	return makeNavButton(true, "&rtrif;", "nav-next", label, target);
}

function addNextAndPrevLinks() {
	var sect1s = document.getElementsByClassName("sect1");
	for (var i = 0; i < sect1s.length; i++) {
		var prev = null;
		var next = null;
		if (i > 0) {
			prev = makePrevButton(getSectionTitle(sect1s[i - 1]), getSectionTarget(sect1s[i - 1]));
		}
		if (i < sect1s.length - 1) {
			next = makeNextButton(getSectionTitle(sect1s[i + 1]), getSectionTarget(sect1s[i + 1]));
		}
		if (prev != null) {
			sect1s[i].appendChild(prev);
		}
		if (next != null) {
			sect1s[i].appendChild(next);
		}
	}
}

function changeTitle() {
	var header = document.getElementById("header");
	var h1 = header.children[0].children[1];
	if (h1.innerHTML.indexOf("Swift ") == 0) {
		h1.innerHTML = h1.innerHTML.substring(6);
	}
}

function initStyle() {
	changeTitle();
    addNextAndPrevLinks();
    displayCurrentSection();
    setUpScrollTracker();
    initHl();
}

var IS_FIREFOX = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
var hashDetection = new hashHandler();
