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

   Steps to run dockerized application on AWS - EC2 instances (Linux AMI).

1. After cloning the code, open the hostname.conf file in root directory of project and set the hostname as PublicDNSName of EC2 instances for all three microservices.

eg: frontendHost=ec2-3-80-53-68.compute-1.amazonaws.com

catalogHost=ec2-3-90-207-174.compute-1.amazonaws.com

catalogReplicaHost=ec2-3-90-207-174.compute-1.amazonaws.com

orderHost=ec2-54-235-33-106.compute-1.amazonaws.com

orderReplicaHost=ec2-54-235-33-106.compute-1.amazonaws.com

2. copy the hostname.conf in conf folder of all the microservices

cp hostname.conf catalog/src/main/java/conf/.

cp hostname.conf order/src/main/java/conf/.

cp hostname.conf frontend/src/main/java/conf/.

3. Build the docker images using below command:

cd frontend

docker build -f Dockerfile_AWS --build-arg PORT=8080 -t frontend .

cd catalog

docker build -f Dockerfile_AWS --build-arg PORT=8081  --build-arg SERVER=1 -t catalog1 .

docker build -f Dockerfile_AWS --build-arg PORT=8083  --build-arg SERVER=2 -t catalog2 .

cd order

docker build -f Dockerfile_AWS --build-arg PORT=8082  --build-arg SERVER=1 -t order1 .

docker build -f Dockerfile_AWS --build-arg PORT=8084  --build-arg SERVER=2 -t order2 .

Check images created using: docker images

4. Compress the images and copy them on created EC2 instances.

docker save frontend -o frontend.tar

docker save catalog1 -o catalog1.tar

docker save catalog2 -o catalog2.tar

docker save order1 -o order1.tar

docker save order2 -o order2.tar

scp -i key-path  frontend.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  catalog1.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  catalog2.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  order1.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  order2.tar ec2-user@Public-DNS-Name:/home/ec2-user

(Note: copy the images to EC2 instance mapped in hostname.conf file)

5. ssh into EC2 instances

ssh -i key-path ec2-user@Public-DNS-Name

6. Install the docker dependencies:

sudo yum update -y

sudo amazon-linux-extras install docker

sudo yum install docker

sudo service docker start

7. load the docker image: sudo docker load < frontend.tar

8. Run image on EC2 instances:  

sudo docker run --network host --name frontend -v /home/ec2-user/logs:/usr/src/myapp/logs frontend:latest

sudo docker run --network host --name catalog1 -v /home/ec2-user/logs:/usr/src/myapp/logs catalog1:latest

sudo docker run --network host --name catalog2 -v /home/ec2-user/logs:/usr/src/myapp/logs catalog2:latest

sudo docker run --network host --name order1 -v /home/ec2-user/logs:/usr/src/myapp/logs order1:latest

sudo docker run --network host --name order2 -v /home/ec2-user/logs:/usr/src/myapp/logs order2:latest

9. Run the client from the local machine.




