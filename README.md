# Pygmy2
Fault Tolerant version of Pygmy

Please note - 

1. JDK(11 or greater)  is required on the host machine to run the clients.
   
2. Logs of the microservices can be seen in the logs folder. Here the logs will be streamed from the container to the host machine. 
   Each microservice/replica will have its own log file.
   Logs for the clients can be seen in client/logs folder.
   
3. run.sh will start microservices in containers and clients. The clients will fire requests randomly for different items. 
   This will happen for 60 seconds and then the script will kill all processes and shut down the containers. 
   
   - To run the client for a longer duration, please change the value in run.sh file.
   - By default, 2 clients will be started. This value can be changed in run.sh file.