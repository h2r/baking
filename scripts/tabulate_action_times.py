from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy
import os
import re

def calculate_overlap(agent1, agent2):

    #print("human: " + str(agent1))
    #print("partner: " + str(agent2))
    sum = 0.0
    for interval1 in agent1:
        for interval2 in agent2:
            overlap = calculate_overlap_intervals(interval1, interval2)
            #print(str(interval1) + ", " + str(interval2))
            #print("overlap " + str(overlap))
            sum += overlap
    #print("total overlap " + str(sum) + "\n")
    return sum

def calculate_overlap_intervals(interval1, interval2):
    max_lower = max(interval1[0], interval2[0])
    min_upper = min(interval1[1], interval2[1])
    return max(0.0, min_upper - max_lower)

if len(argv) > 1:

    files = []
    for arg in argv[1:]:
        if os.path.isdir(arg):
            files.extend(glob(arg + "/*.csv"))
        elif os.path.isfile(arg):
            files.append(arg)
    data = dict()
    overlap = dict()
    for filename in files:
        file = open(filename, 'rb')
        action_times = dict()
        action_times["partner"] = []
        action_times["human"] = []
        recipe = "unknown"
            
        max_time = 0.0
        for line in file.readlines():
            if "Recipe" in line:
                items = line.split(', ')
                recipe = items[1].replace('\n', '')
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
                max_time = max(max_time, times[1])
                params = action.split('[')[1].split(', ')
                agent = params[0]
                if times[1] > times[0]:
                    action_times[agent].append(times)
        if recipe not in data.keys():
            data[recipe] = dict()
            overlap[recipe] = []
        
        for agent, condensed in action_times.iteritems():
            if agent not in data[recipe].keys():
                data[recipe][agent] = []
            agents_time = 0.0
            for interval in condensed:
                agents_time += interval[1] - interval[0]
            #print(agent + ": " + str(agents_time) + ", " + str(agents_time / max_time))
            data[recipe][agent].append(agents_time / max_time)
            recipe_overlap = calculate_overlap(action_times["human"], action_times["partner"])
            overlap[recipe].append(recipe_overlap / max_time)
    
    print("coordinates{")
    for recipe, d in data.iteritems():
        print("(" + recipe + ", " + str(numpy.mean(overlap[recipe])) + ")")
    print("};")
