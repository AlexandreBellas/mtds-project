### Setup environment
**Prerequisists:** python3.9, pip, venv 
```
python3.9 -m venv venv
source venv/bin/activate
```


To start Spark Master-Nodes

```
sudo /usr/local/spark/bin/spark-class org.apache.spark.deploy.master.Master

```

To start Spark Worker-Nodes
```
sudo /usr/local/spark/bin/spark-class org.apache.spark.deploy.worker.Worker --memory 4G --cores 4 spark://172.20.10.3:7077
```