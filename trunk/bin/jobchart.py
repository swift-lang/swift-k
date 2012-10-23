#!/usr/bin/python

#
# To get meaningful values for JOB_*_SCHED:
# 	log4j.logger.org.globus.cog.karajan.scheduler.LateBindingScheduler=DEBUG
#
# For JOB_SUBMITTED_TH (this may or may not work properly):
#	log4j.logger.org.globus.cog.abstraction.impl.execution.local.JobSubmissionTaskHandler=INFO
#	log4j.logger.org.globus.cog.abstraction.impl.execution.gt2.JobSubmissionTaskHandler=INFO
# 
# Without the proper log messages, these will default to the JOB_SUBMITTED/COMPLETED values
#

import sys
import re
import datetime
import time
import os

def usage():
	print "Usage: "
	print "\tjobchart <file.log>"

if len(sys.argv) < 2:
	usage()
	sys.exit()
	
log = sys.argv[1]
title = log[:-4]
	
def taskStatus(type, status):
	return TIMESTAMP + ".* TaskImpl Task\(type=" + type + \
		", identity=urn:([\d-]*)-\d*\) setting status to " + status + ".*";

	
TIMESTAMP = "(\d\d\d\d)-(\d\d)-(\d\d) (\d\d):(\d\d):(\d\d),(\d\d\d)" 
APP_THREAD = re.compile(".* vdl:execute2 Job (\S*) running in thread (\S*) .*")
APP_START = re.compile(TIMESTAMP + ".* vdl:execute2 Running job (\S*) .*")
APP_END = re.compile(TIMESTAMP + ".* vdl:execute2 Completed job (\S*) .*")
JOB_SUBMITTED_SCHED = re.compile(TIMESTAMP + ".* LateBindingScheduler Submitting task Task\(type=1, identity=urn:([\d-]*)-\d*\).*");
JOB_SUBMITTED_TH = re.compile(TIMESTAMP + ".* JobSubmissionTaskHandler Submitting task Task\(type=1, identity=urn:([\d-]*)-\d*\).*");
JOB_COMPLETED_SCHED = re.compile(TIMESTAMP + ".* LateBindingScheduler Task\(type=1, identity=urn:([\d-]*)-\d*\) Completed.*");
JOB_SUBMITTED = re.compile(taskStatus("1", "Submitted"))
JOB_ACTIVE = re.compile(taskStatus("1", "Active"))
JOB_COMPLETED = re.compile(taskStatus("1", "Completed"))
JOB_FAILED = re.compile(taskStatus("1", "Failed"))
TRANSFER_SUBMITTED = re.compile(taskStatus("2", "Submitted"))
TRANSFER_ACTIVE = re.compile(taskStatus("2", "Active"))
TRANSFER_COMPLETED = re.compile(taskStatus("2", "Completed"))
TRANSFER_FAILED = re.compile(taskStatus("2", "Failed"))
FILEOP_SUBMITTED = re.compile(taskStatus("4", "Submitted"))
FILEOP_ACTIVE = re.compile(taskStatus("4", "Active"))
FILEOP_COMPLETED = re.compile(taskStatus("4", "Completed"))
FILEOP_FAILED = re.compile(taskStatus("4", "Failed"))
		
class Interval:
	def __init__(self):
		self.start = None
		self.end = None

class Job:
	def __init__(self, name, thread):
		global mintime
		self.name = name
		self.thread = thread
		self.appstart = None
		self.sjobsubmitted = None
		self.sjobcompleted = None
		self.thjobsubmitted = None
		self.jobsubmitted = None
		self.jobactive = None
		self.jobdone = None
		self.jobfailed = None
		self.append = None
		self.transfers = {}
		self.fileops = {}
		
	def __repr__(self):
		return "Job[" + self.name + ", " + str(self.appstart) + "-" + str(self.append) + "]"
		
	def appStart(self, time, id, args):
		self.appstart = time
		
	def appEnd(self, time, id, args):
		self.append = time
		
	def jobSubmitted(self, time, id, args):
		if not self.jobsubmitted:
			self.jobsubmitted = time
				
	def sJobSubmitted(self, time, id, args):
		self.sjobsubmitted = time
			
	def thJobSubmitted(self, time, id, args):
		self.thjobsubmitted = time
		
	def jobActive(self, time, id, args):
		if not self.jobactive:
			self.jobactive = time
		
	def jobCompleted(self, time, id, args):
		if not self.jobdone:
			self.jobdone = time
			
	def sJobCompleted(self, time, id, args):
		self.sjobcompleted = time
		
	def jobFailed(self, time, id, args):
		if not self.jobdone:
			self.jobdone = time
			self.jobfailed = True
		
	def transferSubmitted(self, time, id, args):
		if not id in self.transfers:
			transfer = Interval()
			self.transfers[id] = transfer
		else:
			transfer = self.transfers[id]
		transfer.start = time
		
	def transferActive(self, time, id, args):
		pass
		
	def transferCompleted(self, time, id, args):
		if not id in self.transfers:
			transfer = Interval()
			self.transfers[id] = transfer
		else:
			transfer = self.transfers[id]
		transfer.end = time
		
	def transferFailed(self, time, id, args):
		self.transferCompleted(time, id, args)
		
	def fileopSubmitted(self, time, id, args):
		if not id in self.fileops:
			fileop = Interval()
			self.fileops[id] = fileop
		else:
			fileop = self.fileops[id]
		fileop.start = time
		
	def fileopActive(self, time, id, args):
		pass
		
	def fileopCompleted(self, time, id, args):
		if not id in self.fileops:
			fileop = Interval()
			self.fileops[id] = fileop
		else:
			fileop = self.fileops[id]
		fileop.end = time
				
	def fileopFailed(self, time, id, args):
		self.fileopCompleted(time, id, args)
		
		
