var DEFAULT_PLOT_COLORS = ["#bf5b17", "#33a02c", "#386cb0", "#f0027f", "#ffff99", "#fdc086", 
                           "#beaed4", "#666666", "#7fc97f", "#ff7f00", "#fdbf6f", "#e31a1c"];

var MIN_AREA_WIDTH = 640;
var MIN_AREA_HEIGHT = 200;

function addPlot() {
	var index = document.currentPlotIndex++;
	$("#plots").append('\
		<div class="plot">\
			<table>\
				<tr>\
					<td><div class="plot-area"></div></td>\
				</tr>\
				<tr>\
					<td class="plot-legend" height="24px">\
						<ul>\
							<li><img src="images/plus.png" class="small-button add-series"/></li>\
							<li><img src="images/x.png" class="small-button remove-plot"/></li>\
						</ul>\
					</td>\
				</tr>\
			</table>\
		</div>');
	var id = "#plot" + index;
	$("#plots .plot").last().attr("id", "plot" + index);
	var el = $(id);
	el.attr("class", "plot");
	el.resizable({containment: "document", grid: [8, 8], 
		stop: function(event, ui) {
			saveLayout();
			resizePlotsContainer();
		}
	});
	el.draggable({containment: "document", grid: [8, 8], snap: true, 
		stop: function(event, ui) {
			saveLayout()
			resizePlotsContainer();
		}
	});
	el.resize(function() {
		fitPlot(index);
		makePlot(index);
	});
	el.bind("plothover",  function (event, pos, item) {
		plotHover(event, pos, index);
	})
	el.mouseout(function() {
		hideHoverDetail(index);
	});
	
	$(id + " .add-series").button();
	$(id + " .add-series").click(function(event) {
		console.log("Showing add series popup");
		showSeriesPopup(index, event.pageX, event.pageY);
		event.stopPropagation();
	});
	$(id + " .remove-plot").button();
	$(id + " .remove-plot").click(function(event) {
		console.log("Removing plot");
		removePlot(index);
		event.stopPropagation();
	});
	
	document.plotSeries[index] = [];
	document.plotOptions[index] = {
		yaxes: [],
		xaxis: {mode: "time", show: true, axisLabel: "Time", axisLabelUseCanvas: 
			true, labelWidth: 24, labelHeight: 24, font: {size: 10}, color: "black",
			tickFormatter: function(val, axis) {
				return new Date(val).toLocaleTimeString();
			}}, 
		legend: {show: false},
		series: {
			lines: {
				show: true
			}
		},
		crosshair: {
			mode: "x"
		},
		grid: {
			hoverable: true,
			autoHighlight: false
		}
	};
	document.plotStructure[index] = {};
	makePlot(index);
	
	saveLayout();
}

var HOVER_DETAIL_DELAY = 1000;

function plotHover(event, pos, plotIndex) {
	hideHoverDetail(plotIndex);
	// display detail if no motion for a second
	document.hoverDetail = {plotIndex: plotIndex, pos: pos, lastMotionTime: new Date().getTime()};
	window.setTimeout(enableHoverDetail, HOVER_DETAIL_DELAY);
}

function hideHoverDetail(plotIndex) {
	if (document.hoverDetail != null) {
		document.hoverDetail = null;
		hide("#plotDetail" + plotIndex);
	}
}

function enableHoverDetail(event) {
	if (!document.hoverDetail) {
		return;
	}
	if (new Date().getTime() - document.hoverDetail.lastMotionTime < HOVER_DETAIL_DELAY) {
		return;
	}
	
	var plotIndex = document.hoverDetail.plotIndex;
	var pos = document.hoverDetail.pos;
	var el = $("#plotDetail" + plotIndex);
	if (el.length == 0) {
		$("body").append('\
			<div class="plot-hover-detail" id="plotDetail' + plotIndex + '"></div>\
		');
		el = $("#plotDetail" + plotIndex);
	}
	else {
		show("#plotDetail" + plotIndex);
	}
	el.css("left", pos.pageX + "px");
	updateHoverDetail(pos);
}

