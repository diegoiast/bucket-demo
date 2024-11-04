#! /bin/bash

#set -x
set -e

SERVER="localhost:5000"
curl -X GET http://${SERVER}/init/

numbers=(2.5 1.4 1.01 1.5 2.0 1.8 1.2 2.7 1.9  2.1 2.99)

for number in "${numbers[@]}"; do
    echo "Adding number: $number"
    curl -X POST "http://${SERVER}/add_number/$number"
done

# Finalize the processing
echo "Finalizing the processor..."
curl -X GET http://${SERVER}/finalize/
