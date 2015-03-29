from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy
import os
import re
import textwrap

actual_agent_names = {"solo": "Solo", "Expert": "Full Knowledge", "AdaptiveByFlow scheduling: true":"Online update"}

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
    
    for filename in files:
        experiments = []
        data = None
        overlap = None
    
        file = open(filename, 'rb')
        
            
        max_time = 0.0
        experiment_agent = None
        recipe = "unknown"
        for line in file.readlines():
            if "Evaluating " in line:
                experiment_agent = line.replace("Evaluating ", "").strip()
                experiment_agent = actual_agent_names[experiment_agent]
                data = dict()
                overlap = dict()
                action_times = dict()
                action_times["Partner"] = []
                action_times["Human"] = []
                
            if "Recipe," in line:
                items = line.split(', ')
                recipe = items[1].replace('\n', '').title();
                if experiment_agent != None:
                    experiments.append([experiment_agent, action_times, data, overlap, recipe])
                
            if "Executing action" in line:
                items = line.split(']')
                #print(str(items))  
                action = items[0].replace("Executing action ", "")
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
                agent = params[0].capitalize()
                if times[1] > times[0]:
                    action_times[agent].append([action, times[0], times[1]])
        if recipe not in data.keys():
            data[recipe] = dict()
            overlap[recipe] = []
        
        total_width = 13.0
        

        for experiment in experiments:
            print("\\begin{frame}{" + experiment[0] + ", " + experiment[4] + "}")
            print("\\fontsize{0.15cm}{1em}")
            print("%" + experiment[0])
            print("\\begin{tikzpicture}[]")
            start = 0.0
            end = 0.0
            max_time = 0.0
            ypos = 0
            action_times = experiment[1]
            for agent, condensed in action_times.iteritems():    
                for interval in condensed:
                    max_time = max(max_time, interval[2])
            print("\\node[align=left, above] at (" + str(total_width)+ ", 3.2) {" + "{:.2f}".format(max_time) + "};")
            for agent, condensed in action_times.iteritems():
                if (len(condensed) > 0):
                    print("\\draw [thick] (0," + str(ypos) + ") -- (" + str(total_width) + "," + str(ypos) + ");")
                    print("\\node[align=left, above] at (0.0," + str(ypos + 0.6)+ ") {\\normalsize " + agent + "};")
                if agent not in data[recipe].keys():
                    data[recipe][agent] = []
                agents_time = 0.0
                
                for interval in condensed:
                    agents_time += interval[2] - interval[1]
                    interval_width = total_width * (interval[2] - interval[1]) / max_time
                    start = total_width * interval[1] / max_time
                    end = start + interval_width
                    ystart = ypos - 0.2
                    yend = ypos + 0.2
                    action = interval[0].replace("_", " ").replace("[", "")

                    # tiny text is approx 2.5mm wide
                    text_width = max(10, int(interval_width / 0.2))
                    wrapped_action = r'\\'.join(textwrap.wrap(action, text_width))

                    print("%" + action)
                    #print("\\draw [thick] (" + str(start) + "," + str(ypos) + ") -- (" + str(end) + "," + str(ypos) + ");")
                    print("\\draw (" + str(start) + "," + str(ystart) + ") -- (" + str(start) + "," + str(yend) + ");")
                    print("\\draw (" + str(end) + "," + str(ystart) + ") -- (" + str(end) + "," + str(yend) + ");")
                    print("\\node[align=left, above] at (" + str(start) + "," + str(yend)+ ") {" + "{:.2f}".format(interval[1]) + "};")
                    if "wait" in action:
                        print("\\fill[blue!40!white] (" + str(start) + "," + str(ystart) + ") rectangle (" + str(end) + "," + str(yend) + ");")
                    else:
                        print("\\node[align=left, below] at (" + str(start + (interval_width) / 2.0) + "," + str(ypos - 0.5)+ ") {" + wrapped_action + "};")
                ypos += 3.0
            print("\\end{tikzpicture}")
            print("\\end{frame}")

