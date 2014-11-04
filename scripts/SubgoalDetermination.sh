#!/bin/sh
java -Xms1g -Xmx4g -jar ~/workspace/baking/h2r-baking.jar $SGE_TASK_ID 10 > ~/results/$SGE_TASK_ID.csv
