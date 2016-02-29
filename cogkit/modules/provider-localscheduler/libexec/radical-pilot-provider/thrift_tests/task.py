#!/usr/bin/env python


def filepath_cleanup(filepath):
	fpath = filepath.strip('\n')
	if fpath.startswith('file://localhost/'):
		l = len('file://localhost/')
		fpath = fpath[l:]

	return fpath

def process(task_filename):

	task_desc = open(task_filename, 'r').readlines()

	index     = 0
	args      = []
	stageins  = []
	stageouts = []

	while index < len(task_desc):

		# We don't process directory options.
		if (task_desc[index].startswith("directory=")):
			l = len("directory=")

		elif (task_desc[index].startswith("executable=")):
			l = len("executable=")
			executable = task_desc[index][l:].strip('\n')
			print executable

		elif (task_desc[index].startswith("arg=")):
			l = len("arg=")
			args.append(task_desc[index][l:].strip('\n'))

		elif (task_desc[index].startswith("stagein.source=")):
			stagein_item = {}
 			l = len("stagein.source=")
			stagein_item['source'] = filepath_cleanup(task_desc[index][l:])
			index += 1
			if (task_desc[index].startswith("stagein.destination=")):
				l = len("stagein.destination=")
				stagein_item['destination'] = filepath_cleanup(task_desc[index][l:])
				index += 1
				if (task_desc[index].startswith("stagein.mode=")):
					l = len("stagein.mode=")
					# Ignore mode for now
					#stagein_item['destination'] = task_desc[index][l:].strip('\n')
					#index += 1
				else:
					index -= 1
			else:
				printf("[ERROR] Stagein source must have a destination")
			stageins.append(stagein_item)

		elif (task_desc[index].startswith("stageout.source=")):
			stageout_item = {}
 			l = len("stageout.source=")
			stageout_item['source'] = filepath_cleanup(task_desc[index][l:])
			index += 1
			if (task_desc[index].startswith("stageout.destination=")):
				l = len("stageout.destination=")
				stageout_item['destination'] = filepath_cleanup(task_desc[index][l:])
				index += 1
				if (task_desc[index].startswith("stageout.mode=")):
					l = len("stageout.mode=")
					# Ignore mode for now
					#stageout_item['destination'] = task_desc[index][l:].strip('\n')
					#index += 1
				else:
					index -= 1
			else:
				printf("[ERROR] Stageout source must have a destination")
			stageouts.append(stageout_item)

		else:
			print ("ignoring option : ", task_desc[index])

		index += 1

	print "ARGS      : ", args
	print "EXEC      : ", executable
	print "STAGEINS  : ", stageins
	print "STAGEOUTS : ", stageouts


process('radical.16:33:15.13764.submit')

