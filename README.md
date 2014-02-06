Multidimensional Slicing
========================


To compile:

```bash
ant compile
```

To produce the jar (gossiply.jar):

```bash
ant compress
```

To run an experiment (profiles defined in the examples/\*.cfg):

```bash
java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar:gossiply.jar" peersim.Simulator example/hilbertplab.cfg
```
