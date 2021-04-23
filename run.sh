#Single script to set up microservices, run the system and destroy the system

# copy properties file with hostname to every microservice folder
cp hostname.conf catalog/src/main/java/conf/.
cp hostname.conf order/src/main/java/conf/.
cp hostname.conf frontend/src/main/java/conf/.

# run all microservices in containers using docker compose
echo "Starting up microservices in containers"
docker compose up --detach

echo "Waiting for servers to come up"
sleep 90

echo "Starting up client(s)"
cd client
javac Client.java

N=2  #number of clients
for ((i=1; i<= $N; i++))
	do
	  > logs/client_$i.log
	  java Client > logs/client_$i.log &
	  echo "Client $i started"
	done
cd ../

echo "Process will run for 60 seconds - value can be changed in run.sh file"
sleep 60

echo "Killing clients"
kill $(ps aux | grep '[C]lient' | awk '{print $2}')

echo "Destroying all containers and images"
docker compose down --rmi all