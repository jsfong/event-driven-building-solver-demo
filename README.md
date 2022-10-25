
# Event Driven Workflow Demo for building solving
This is a demo for event driven workflow for building solving. 

The demo is focus on the architecture than the correctness of the domain problem solving.
<br></br>

# Problem Statement
Given an user input, the software (a.k.a solver) need to "solve" / calculate each element of the building base on predefined algorithm. 
Each calculated element will be another input to another calculation. 

For example:
   
1. User draw a site with dimension 200 meter x 400 meter and other parameter as input config.
2. This input config trigger site solver to run and product a site element.
3. Site element will trigger building solver to run and product a building element.
4. Building element then trigger Level solver
5. etc..
<br></br>

## CREATE use case
From the sample use case below, user provide an input **input config**, the input will further trigger **Site Solver** to solve and produce **Site Element**. **Site Element** is further trigger **Building Solver** so on and so forth. 

## UPDATE use case
The software also should support any update of the calculated element. An update of the element should **recalculate** all the element below. 


![usecase1-Simplify use case](https://user-images.githubusercontent.com/6212089/180974104-106ab1d8-d839-44fb-a006-9dc58ab930a4.jpg)

<br></br>

# Existing Architecture
Current architecture is a monolith java application with multi-threading. In very high level, it is using **choreography** pattern. Only the solver know when it should be trigger.

Below are the main component of the current architecture:

## Engine
 - Multi-threading process 
 - Load all solver in the format of jar file
 - Decide which Solver to trigger via reflection API.
 - Hold output of a model (collection of elements) 
 - Interface with user request Restful API

## Solver
 -   Jar file contain logic of how to calculate / solver particular element
 -   Contain a firing / triggering rule file to tell the engine when and how to trigger itself. (Similar to Faas)
 -   Output element when finish solving.      

## Model / Element change detector
 -   Detect any changes of the model to element either user input or output of solver
 -   Publish the changes to the task executor to schedule solver triggering.

![image](https://user-images.githubusercontent.com/6212089/180975502-17c00e42-0a00-48da-8128-9ae05690f35c.png)

## Existing Architecture Limitations

 1. Monolith nature - unable to scale horizontally. Single point of failure. Solver can cause engine to die (eg. OOM).
 2. Multi-threading / event nature - aggregation elements is difficult. 
 3. No session management implement - to support update use case (remove old elements).

<br></br>

# New Event Driven Architecture

Below are the event driven parallel process architecture approve. 

 - Engine run as orchestrator to coordinate which element to trigger by publishing different ***solver job config*** into ***Solver Job Stream***.
 - The reason for engine to be an orchestrator is to have more control on how to trigger. 
	 - eg. For aggregator solver like Area Solver, engine will wait and aggregate the required element then only trigger the solver.
 - Solver will read from ***Solver Job Stream*** and solve statelessly and publish the result as **Element** back to ***Element Stream***.
 - Engine will read from ***Element Stream*** and route by create corresponding ***solver job config*** into ***Solver Job Stream***.
 - ***Element Stream*** will be sink into a graph database. This will be the output to user.
 - Engine also handle Restful API.

![usecase1-Overview (1)](https://user-images.githubusercontent.com/6212089/180979669-91e46e8a-a2f2-46a0-ae53-792dd8794645.jpg)


## CREATE model
**Engine**
![Create Model](/gif/create_model.gif)

**Model**
![Create Model](/gif/create_graph.jpg)


## UPATE Model
**Engine**

Update is similar to Create. Instead of start with Input config, we put the editted element into the queue for processing.

**Model**

![Update Model](/gif/update_graph.gif)

## DELETE Element
**Model**

Similar to udpate element step where the element which intend to be delete and all it's child will mark as "invalid" and to be delete asychoronously.

<br></br>

# PROs & CONs

## Advantages of event driven parallel architecture

 1. Prevent single point of failure. Failure in Engine and Solver will not propagate across to each other.
 2. Can easily scale horizontally.

## Disadvantages

 1. Data driven and event trigger causing debugging to be difficult.


# Tech Stack for this demo
| Component |Tech  |
|--|--|
| Engine | Spring boot  |
| Graph DB| neo4j  |
| Stream| Kafka|
| Aggregation | Kafka Stream |

## Kafka Topic
| Topic|Remark|
|--|--|
|element-input|Elements stream that hold all the calculated elements|
|solver-job-input|Contain solver job configs for corresponding solver|
|solver-metrics| Metrics info produced by solver|
|aggregated-room| Aggregated room collect per site|

<br></br>

# Horizontal Scaling

## Engine
Partition the ***element event stream*** using model id. 
In Kafka, it is as simple as creating multiple partition of element event stream and produce the element with model Id as key.

The idea is to ensure element with same model id always go to same instance of engine.
Similar to sticky session load-balancer.

<br></br>

## Solver
Simply increase the instance of solver.

In Kafka, increase ***solver job stream*** partition and increase instance of solver as consumer into the same consumer group.
 
 <br></br>

## Challenge - Aggregation of elements
One of the challenge in the problem statement is how to aggregation elements in parallel event driven architecture.
There two kind of solvers:
 - Normal solver - taking one input and generate one or multiple output
 - Aggregation solver - waiting for multiple input and generate one or multiple output

Below are the challenge of aggregation solver, taking example of problem statement above where **Area solver** need to wait for the 4 rooms element before it calculate the area.

 1. Every element is a output of a solver running paralleling, how to know **when** all require elements is calculated?
 2. Solver might generate one or multiple elements as its output depend on solver algorithm, how to know **how many** require element we are expecting?
 3. Aggregation solver is an **expensive** solver to run. Re-trigger aggregation solver each time the require element generated is not practical. That might other downstream solver that will run after aggregation solver. 

### Session Window Approach
![Untitled Diagram](https://user-images.githubusercontent.com/6212089/181809426-13e67b6d-acbd-4b75-bb99-28df65ac3008.jpg)


<br></br>

# Setup
## Tools
| Tool | Source  | Remark |
|--|--|--|
| Kafka UI Tool | https://www.kafkatool.com/download.html  | To visualise kafka stream |
| neo4j tool | http://localhost:7474/browser/ | To visualise Elements in graph form|

# How to run the demo

1. ``cd /docker/simple``
2. exec ``docker-compose up -d``
3. Ensure all the docker service is up

```
λ docker-compose up -d
Creating network "simple_default" with the default driver
Creating neo4j        ... done
Creating sn-zookeeper ... done
Creating sn-kafka     ... done
Creating simple_init-kafka_1 ... done
```

4. ``cd /mondelruntime``
5. Open the project in IDE eg Intelij & run ModelRuntimeApplication springboot application. This will start the engine.
6. Ensure engine run without error.
7. Open the Kafka UI Tool (Offset Explorer). Configure it to connect to local kafka. This tool can be use for checking the consumer (if data exist) and data in the stream.
![Offset Explorer](/gif/offset_explorer_config.JPG)

8. ``cd /solver``
9. Open the project in IDE eg Intelij & run SolverApplication springboot application. This will start the demo solver.
10. Open Postman and import the script in ``/script``
11. Execute the Postman script ``Create element to stream``
12. Either use the Postman script ``Get all element`` with URL ``localhost:8080/stream/element/3``
13. Or use neo4j ``http://localhost:7474/browser/`` and execute the query below

```
//Get  model
match (n:Element)
where n.modelId='3'
return n
```

14. We should able to get a graph as below (Which tally with the example above).

![Sample Model](/gif/sample_model.JPG)