function updateHoverDetail() {
	if (!document.hoverDetail) {
		return;
	}
	var plotIndex = document.hoverDetail.plotIndex;
	var pos = document.hoverDetail.pos;
	
	var el = $("#plotDetail" + plotIndex);
	
	var d = new Date(pos.x);
	var text = "Date: " + d.toDateString() + "\n";
	    text += "Time: " + d.toLocaleTimeString() + "\n";
	var series = document.plotSeries[plotIndex];
	for (var i = 0; i < series.length; i++) {
		text = text + series[i].label + ": " + getSeriesValue(plotIndex, i, pos.x) +"\n";
	}
	el.html(text);
	el.css("top", (pos.pageY - el.outerHeight() - 8) + "px");
}

function getSeriesValue(plotIndex, seriesIndex, x) {
	var series = document.plots[plotIndex].getData()[seriesIndex];
	var x = 1000 * Math.floor(x / 1000);
	var p = binSearch(x, series.data, 0, series.data.length - 1);
	if (series.yaxis.tickFormatter) {
		if (series.yaxis.options.axisLabel == "Count") {
			// "Active: 10 Count" just looks silly
			return series.yaxis.tickFormatter(p, series.yaxis);
		}
		else {
			return series.yaxis.tickFormatter(p, series.yaxis) + " " + series.yaxis.options.axisLabel;
		}
	}
	else {
		return p;
	}
}

function binSearch(value, array, begin, end) {
	if (begin == end) {
		return array[begin][1];
	}
	var mid = Math.round((begin + end) / 2);
	var vmid = array[mid][0];
	if (value == vmid) {
		return array[mid][1];
	}
	else if (value < vmid) {
		return binSearch(value, array, begin, mid - 1);
	}
	else {
		return binSearch(value, array, mid + 1, end);
	}
}

function makePlot(index) {
	fitPlot(index);
	
	var plot = $.plot($("#plot" + index + " .plot-area"), document.plotSeries[index], document.plotOptions[index]);
	
	document.plots[index] = plot;
	plot.setupGrid();
	plot.draw();
	
	resizePlotsContainer();
}

function removePlot(index) {
	delete document.plots[index];
	delete document.plotOptions[index];
	delete document.plotSeries[index];
	delete document.plotStructure[index];
	
	$("#plot" + index).remove();
	
	removeSeriesUpdate(index);
	
	saveLayout();
}

function fitPlot(index) {
	var cSel = "#plot" + index;
	var w = $(cSel).width();
	var h = $(cSel).height();
	var hs = 14;
	
	setSize(cSel + " table", w, h);
	setSize(cSel + " .plot-area", w - hs, h - 44);
	
	var canvasSel = cSel + " canvas";
	setSize(canvasSel, w - hs, h - 38);
	$(canvasSel).attr("width", (w - hs));
	$(canvasSel).attr("height", (h - 38));
}

function setSize(selector, w, h) {
	$(selector).width(w + "px");
	$(selector).height(h + "px");
}

function initializePlots() {
	$("body").append('\
		<ul id="plot-popup" class="popup-menu" style="position: absolute; left: 0px; top: -300px;">\
			<li><a href="#" id="plot-menu-item-color">Color...</a></li>\
			<li><a href="#" id="plot-menu-item-remove-series">Remove Series</a></li>\
		</ul>\
	');
	
	hide("#plot-popup");
	
	document.plotOptions = {};
	document.plotSeries = {};
	document.plots = {};
	document.currentPlotIndex = 0;
	document.plotStructure = {};
	document.seriesInfoMap = {};
	document.activePopup = null;
	
	$("html").click(function() {
		if (document.activePopup) {
			hidePopup(document.activePopup.selector);
			if (document.activePopup.hideCB) {
				document.activePopup.hideCB();
			}
		}
	});
	
	loadSeriesInfo();
	
	loadLayout();
}

function hide(id) {
	$(id).css("visibility", "hidden").css("display", "none");
}

function show(id) {
	$(id).css("visibility", "").css("display", "block");
}

