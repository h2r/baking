#!/bin/sh
. Initialize_Directories.sh "many_agents_scheduling" $1 $2
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsScheduling $SGE_TASK_ID 2>>"$ERRORS" 1>> "$RESULTS"
