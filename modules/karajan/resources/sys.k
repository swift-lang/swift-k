namespace(sys) {
    export(parallel, def("org.globus.cog.karajan.compiled.nodes.Parallel"))
	export(uparallel, def(classname="org.globus.cog.karajan.compiled.nodes.UParallel"))
	export(unsynchronized, def(classname="org.globus.cog.karajan.compiled.nodes.UnsynchronizedNode"))
	export(race, def(classname="org.globus.cog.karajan.compiled.nodes.Race"))
	export(try, def(classname="org.globus.cog.karajan.compiled.nodes.Try"))
	export(catch, def(classname="org.globus.cog.karajan.compiled.nodes.Catch"))
	export(maybe, def(classname="org.globus.cog.karajan.compiled.nodes.Maybe"))
	export(exclusive, def(classname="org.globus.cog.karajan.compiled.nodes.Exclusive"))
	export(cache, def(classname="org.globus.cog.karajan.compiled.nodes.CacheNode"))
	
	export(future, def(classname="org.globus.cog.karajan.compiled.nodes.FutureNode"))
	export(futureChannel, def(classname="org.globus.cog.karajan.compiled.nodes.FutureChannelNode"))
	
	export(echo, def(classname="org.globus.cog.karajan.compiled.nodes.Echo"))
	export(print, def(classname="org.globus.cog.karajan.compiled.nodes.Print"))
    
    export(for, def(classname="org.globus.cog.karajan.compiled.nodes.For"))
    export(parallelFor, def(classname="org.globus.cog.karajan.compiled.nodes.ParallelFor"))
	export(uParallelFor, def(classname="org.globus.cog.karajan.compiled.nodes.UParallelFor"))
	export(each, def(classname="org.globus.cog.karajan.compiled.nodes.Each"))
        
    export(restartOnError, def(classname="org.globus.cog.karajan.compiled.nodes.RestartOnErrorNode"))
    export(throw, def(classname="org.globus.cog.karajan.compiled.nodes.Throw"))
	export(exception, def(classname="org.globus.cog.karajan.compiled.nodes.functions.KException"))
    		
	namespace(channel) {
		export(new, def(classname="org.globus.cog.karajan.compiled.nodes.ChannelNew"))
		export(from, def(classname="org.globus.cog.karajan.compiled.nodes.ChannelFrom"))
		export(to, def(classname="org.globus.cog.karajan.compiled.nodes.ChannelTo"))
		export(fork, def(classname="org.globus.cog.karajan.compiled.nodes.ChannelFork"))
		export(close, def(classname="org.globus.cog.karajan.compiled.nodes.ChannelClose"))
	}
    
    export(formatNumber, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FormatNumber"))
    export(formatDate, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FormatDate"))
    export(contains, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FileContains"))
	export(file:contains, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FileContains"))
    export(UID, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$UID"))
    export(readFile, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FileRead"))
	export(file:read, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$FileRead"))
	export(property, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Property"))
	export(getenv, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$GetEnv"))
	export(outputStream, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$OutputStream"))
	export(closeStream, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$CloseStream"))
	export(contextAttribute, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$ContextAttribute"))
	
	export(discard, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Discard"))
	export(range, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Range"))
	export(filter, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Filter"))
	export(sort, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Sort"))
	
	export(dot, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Dot"))
	export(cross, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Cross"))
	export(unique, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Unique"))
	
	namespace(str) {
		export(strip, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Strip"))
		export(split, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Split"))
		export(matches, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Matches"))
		export(chr, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Chr"))
		export(substring, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$SubString"))
		export(quote, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Quote"))
		export(number, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$ParseNumber"))
	}
	
	export(time, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Time"))
	export(once, def(classname="org.globus.cog.karajan.compiled.nodes.Once"))
    
	export(wait, def(classname="org.globus.cog.karajan.compiled.nodes.WaitNode"))
	export(executeFile, def(classname="org.globus.cog.karajan.compiled.nodes.ExecuteFile"))
	export(file:execute, def(classname="org.globus.cog.karajan.compiled.nodes.ExecuteFile"))
	export(file:write, def(classname="org.globus.cog.karajan.compiled.nodes.FileWrite"))
	
	export(CBFFunction, def(classname="org.globus.cog.karajan.compiled.nodes.user.CBFFunction"))
	export(apply, def(classname="org.globus.cog.karajan.compiled.nodes.Apply"))
	
	export(if, def(classname="org.globus.cog.karajan.compiled.nodes.If"))
	export(while, def(classname="org.globus.cog.karajan.compiled.nodes.While"))
	export(next, def(classname="org.globus.cog.karajan.compiled.nodes.Next"))
	
	namespace(math) {
		export(sum, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Sum"))
		export(sqrt, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Sqrt"))
		export(equalsNumeric, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$EqualsNumeric"))
		export(min, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Min"))
		export(max, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Max"))
		export(floor, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Floor"))
		export(round, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Round"))
		export(ln, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Ln"))
		export(exp, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Exp"))
		export(random, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Random"))
		export(sin, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Sin"))
		export(cos, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Cos"))
		export(tan, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Tan"))
		export(asin, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$ASin"))
		export(acos, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$ACos"))
		export(atan, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$ATan"))
		export(sinh, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Sinh"))
		export(cosh, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Cosh"))
		export(tanh, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Tanh"))
		export(pow, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Pow"))
	}
	
	export(equals, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Equals"))
	
	namespace(list) {
		export(append, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Append"))
		export(list, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Cons"))
		export(prepend, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Prepend"))
		export(join, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Join"))
		export(size, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Size"))
		export(last, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Last"))
		export(first, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$First"))
		export(butLast, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$ButLast"))
		export(butFirst, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$ButFirst"))
		export(isEmpty, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$IsEmpty"))
		export(get, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$Get"))
		export(isList, def(classname="org.globus.cog.karajan.compiled.nodes.functions.List$IsList"))
	}
	
	namespace(map) {
		export(map, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Cons"))
		export(put, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Put"))
		export(delete, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Delete"))
		export(get, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Get"))
		export(entry, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$EntryCons"))
		export(size, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Size"))
		export(contains, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Map$Contains"))
	}
	
	export(stats, def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Stats"))
	   	
	export('&', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Logic$And"))
	export('|', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Logic$Or"))
	export('!', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Logic$Not"))

	namespace(math) {
		export('+', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Sum"))
		export('*', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Product"))
		export('-', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Difference"))
		export('neg', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Neg"))
		export('/', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Quotient"))
		export('%', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$Remainder"))
		export('>', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$GreaterThan"))
		export('<', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$LessThan"))
		export('>=', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$GreaterOrEqual"))
		export('<=', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Math$LessOrEqual"))
	}
	
	export('==', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$Equals"))
	export('!=', def(classname="org.globus.cog.karajan.compiled.nodes.functions.Misc$NotEquals"))
}