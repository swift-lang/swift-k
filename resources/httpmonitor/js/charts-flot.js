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
	stateTimesPie0(id, data, STATES1);
}

function stateTimesPie2(id, data) {
	stateTimesPie0(id, data, STATES2);
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
	stateTimesStrip0(id, data, STATES1, "#legend");
}

function stateTimesStrip2(id, data) {
	stateTimesStrip0(id, data, STATES2, null);
}

function stateTimesStrip0(id, data, states, legendid) {
	var x = new Array(states.length);
	//x[0] = ["State", "Duration"];
	for (var i = 0; i < states.length; i++) {
		//x[i] = [{label: STATES_W[states[i]], data: 0.0, color: STATE_COLORS[states[i]]}];
		//x[i + 1] = [STATES_W[states[i]], 0.0];
		x[i] = {label: STATES_W[states[i]], color: STATE_COLORS[states[i]], data: [[0.0, 1]]};
		for (var j = 0; j < data.length; j++) {
			if (states[i] == data[j][0]) {
				x[i].data[0][0] = data[j][1];
			}
		}
	}
	
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

function histogram(id, data, min, max, color, logScale) {
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

function stuffPerSitePlot(id, data, cprop, fprop) {
	var cseries = {data: [], label: "Completed", color: COMPLETED_COLOR, };
	var fseries = {data: [], label: "Failed", color: FAILED_COLOR};
	for (var site in data) {
		cseries.data.push([data[site].name, data[site][cprop]]);
		fseries.data.push([data[site].name, data[site][fprop]]);
	}
		
	var ds = [cseries, fseries];

	$.plot(id, ds , {
		series: {
			stack: true,
			bars: {
				show: true,
				barWidth: 0.6,
				align: "center",
				fill: 1
			},
			shadowSize: 0
		},
		xaxis: {
			mode: "categories",
			font: { size: 12, weight: "bold", color: "#000000" }
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
