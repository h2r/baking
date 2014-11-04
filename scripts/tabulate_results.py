from sys import argv
from glob import glob
from csv import reader, Error
import math


def calculate_interval(num_successes, num_trials):
	num_failures = num_trials - num_successes
	z = 1.96 # for 95% confidence
	z2 = 1.96 * 1.96

	num_smoothed_trials = num_trials + z2
	num_smoothed_success = (num_successes + 0.5 * z2) / num_smoothed_trials
	num_smoothed_failure = 1.0 - num_smoothed_success
	interval = z * math.sqrt(num_smoothed_success * num_smoothed_failure / num_smoothed_trials)
	return interval

if len(argv) > 1:
	directory = argv[1]
	files = glob(directory + "/*.csv")
	data = dict()
	for filename in files:
		print("Processing file " + filename)
		file = open(filename, 'rb')
		
		csvreader = reader(file, delimiter=",")
                try:
		    labels = csvreader.next()
		    data_line = csvreader.next()
                except:
		    print("error on file")
		    continue
		depth = data_line[0]
		depth_type = data_line[1]

		if depth_type not in data.keys():
			data[depth_type] = dict()
		if depth not in data[depth_type].keys():
			data[depth_type][depth] = [0, 0, 0, 0]
		for i in range(4):
			data[depth_type][depth][i] += int(data_line[i+2])

	print("Depth, Depth Type, Successes, Estimate Successes, Informed Guesses, Total Trials")
	for depth_type, data_by_depth in data.iteritems():
		for depth, data_sum in data_by_depth.iteritems():
			probability_success = data_sum[0] / data_sum[3]
			interval = calculate_interval(data_sum[0], data_sum[3])
			print(str(depth) + ", " + str(depth_type	) + ", " + str(data_sum) + ", " + probability_success + " +- " + str(interval))
