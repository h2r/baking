from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy
import os
import re

if len(argv) > 1:

    files = []
    for arg in argv[1:]:
        if os.path.isdir(arg):
            files.extend(glob(arg + "/*.csv"))
        elif os.path.isfile(arg):
            files.append(arg)
    data = dict()
    for filename in files:
        print("Processing file " + filename)
        total_files += 1
        print(filename)
        trial_id = re.findall(r'\d+', filename)[1]
        print(trial_id)
        number_recipes = 5 * int(1 + int(trial_id) / 350)
        file = open(filename, 'rb')
        action_times = dict()
       	action_times["partner"] = []
       	action_times["human"] = []
        for line in file.readlines():
        	if "Executing action" in line:
				items = line.split(']')
				action = items[0]
				times = items[1].split(', ')
				params = action.split('[')[1].split(', ')
				agent = params[0]
				if times[1] > times[0]:
					action_times[agent].append(times)
		for agent, times in action_times.iteritems():
			print(agent + ": " + str(times))
