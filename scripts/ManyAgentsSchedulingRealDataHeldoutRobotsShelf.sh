#!/bin/sh
TIMESTAMP=$(date +"%m_%d_%y__%M_%k")
#echo $TIMESTAMP


DIRECTORY="$HOME/results/real_data_ho_${JOB_ID:-$TIMESTAMP}"
ERR_DIRECTORY="$HOME/errors/real_data_ho_${JOB_ID:-$TIMESTAMP}"
SAVE_DIRECTORY="$HOME/saved/real_data_ho_${3:-$JOB_ID}"
SAVE_FILE="$SAVE_DIRECTORY/${4:-$SGE_TASK_ID}.save"
#echo $SAVE_FILE
#echo $DIRECTORY
mkdir -p $DIRECTORY
mkdir -p $ERR_DIRECTORY
mkdir -p $SAVE_DIRECTORY
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsSchedulingRealDataHeldOut $SAVE_FILE ${SGE_TASK_ID:-1} breakfast robots shelf    2>>"$ERR_DIRECTORY/${4:-$SGE_TASK_ID}.err" 1>> "$DIRECTORY/${2:-$SGE_TASK_ID}.csv"