class LogEvent:
	#encapsulates a regexp, a regexp group index representing the id of the item,
	#whether that id is a thread id or an app id, and the method to invoke
	#when the pattern matches
	def __init__(self, regexp, isThreadId, fun, groupIndex = 7):
		self.regexp = regexp
		self.groupIndex = groupIndex
		self.isThreadId = isThreadId
		self.fun = fun
		
	def process(self, line):
		m = self.regexp.match(line)
		if m:
			groups = list(m.groups())
			id = groups[self.groupIndex]
			job = None
			if self.isThreadId and id in threads:
				job = threads[id]
			elif id in jobs:
				job = jobs[id]
			if job:
				self.fun(job, self.getTime(groups[0:7]), id, groups[7:])
			return True
		else:
			return False
			
	def getTime(self, t):
		t = time.mktime((int(t[0]), int(t[1]), int(t[2]), int(t[3]), int(t[4]), int(t[5]), 0, 0, 0))+float(t[6])/1000
		global mintime, maxtime
		if not mintime:
			mintime = time
		if t < mintime:
			mintime = t
		if t > maxtime:
			maxtime = t
		return t
		
		

TS = [ LogEvent(APP_START, False, Job.appStart), LogEvent(APP_END, False, Job.appEnd), 
	   LogEvent(JOB_SUBMITTED, True, Job.jobSubmitted), LogEvent(JOB_ACTIVE, True, Job.jobActive),
       LogEvent(JOB_COMPLETED, True, Job.jobCompleted), LogEvent(JOB_FAILED, True, Job.jobFailed),
	   LogEvent(TRANSFER_SUBMITTED, True, Job.transferSubmitted), LogEvent(TRANSFER_ACTIVE, True, Job.transferActive), 
	   LogEvent(TRANSFER_COMPLETED, True, Job.transferCompleted), LogEvent(TRANSFER_FAILED, True, Job.transferFailed), 
	   LogEvent(FILEOP_SUBMITTED, True, Job.fileopSubmitted), LogEvent(FILEOP_ACTIVE, True, Job.fileopActive),
	   LogEvent(FILEOP_COMPLETED, True, Job.fileopCompleted), LogEvent(FILEOP_FAILED, True, Job.fileopFailed),
	   LogEvent(JOB_SUBMITTED_SCHED, True, Job.sJobSubmitted), LogEvent(JOB_SUBMITTED_TH, True, Job.thJobSubmitted),
	   LogEvent(JOB_COMPLETED_SCHED, True, Job.sJobCompleted) ]

	
threads = {}
jobs = {}
mintime = 0
maxtime = 0

f = open(log)

#first process the app threads and populate the job map
for line in f:
	m = APP_THREAD.match(line)
	if m:
		(name, thread) = m.groups()
		job = Job(name, thread)
		threads[thread] = job
		jobs[name] = job
		
if len(jobs) == 0:
	print "ERROR: no jobs found"
	sys.exit()
	
f.seek(0)
#now let's populate the jobs
for line in f:
	for le in TS:
		if le.process(line):
			break

f.close()

print str(mintime) + " - " + str(maxtime)

joblist = []
for job in jobs.values():
	joblist.append(job)

for i in range(0, len(joblist) - 1):
	for j in range(i + 1, len(joblist)):
		if joblist[i].appstart > joblist[j].appstart:
			tmp = joblist[j]
			joblist[j] = joblist[i]
			joblist[i] = tmp

duration = maxtime - mintime
if duration <= 60:
	xstubs = 5
	xtics = 1
	mywidth = 8.0
elif duration <= 3600:
	xstubs = 600
	xtics = 60
	mywidth = 8.0
elif duration <= 14400:
	xstubs = 1800
	xtics = 600
	mywidth = max(8.0, duration / 900)
elif duration <= 43200:
	xstubs = 3600
	xtics = 600
	mywidth = max(8.0, duration / 3600)
elif duration <= 86400:
	xstubs = 7200
	xtics = 1200
	mywidth = max(8.0, duration / 7200)
elif duration <= 86400*2:
	xstubs = 14400
	xtics = 3600
	mywidth = max(8.0, duration / 14400)
elif duration <= 86400*8:
	xstubs = 86400
	xtics = 14400
	mywidth = max(8.0, duration / 86400)
else:
	print "ERROR: $main::duration s workflow is just too long!"
	sys.exit()

