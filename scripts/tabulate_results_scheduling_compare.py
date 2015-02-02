from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy


if len(argv) > 1:
    files = argv[1:]
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
            if len(line) != 4:
                continue
            edges = line[0]
            if edges not in data.keys():
                data[edges] = []
            ideal = float(line[2])
            other = float(line[1])
            diff = (other - ideal) / ideal
            print(str(diff))
            data[edges].append(diff)
                    

    print("edges, fraction suboptimal, error")
    results = []
    for edges, line in data.iteritems():
        print(str(edges) + ", " +  str( numpy.mean(line)) + " +- " + str(1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))) + ", " + str(len(line)))
        results.append([edges, numpy.mean(line), 1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line)), len(line) ])
   
    sorted_results = sorted(results, key= lambda line: int(line[0]))
    print("coordinates {")
    for line in sorted_results:
        print("(" + str(line[0]) + ", " + str(line[1]) + " )\t+- (" + str(line[2]) + ", " + "0.0)")
    print("};")