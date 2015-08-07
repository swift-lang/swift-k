ai = 0
Set args = WScript.Arguments
Dim info, fs
Set fs = WScript.CreateObject("Scripting.FileSystemObject")
Set shell = WScript.CreateObject("WScript.Shell")
MandatoryArg = True
OptionalArg = False


Sub fail(message, code)
	log message
	If STATUSMODE = "files" Then
		errf = WFDIR + "\status\" + JOBDIR + "\" + ID + "-error"
		log "Error file: " + errf
		Set sf = fs.OpenTextFile(errf, 2, True)
		sf.Write(message)
		sf.Write(vbLf)
		sf.Close
		WScript.Quit(0)
	Else
		WScript.Echo(message)
		WScript.Quit(code)
	End If
End Sub

Sub openinfo(name) 
	Set info = fs.OpenTextFile(name, 8, True)
End Sub

Sub closeinfo
	info.close()
End Sub

Function getArgVal(cls, name)
	arr = getArgVals(cls, name)
	If UBound(arr) = 0 Then
		getArgVal = Empty
	Else
		getArgVal = RTrim(Join(arr))
	End If
End Function

Function getArgVals(cls, name)
	Dim val(255)
	sz = 0
	again = True
	Do While again
		done = (ai >= args.count)
		If Not(done) Then
			If Left(args(ai), 1) = "-" Then
				done = True
			End If
		End If
		If done Then
			If UBound(val) = 0 and cls = MandatoryArg Then
				fail "Expected " + name, 253
			End If
			again = False
		Else
			val(sz) = args(ai)
			ai = ai + 1
			sz = sz + 1
		End If
	Loop
	getArgVals = val
End Function

Function getRestArgs()
	 Dim val(255)
	 sz = 0
	 Do While ai < args.count
	 	val(sz) = args(ai)
	 	ai = ai + 1
	 	sz = sz + 1
	 Loop
	 getRestArgs = val
End Function

Function getOptArg() 
	getOptArg = getArgVal(OptionalArg, "")
End Function

Function expectArg(name)
	nope = (ai >= args.count)
	If Not(nope) Then
		If args(ai) <> "-" + name Then
			nope = True
		End If
	End If
	If nope Then
		fail "Expected argument " + name, 252
	Else
		ai = ai + 1
	End If
End Function

Sub logstate(args)
	'todo: timestamp and whatever else the "standard" format
	info.Write("Progress " + args)
	info.write(vbLf)
End Sub	

Sub log(args)
	'todo: timestamp and whatever else the "standard" format
	info.Write(args)
	info.write(vbLf)
End Sub


Function deleteIfExists(name)
	If fs.FileExists(name) Then
		fs.DeleteFile(name)
	End If
End Function

Sub mkdir(f)
	If f = "" Then
		fail "mkdir called with empty argument", 249
	End If
	If Not fs.FolderExists(f) Then
		mkdir fs.GetParentFolderName(f)
		fs.CreateFolder(f)
	End If
End Sub

