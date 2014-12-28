from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy


if len(argv) > 1:
    directory = argv[1]
    files = glob(directory + "/*.csv")
    data = dict()
    for filename in files:
        print("Processing file " + filename)
        file = open(filename, 'rb')
        
        csvreader = reader(file, delimiter=",")
        try:
            isFirst = True
            data_lines = []
            for line in csvreader:
                if line:
                    if isFirst:
                        labels = line
                        isFirst = False
                    else:
                        data_lines.append(line)
        except:
            print("error on file")
            continue

        #Agent, Successes, Trials, Average reward, average successful reward
        for line in data_lines:
	    #print(str(line))
            agent = line[0]
            if agent not in data.keys():
                data[agent] = dict()
	    edges = line[1]
	    if edges not in data[agent].keys():
		    data[agent][edges] = []
            data[agent][edges].append(float(line[3]))
	    

    print("Agent, Edges, Average time")
    for agent, data_by_edges in data.iteritems():
	    for edges, line in data_by_edges.iteritems():
		    #print(str(line))
                    interval = 1.96 * numpy.std(line, ddof=1) / math.sqrt(len(line))
		    print(str(agent) + ", " + str(edges) + ", " + str(numpy.mean(line)) + ", " + str(interval))
