#!/bin/sh
#$1 Experiment type (real_data_ho)
#$2 Experiment ID (JOB_ID)
#$3 Unique Id (SGE_TASK_ID)

EXPERIMENTTYPE=$1
TIMESTAMP=$(date +"%m_%d_%y__%M_%H")

NEWID=${JOB_ID:-$TIMESTAMP}
EXPERIMENTID=${2:-$NEWID}
export UNIQUEID=${$3:-SGE_TASK_ID}

#echo $TIMESTAMP

DIRECTORY="$HOME/results/$EXPERIMENTTYPE/$NEWID"
export RESULTS="$DIRECTORY/$UNIQUEID.csv"
ERR_DIRECTORY="$HOME/errors/$EXPERIMENTTYPE/$NEWID"
export ERRORS="$ERR_DIRECTORY/$UNIQUEID.err"
SAVE_DIRECTORY="$HOME/saved/$EXPERIMENTTYPE_$EXPERIMENTID"
export SAVE_FILE="$SAVE_DIRECTORY/$UNIQUEID.save"
#echo $SAVE_FILE
#echo $DIRECTORY
mkdir -p $DIRECTORY
mkdir -p $ERR_DIRECTORY
mkdir -p $SAVE_DIRECTORY