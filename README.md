
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
![image](https://user-images.githubusercontent.com/6212089/180975502-17c00e42-0a00-48da-8128-9ae05690f35c.png)
