function stateTimesChart(id, data) {
	var x = new Array(data.length);
	
	for (var i = 0; i < data.length; i++) {
		x[i] = [STATES_W[i], data[i]]; 
	}

	$.plot(id, [ x ], {
		series: {
			bars: {
				show: true,
				barWidth: 0.6,
				align: "center",
				lineWidth: 0,
				fillColor: "#00009f"
			}
		},
		grid: {
			show: true,
			backgroundColor: "#ffffff"
		},
		xaxis: {
			mode: "categories",
			tickLength: 0
		}
	});
}

function stateTimesPie1(id, data) {
	stateTimesPie0(id, data, STATES_PIE_1);
}

function stateTimesPie2(id, data) {
	stateTimesPie0(id, data, STATES_PIE_2);
}

function stateTimesPie0(id, data, states) {
	var x = new Array(states.length);
	
	for (var i = 0; i < states.length; i++) {
		x[i] = {label: STATES_W[states[i]], data: data[states[i]], color: STATE_COLORS[states[i]]}; 
	}

	$.plot(id, x , {
		series: {
			pie: {
				show: true
			}
		},
		legend: {
			show: true
		}
	});
}

function stateTimesStrip1(id, data) {
	var x = new Array();
	var index = 0;
	for (var j = 0; j < data.length; j++) {
		for (var i = 0; i < STATES1.length; i++) {
			if (STATES1[i] == data[j][0]) {
				x[index++] = {label: STATES_W[STATES1[i]], color: STATE_COLORS[STATES1[i]], data: [[data[j][1], 1]]};
			}
		}
	}
	makeLegend();
	stateTimesStripPlot(id, x, null);
}

function makeLegend() {
	var table = $("#legend").append($("<table>"));
	for (var i = 0; i < STATES1.length; i++) {
		var tr = $("<tr>");
		var tdc = $("<td>");
		var tdl = $("<td>");
		tdc.css("background-color", STATE_COLORS[STATES1[i]]);
		tdl.html(STATES_W[STATES1[i]]);
		table.append(tr);
		tr.append(tdc);
		tr.append(tdl);
	}
}

function stateTimesStrip2(id, data) {
	var lastStageInIndex = -1;
	for (var j = 0; j < data.length; j++) {
		if (data[j][0] == 2) {
			lastStageInIndex = j;
		}
	}
	if (lastStageInIndex != -1) {
		var x = new Array();
		var index = 0;
		for (var j = lastStageInIndex; j < data.length; j++) {
			for (var i = 0; i < STATES1.length; i++) {
				if (STATES1[i] == data[j][0]) {
					x[index++] = {label: STATES_W[STATES1[i]], color: STATE_COLORS[STATES1[i]], data: [[data[j][1], 1]]};
				}
			}
			// failed or completed
			if (data[j][0] == 7 || data[j][0] == 10) {
				break;
			}
		}
		stateTimesStripPlot(id, x, null);
	}
}

function stateTimesStripPlot(id, x, legendid) {	
	$.plot(id, x , {
		series: {
			stack: true,
			bars: {
				show: true,
				barWidth: 0.6,
				horizontal: true,
				lineWidth: 1,
				fill: 1
			}
		},
		grid: {
			show: false
		},
		legend: {
			show: legendid != null,
			container: legendid == null ? null : $(legendid)
		},
		yaxis: {
			mode: "categories",
			tickLength: 0
		}
	});
}


function histogram(id, data, min, max, color, logScale, xticks) {
	if (xticks == null) {
		xticks = NULL_TICKS;
	}
	if (typeof(xticks) == "function") {
		xticks = xticks(min, max);
	}
	var x = new Array(data.length);
	
	//console.log("min: " + min + ", max: " + max);

	var ymax = 0;
	for (var i = 0; i < data.length; i++) {
		var p = 1.0 * i / data.length;
		var v = p * (max - min) + min;
		x[i] = [v, data[i]];
		if (data[i] > ymax) {
			ymax = data[i];
		}
	}
	
	//console.log("Histogram data:");
	//console.log(x);
	
	var ds = [{data: x}];

	if (logScale) {
		$.plot(id, ds , {
			series: {
				lines: {
					show: true,
					fill: true,
					fillColor: color
				},
				shadowSize: 0
			},
			xaxis: {
				tickSize: xticks.size,
				tickFormatter: xticks.formatter
			},
			yaxis: {
				ticks: [[1, "1"], [10, "10"], [100, "100"], [1000, "1k"], [10000, "10k"], [1e5, "100k"], [1e6, "1m"], [1e7, "10m"]],
				transform: function(y) {
					if (y == 0) {
						return 0;
					}
					else {
						return Math.log(y + 1);
					}
				},
				min: 0,
				max: ymax
			},
			colors: [color]
		});
	}
	else {
		$.plot(id, ds , {
			series: {
				lines: {
					show: true,
					fill: true,
					fillColor: color
				},
				shadowSize: 0
			},
			colors: [color]
		});
	}
}

function getTickUnit(min, max) {
	if (max < 1000) {
		return "ms";
	}
	else if (max < 5 * 60 * 1000) {
		return "s";
	}
	else if (max < 5 * 3600 * 1000) {
		return "m";
	}
	else {
		return "h";
	}
}