function showPopup(selector, x, y, selectCB, hideCB) {
	$(selector).css("top", y + "px");
	$(selector).css("left", x + "px");
	$(selector).menu({
		select: function(event, ui) {
			console.log(selector + " item selected " + ui);
			selectCB(ui.item.children("a").attr("id"), event);
			hidePopup(selector);
			hideCB();
		}
	});
	$(selector).click(function(event) {
		console.log(selector + " stopping propagation");
	    event.stopPropagation();
	});
	document.activePopup = {selector: selector, hideCB: hideCB};
	show(selector);
}

function hidePopup(selector) {
	// need to destroy the menu because it doesn't properly
	// reset one of its internal variables after an item
	// is selected resulting in subsequent menu displays
	// ignoring the click
	hide(selector);
	$(selector).menu("destroy");
}

function showPlotPopup(plotIndex, seriesIndex, x, y) {
	console.log("showPlotPopup(" + plotIndex + ", " + seriesIndex + ")");
	document.currentPlot = plotIndex;
	document.currentSeries = seriesIndex;
	showPopup("#plot-popup", x, y, plotPopupOptionSelected, hidePlotPopup);
}

function showSeriesPopup(plotIndex, x, y) {
	console.error("showSeriesPopup(" + plotIndex + ")");
	document.currentPlot = plotIndex;
	showPopup("#series-popup", x, y, seriesPopupOptionSelected, hideSeriesPopup);
}

function hidePlotPopup() {
	document.currentPlot = -1;
	document.currentSeries = -1;
}

function hideSeriesPopup() {
	document.currentPlot = -1;
}

function plotPopupOptionSelected(itemId, event) {
	var plotIndex = document.currentPlot;
	var seriesIndex = document.currentSeries;
	console.log("Begin handle " + plotIndex + ", " + seriesIndex + ", " + itemId);
	if (itemId == "plot-menu-item-remove-series") {
		removeSeries(plotIndex, seriesIndex);
	}
	else if (itemId == "plot-menu-item-color") {
		showColorPicker(plotIndex, seriesIndex, event.pageX, event.pageY);
	}
	console.log("End handle " + itemId);
}

function seriesPopupOptionSelected(key, event) {
	var plotIndex = document.currentPlot;
	console.log("Begin handle " + key);
	addSeries(plotIndex, document.seriesInfoMap[key], key);
	console.log("End handle " + key);
}

function showColorPicker(plotIndex, seriesIndex, x, y) {
	console.log("Show color picker");
	var sel = "#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ") .legend-color";
	$(sel).append('<div id="color-picker"><input type="text"></input></div>');
	$("#color-picker input").spectrum({flat: true, showInput: true, color: getSeriesColor(plotIndex, seriesIndex), 
		cancelText: "Cancel", chooseText: "Select",
		showInput: true, showInitial: true, preferredFormat: "hex",
		change: function(color) {
			console.log("Color change: " + color);
			$("#color-picker").remove();
			setSeriesColor(plotIndex, seriesIndex, color.toHexString());
		},
	});
	
	$(".sp-cancel").button();
	$(".sp-choose").button();
}

function getSeriesColor(plotIndex, seriesIndex) {
	return document.plots[plotIndex].getData()[seriesIndex].color;
}

function setSeriesColor(plotIndex, seriesIndex, color) {
	document.plots[plotIndex].getData()[seriesIndex].color = color;
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ") .legend-color").css("background-color", color);
	document.plots[plotIndex].draw();
	document.plotSeries[plotIndex][seriesIndex].color = color;
	saveLayout();
}

function getMethods(obj) {
    var res = [];
    for(var m in obj) {
        if(typeof obj[m] == "function") {
            res.push(m)
        }
    }
    return res;
}

function removeSeries(plotIndex, seriesIndex) {	
	document.plotSeries[plotIndex].splice(seriesIndex, 1);
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ")").remove();
	makePlot(plotIndex);
	
	removeSeriesUpdate(plotIndex, seriesIndex);
	saveLayout();
}

