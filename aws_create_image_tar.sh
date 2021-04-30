#copy the hostname.conf in conf folder of all the microservices

cp hostname.conf catalog/src/main/java/conf/.

cp hostname.conf order/src/main/java/conf/.

cp hostname.conf frontend/src/main/java/conf/.


#Build the docker images using below command:

cd frontend

docker build -f Dockerfile_AWS --build-arg PORT=8080 -t frontend .

cd ../

cd catalog

docker build -f Dockerfile_AWS --build-arg PORT=8081  --build-arg SERVER=1 -t catalog1 .

docker build -f Dockerfile_AWS --build-arg PORT=8083  --build-arg SERVER=2 -t catalog2 .

cd ../

cd order

docker build -f Dockerfile_AWS --build-arg PORT=8082  --build-arg SERVER=1 -t order1 .

docker build -f Dockerfile_AWS --build-arg PORT=8084  --build-arg SERVER=2 -t order2 .

cd ../

#Compress the images and copy them on created EC2 instances.

docker save frontend -o frontend.tar

docker save catalog1 -o catalog1.tar

docker save catalog2 -o catalog2.tar

docker save order1 -o order1.tar

docker save order2 -o order2.tar