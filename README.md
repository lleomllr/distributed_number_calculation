# distributed-number-calculation
The aim of this project is to harness the power of several machines to distribute the processing of a large number of tasks and thus benefit from high computing capacity.
The tasks are to calculate the persistence of a number. 
The system consists of a server responsible for producing the tasks to be executed and distributing them to 'workers'. Workers are remote machines that perform the calculations and send the results back to the server. The server must be able to know which 'workers' are available and have an estimate of their current load in order to distribute future tasks as effectively as possible.
A client program is used to monitor the system. 

The programme works well but is slow to run. There is still room for optimisation, particularly in file management. 

Translated with www.DeepL.com/Translator (free version)
