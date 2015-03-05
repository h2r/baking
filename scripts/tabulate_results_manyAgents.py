from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy
import os

if len(argv) > 1:
    files = []
    for arg in argv[1:]:
        if os.path.isdir(arg):
            files.extend(glob(arg + "/*.csv"))
        elif os.path.isfile(arg):
            files.append(arg)
    data = dict()
    total_files = 0
    valid_files = 0
    for filename in files:
        print("Processing file " + filename)
        total_files += 1
        file = open(filename, 'rb')
        
        csvreader = reader(file, delimiter=",")
        containedResult = False
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
            if len(line) != 5 or '\t' in line or line[0] is 'Agent':
                continue
            isValid = True
            for item in line:
                if '\t' in item or '[' in item or ']' in item:
                    isValid = False
            if not isValid:
                continue
            agent = line[0]
            if agent not in data.keys():
                data[agent] = [[],[],[],[]]
            d = [0,0,0,0]
            try:
                for i in range(4):
                    d[i] = float(line[i+1])
                for i in range(4):
                    data[agent][i].append(d[i])
                    containedResult = True
            except:
                print(str(line))
        if not containedResult:
            print("file did not contain result")
            file.close()
            file = open(filename, 'rb')
            for line in file.readlines():
                print(str(line))
        else:
            valid_files += 1
            file.close()
        
    
    results = []
    print("total: " + str(total_files) + " valid: " + str(valid_files))    
    print("Agent, Successes, Trials, Average reward, average successful reward")
    for agent, line in data.iteritems():
        if sum(line[0]) == 0:
            continue
        print(str(agent) + ", " + str(int(sum(line[0]))) + ", " + str(int(sum(line[1]))) + ", " + str( numpy.mean(line[2])) + ", " + str(numpy.mean(line[3])) + " +- " + str(1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3]))))
        results.append([agent, int(sum(line[0])), int(sum(line[1])), numpy.mean(line[2]), 1.96 * numpy.std(line[2], ddof=1)/math.sqrt(len(line[2])), numpy.mean(line[3]), 1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3]))])
   

    print("\n\n\n\n\n\n")
    sorted_results = sorted(results, key= lambda line: line[0])
    
    yCoords = "symbolic y coords={"
    for line in sorted_results:
        yCoords = yCoords + "{" + str(line[0]) + "},"
    yCoords = yCoords + "}"
    print(yCoords)
    

    print("coordinates {")
    for line in sorted_results:
        print("(" + str(float(line[1]) / line[2])  + "," + str(line[0]) + ")")
    print("};")

    print("coordinates {")
    for line in sorted_results:
        print("(" + str(line[3]) + "," + str(line[0]) + ")\t+- (" + str(line[4]) + ", " + str(line[4]) + ")")
    print("};")

    print("coordinates {")
    for line in sorted_results:
        print("("  + str(line[5]) + "," + str(line[0]) + ")\t+- (" + str(line[6]) + ", " + str(line[6]) + ")")
    print("};")



