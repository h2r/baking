#!/bin/sh

TIMESTAMP=$(date +"%m_%d_%y__%M_%k")
#echo $TIMESTAMP


DIRECTORY="$HOME/results/scheduling_results_${JOB_ID:-$TIMESTAMP}"
#echo $DIRECTORY
mkdir -p $DIRECTORY

java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.SchedulingComparison > "$DIRECTORY/$SGE_TASK_ID.csv"