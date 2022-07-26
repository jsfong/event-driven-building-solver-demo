
# Event Driven Workflow Demo for building solving
This is a demo for event driven workflow. The demo more focus on the architecture than the correctness of the domain solving.

## Problem Statement
Given an user input, the software need to "solve" calculate each element of the building. Each calculated element will be an input to another calculation. 

**CREATE use case**
From the sample use case below, user provide an input **input config**, the input will further trigger **Site Solver** to solve and produce **Site Element**. **Site Element** is further trigger **Building Solver** so on and so forth. 

**UPDATE use case**
The software also should support any update of the calculated element. An update of the element should recalculate all the element below. 


![usecase1-Simplify use case](https://user-images.githubusercontent.com/6212089/180974104-106ab1d8-d839-44fb-a006-9dc58ab930a4.jpg)

## Existing Architecture
Current architecture is a monolith java application with multithreading. In very high level, the current architecture is using **choreography** pattern. Only the solver know when it should be trigger.

Below are the main component of the current architecture:

### Engine
 - Multi-threading process 
 - Load all solver in the format of jar file
 - Decide which Solver to trigger via reflection API 
 - Hold output of a model (collection of elements) 
 - Interface with user request Restful API

### Solver
 -   Jar file contain logic of how to calculate / solver particular element
 -   Contain a firing rule file to tell the engine when and how to trigger itself. (Similar to Faas)
 -   Output element when finish solving.      

### Model / Element change detector
 -   Detect any changes of the model to element either user input or output of solver
 -   Publish the changes to the task executor to schedule solver triggering.

![image](https://user-images.githubusercontent.com/6212089/180975502-17c00e42-0a00-48da-8128-9ae05690f35c.png)

### Existing Architecture Limitations

 1. Monolith nature - unable to scale horizontally. Single point of failure. Solver can cause engine to die (eg. OOM).
 2. Multi-threading / event nature - aggregation elements is difficult. (When it is complete or which element to aggregate). 
 3. No session management implement - to support update use case (remove old elements).

## Event Driven Architecture
![usecase1-Overview](https://user-images.githubusercontent.com/6212089/180979449-5bc66599-0ef0-403b-ac50-4fde202751f8.jpg)

