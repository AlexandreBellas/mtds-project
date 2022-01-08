# Demo project: MPI

This folder relates to the demo project implemented in C++ with MPI technology.
The task was about reading multiple files in parallel to count the frequency of
each word present in all the files.

## Setup

To setup the project, you must

- have MPI installed in your machine
- have a folder called `files` in the root of the project
- have at least one file in the `files` folder
- have all the files in the `files` folder with `.txt` extension
- have all the files in the `files` folder with number as names (e.g. `1.txt`,
`2.txt`, ...)
- run the project with the number of processes equal to the number of files in
the `files` folder

## Execution

To execute the project, you should compile the code

```bash
mpic++ -o main main.cpp
```

and run it respecting the rule about number of processes and number of files

```bash
mpirun -np 3 main # if you have 3 files and 3 available processors!
```