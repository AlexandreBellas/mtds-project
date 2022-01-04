# Project 3: Compute Infrastructure

This folder represents the "Compute Infrastructure" implementation using Kafka
as the main technology, along with HTML and Javascript for the front-end and
NodeJS for the back-end.

## Requirements

The requirements to run the project are
- have both [NodeJS and NPM](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
installed
- have [Docker](https://docs.docker.com/get-docker/) installed

To make sure if you have all the requirements, type the following commands
```bash
node -v
```

```bash
npm -v
```

```bash
docker -v
```

If all the commands give you feedback, it means that everything is fine.

Don't worry about Kafka. It will be installed automatically in a docker
container.

## How to configure

To configure the project, there are necessary environment variables to be
settled up. Take the `.env.example` file as example and fill all the variables
with the proper values in a new file called `.env` in the root of the project.

Here is an example on what your `.env` file could be:

```txt
GROUP_ID=tasks
CLIENT_ID=mtds-project3
BROKER_NAME=localhost:29092
MAX_TASK_TIME=10000

SERVER_PORT=3005
NUM_PROCESSES=4
TOPIC=task
```

## How to run

The two scripts `start.sh` and `stop.sh` located in the root of the project will
do all the magic to run the project. Afterwards, open `frontend/index.html` file
to interact with the backend. Later on, to follow the logs from the backend, run
the command

```bash
npm run logs
```

### Manual run

If it is necessary anyway to run the project manually, first run the command

```bash
docker compose up -d
```

and then open two terminals, typing in one of them

```bash
npm run server
```

and in the other
```
npm run process [-- -i <num>]
```

with the flag `-i` defining the number of processes you would like to spawn.
If `num` isn't defined, it has default value of 1. For example, if you want to
spawn 4 processes, type

```bash
npm run process -- -i 4
```

To follow the logs of the processes, type in the second terminal
```bash
npm run logs
```

To kill the server, just type `Ctrl+C`. To kill all the processes, type
```
npm run kill:process
```

### Debug run

To run the project in debug mode (it means: when you change the code, it
automatically restarts), follow the rules above just changing the commands to:

```bash
npm run dev:server
```

```
npm run dev:process
```
