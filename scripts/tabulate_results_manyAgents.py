from sys import argv
from glob import glob
from csv import reader, Error
import math
import statistics


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
            agent = line[0]
            if agent not in data.keys():
                data[agent] = [[],[],[],[]]
            for i in range(4):
                data[agent][i].append(int(line[i+1]))

    print("Agent, Successes, Trials, Average reward, average successful reward")
    for agent, line in data.iteritems():
        print(str(agent) + ", " + str(sum(line[0])) + ", " + str(sum(line[1])) + ", " + str( statistics.mean(line[2])) + str(statistics.mean(line[3])) + " +- " + str(statistics.stdev(line[3])))
