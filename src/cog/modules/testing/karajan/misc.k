/*
 * All kinds of utility elements
 */

import("sys.k") 
import("java.k")
import("task.k")
import("html.k")

export(
	element(basename, [file]
		last(split(file, "/"))
	)
	
	element(dirname, [file]
		concat(
			set(elements, butLast(split(file, "/")))
			
			for(element, elements
				element
				"/"
			)
		)
	)
		
	element(date:now, [], java:new("java.util.Date"))

	/*
	 * Writes an error with trace information
	 * to an html file
	 */	
	element(error, [error, trace, exception]
		set(errorFile, concat("error", uid(), ".html"))
		
		a(href = errorFile, style="color: #000000", "Failed")

		html:write("output/{errorFile}"
			html:quickstart("Test error",
				h2("Karajan stack trace:")
				pre(trace)
				br() br()
				h2("Java stack trace:")
				pre(exception)
			)
		)
	)

	/* 
	 * A bunch of elements that define
	 * the possible results of a test.
	 * They could (should?) also be constants/globals
	 */	
	element(status:timeout, [], 1)
	
	element(status:failed, [], 0)
	
	element(status:passed, [], 2)

	element(status:na, [], "n/a")

	/*
	 * Checks if an element completes within a given time.
	 * '#' is a special argument name (similar to ...) that
	 * is used to get a map of all the arguments received by
	 * this element.
	 */	
	element(timeoutWrapper,  [timeout, wrapped, provider, optional(host, host1, host2), #]
		race(
			//whichever is done first
			sequential(
				wait(delay = timeout)
				print("Timeout")
				status:timeout()
				td("Timed-out", bgcolor = COLOR:TIMEOUT)
			)

			choice(
				//whichever does not fail
				sequential(
					executeElement(wrapped, args = #)
				)
				sequential(
					print("Failed")
					status:failed()

					td("", bgcolor = COLOR:FAILED, error(error, trace, exception))
				)
			)
		)
	)

	element(hex4, [value]
		if(
			value < 10 value
			value == 10 "a"
			value == 11 "b"
			value == 12 "c"
			value == 13 "d"
			value == 14 "e"
			value == 15 "f"
		)
	)

	element(nibbles, [value]
		if(value > 15, nibbles(int(value/16)))
		value % 16
	)

	element(hex, [value, optional(pad)]
		default(pad, 0)
		set(nibbles, list(nibbles(int(value))))
		concat(
			if(
				list:size(nibbles) < pad
				then(
					for(i, range(1, pad - list:size(nibbles)), "0")
				)
			)
			for(nibble, list(nibbles(int(value))), hex4(nibble))
		)
	)

	element(hex8, [value]
		hex(value, pad = 2)
	)

	element(colorizedTime, [value]
		set(v, value / 200)
		set(log, ln(1 + 255 / (1 + v)))
		set(red, hex8(192 - 33 * log))
		set(green, hex8(33 * log))
		"<div style='background-color: #{red}{green}00; border: solid 1px #{red}{green}00; color: #ffffff;'>{value} ms</div>"
	)

	/*
	 * Checks for invalid provider errors when executing
	 * a task. They mean that the host does not have a service
	 * for the respective provider
	 */
	element(testWrapper,  [provider, test, type, host, optional(host2)]
		if(host:hasService(host, type, provider) & choice(host:hasService(host2, type, provider), true)
			choice(
				sequential(
					set(time, time(executeElement(test, host, maybe(host2), provider)))
					print("Completed")
					status:passed()
					set(ctime, colorizedTime(time))
					td(bgcolor = COLOR:PASSED, "Passed {ctime}")
				)
				catch("(.*[Ee]xpired credentials.*)|(.*Proxy file.*not found.*)"
					print("Invalid proxy")
					status:na()
					td(bgcolor = "#b0b090", "Expired credentials")
				)
			)
			else(
				print("No service")
				status:na()
				td(bgcolor = "#b0b0b0", "")
			)
		)
	)
	
	element(proxyIsValid, []
		sequentialChoice(
			sequential(
				discard(
					java:invokeMethod("getDefaultCredential", classname="org.globus.gsi.GlobusCredential")
				)
				true
			)
		
			false
		)
	)
)