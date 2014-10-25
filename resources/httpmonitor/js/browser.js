$(window).on('hashchange', function() {
	var url = window.location.href;
	
	var index = url.indexOf("#browser");
	if (index != -1) {
		browserSetAddr(url.substring(index));
	}
});

function browserDataReceived(id, data, error) {
	if (error != null) {
		noty({text: error});
	}
	else {
		display("browser-container", document.browserDisplayTemplate, {data: data});
		updateLinks();
		resizeTabs("#browser");
	}
	pauseUpdates("browser");
}

function updateLinks() {
	$("#browser a").each(function() {
		var href = $(this).attr("href");
		if (href != null && href.indexOf("#browser") == 0) {
			$(this).click(function() {
				console.log("Navigating to " + href);
				browserSetAddr(href);
			});
		}
	});
}

function browserSetHome(addr) {
	var url = window.location.href;
	document.browserHome = addr;
	var index = url.indexOf("#browser");
	if (index != -1) {
		browserSetAddr(url.substring(index));
	}
	else {
		browserSetAddr(addr);
	}
}

function browserEnable() {
	browserSetAddr(document.browserAddr);
}

function browserDisable() {
	stopUpdatesByID("browser");
}

function browserSetAddr(addr) {
	if (document.browserAddr == addr) {
		console.log("browserSetAddr(" + addr + ") (already there)");
	}
	else {
		console.log("browserSetAddr(" + addr + ")");
		document.browserAddr = addr;
		document.browserDisplayTemplate = "browser-template-" + getCGIParam(addr, "type", "apps");
		if (window.location.href != addr) {
			window.location.href = addr;
		}
		stopUpdatesByID("browser");
		registerUpdate("browser", "browser.state" + addr, browserDataReceived);
	}
}

function browserGetAddr() {
	return document.browserAddr;
}


function getCGIParam(addr, name, defVal) {
	var index = addr.indexOf(name + "=");
	if (index == -1) {
		return defVal;
	}
	var end = addr.indexOf("&", index);
	if (end == -1) {
		return addr.substring(index + name.length + 1);
	}
	else {
		return addr.substring(index + name.length + 1, end);
	}
}

function formatTimestamp(ts) {
	return new Date(ts).toISOString();
}

function formatPercent(x) {
	return Math.round(x * 100) + "%";
}

function cutString(str, len) {
	if (str.length > len) {
		return str.substring(0, len - 3) + "...";
	}
	else {
		return str;
	}
}

function formatInterval(ms) {
	s = Math.round(ms / 1000);
	sp = s % 60;
	m = (s - sp) / 60;
	mp = m % 60;
	h = (m - mp) / 60;
	return zeroPad(h) + ":" + zeroPad(mp) + ":" + zeroPad(sp);
}

function zeroPad(v) {
	if (v < 9) {
		return "0" + v;
	}
	else {
		return v;
	}
}

var STATES_W = ["Initializing", "Sel. site", "Submitting", "Submitted", "Stage in", "Active", "Stage out"];
var STATE_COLORS = ["#984ea3", "#ff7f00", "#efe733", "#a65628", "#e41a1c", "#377eb8", "#4daf4a"];
var STATES1 = [0, 1, 2, 3, 4, 5, 6];
var STATES2 = [4, 5, 6];
var COMPLETED_COLOR = "#30d020";
var FAILED_COLOR = "#d03020";



function sumTimes1(data) {
	return sumTimes0(data, STATES1);
}

function sumTimes2(data) {
	return sumTimes0(data, STATES2);
}

function sumTimes0(data, states) {
	var sum = 0;
	for (var i = 0; i < states.length; i++) {
		sum += data[states[i]];
	}
	return sum;
}

function scaleAndFormatMS(ms) {
	if (ms < 10000) {
		return ms + " ms";
	}
	var s = ms / 1000;
	if (s < 60) {
		return s.toFixed(0) + " s";
	}
	return formatInterval(ms);
}

function countPerSitePlot(id, data) {
	stuffPerSitePlot(id, data, "completedCount", "failedCount");
}

function timePerSitePlot(id, data) {
	stuffPerSitePlot(id, data, "completedTimeAvg", "failedTimeAvg");
}

function cpuLoadSpark(sel, data) {
	var x = new Array();
	var last = null;
	for (var k in data) {
		x.push([data[k].t, Math.round(data[k].l * 100)]);
		last = data[k];
	}
	$(sel).sparkline(x, {width: "100px", height: "16px", chartRangeMin: 0, chartRangeMax: 100,
		tooltipFormat: "<span style='color: {{color}}'>&#9679;</span> {{prefix}}{{y}}{{suffix}}</span>"});
	if (last != null) {
		$(sel).after(formatPercent(last.l));
	}
}