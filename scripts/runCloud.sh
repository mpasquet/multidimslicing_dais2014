#!/bin/sh

for i in `seq $1 $2`
        do java -Xmx2048M -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar:gossiply.jar:java-bloomfilter-1.0.jar:java-bloomfilter.jar" peersim.Simulator example/$4 > results/$3_$i.txt &
           sleep 5;
done