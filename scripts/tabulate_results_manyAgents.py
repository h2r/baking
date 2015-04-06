from sys import argv
from glob import glob
from csv import reader, Error
import math
import numpy
import os

def get_data(csvreader):
    isFirst = True
    data_lines = []
    for line in csvreader:
        if line:
            if isFirst:
                labels = line
                isFirst = False
            else:
                data_lines.append(line)
    return data_lines

def extract_data(data_lines):
    #Agent, Successes, Trials, Average reward, average successful reward
    recipe = "unknown"
    agent = "unknown"
    data = dict()
    data_recipes = dict()
    
    for line in data_lines:
        if "Evaluating" in line[0]:
            agent = line[0]
        if len(line) == 2 and line[0] == "Recipe":
            recipe = line[1]
        if (len(line) != 5 and len(line) != 6) or '\t' in line or line[0] is 'Agent':
            continue
        isValid = True
        for item in line:
            if '\t' in item or '[' in item or ']' in item:
                isValid = False
        if not isValid:
            continue
        print(str(line))
        agent = line[0]
        if recipe not in data_recipes.keys():
            data_recipes[recipe] = dict()
        if agent not in data.keys():
            data[agent] = [[],[],[],[]]
        if agent not in data_recipes[recipe].keys():
            data_recipes[recipe][agent] = [[],[],[],[]]
        d = [0,0,0,0]

        try:
            for i in range(4):
                d[i] = float(line[i+1])
            for i in range(4):
                data[agent][i].append(d[i])
                data_recipes[recipe][agent][i].append(d[i])
                containedResult = True
        except:
            print(str(line))
    print("\n")
    if len(data.keys()) < 3:
        return None

    condensed_data = dict()
    for agent, lines in data.iteritems():
        if agent != 'solo':
            if agent not in condensed_data.keys():
                condensed_data[agent] = [[],[],[],[]]
            condensed_data[agent][0].extend(lines[0])
            condensed_data[agent][1].extend(lines[1])
            for i in range(len(lines[2])):
                condensed_data[agent][2].append(lines[2][i] - data['solo'][2][i])
            for i in range(len(lines[3])):
                condensed_data[agent][3].append(lines[3][i] - data['solo'][3][i])

    condensed_recipes = dict()
    for recipe, data_agent in data_recipes.iteritems():
        print(str(data_agent))
        if recipe not in condensed_recipes.keys():
            condensed_recipes[recipe] = dict()
        for agent, lines in data_agent.iteritems():
            if agent != 'solo':
                if agent not in condensed_recipes[recipe].keys():
                    condensed_recipes[recipe][agent] = [[],[],[],[]]
                condensed_recipes[recipe][agent][0].extend(lines[0])
                condensed_recipes[recipe][agent][1].extend(lines[1])
                for i in range(len(lines[2])):
                    condensed_recipes[recipe][agent][2].append(lines[2][i] - data_agent['solo'][2][i])
                for i in range(len(lines[3])):
                    condensed_recipes[recipe][agent][3].append(lines[3][i] - data_agent['solo'][3][i])
     
    #return [data, data_recipes]           
    return [condensed_data, condensed_recipes]

def append_data(data, data_recipes, append):
    newData = append[0]
    newData_recipes = append[1]

    for agent, lines in newData.iteritems():
        if agent not in data.keys():
            data[agent] = [[],[],[],[]]
        for i in range(4):
            data[agent][i].extend(lines[i])
    for recipe, data_agent in newData_recipes.iteritems():
        if recipe not in data_recipes.keys():
            data_recipes[recipe] = dict()
        for agent, lines in data_agent.iteritems():
            if agent not in data_recipes[recipe].keys():
                data_recipes[recipe][agent] = [[],[],[],[]]
            for i in range(4):
                data_recipes[recipe][agent][i].extend(lines[i])

def print_results(data, data_recipes, total_files, valid_files):
    results = []
    print("total: " + str(total_files) + " valid: " + str(valid_files))    
    print("Agent, Successes, Trials, Average reward, average successful reward")

    print("\t X Y")
    for agent, line in data.iteritems():
        if sum(line[0]) == 0:
            continue
        for num in line[2]:
            print(agent + " " + str(num))
        #print(str(agent) + ", " + str(int(sum(line[0]))) + ", " + str(int(sum(line[1]))) + ", " + str( numpy.mean(line[2])) + ", " + str(numpy.mean(line[3])) + " +- " + str(1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3]))))
        results.append([agent, int(sum(line[0])), int(sum(line[1])), numpy.mean(line[2]), 1.96 * numpy.std(line[2], ddof=1)/math.sqrt(len(line[2])), numpy.mean(line[3]), 1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3])), min(line[3]), max(line[3])])

    print("\n\n")
    results_recipes = dict()
    for recipe, data_by_agent in data_recipes.iteritems():
        if recipe not in results_recipes.keys():
            results_recipes[recipe] = []
        for agent, line in data_by_agent.iteritems():
            #for num in line[2]:
            #    print(agent + " " + str(num))
            results_recipes[recipe].append([agent, int(sum(line[0])), int(sum(line[1])), numpy.mean(line[2]), 1.96 * numpy.std(line[2], ddof=1)/math.sqrt(len(line[2])), numpy.mean(line[3]), 1.96 * numpy.std(line[3], ddof=1)/math.sqrt(len(line[3])), min(line[3]), max(line[3])])
        
   
    for recipe, results_by_agent in data_recipes.iteritems():
        print("%%" + recipe)
        print("\t X Y Y_error Label")
        for agent, line in results_by_agent.iteritems():
            print("\t{" + agent.strip() + "} " + str(line[2]) + " " + str(line[2]))



if __name__ == "__main__":

    if len(argv) > 1:
        files = []
        for arg in argv[1:]:
            if os.path.isdir(arg):
                files.extend(glob(arg + "/*.csv"))
            elif os.path.isfile(arg):
                files.append(arg)
        data = dict()
        data_recipes = dict()
        total_files = 0
        valid_files = 0
        for filename in files:
            #print("Processing file " + filename)
            total_files += 1
            file = open(filename, 'rb')
            
            csvreader = reader(file, delimiter=",")
            containedResult = False
            try:
                data_lines = get_data(csvreader)
            except:
                print("error on file")
                continue
            
            results = extract_data(data_lines)
            if results != None:
                append_data(data, data_recipes, results)
                valid_files += 1
            else:
                print("error on file " + filename)
            
            file.close()
        print_results(data, data_recipes, total_files, valid_files)

        