var NULL_TICKS = {size: null, formatter: null}
var TIME_INTERVAL_TICKS = timeIntervalTicks

function timeIntervalTicks(min, max) {
	var u = getTickUnit(max);
	if (u == "ms") {
		return {size: max / 7, formatter: function(val, axis) {return val.toFixed(0) + "ms";}}
	}
	else if (u == "s") {
		return {size: max / 7, formatter: function(val, axis) {return (val / 1000).toFixed(1) + "s";}}
	}
	else if (u == "m") {
		return {size: round(max / 7, 60 * 1000), formatter: function(val, axis) {return (val / 1000 / 60).toFixed(0) + "m";}}
	}
	else if (u == "h") {
		return {size: round(max / 7, 3600 * 1000), formatter: function(val, axis) {return (val / 1000 / 3600).toFixed(0) + "h";}}
	}
}

function round(x, n) {
	if (x < n) {
		return n;
	}
	else {
		return Math.round(x / n) * n;
	}
}

function stuffPerSitePlot(id, data, cprop, fprop, yticks) {
	if (yticks == null) {
		yticks = NULL_TICKS;
	}
	var cseries = {data: [], label: "Completed", color: COMPLETED_COLOR};
	var fseries = {data: [], label: "Failed", color: FAILED_COLOR};
	var ymax = Number.MIN_VALUE;
	var index = 0;
	var ticks = new Array();
	var siteNames = new Array();
		
	for (var site in data) {
		var y = data[site][cprop] + data[site][fprop];
		if (y > ymax) {
			ymax = y;
		}
		ticks.push(index + 0.5);
		siteNames.push(data[site].name);
		cseries.data.push([index++, data[site][cprop]]);
		fseries.data.push([index++, data[site][fprop]]);
	}
	
	var ds = [cseries, fseries];
	
	if (typeof(yticks) == "function") {
		yticks = yticks(0, ymax);
	}

	$.plot(id, ds , {
		series: {
			bars: {
				show: true,
				align: "center",
				fill: 1,
				barWidth: 0.8
			},
			shadowSize: 0
		},
		xaxis: {
			font: { size: 12, weight: "bold", color: "#000000" },
			ticks: ticks,
			tickFormatter: function(x, axis) {
				return siteNames[(x - 0.5) / 2];
			}
		},
		yaxis: {
			tickSize: yticks.size,
			tickFormatter: yticks.formatter
		},
		legend: {
			show: true,
		},
		grid: {
			axisMargin: 32,
		},
		colors: [COMPLETED_COLOR, FAILED_COLOR]
	});
}

function cpuLoadPlot(sel, data) {
	var x = new Array();
	for (var k in data) {
		x.push([data[k].t, Math.round(data[k].load * 100)]);
	}
	
	$.plot(sel, [x], {
		xaxis: {mode: "time"},
		yaxis: {
			tickFormatter: function(v, o) {
				return v + "%";
			}
		},
		series: {
			lines: {
				lineWidth: 1,
				show: true,
				fill: true,
			},
			shadowSize: 0,
			color: "#4040ff"
		}
	});
}

var UNITS = ["B", "KB", "MB", "GB", "TB"];

function formatStorage(v) {
	var shift = 0;
	while (v > 1024) {
		v = v / 1024;
		shift++;
	}
	return Math.round(v) + UNITS[shift];
}

function formatThroughput(v) {
	var shift = 0;
	while (v > 1024) {
		v = v / 1024;
		shift++;
	}
	return Math.round(v) + UNITS[shift] + "/s";
}

function diskUsagePlots(prefix, data) {
	for (var i = 0; i < data.length; i++) {
		var d2 = data[i];
		var used = new Array();
		var avail = new Array();
		for (var j in d2.data) {
			used.push([d2.data[j].t, d2.data[j].used]);
			avail.push([d2.data[j].t, d2.data[j].used + d2.data[j].avail]);
		}
	
		$.plot(prefix + i, [{color: "#ff4000", data: used}, {color: "#40ff00", data: avail}], {
			xaxis: {
				mode: "time",
				ticks: 2
			},
			yaxis: {
				tickFormatter: formatStorage,
				min: 0,
				max: d2.data[0].used + d2.data[0].avail
			},
			series: {
				stack: true,
				lines: {
					lineWidth: 1,
					show: true,
					fill: true,
				},
				shadowSize: 0,
			}
		});
	}
}

function ioLoadPlots(prefix, data, key, formatter, color) {
	for (var i = 0; i < data.length; i++) {
		var d2 = data[i];
		var y = new Array();
		for (var j in d2.data) {
			y.push([d2.data[j].t, d2.data[j][key]]);
		}
	
		$.plot(prefix + i, [{color: color, data: y}], {
			xaxis: {
				mode: "time",
				ticks: 2
			},
			yaxis: {
				tickFormatter: formatter
			},
			series: {
				lines: {
					lineWidth: 1,
					show: true,
					fill: true,
				},
				shadowSize: 0,
			}
		});
	}
}
