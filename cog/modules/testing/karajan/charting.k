/* 
 * The charting library is a wrapper around jfreechart classes
 */
import("sys.k")
import("java.k")
 

namespace("charting"
  export(
	element(DateAxis, [title],
		set(axis, java:new("org.jfree.chart.axis.DateAxis", title))
		
		java:invokeMethod("setDateFormatOverride", object=axis, types=["java.text.DateFormat"],
			java:invokeMethod("getDateTimeInstance", classname="java.text.DateFormat")
		)
		java:invokeMethod("setTickMarksVisible", object=axis, types=["boolean"], true())
		
		axis
	)
	
	element(numberAxis, [title]
		java:new("org.jfree.chart.axis.NumberAxis", title)
	)
	
	element(SymbolicAxis, [title, items]
		java:new(classname="org.jfree.chart.axis.SymbolicAxis", types=["java.lang.String", "java.lang.String[]"], title, items)
	)

	element(TimeSeriesChart, [title, legend, xaxis, yaxis, data]
		java:invokeMethod("createTimeSeriesChart", classname="org.jfree.chart.ChartFactory"
			types=["java.lang.String", "java.lang.String", "java.lang.String", 
					"org.jfree.data.XYDataset", "boolean", "boolean", "boolean"]
			title, xaxis, yaxis, data, legend, false, false
		)
	)
	
	element(XYSeriesCollection, [...]
		set(xysc, java:new("org.jfree.data.XYSeriesCollection"))
		
		for(arg, ...
			java:invokeMethod(object=xysc, method="addSeries", types="org.jfree.data.XYSeries", arg)
		)
		
		xysc
	)

	
	element(Chart, [title, plot]
		java:new(classname="org.jfree.chart.JFreeChart", types="java.lang.String, org.jfree.chart.plot.Plot", title, plot)
	)
	
	element(StandardXYItemRenderer, [shapes, filled]
		set(renderer, java:new("org.jfree.chart.renderer.StandardXYItemRenderer"))
		
		java:invokeMethod("setPlotShapes", object=renderer, types="boolean", shapes)
		java:invokeMethod("setShapesFilled", object=renderer, types="boolean", filled)
		
		renderer
	)

	
	element(CombinedDomainXYPlot, [optional(domainaxis, shapes, filled), ...]
		default(shapes, false)
		default(filled, false)

		set(cdxyp,
			java:new("org.jfree.chart.plot.CombinedDomainXYPlot", maybe(types="org.jfree.chart.axis.ValueAxis", domainaxis))
		)
		java:invokeMethod("setRenderer", object=cdxyp, types="org.jfree.chart.renderer.XYItemRenderer",
			StandardXYItemRenderer(shapes, filled)
		)
		for(arg, ...
			java:invokeMethod("add", object=cdxyp, types="org.jfree.chart.plot.XYPlot", arg)
		)
		
		cdxyp
	)
	
	
	element(StandardXYPlot, [optional(valueaxis, shapes, filled), ...]
		default(shapes,	false)
		default(filled, false)
		java:new("org.jfree.chart.plot.XYPlot"
			types="org.jfree.data.XYDataset, org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis, org.jfree.chart.renderer.XYItemRenderer"
			XYSeriesCollection(...())
			java:null()
			choice(
				valueaxis
				null()
			)
			StandardXYItemRenderer(shapes, filled)
		)
	)
	
	element(ChartPanel, [chart]
		java:new("org.jfree.chart.ChartPanel", types="org.jfree.chart.JFreeChart", chart)
	)
	
	element(contentPane, [frame]
		java:invokeMethod(object=frame, method="getContentPane")
	)
	
	element(ChartFrame, [chart]
		print("chart: {chart}")
		set(panel, chartPanel(chart))
		set(frame, java:new("javax.swing.JFrame"))
		set(contentPane, contentPane(frame))
		
		java:invokeMethod("add", object = contentPane, types="java.awt.Component", panel)
		java:invokeMethod("pack", object= frame)
		java:invokeMethod("show", object= frame)
	)
	
	element(XYSeriesCollection, [...]
		set(xysc, java:new("org.jfree.data.XYSeriesCollection"))

		for(arg, ...
			java:invokeMethod("addSeries", object=xysc, types="org.jfree.data.XYSeries", arg)
		)
		
		xysc
	)
	
	element(XYSeries, [name, ...]
		set(xys, java:new("org.jfree.data.XYSeries", name))
		
		for(arg, ...
			java:invokeMethod("add", object=xys, types=["double", "double"], each(arg))
		)
		
		xys
	)
	
	element(CombinedDataset, [...]
		set(cd, java:new("org.jfree.data.CombinedDataset"))

		for(arg, ...
			java:invokeMethod("add", object=cd, types="org.jfree.data.SeriesDataset", arg)
		)
		
		cd
	)
		
	element(XYData, [x, y]
		list(x, y)
	)
	
	
	element(SaveChartAsPNG, [file, optional(width, height), chart]
		default(width, 640)
		default(height, 400)
		java:invokeMethod(classname="org.jfree.chart.ChartUtilities", method="saveChartAsPNG"
			types=["java.io.File", "org.jfree.chart.JFreeChart", "int", "int"], file, chart, width, height)
	)
  )
)