function addSeries(plotIndex, seriesInfo, seriesKey, color) {
	var axes = document.plotStructure[plotIndex];
		
	var axisNumber;
	
	if (seriesInfo.unit in axes) {
		axisNumber = axes[seriesInfo.unit].index;
	}
	else {
		axisNumber = createAxis(plotIndex, seriesInfo);
		axes[seriesInfo.unit] = {};
		axes[seriesInfo.unit].index = axisNumber;
		axes[seriesInfo.unit].usageCount = 0;
	}
	
	axes[seriesInfo.unit].usageCount++;
	
	var series = document.plotSeries[plotIndex];
	var seriesIndex = series.length;
	if (!color) {
		color = DEFAULT_PLOT_COLORS[seriesIndex];
	}
	series.push({label: seriesInfo.label, shadowSize: 0, color: color, data: [], yaxis: axisNumber});
	addLegendItem(plotIndex, seriesIndex, seriesInfo, color);
	
	setupSeriesUpdate(seriesKey, plotIndex, seriesIndex);
	makePlot(plotIndex);
	saveLayout();
}

function addLegendItem(plotIndex, seriesIndex, seriesInfo, color) {
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ")").before('\
		<li>\
			<div class="legend-button"><div class="legend-color"></div></div><span class="legend-label">' + seriesInfo.label + '</span>\
		</li>\
	');
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ") .legend-button").button();
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ") .legend-button").click(function(event) {
		console.log("Showing series popup");
		showPlotPopup(plotIndex, seriesIndex, event.pageX, event.pageY);
		event.stopPropagation();
	});
	$("#plot" + plotIndex + " .plot-legend ul li:nth-child(" + (seriesIndex + 1) + ") .legend-color").css("background-color", color);
}

function createAxis(plotIndex, seriesInfo) {	
	var yaxes = document.plotOptions[plotIndex].yaxes;
	var index = yaxes.length;
	
	var pos = index == 0 ? "left" : "right";
	var formatter;
	if (seriesInfo.unitType == "SI") {
		formatter = formatTickSI;
	}
	else if (seriesInfo.unitType == "P2") {
		formatter = formatTickP2;
	}
	else {
		formatter = formatTickFixed;
	}
	yaxes.push({show: true, axisLabel: seriesInfo.unit, axisLabelUseCanvas: true, 
		labelWidth: 28, labelHeight: 24, position: pos, tickWidth: 24,
		font: {size: 10}, color: "black",
		tickFormatter: function(val, axis) {
			return formatter(val, axis, plotIndex, index, seriesInfo);
		}});
	
	console.log("Adding axis " + seriesInfo.unit + " for plot " + plotIndex);
	return index + 1;
}

function formatTickSI(val, axis, plotIndex, axisIndex, seriesInfo) {
	if (!axis.log) {
		lm = getLogAndMultiplier(1000, Math.max(Math.abs(axis.min), Math.abs(axis.max)));
		axis.log = lm.log;
		axis.mul = lm.mul;
		var axisOpt = axis.options; 
		axisOpt.axisLabel = getPrefixSI(axis.log) + seriesInfo.unit;
		axisOpt.tickWidth = 48;
		axisOpt.labelWidth = 52;
	}
	val = val * axis.mul;
	return val.toFixed(3);
}

function formatTickP2(val, axis, plotIndex, axisIndex, seriesInfo) {
	lm = getLogAndMultiplier(1024, Math.max(Math.abs(axis.min), Math.abs(axis.max)));
	if (!axis.log || axis.log != lm.log) {
		axis.log = lm.log;
		axis.mul = lm.mul;
		var axisOpt = axis.options;
		axisOpt.axisLabel = getPrefixP2(axis.log) + seriesInfo.unit;
		axisOpt.tickWidth = 48;
		axisOpt.labelWidth = 52;
	}
	val = val * axis.mul;
	return val.toFixed(3);
}

function getPrefixP2(om) {
	switch (om) {
		case 0: return "";
		case 1: return "K";
		case 2: return "M";
		case 3: return "G";
		case 4: return "T";
		case 5: return "P";
		case 6: return "E";
		default:
			    return "?";
	}
}

function getPrefixSI(om) {
	switch (om) {
		case -5: return "f";
		case -4: return "p";
		case -3: return "n";
		case -2: return "μ";
		case -1: return "m";
		case 0: return "";
		case 1: return "K";
		case 2: return "M";
		case 3: return "G";
		case 4: return "T";
		case 5: return "P";
		default:
			    return "?";
	}
}


