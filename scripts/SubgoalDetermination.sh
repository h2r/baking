#!/bin/sh
TIMESTAMP=$(date +"%m_%d_%y__%M_%k")
#echo $TIMESTAMP

RECIPE_TYPE=$1
DIRECTORY="$HOME/results/subgoal_results_${JOB_ID:-$TIMESTAMP}"
ERR_DIRECTORY="$HOME/errors/subgoal_results_${JOB_ID:-$TIMESTAMP}"
echo $DIRECTORY
mkdir -p $DIRECTORY
mkdir -p $ERR_DIRECTORY
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.SubgoalDetermination $RECIPE_TYPE 2>"$ERR_DIRECTORY/$SGE_TASK_ID.csv" 1>"$DIRECTORY/$SGE_TASK_ID.csv"
