from sys import argv
from glob import glob
from csv import reader, Error
import math, numpy


def calculate_interval(successes, trials):
    num_trials = sum(trials)
    num_successes = sum(successes)
    num_failures = num_trials - num_successes
    z = 1.96 # for 95% confidence
    z2 = 1.96 * 1.96

    num_smoothed_trials = num_trials + z2
    num_smoothed_success = (num_successes + 0.5 * z2) / num_smoothed_trials
    num_smoothed_failure = 1.0 - num_smoothed_success
    interval = z * math.sqrt(num_smoothed_success * num_smoothed_failure / num_smoothed_trials)
    return num_smoothed_success, interval

if len(argv) > 1:
    directory = argv[1]
    files = glob(directory + "/*.csv")
    data = dict()
    for filename in files:
        #print("Processing file " + filename)
        file = open(filename, 'rb')
        
        csvreader = reader(file, delimiter=",")
        try:
            isFirst = True
            data_lines = []
            for line in csvreader:
		if len(line) != 7:
		    continue
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
            depth = line[1]
            depth_type = line[2]
            if depth_type not in data.keys():
                data[depth_type] = dict()
            if depth not in data[depth_type].keys():
                data[depth_type][depth] = [[], [], [], []]
            for i in range(4):
                data[depth_type][depth][i].append(int(line[i+3]))



    print("Depth, Depth Type, Successes, Estimate Successes, Informed Guesses, Total Trials")
    for depth_type, data_by_depth in data.iteritems():
        for depth, data_sum in data_by_depth.iteritems():
            probability_success, interval = calculate_interval(data_sum[0], data_sum[3])
            data_sum_str = str([sum(x) for x in data_sum])
            data_mean_str = str(numpy.mean(probability_success))
            
            print(str(depth) + ", " + str(depth_type) + ", " + data_sum_str + ", " + data_mean_str + " +- " + str(interval))
