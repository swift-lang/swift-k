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
	document.browserHome = addr;
}

function browserEnable() {
	if (document.browserAddr == null) {
		browserSetAddr(document.browserHome);
	}
	else {
		browserSetAddr(document.browserAddr);
	}
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
	return new Date(ts).toISOString().replace("T", " ");
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
	return zeroPad(h, 2) + "h " + zeroPad(mp, 2) + "m " + zeroPad(sp, 2) + "." + zeroPad(ms % 1000, 3) + "s";
}

function zeroPad(v, len) {
	var s = v.toString();
	while (s.length < len) {
		s = "0" + s;
	}
	return s;
}

var STATES_W = ["Initializing", "Sel. site", "Stage in", "Submitting", "Submitted", "Active", "Stage out", "Retrying", 
                "Replicating", "Finished in prev. run", "Completed", "Failed"];
var STATE_COLORS = ["#77a1b5", "#e03fd8", "#0307d4", "#7bc8f6", "#f9bc08", "#c9ff27", "#058907", "#fd411e"];
var STATES1 = [0, 1, 2, 3, 4, 5, 6, 7];
var STATES2 = [2, 3, 4, 5, 6, 7];
var COMPLETED_COLOR = "#50f010";
var FAILED_COLOR = "#f06000";

var STATES_PIE_1 = [0, 1, 2, 3, 4, 5, 6];
var STATES_PIE_2 = [2, 3, 4, 5, 6];

function sumTimes1(data) {
	return sumTimes0(data, STATES_PIE_1);
}

function sumTimes2(data) {
	return sumTimes0(data, STATES_PIE_2);
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

function timePerSitePlot(id, data, yticks) {
	stuffPerSitePlot(id, data, "completedTimeAvg", "failedTimeAvg", yticks);
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