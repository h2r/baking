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
                #print(str(items))  
                action = items[0]
                if 'wait' in action:
                    continue
                #print(str(action))
                times = items[1].split(', ')
                #print(str(times))
                if len(times) == 3:
                    times = times[1:]
                #print(str(times))
                times[0] = float(times[0])
                times[1] = float(times[1])
                params = action.split('[')[1].split(', ')
                agent = params[0]
                if times[1] > times[0]:
                    action_times[agent].append(times)
        max_time = 0.0
        condensed_times = dict()
        for agent, times in action_times.iteritems():
            condensed = []
            last = 0.0
            first = 0.0
            for time in times:
                if time[0] != last and first != last:
                    condensed.append([first, last])
                    first = time[0]
                last = time[1]
            if first != last:
                condensed.append([first, last])
            condensed_times[agent] = condensed
            print(agent + ": " + str(condensed))
            if last > max_time:
                max_time = last
        for agent, condensed in condensed_times.iteritems():
            agents_time = 0.0
            for interval in condensed:
                agents_time += interval[1] - interval[0]
            print(agent + ": " + str(agents_time) + ", " + str(agents_time / max_time))