function getLogAndMultiplier(base, value) {
	var l = 0;
	var m = 1;
	
	while (value > base) {
		l = l + 1;
		m = m / base;
		value = value / base;
	}
	while (value < 1 / base) {
		l = l - 1;
		m = m * base;
		value = value * base;
	}
	return {log: l, mul: m};
}

function formatTickFixed(val, axis, plotIndex, axisIndex, seriesInfo) {
	var r = Math.round(val);
	if (r != val) {
		return "";
	}
	else {
		return r;
	}
}

function setupSeriesUpdate(seriesKey, plotIndex, seriesIndex) {
	if (!document.updateInfo) {
		document.updateInfo = {};
	}
	
	if (!(seriesKey in document.updateInfo)) {
		document.updateInfo[seriesKey] = {lastTime: 0, targets: []};
		registerUpdate(seriesKey, "plotData.state", seriesDataCB, function() {
			var ui = document.updateInfo[seriesKey];
			if (!ui) {
				console.error("Series info not found for " + seriesKey);
				console.mlog(document.updateInfo);
			}
			return {name: seriesKey, start: ui.lastTime};
		});
	}
	
	document.updateInfo[seriesKey].targets.push({plot: plotIndex, series: seriesIndex});
}

function removeSeriesUpdate(plotIndex, seriesIndex, seriesKey) {
	var keys = [];
	if (seriesKey) {
		keys.push(seriesKey);
	}
	else {
		// search through all
		for (var k in document.updateInfo) {
			keys.push(k);
		}
	}
	
	for (var s = 0; s < keys.length; s++) {
		seriesKey = keys[s];
		var a = document.updateInfo[seriesKey].targets;
		for (var i = 0; i < a.length; i++) {
			var v = a[i];
			if (v.plot == plotIndex && (!seriesIndex || v.series == seriesIndex)) {
				a.splice(i, 1);
				if (a.length == 0) {
					delete document.updateInfo[seriesKey];
				}
				return;
			}
		}
	}
}

console.mlog = function(obj) {
	console.log(JSON.stringify(obj, undefined, 2));
}

function seriesDataCB(seriesKey, rdata, error) {
	if (error == null) {
		try {
			var ui = document.updateInfo[seriesKey];
			for (var i = 0; i < ui.targets.length; i++) {
				var target = ui.targets[i];
				var plot = document.plots[target.plot];
				var pdata = plot.getData();
				var sdata = pdata[target.series].data;
				for (var j = 0; j < rdata.length; j++) {
					sdata.push(rdata[j]);
				}
				
				ui.lastTime = rdata[rdata.length - 1][0];
				
				plot.setData(pdata);
				plot.setupGrid();
				plot.draw();
				if (document.hoverDetail != null && document.hoverDetail.plotIndex == target.plot) {
					plot.setCrosshair(document.hoverDetail.pos);
				}
				//makePlot(target.plot);
			}
			if (document.runFinished) {
				stopUpdatesByID(seriesKey);
			}
		}
		catch (err) {
			stopUpdatesByID(seriesKey);
			console.error(err);
		}
	}
}

function loadSeriesInfo() {
	console.log("Requesting series info...");
	asyncRequest("load-series", "plotSeriesInfo.state", loadSeriesInfoCB);
}

function loadSeriesInfoCB(id, data, error) {
	if (error != null) {
		console.log(error);
		document.seriesInfo = new Array();
	}
	else {
		document.seriesInfo = data;
	}
	createSeriesInfoMap();
	createSeriesPopup();
}

/**
 * Makes an id -> seriesInfo map for 
 * quick access based on id
 */
function createSeriesInfoMap() {
	var map = {};
	for (var i = 0; i < document.seriesInfo.length; i++) {
		var si = document.seriesInfo[i];
		for (var j = 0; j < si.series.length; j++) {
			var sj = si.series[j];
			
			map[sj.key] = sj;
		}
	}
	document.seriesInfoMap = map;
}