Function prepareOne(v) 
	'Arguments with spaces must be quoted with a double quote
	'Literal double quotes must be escaped with a backslash
	'Literal backslashes must be escaped (with a backslash) if they appear before a double quote
	start = 1
	Do While start < Len(v) and Not start = 0
		start = InStr(start, v, "\")
		If start <> 0 Then
			v = Left(v, start) + "\" + Right(v, Len(v) - start)
			start = start + 2
		End If
	Loop
	start = 1
	Do While start < Len(v) and Not start = 0
		start = InStr(start, v, """")
		If start <> 0 Then
			v = Left(v, start - 1) + "\" + Right(v, Len(v) - start + 1)
			start = start + 2
		End If
	Loop
	If Not InStr(v, " ") = 0 Then
		v = """" + v + """"
	End If
	prepareOne = v
End Function

Function prepareArgs(args)
	For i = 0 To UBound(args)
		args(i) = prepareOne(args(i))
	Next
	prepareArgs = join(args)
End Function


WFDIR = fs.GetAbsolutePathName(".")

openinfo("wrapper.log")

ID = getArgVal(MandatoryArg, "job id")
expectArg("jobdir")
JOBDIR = getArgVal(MandatoryArg, "job dir")

mkdir WFDIR + "\info\" + JOBDIR

closeinfo
deleteIfExists(WFDIR + "\info\" + JOBDIR + "\" + ID + "-info")
openinfo(WFDIR + "\info\" + JOBDIR + "\" + ID + "-info")

logstate "LOG_START"

expectArg("scratch")
SCRATCH = getArgVal(MandatoryArg, "job dir")
'though we'll ignore SCRATCH here

expectArg("e")
EXEC = getArgVal(MandatoryArg, "executable")

expectArg("out")
STDOUT=getArgVal(MandatoryArg, "stdout")

expectArg("err")
STDERR=getArgVal(MandatoryArg, "stderr")

expectArg("i")
STDIN=getOptArg()

expectArg("d")
DIRS=getOptArg()

expectArg("if")
INF=getOptArg()

expectArg("of")
OUTF=getOptArg()

expectArg("cdmfile")
'ignored, but read if specified
CDMFILE=getOptArg()

expectArg("status")
STATUSMODE=getArgVal(MandatoryArg, "status")

expectArg("a")
Dim ARGS
ARGS=getRestArgs()

If STATUSMODE = "files" Then
	mkdir WFDIR + "\status\" + JOBDIR
End If

'No linking on Windows
COPYNOTLINK=True	
DIR=WFDIR + "\jobs\" + JOBDIR + "\" + ID

Set env = shell.Environment("PROCESS")
If Not env("PATHPREFIX") = "" Then
	env("PATH") = env("PATHPREFIX") + ";" + env("PATH")
End If

If Not env("SWIFT_EXTRA_INFO") = "" Then
	log "EXTRAINFO=" + env("SWIFT_EXTRA_INFO")
End If


log "DIR=" + DIR
log "EXEC=" + EXEC
log "STDIN=" + STDIN
log "STDOUT=" + STDOUT
log "STDERR=" + STDERR
log "DIRS=" + DIRS
log "INF=" + INF
log "OUTF=" + OUTF
log "STATUSMODE=" + STATUSMODE
log "ARGS=" + Join(ARGS)

logstate "CREATE_JOBDIR"
log "Jobdir: " + DIR
mkdir DIR
log "Created job directory: " + DIR

logstate "CREATE_INPUTDIR"

For Each D in Split(DIRS, "|")
	mkdir DIR + "\" + D
	log "Created output directory: " + DIR + "\" + D
Next

logstate "LINK_INPUTS"
For Each L in Split(INF, "|")
	fs.CopyFile WFDIR + "\shared\" + L, DIR + "\" + L
	log "Copied input: " + WFDIR + "\shared\" + L + " to " + DIR + "\" + L
Next

logstate "EXECUTE"
shell.CurrentDirectory = DIR

If LCase(fs.GetAbsolutePathName(EXEC)) <> LCase(EXEC) Then
	'relative name
	If Not fs.FileExists(EXEC) Then
		found = False
		'search in path
		dirs = split(env("PATH"), ";")
		For Each d in dirs
			If fs.FileExists(d + "\" + EXEC) Then
				found = True
				Exit For
			End If
		Next
		If Not Found Then
			fail "The executable (" + EXEC + ") was not found in the current directory or search path", 252
		End If
	End If
Else
	If Not fs.FileExists(EXEC) Then
		fail "Executable (" + EXEC + ") does not exist", 251
	End If
End If

Set min = Nothing
Set mout = Nothing
Set merr = Nothing
If STDIN <> "" Then
	Set min = fs.OpenTextFile(STDIN, 1, False)
End If
If STDOUT <> "" Then
	Set mout = fs.OpenTextFile(STDOUT, 2, True)
End If
If STDERR <> "" Then
	Set merr = fs.OpenTextFile(STDERR, 2, True)
End If
qargs = prepareArgs(ARGS)
log "Cmd: " + prepareOne(EXEC) + " " + qargs
Set p = shell.exec(prepareOne(EXEC) + " " + qargs)
log "Executable started"

Do Until p.StdOut.AtEndOfStream and p.StdErr.AtEndOfStream and p.Status <> 0
	some = False
	If Not min Is Nothing Then
		l = min.ReadLine
		p.StdIn.Write(l)
		some = True
	End If
	If Not p.StdOut.AtEndOfStream Then
		l = p.StdOut.ReadLine
		If Not mout Is Nothing Then
			mout.Write(l)
		End If
		some = True
	End If
	If Not p.StdErr.AtEndOfStream Then
		l = p.StdErr.ReadLine
		If Not merr Is Nothing Then
			merr.Write(l)
		End If
		some = True
	End If
	WScript.Sleep(100)
Loop
If Not min Is Nothing Then
	min.close()
End If
If Not mout Is Nothing Then
	mout.close()
End If
If Not merr Is Nothing Then
	merr.close()
End If
If p.ExitCode <> 0 Then
	fail "Exit code " + CStr(p.ExitCode), p.ExitCode
End If
	
shell.CurrentDirectory = WFDIR

log "Moving back to workflow directory " + WFDIR
logstate "EXECUTE_DONE"
log "Job ran successfully"

MISSING = ""
For Each O in Split(OUTF, "|")
	If Not fs.FileExists(DIR + "\" + O) Then
		If MISSING = "" Then
			MISSING = O
		Else
			MISSING = MISSING + ", " + O
		End If
	End If
Next
If Not MISSING = "" Then
	fail "The following output files were not created by the application: " + MISSING, 254
End If

logstate "MOVING_OUTPUTS"
For Each O in split(OUTF, "|")
	fs.MoveFile DIR + "\" + O,  "shared\" + O
Next

logstate "RM_JOBDIR"
fs.DeleteFolder DIR, True

If STATUSMODE = "files" Then
	logstate "TOUCH_SUCCESS"
	Set sf = fs.OpenTextFile("status\" + JOBDIR + "\" + ID + "-success", 2, True)
	sf.Close
End If

logstate "END"

closeinfo

'ensure we exit with a 0 after a successful exection
WScript.Quit(0)

