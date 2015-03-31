from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy


if len(argv) > 1:
    files = argv[1:]
    data = dict()
    times = dict()
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
            if len(line) < 4:
                continue
            edges = line[0]
            if edges not in data.keys():
                data[edges] = dict()
                times[edges] = dict()
            trueValue = 0.0
            datum = dict()
            for i in range(1, len(line), 3):
                try:
                    label = line[i].strip()
                    value = float(line[i+1])
                    time = float(line[i+2])
                    if label == "milp":
                        trueValue = value
                    if label not in times[edges].keys():
                        times[edges][label] = []
                    times[edges][label].append(time)
                    datum[label] = value
                except ValueError as e:
                    pass
            if trueValue == 0.0:
                print(str(line))
            for label, value in datum.iteritems():
                if label not in data[edges].keys():
                    data[edges][label] = []
                data[edges][label].append((value - trueValue) / trueValue)
                    
            

    print("edges, values, error, times, error")
    all_results = dict()
    all_times = dict()
    for label in labels:
        all_results[label] = []
    for edges, lines in data.iteritems():
        for label, line in lines.iteritems():
            if label not in all_results.keys():
                all_results[label] = []
            print(str(edges) + ", " +  label + ", " + str( numpy.mean(line)) + " +- " + str(1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))))
            all_results[label].append([edges, numpy.mean(line), 1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))])
   
    for edges, lines in times.iteritems():
        for label, line in lines.iteritems():
            if label not in all_times.keys():
                all_times[label] = []
            print(str(edges) + ", " +  label + ", " + str( numpy.mean(line)) + " +- " + str(1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))))
            all_times[label].append([edges, numpy.mean(line), 1.96 * numpy.std(line, ddof=1)/math.sqrt(len(line))])
   
    colors = ["blue", "red", "black", "green!40!black"]
    keys = all_results.keys()
    for label, results in all_results.iteritems():
        if len(results) == 0:
            keys.remove(label)
    print(str(keys))
    colors = dict(zip(keys, colors))
    print("Differences")
    for label, results in all_results.iteritems():
        if len(results) == 0:
            continue
        sorted_results = sorted(results, key= lambda line: int(line[0]))
        print("\\addplot[color=" + colors[label] + ", error bars/.cd,y dir=both,y explicit]")
        print("coordinates {")
        for line in sorted_results:
            print("(" + str(line[0]) + ", " + str(line[1]) + " )\t+- (" + str(line[2]) + ", " + str(line[2]) + ")")
        print("};")
        print("\\addlegendentry{" + label + "}")
    print("Times")
    for label, results in all_times.iteritems():
        if len(results) == 0:
            continue
        sorted_results = sorted(results, key= lambda line: int(line[0]))
        print("\\addplot[color=" + colors[label] + ", error bars/.cd,y dir=both,y explicit]")
        print("coordinates {")
        for line in sorted_results:
            print("(" + str(line[0]) + ", " + str(line[1]) + " )\t+- (" + str(line[2]) + ", " + str(line[2]) + ")")
        print("};")
        print("\\addlegendentry{" + label + "}")

