#Single script to set up microservices, run the system and destroy the system

if [ $# -eq 0 ]
then
	echo "Incorrect number of arguments passed"
	exit
fi

mode=$1
if [ $mode = "all" ]
then 
	echo "Mode = all, will start up all microservices - catalog, order and frontend and clients (default 3)"
elif [ $mode = "catalog" ] || [ $mode = "order" ] || [ $mode = "frontend" ] || [ $mode = "client" ]
then
	echo "Mode = $mode, Starting up with only the desired server/client"
else
	echo "Incorrect mode selected"
	exit
fi

re='^[0-9]+$'
if ! [ -z $2 ] && ! [[ $2 =~ $re ]] 
then
   echo "Incorrect number of clients"
   exit 1
fi


serverUp="false"
clientUp="false"

if [ $mode = "all" ] || [ $mode = "catalog" ]
then 
  	#Run microservice catalog
	echo "Starting up Catalog"
	serverUp="true"
	cp hostname.conf ./catalog/src/main/java/conf/.
	cd catalog
	> logs/catalog.log
	mvn clean install > logs/catalog.log
	nohup mvn ninja:run -Dninja.jvmArgs="-Dninja.external.configuration=conf/hostname.conf" > logs/catalog.log &
	cd ../
fi

if [ $mode = "all" ] || [ $mode = "order" ]
then
  	#Run microservice order
	echo "Starting up Order"
	serverUp="true"
	cp hostname.conf ./order/src/main/java/conf/.
	cd order
	> logs/order.log
	mvn clean install > logs/order.log
	nohup mvn ninja:run -Dninja.jvmArgs="-Dninja.external.configuration=conf/hostname.conf" > logs/order.log &
	cd ../
fi

if [ $mode = "all" ] || [ $mode = "frontend" ]
then
	#Run microservice frontend
	echo "Starting up Frontend"
	serverUp="true"
	cp hostname.conf ./frontend/src/main/java/conf/.
	cd frontend
	> logs/frontend.log
	mvn clean install > logs/frontend.log
	nohup mvn ninja:run -Dninja.jvmArgs="-Dninja.external.configuration=conf/hostname.conf" > logs/frontend.log &
	cd ../
fi

sleep 5
if [ $mode = "all" ] || [ $mode = "client" ]
then 
	N=3
	if [ $# -eq 2 ]
	then
		N=$2 
	fi
	echo "Starting up $N client(s)"
	clientUp="true"

	cd client
	javac Client.java

	for ((i=1; i<= $N; i++))
	do
	  > logs/client_$i.log
	  java Client > logs/client_$i.log &
	  echo "Client $i started"
	done
	cd ../
fi

echo "Process will run for 120 seconds - value can be changed in run.sh file"
sleep 120

if [ $clientUp = "true" ]
then
	echo "Stopping client(s)"
	kill $(ps aux | grep '[C]lient' | awk '{print $2}')
fi

if [ $serverUp = "true" ]
then
	echo "Destroying all microservices"
	kill $(ps aux | grep '[n]inja' | awk '{print $2}')
fi