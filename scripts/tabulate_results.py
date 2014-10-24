from sys import argv
from glob import glob
from csv import reader

if len(argv > 1):
	directory = argv[1]
	files = glob(directory + "/*.csv")
	data = dict()
	for filename in files:
		file = open(filename, 'rb')
		csvreader = reader(file, delimiter=",")

		labels = csvreader.next()
		data_line = csvreader.next()

		depth = data_line[0]
		depth_type = data_line[1]

		if depth_type not in data.keys():
			data[depth_type] = dict()
		if depth not in data[depth_type].keys():
			data[depth_type][depth] = [0, 0, 0, 0]
		for i in range(4):
			data[depth_type][depth][i] += data_line[i+2]

	print("Depth, Depth Type, Successes, Estimate Successes, Informed Guesses, Total Trials")
	for depth_type, data_by_depth in data.iteritems():
		for depth, data_sum in data.iteritems():
			print(str(depth) + ", " + str(depth_type) + ", " + str(data_sum))