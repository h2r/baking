#!/bin/sh
. Initialize_Directories.sh "real_data_ho" $1 $2
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsSchedulingRealDataHeldOut $SAVE_FILE $UNIQUE
ID breakfast people noshelf 2>>"$ERRORS" 1>> "$RESULTS"
