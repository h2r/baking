from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy


if len(argv) > 1:
    directories = argv[1:]
    data = dict()
    for directory in directories:
        files = glob(directory + "/*.csv")
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
                if len(line) != 5:
                    continue
                agent = line[0]
                if agent not in data.keys():
                    data[agent] = [[],[],[],[]]
                for i in range(4):
                    data[agent][i].append(float(line[i+1]))

    results = []
    
    print("Agent, Successes, Trials, Average reward, average successful reward")
    for agent, line in data.iteritems():
        print(str(agent) + ", " + str(int(sum(line[0]))) + ", " + str(int(sum(line[1]))) + ", " + str( numpy.mean(line[2])) + ", " + str(numpy.mean(line[3])) + " +- " + str(1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3]))))
        results.append([agent, int(sum(line[0])), int(sum(line[1])), numpy.mean(line[2]), 1.96 * numpy.std(line[2], ddof=1)/math.sqrt(len(line[2])), numpy.mean(line[3]), 1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3]))])
   
    sorted_results = sorted(results, key= lambda line: line[0])
    print("coordinates {")
    for line in sorted_results:
        print("(" + str(float(line[1]) / line[2])  + ", " + str(line[0]) + ")")
    print("};")

    print("coordinates {")
    for line in sorted_results:
        print("(" + str(line[3]) + ", " + str(line[0]) + " )\t+- (" + str(line[4]) + ", " + str(line[4]) + ")")
    print("};")

    print("coordinates {")
    for line in sorted_results:
        print("("  + str(line[5]) + ", " + str(line[0]) + " )\t+- (" + str(line[6]) + ", " + str(line[6]) + ")")
    print("};")



