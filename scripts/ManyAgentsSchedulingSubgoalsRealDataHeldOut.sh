#!/bin/sh
TIMESTAMP=$(date +"%m_%d_%y__%M_%k")
#echo $TIMESTAMP


DIRECTORY="$HOME/results/subgoals_real_data_ho_${JOB_ID:-$TIMESTAMP}"
ERR_DIRECTORY="$HOME/errors/subgoals_real_data_ho_${JOB_ID:-$TIMESTAMP}"
#echo $DIRECTORY
mkdir -p $DIRECTORY
mkdir -p $ERR_DIRECTORY
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsSchedulingSubgoalsRealdataHeldout $SGE_TASK_ID 2>"$ERR_DIRECTORY/$SGE_TASK_ID.err" 1> "$DIRECTORY/$SGE_TASK_ID.csv"
