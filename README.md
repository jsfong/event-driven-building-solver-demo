


# Event Driven Workflow Demo for building solving
This is a demo for event driven workflow. The demo more focus on the architecture than the correctness of the domain problem solving.

## Problem Statement
Given an user input, the software need to "solve" calculate each element of the building. Each calculated element will be an input to another calculation. 

**CREATE use case**
From the sample use case below, user provide an input **input config**, the input will further trigger **Site Solver** to solve and produce **Site Element**. **Site Element** is further trigger **Building Solver** so on and so forth. 

**UPDATE use case**
The software also should support any update of the calculated element. An update of the element should **recalculate** all the element below. 


![usecase1-Simplify use case](https://user-images.githubusercontent.com/6212089/180974104-106ab1d8-d839-44fb-a006-9dc58ab930a4.jpg)

## Existing Architecture
Current architecture is a monolith java application with multi-threading. In very high level, it is using **choreography** pattern. Only the solver know when it should be trigger.

Below are the main component of the current architecture:

### Engine
 - Multi-threading process 
 - Load all solver in the format of jar file
 - Decide which Solver to trigger via reflection API.
 - Hold output of a model (collection of elements) 
 - Interface with user request Restful API

### Solver
 -   Jar file contain logic of how to calculate / solver particular element
 -   Contain a firing / triggering rule file to tell the engine when and how to trigger itself. (Similar to Faas)
 -   Output element when finish solving.      

### Model / Element change detector
 -   Detect any changes of the model to element either user input or output of solver
 -   Publish the changes to the task executor to schedule solver triggering.

![image](https://user-images.githubusercontent.com/6212089/180975502-17c00e42-0a00-48da-8128-9ae05690f35c.png)

### Existing Architecture Limitations

 1. Monolith nature - unable to scale horizontally. Single point of failure. Solver can cause engine to die (eg. OOM).
 2. Multi-threading / event nature - aggregation elements is difficult. 
 3. No session management implement - to support update use case (remove old elements).

## Event Driven Architecture

Below are the event driven parallel process architecture approve. 

 - Engine run as orchestrator to coordinate which element to trigger by publishing different ***solver job config*** into ***Solver Job Stream***.
 - The reason for engine to be an orchestrator is to have more control on how to trigger. 
	 - eg. For aggregator solver like Area Solver, engine will wait and aggregate the required element then only trigger the solver.
 - Solver will read from ***Solver Job Stream*** and solve statelessly and publish the result as **Element** back to ***Element Stream***.
 - Engine will read from ***Element Stream*** and route by create corresponding ***solver job config*** into ***Solver Job Stream***.
 - ***Element Stream*** will be sink into a graph database. This will be the output to user.
 - Engine also handle Restful API.

![usecase1-Overview (1)](https://user-images.githubusercontent.com/6212089/180979669-91e46e8a-a2f2-46a0-ae53-792dd8794645.jpg)
#### Advantages of event driven parallel architecture

 1. Prevent single point of failure. Failure in Engine and Solver will not propagate across to each other.
 2. Can easily scale horizontally.

#### Disadvantages

 1. Data driven and event trigger causing debugging to be difficult.


### Tech Stack for this demo
| Component |Tech  |
|--|--|
| Engine | Spring boot  |
| Graph DB| neo4j  |
| Stream| Kafka|
| Aggregation | Kafka Stream |

### Kafka Topic
| Topic|Remark|
|--|--|
|element-input|Elements stream that hold all the calculated elements|
|solver-job-input|Contain solver job configs for corresponding solver|
|solver-metrics| Metrics info produced by solver|
|aggregated-room| Aggregated room collect per site|

## Horizontal Scaling

### Engine
Partition the ***element event stream*** using model id. 
In Kafka, it is as simple as creating multiple partition of element event stream and produce the element with model Id as key.

The idea is to ensure element with same model id always go to same instance of engine.
Similar to sticky session load-balancer.

### Solver
Simply increase the instance of solver.

In Kafka, increase ***solver job stream*** partition and increase instance of solver as consumer into the same consumer group.
 
## Challenge - Aggregation of elements
One of the challenge in the problem statement is how to aggregation elements in parallel event driven architecture.
In monolith design, it can be simply solve by have 

## Setup
## Tools
| Tool | Source  | Remark |
|--|--|--|
| Kafka UI Tool | https://www.kafkatool.com/download.html  | To visualise kafka stream |
| neo4j tool | http://localhost:7474/browser/ | To visualise Elements in graph form|


