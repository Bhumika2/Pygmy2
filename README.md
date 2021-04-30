# Pygmy2
Fault Tolerant version of Pygmy

Please note - 

1. JDK(11 or greater)  is required on the host machine to run the clients.
   
2. Logs of the microservices can be seen in the logs folder. Here the logs will be streamed from the container to the host machine. 
   Each microservice/replica will have its own log file.
   Logs for the clients can be seen in client/logs folder.
   
3. run.sh is a single script which will start microservices in containers and clients on the local machine. 
   
   The clients will fire requests randomly for different items. 
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

2. Run the script aws_create_image_tar.sh to create images and compress them

3. Copy the compressed images to EC2 instances. (Replace key path and public DNS name)

scp -i key-path  frontend.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  catalog1.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  catalog2.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  order1.tar ec2-user@Public-DNS-Name:/home/ec2-user

scp -i key-path  order2.tar ec2-user@Public-DNS-Name:/home/ec2-user

(Note: copy the images to EC2 instance mapped in hostname.conf file)

4. ssh into EC2 instances

ssh -i key-path ec2-user@Public-DNS-Name

5. Install the docker dependencies:

sudo yum update -y

sudo amazon-linux-extras install docker

sudo yum install docker

sudo service docker start

6. load the docker image: sudo docker load < frontend.tar

7. Run image on EC2 instances:  

sudo docker run --network host --name frontend -v /home/ec2-user/logs:/usr/src/myapp/logs frontend:latest

sudo docker run --network host --name catalog1 -v /home/ec2-user/logs:/usr/src/myapp/logs catalog1:latest

sudo docker run --network host --name catalog2 -v /home/ec2-user/logs:/usr/src/myapp/logs catalog2:latest

sudo docker run --network host --name order1 -v /home/ec2-user/logs:/usr/src/myapp/logs order1:latest

sudo docker run --network host --name order2 -v /home/ec2-user/logs:/usr/src/myapp/logs order2:latest

8. Run the client from the local machine.




