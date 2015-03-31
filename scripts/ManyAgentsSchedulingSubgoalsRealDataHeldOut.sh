#!/bin/sh
. Initialize_Directories "subgoals_real_data_ho" $1 $2
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsSchedulingSubgoalsRealdataHeldout $UNIQUEID 2>>"$ERRORS" 1>> "$RESULTS"
