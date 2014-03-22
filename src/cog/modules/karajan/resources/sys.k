namespace(sys) {
    export(parallel, def("org.globus.cog.karajan.compiled.nodes.Parallel"))
	export(uparallel, def("org.globus.cog.karajan.compiled.nodes.UParallel"))
	export(unsynchronized, def("org.globus.cog.karajan.compiled.nodes.UnsynchronizedNode"))
	export(race, def("org.globus.cog.karajan.compiled.nodes.Race"))
	export(try, def("org.globus.cog.karajan.compiled.nodes.Try"))
	export(catch, def("org.globus.cog.karajan.compiled.nodes.Catch"))
	export(maybe, def("org.globus.cog.karajan.compiled.nodes.Maybe"))
	export(exclusive, def("org.globus.cog.karajan.compiled.nodes.Exclusive"))
	export(cache, def("org.globus.cog.karajan.compiled.nodes.CacheNode"))
	
	export(future, def("org.globus.cog.karajan.compiled.nodes.FutureNode"))
	export(futureChannel, def("org.globus.cog.karajan.compiled.nodes.FutureChannelNode"))
	
	export(echo, def("org.globus.cog.karajan.compiled.nodes.Echo"))
	export(print, def("org.globus.cog.karajan.compiled.nodes.Print"))
    
    export(for, def("org.globus.cog.karajan.compiled.nodes.For"))
    export(parallelFor, def("org.globus.cog.karajan.compiled.nodes.ParallelFor"))
	export(uParallelFor, def("org.globus.cog.karajan.compiled.nodes.UParallelFor"))
	export(each, def("org.globus.cog.karajan.compiled.nodes.Each"))
        
    export(restartOnError, def("org.globus.cog.karajan.compiled.nodes.RestartOnErrorNode"))
    export(throw, def("org.globus.cog.karajan.compiled.nodes.Throw"))
	export(exception, def("org.globus.cog.karajan.compiled.nodes.functions.KException"))
    		
	namespace(channel) {
		export(new, def("org.globus.cog.karajan.compiled.nodes.ChannelNew"))
		export(from, def("org.globus.cog.karajan.compiled.nodes.ChannelFrom"))
		export(to, def("org.globus.cog.karajan.compiled.nodes.ChannelTo"))
		export(fork, def("org.globus.cog.karajan.compiled.nodes.ChannelFork"))
		export(close, def("org.globus.cog.karajan.compiled.nodes.ChannelClose"))
	}
    
    export(formatNumber, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FormatNumber"))
    export(formatDate, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FormatDate"))
    export(contains, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FileContains"))
	export(file:contains, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FileContains"))
    export(UID, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$UID"))
    export(readFile, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FileRead"))
	export(file:read, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$FileRead"))
	export(property, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Property"))
	export(getenv, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$GetEnv"))
	export(outputStream, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$OutputStream"))
	export(closeStream, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$CloseStream"))
	export(contextAttribute, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$ContextAttribute"))
	
	export(discard, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Discard"))
	export(range, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Range"))
	export(filter, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Filter"))
	export(sort, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Sort"))
	
	export(dot, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Dot"))
	export(cross, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Cross"))
	export(unique, def("org.globus.cog.karajan.compiled.nodes.functions.Unique"))
	
	namespace(str) {
		export(strip, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Strip"))
		export(split, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Split"))
		export(matches, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Matches"))
		export(chr, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Chr"))
		export(substring, def("org.globus.cog.karajan.compiled.nodes.functions.Str$SubString"))
		export(quote, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Quote"))
		export(number, def("org.globus.cog.karajan.compiled.nodes.functions.Str$ParseNumber"))
		export(join, def("org.globus.cog.karajan.compiled.nodes.functions.Str$Join"))
	}
	
	export(time, def("org.globus.cog.karajan.compiled.nodes.functions.Time"))
	export(once, def("org.globus.cog.karajan.compiled.nodes.Once"))
    
	export(wait, def("org.globus.cog.karajan.compiled.nodes.WaitNode"))
	export(executeFile, def("org.globus.cog.karajan.compiled.nodes.ExecuteFile"))
	export(file:execute, def("org.globus.cog.karajan.compiled.nodes.ExecuteFile"))
	export(file:write, def("org.globus.cog.karajan.compiled.nodes.FileWrite"))
	
	export(CBFFunction, def("org.globus.cog.karajan.compiled.nodes.user.CBFFunction"))
	export(apply, def("org.globus.cog.karajan.compiled.nodes.Apply"))
	
	export(if, def("org.globus.cog.karajan.compiled.nodes.If"))
	export(while, def("org.globus.cog.karajan.compiled.nodes.While"))
	export(next, def("org.globus.cog.karajan.compiled.nodes.Next"))
	
	namespace(math) {
		export(sum, def("org.globus.cog.karajan.compiled.nodes.functions.Sum"))
		export(sqrt, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Sqrt"))
		export(equalsNumeric, def("org.globus.cog.karajan.compiled.nodes.functions.Math$EqualsNumeric"))
		export(min, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Min"))
		export(max, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Max"))
		export(floor, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Floor"))
		export(round, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Round"))
		export(ln, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Ln"))
		export(exp, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Exp"))
		export(random, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Random"))
		export(sin, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Sin"))
		export(cos, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Cos"))
		export(tan, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Tan"))
		export(asin, def("org.globus.cog.karajan.compiled.nodes.functions.Math$ASin"))
		export(acos, def("org.globus.cog.karajan.compiled.nodes.functions.Math$ACos"))
		export(atan, def("org.globus.cog.karajan.compiled.nodes.functions.Math$ATan"))
		export(sinh, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Sinh"))
		export(cosh, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Cosh"))
		export(tanh, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Tanh"))
		export(pow, def("org.globus.cog.karajan.compiled.nodes.functions.Math$Pow"))
	}
	
	export(equals, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Equals"))
	
	namespace(list) {
		export(append, def("org.globus.cog.karajan.compiled.nodes.functions.List$Append"))
		export(list, def("org.globus.cog.karajan.compiled.nodes.functions.List$Cons"))
		export(prepend, def("org.globus.cog.karajan.compiled.nodes.functions.List$Prepend"))
		export(join, def("org.globus.cog.karajan.compiled.nodes.functions.List$Join"))
		export(size, def("org.globus.cog.karajan.compiled.nodes.functions.List$Size"))
		export(last, def("org.globus.cog.karajan.compiled.nodes.functions.List$Last"))
		export(first, def("org.globus.cog.karajan.compiled.nodes.functions.List$First"))
		export(butLast, def("org.globus.cog.karajan.compiled.nodes.functions.List$ButLast"))
		export(butFirst, def("org.globus.cog.karajan.compiled.nodes.functions.List$ButFirst"))
		export(isEmpty, def("org.globus.cog.karajan.compiled.nodes.functions.List$IsEmpty"))
		export(get, def("org.globus.cog.karajan.compiled.nodes.functions.List$Get"))
		export(isList, def("org.globus.cog.karajan.compiled.nodes.functions.List$IsList"))
	}
	
	namespace(map) {
		export(map, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Cons"))
		export(put, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Put"))
		export(delete, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Delete"))
		export(get, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Get"))
		export(entry, def("org.globus.cog.karajan.compiled.nodes.functions.Map$EntryCons"))
		export(size, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Size"))
		export(contains, def("org.globus.cog.karajan.compiled.nodes.functions.Map$Contains"))
	}
	
	export(stats, def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Stats"))
	   	
	export('&', def("org.globus.cog.karajan.compiled.nodes.functions.Logic$And"))
	export('|', def("org.globus.cog.karajan.compiled.nodes.functions.Logic$Or"))
	export('!', def("org.globus.cog.karajan.compiled.nodes.functions.Logic$Not"))

	namespace(math) {
		export('+', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Sum"))
		export('*', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Product"))
		export('-', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Difference"))
		export('neg', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Neg"))
		export('/', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Quotient"))
		export('%', def("org.globus.cog.karajan.compiled.nodes.functions.Math$Remainder"))
		export('>', def("org.globus.cog.karajan.compiled.nodes.functions.Math$GreaterThan"))
		export('<', def("org.globus.cog.karajan.compiled.nodes.functions.Math$LessThan"))
		export('>=', def("org.globus.cog.karajan.compiled.nodes.functions.Math$GreaterOrEqual"))
		export('<=', def("org.globus.cog.karajan.compiled.nodes.functions.Math$LessOrEqual"))
	}
	
	export('==', def("org.globus.cog.karajan.compiled.nodes.functions.Misc$Equals"))
	export('!=', def("org.globus.cog.karajan.compiled.nodes.functions.Misc$NotEquals"))
}