while mywidth > 24.0:
    print "Warning: too wide picture, halfing width";
    mywidth = mywidth / 2.0

if len(joblist) <= 51:
	myheight = 5.0
else:
	myheight = len(joblist) / 10

width = mywidth
height = myheight

if duration > 7200:
	scaleduration = int(int(duration / 3600 + 1) * 3600.0)
else:
	scaleduration = int(duration) + 1;
	
reorder = [0, 3, 4, 5, 7, 1, 2, 6]

def writeTimeline(t, g):
	if not t[0]:
		t[0] = mintime
	for i in range(1, len(t)):
		if not t[i]:
			t[i] = t[i - 1]
	for i in range(0, len(t)):
		t[i] = t[i] - mintime
	
	for i in range(0, len(reorder)):
		g.write(str(t[reorder[i]]))
		g.write(" ")
	

g = open(sys.argv[1]+".data", "w")
for j in joblist:
	timeline = [j.appstart, j.sjobsubmitted, j.thjobsubmitted, j.jobsubmitted, j.jobactive, j.jobdone, j.sjobcompleted, j.append]
	g.write(j.name + " ")
	writeTimeline(timeline, g)
	g.write("\n")

g.close()


pls = log + ".pls"
data = log + ".data"
eps = log + ".eps"
image = log + ".png"

pl = open(pls, "w")
pl.write("#proc getdata\n")
pl.write("	file: " + data + "\n")
	
pl.write("#proc categories\n")
pl.write("  axis: y\n")
pl.write("  comparemethod: exact\n")
pl.write("	listsize: " + str(len(joblist)) + "\n")
	
pl.write("#proc areadef\n")
pl.write("	xrange: 0.0 " + str(scaleduration) + "\n")
pl.write("	yautorange: categories\n")
pl.write("	ycategories: datafield=1\n")
pl.write("	frame: width=0.5 color=gray(0.3)\n")
pl.write("	rectangle: 0 0 " + str(width) + " " + str(height) + "\n")
pl.write("	title: " + title + "\n")
pl.write("	titledetails: align=C style=I adjust=0,0.2\n")
	
pl.write("#proc yaxis\n")
pl.write("  ticincrement: 1\n")
pl.write("  grid: color=rgb(1,0.9,0.8) style=1 dashscale=2\n")

pl.write("#proc yaxis\n")
pl.write("  labeldetails: adjust=-0.5\n")
pl.write("  stubs: categories\n")
pl.write("  grid: color=gray(0.8)\n")
pl.write("  tics: yes\n")
pl.write("  minorticinc: 1\n")
	
pl.write("#proc xaxis\n")
pl.write("  ticincrement: " + str(xtics) + "\n")
pl.write("  grid: color=rgb(1,0.9,0.8) style=1 dashscale=2\n")

pl.write("#proc xaxis\n")
pl.write("  label: time [s]\n")
pl.write("  tics: yes\n")
pl.write("  stubs: incremental " + str(xstubs) + "\n")
pl.write("  minorticinc: " + str(xtics) + "\n")
pl.write("  grid: color=gray(0.8)\n")

colors = ["blue", "orange", "kelleygreen", "claret"]
legend = ["Pre-processing", "Queue", "Execution", "Post-processing"]
widths = ["0.04", "0.04", "0.04", "0.04"]

for i in range(0, 4): 
	pl.write("#proc bars\n")
	pl.write("	horizontalbars: yes\n")
#	pl.write("	locfield: 1\n")
	pl.write("	segmentfields: " + str(i + 2) + " " + str(i + 3) + "\n")
	pl.write("	barwidth: " + widths[i] + "\n")
	pl.write("	color: " + colors[i] + "\n")
#	pl.write("	outline: color=" + colors[i] + "\n")
	pl.write("	outline: no\n")
#	pl.write("	showvalues: yes\n")
	
	pl.write("#proc legendentry\n")
	pl.write("  sampletype: color\n")
	pl.write("  label: " + legend[i] + "\n")
	pl.write("  details: " + colors[i] + "\n")
	
scolors = ["black", "purple", "yellow"]
slegend = ["Scheduler job submit", "Task handler job submit", "Scheduler job completion"]

for i in range(0, 3):	
	pl.write("#proc scatterplot\n")
	pl.write("	yfield: 1\n")
	pl.write("	xfield: " + str(i + 7) + "\n")
	pl.write("	linelen: 0.11\n")
	pl.write("	linedir: v\n")
	pl.write("	linedetails: color=" + scolors[i] + " width=1.0\n")
	
	pl.write("#proc legendentry\n")
	pl.write("  sampletype: color\n")
	pl.write("  label: " + slegend[i] + "\n")
	pl.write("  details: " + scolors[i] + "\n")

	
pl.write("#proc legend\n")
pl.write("  format: across\n")
pl.write("  location: min+0.5 max+0.2\n")
	
pl.close()
os.system("ploticus " + pls + " -eps -o " + eps)
os.system("convert -density 96x96 " + eps + " " + image)
#os.system("eog " + image)