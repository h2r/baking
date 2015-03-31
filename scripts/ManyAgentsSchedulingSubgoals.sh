#!/bin/sh
. Initialize_Directories "many_agents_scheduling_subgoals" $1 $2
java -Xms1g -Xmx4g -cp ~/workspace/baking/h2r-baking.jar edu.brown.cs.h2r.baking.Experiments.ManyAgentsSchedulingSubgoals $UNIQUEID 2>>"$ERRORS" 1>> "$RESULTS"
