namespace(swiftscript) {
	export(extractInt, def("org.griphyn.vdl.karajan.lib.swiftscript.ExtractInt"))
	export(extractFloat, def("org.griphyn.vdl.karajan.lib.swiftscript.ExtractFloat"))
	export(readData, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadData"))
	export(readData2, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured"))
	export(readStructured, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured"))
	export(writeData, def("org.griphyn.vdl.karajan.lib.swiftscript.WriteData"))
	export(strcat, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$StrCat"))
	export(strcut, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$StrCut"))
	export(strstr, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$StrStr"))
	export(strsplit, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$StrSplit"))
	export(system, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ExecSystem"))
    export(strjoin, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$StrJoin"))
	export(regexp, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Regexp"))
	export(toInt, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToInt"))
	export(toFloat, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToFloat"))
	export(format, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Format"))
	export(pad, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Pad"))
	export(sprintf, def("org.griphyn.vdl.karajan.lib.swiftscript.Sprintf"))
	export(toString, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToString"))
	export(assert, def("org.griphyn.vdl.karajan.lib.swiftscript.Assert"))
	export(trace, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Trace"))
	export(tracef, def("org.griphyn.vdl.karajan.lib.swiftscript.Tracef"))
	export(printf, def("org.griphyn.vdl.karajan.lib.swiftscript.Printf"))
	export(fprintf, def("org.griphyn.vdl.karajan.lib.swiftscript.Fprintf"))

	/* included for backwards compatibility */
	export(readdata, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadData"))
	export(readdata2, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured"))
	export(readstructured, def("org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured"))
	export(writedata, def("org.griphyn.vdl.karajan.lib.swiftscript.WriteData"))
	export(toint, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToInt"))
	export(tofloat, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToFloat"))
	export(tostring, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$ToString"))

	export(java, def("org.griphyn.vdl.karajan.lib.swiftscript.Java"))
	export(filename, def("org.griphyn.vdl.karajan.lib.swiftscript.FileName"))
	export(filenames, def("org.griphyn.vdl.karajan.lib.swiftscript.FileNames"))
	export(dirname, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Dirname"))
	export(length, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Length"))
	export(exists, def("org.griphyn.vdl.karajan.lib.swiftscript.Misc$Exists"))
	export(arg, def("org.griphyn.vdl.karajan.lib.swiftscript.FnArg"))
	export(args, def("org.griphyn.vdl.karajan.lib.swiftscript.FnArgs"))
}

namespace(swift) {
	import(sys)
	export(SWIFT_CONFIG, contextAttribute("SWIFT:CONFIG"))
	export(SWIFT_TICKER, null)

	export(getDatasetProvenanceID, def("org.griphyn.vdl.karajan.lib.GetDatasetProvenanceID"))
	export(startProgressTicker, def("org.griphyn.vdl.karajan.lib.RuntimeStats$StartProgressTicker"))
	export(stopProgressTicker, def("org.griphyn.vdl.karajan.lib.RuntimeStats$StopProgressTicker"))
	export(initProgressState, def("org.griphyn.vdl.karajan.lib.RuntimeStats$InitProgressState"))
	export(setProgress, def("org.griphyn.vdl.karajan.lib.RuntimeStats$SetProgress"))
	export(new, def("org.griphyn.vdl.karajan.lib.New"))
	export(field, def("org.griphyn.vdl.karajan.lib.GetFieldConst"))
	export(createArray, def("org.griphyn.vdl.karajan.lib.CreateArray"))
	/* used from VDL2 for arguments to apps and returns relative paths */
	export(fileName, def("org.griphyn.vdl.karajan.lib.FileName"))

	export(jobConstraints, def("org.griphyn.vdl.karajan.lib.JobConstraints"))
	export(getField, def("org.griphyn.vdl.karajan.lib.GetField"))
	export(getFieldSubscript, def("org.griphyn.vdl.karajan.lib.GetFieldSubscript"))
	export(setFieldValue, def("org.griphyn.vdl.karajan.lib.SetFieldValue"))
	export(appendArray, def("org.griphyn.vdl.karajan.lib.AppendArray"))
	export(getFieldValue, def("org.griphyn.vdl.karajan.lib.GetFieldValue"))
	export(waitFieldValue, def("org.griphyn.vdl.karajan.lib.WaitFieldValue"))
	export(getArrayIterator, def("org.griphyn.vdl.karajan.lib.GetArrayIterator"))
	export(sliceArray, def("org.griphyn.vdl.karajan.lib.SliceArray"))
	export(isFileBound, def("org.griphyn.vdl.karajan.lib.IsFileBound"))
	export(isRestartable, def("org.griphyn.vdl.karajan.lib.IsRestartable"))
	export(fringePaths, def("org.griphyn.vdl.karajan.lib.FringePaths"))
	export(closeDataset, def("org.griphyn.vdl.karajan.lib.CloseDataset"))
	export(partialCloseDataset, def("org.griphyn.vdl.karajan.lib.PartialCloseDataset"))
	export(setWaitCount, def("org.griphyn.vdl.karajan.lib.SetWaitCount"))
	export(cleanDataset, def("org.griphyn.vdl.karajan.lib.CleanDataset"))
	export(setDatasetValues, def("org.griphyn.vdl.karajan.lib.SetDatasetValues"))
	export(range, def("org.griphyn.vdl.karajan.lib.Range"))

	export(isLogged, def("org.griphyn.vdl.karajan.lib.IsLogged"))
	export(logVar, def("org.griphyn.vdl.karajan.lib.LogVar"))

	export(executable, def("org.griphyn.vdl.karajan.lib.Executable"))
	export(TCProfile, def("org.griphyn.vdl.karajan.lib.TCProfile"))
	export(siteProfile, def("org.griphyn.vdl.karajan.lib.SiteProperty"))

	export(setFutureFault, def("org.griphyn.vdl.karajan.lib.SetFutureFault"))

	export(configProperty, def("org.griphyn.vdl.karajan.functions.ConfigProperty"))
	export(processBulkErrors, def("org.griphyn.vdl.karajan.functions.ProcessBulkErrors"))

	export(niceName, def("org.griphyn.vdl.karajan.lib.NiceName"))

	export(cacheAddAndLockFile, def("org.griphyn.vdl.karajan.lib.CacheAddAndLockFile"))
	export(cacheAddFile, def("org.griphyn.vdl.karajan.lib.CacheAddFile"))
	export(cacheFileRemoved, def("org.griphyn.vdl.karajan.lib.CacheFileRemoved"))
	export(cacheUnlockFiles, def("org.griphyn.vdl.karajan.lib.CacheUnlockFiles"))
	
	export(LOG:DEBUG, "debug")
	export(LOG:INFO, "info")
	export(LOG:WARN, "warn")
	export(LOG:ERROR, "error")
	export(LOG:FATAL, "fatal")
	

	export(log, def("org.griphyn.vdl.karajan.lib.Log"))

	export(unitStart, def("org.griphyn.vdl.karajan.lib.UnitStart"))
	export(unitEnd, def("org.griphyn.vdl.karajan.lib.UnitEnd"))
	export(currentThread, def("org.griphyn.vdl.karajan.lib.CurrentThread"))

	export(dirname, def("org.griphyn.vdl.karajan.lib.PathUtils$DirName"))
	export(reldirname, def("org.griphyn.vdl.karajan.lib.PathUtils$RelDirName"))
	export(basename, def("org.griphyn.vdl.karajan.lib.PathUtils$BaseName"))
	export(provider, def("org.griphyn.vdl.karajan.lib.PathUtils$Provider"))
	export(hostname, def("org.griphyn.vdl.karajan.lib.PathUtils$HostName"))
	export(dircat, def("org.griphyn.vdl.karajan.lib.PathUtils$DirCat"))
	export(pathnames, def("org.griphyn.vdl.karajan.lib.PathUtils$PathNames"))
	export(splitFileURL, def("org.griphyn.vdl.karajan.lib.PathUtils$SplitFileURL"))

	export(execute, def("org.griphyn.vdl.karajan.lib.Execute"))
	export(expandArguments, def("org.griphyn.vdl.karajan.lib.ExpandArguments"))

	export(tParallelFor, def("org.griphyn.vdl.karajan.lib.ThrottledParallelFor"))
	export(throttled, def("org.griphyn.vdl.karajan.lib.Throttled"))
	export(while, def("org.griphyn.vdl.karajan.lib.While"))

	export(appStageins, def("org.griphyn.vdl.karajan.lib.AppStageins"))
	export(appStageouts, def("org.griphyn.vdl.karajan.lib.AppStageouts"))
	export(remoteFileName, def("org.griphyn.vdl.karajan.lib.RemoteFileName"))
	export(remoteFileNames, def("org.griphyn.vdl.karajan.lib.RemoteFileNames"))
	export(remoteFileNamesWS, def("org.griphyn.vdl.karajan.lib.RemoteFileNamesWS"))
	export(isDone, def("org.griphyn.vdl.karajan.lib.IsDone"))
	export(mark, def("org.griphyn.vdl.karajan.lib.Mark"))
	export(parameterlog, def("org.griphyn.vdl.karajan.lib.Parameterlog"))
	export(getStagingInfo, def("org.griphyn.vdl.karajan.lib.GetStagingInfo"))
	export(getStandardFilesInfo, def("org.griphyn.vdl.karajan.lib.GetStandardFilesInfo"))
	export(readCollectList, def("org.griphyn.vdl.karajan.lib.ReadCollectList"))
	export(processStageouts, def("org.griphyn.vdl.karajan.lib.ProcessStageouts"))
	export(doRestartLog, def("org.griphyn.vdl.karajan.lib.DoRestartLog"))
	export(unwrapClosedList, def("org.griphyn.vdl.karajan.lib.UnwrapClosedList"))
	
	export(siteCatalog, def("org.griphyn.vdl.karajan.lib.SiteCatalog"))
	
	export(getURLPrefix, def("org.griphyn.vdl.karajan.lib.GetURLPrefix"))
	
	export(generateJobId, def("org.griphyn.vdl.karajan.lib.GenerateJobId"))
}

namespace(cdm) {
  	export(query, def("org.globus.swift.data.Query$Q"))
	export(get, def("org.globus.swift.data.Query$Get"))
  	export(file, def("org.globus.swift.data.Query$File"))
  	export(broadcast, def("org.globus.swift.data.Action$Broadcast"))
  	export(external, def("org.globus.swift.data.Action$External"))
  	export(wait, def("org.globus.swift.data.Action$Wait"))
}
