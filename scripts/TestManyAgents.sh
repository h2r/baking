#!/bin/sh
java -Xms1g -Xmx4g -jar ~/workspace/baking/h2r-baking.jar 8 $SGE_TASK_ID > ~/manyAgentsResults/$SGE_TASK_ID.csv
