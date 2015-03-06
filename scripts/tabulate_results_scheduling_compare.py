from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy


if len(argv) > 1:
    files = argv[1:]
    data = dict()
    correct = dict()
    labels = []
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
            if len(line) < 3:
                continue
            edges = line[0]
            if edges not in data.keys():
                data[edges] = []
                correct[edges] = []
            ideal = float(line[1])
            for i in range(1, len(line)):
                try:
                    other = float(line[i])
                    diff = (other - ideal) / ideal
                    c = 1 if (diff < 0.00001) else 0
                    if i-1 >= len(data[edges]):
                        data[edges].append([])
                        correct[edges].append([])
                    if other > 0.0:
                        data[edges][i-1].append(diff)
                        correct[edges][i-1].append(c)
                except ValueError as e:
                    pass
                    

    print("edges, fraction suboptimal, error")
    all_results = dict()
    for label in labels:
        all_results[label] = []
    for edges, lines in data.iteritems():
        correct_lines = correct[edges]
        for i in range(len(lines)):
            line = lines[i]
            label = labels[i]
            c = correct_lines[i]
            print(str(edges) + ", " +  str( numpy.mean(line)) + " +- " + str(1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))) + ", " + str(sum(c)) + "/" + str(len(line)))
            all_results[label].append([edges, numpy.mean(line), 1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line)), sum(c), len(line) ])
   
    print(len(all_results))
    for label, results in all_results.iteritems():


        sorted_results = sorted(results, key= lambda line: int(line[0]))
        print("% " + label)
        for line in sorted_results:
             print("% " + str(line[0]) + " - " +  str(line[3]) + "/" + str(line[4]))
        print("coordinates {")
        for line in sorted_results:
            print("(" + str(line[0]) + ", " + str(line[1]) + " )\t+- (" + str(line[2]) + ", " + str(line[2]) + ")")
        print("};")