function createSeriesPopup() {
	$("body").append('\
		<ul id="series-popup" class="popup-menu" style="position: absolute">\
		</ul>\
	');
	for (var i = 0; i < document.seriesInfo.length; i++) {
		var si = document.seriesInfo[i];
		$("#series-popup").append('\
			<li><a href="#">' + si.category + '</a>\
			<ul>\
			</ul>\
		');
		
		for (var j = 0; j < si.series.length; j++) {
			var sj = si.series[j];
			$("#series-popup li:nth-child(" + (i + 1) + ") ul").append('<li><a href="#" id="' + 
					sj.key + '">' + sj.label + '</a></li>');
		}
	}
	
	hide("#series-popup");
}

function resizePlotsContainer() {
	var w = 0, h = 0;
	
	for (var i = 0; i < document.currentPlotIndex; i++) {
		var p = $("#plot" + i);
		if (p.length != 0) {
			var pos = p.position();
			var wi = p.outerWidth() + pos.left;
			var hi = p.outerHeight() + pos.top;
			if (w < wi) {
				w = wi;
			}
			if (h < hi) {
				h = hi;
			}
		}
	}
	
	if (w < $("#tabs").width() && h < $("#tabs").height()) {
		return;
	}
	
	if (w < MIN_AREA_WIDTH) {
		w = MIN_AREA_WIDTH;
	}
	if (h < MIN_AREA_HEIGHT) {
		h = MIN_AREA_HEIGHT;
	}
	
	$("#tabs").css("width", (w + 8) + "px");
	$("#tabs").css("height", (h + 8) + "px");
}

function saveLayout() {
	if (document.inhibitLayoutSaving) {
		return;
	}
	
	var plots = {};
	
	for (var k1 in document.updateInfo) {
		var uio = document.updateInfo[k1];
		
		for (var k2 = 0; k2 < uio.targets.length; k2++) { 
			var ui = uio.targets[k2];
						
			var plot = plots[ui.plot];
			if (!plot) {
				var cSel = "#plot" + ui.plot;
				var w = $(cSel).width();
				var h = $(cSel).height();
				var pos = $(cSel).offset();
				
				plot = {w: w, h: h, x: pos.left, y: pos.top, series: []};
				plots[ui.plot] = plot;
			}
			
			var series = plot.series; 
			
			
			var sdata = document.plotSeries[ui.plot][ui.series];
			var si = document.seriesInfoMap[k1];
			
			if (!si) {
				si = {unit: "N/A", unitType: "FIXED"};
			}
						
			series.push({key: k1, seriesIndex: ui.series, color: sdata.color, seriesInfo: si});
		}
	}
	
	document.cookie = "layout=" + encodeURIComponent(JSON.stringify(plots, undefined, 2)) + "; path=/";

	console.mlog("Saved layout");
}

function getSavedLayout() {
	var v = document.cookie;
	var start = v.indexOf("layout=");
	
	if (start < 0) {
		// no saved layout
		return null;
	}
	
	var end = v.indexOf(";", start);
	if (end == -1) {
		end = v.length;
	}
	
	v = JSON.parse(decodeURIComponent(v.substring(start + "layout=".length, end)));
	
	return v;
}

function loadLayout() {
	document.inhibitLayoutSaving = true;
	try {
		var v = getSavedLayout();
		
		if (!v) {
			return;
		}
		
		console.mlog("Loaded layout");
		
		for (var plotIndex in v) {
			plotIndex = parseInt(plotIndex);
			addPlot();
			var plot = v[plotIndex];
			
			$("#plot" + plotIndex).width(plot.w);
			$("#plot" + plotIndex).height(plot.h);
			$("#plot" + plotIndex).css("position", "absolute");
			$("#plot" + plotIndex).css("top", plot.y + "px");
			$("#plot" + plotIndex).css("left", plot.x + "px");
			
			console.log("Plot layout " + plotIndex + ": (" + plot.x + ", " + plot.y + "), (" + plot.w + " x " + plot.h + ")");
			
			for (var i = 0; i < plot.series.length; i++) {
				var s = plot.series[i];
				
				console.mlog(s);
				
				addSeries(plotIndex, s.seriesInfo, s.key, s.color);
				console.log("Plot series: " + s.key);
				
				fitPlot(plotIndex);
				makePlot(plotIndex);
			}
		}
	}
	catch (err) {
		console.error("Error loading layout: " + err);
	}
	finally {
		document.inhibitLayoutSaving = false;
	}
}

