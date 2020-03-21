
## Project explanation:

In the system, you can create tasks that define:
1. Data source
2. Metric
3. Unit and length of the “back timeframe”
4. Unit and length of the “interval scheduler”
5. Output channels

Each task is scheduled and saved in MongoDB, then the system takes the metric-data from the data source using service API. 

Afterwards, the system searches for anomalies in the data using EGAD (Extensible Generic Anomaly Detection System) which helps by automatically detecting anomalies in large scale time-series data.
If and when it finds anomalies, it sends an alert to the output channel and saves the anomaly in MongoDB.

The system supports in search for tasks and for view all anomalies in the system.
In addition, there is a support of query/filter documents in MongoDB set out in a generic way.

The website is implemented using Play Framework which is built on Akka and helps 
to create highly-scalable web applications.
Furthermore, the system is entirely multithreaded using the Actor model and can be configured to fit different scales. 


I chose to implement a few modules as part of the platform:
* Service API - Received request from the user and converts it into Task object.
* Scheduler - Receive Task and schedule it in the due-time specified in the Task.
* Data extractor - Get Task and extract data from the specific database, according to relevant fields. This is a generic component so we will be able to extract information from any database.
* Converter - Convert the data to time-series format. A generic component that is able to convert to any format because we can get the data from the database in different formats.
* Detectors - I used EGADS (Extensible Generic Anomaly Detection System), an open-source Java package to automatically detect anomalies in large scale time-series data. 
* Anomaly writer - Generic component that alerts the anomalies on email.

Moreover: 
1. I used the Actor model to make the system multithreaded, yet easy to maintain.
2. I used MongoDB database to store Tasks and anomalies found by the detectors. I created a generic CRUD controller so I could save anything in the DB. In addition, creating a module that can query/filter documents in MongoDB so we could investigate our data.
3. I did benchmarks on our data to choose the best model and parameters for the anomaly detection algorithm on our data.
4. I created a web app compatible with mobile, using play framework and angularJS.
5. The project uses AWS features such as EC2 instance for running the project, a MongoDB instance, etc.


### Project architecture:
![Alt Text](https://user-images.githubusercontent.com/35124012/77227594-0d3d4700-6b8a-11ea-810f-dd9d0f34102f.png)


### Anomaly example
**Anomaly from metric:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206127-edad0c80-6afe-11ea-80c4-ce8ea09adf85.png)

**How the program found the anomaly:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206180-09181780-6aff-11ea-97e4-adf7e1375a7f.png)


## Website screenshots

**Create task:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206273-3a90e300-6aff-11ea-9ff9-5cb3423b3d29.png)

**Search tasks:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206322-598f7500-6aff-11ea-9f50-4b5fe9df3daf.png)

**Anomalies page:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206384-804dab80-6aff-11ea-9305-d89486fa60f0.png)

**Email alert:**
![Alt Text](https://user-images.githubusercontent.com/35124012/77206421-98252f80-6aff-11ea-8991-8e03029c48a0.png)



### Build

```
sbt test:compile
```

### Run

```
sbt run
```  

<http://localhost:9000/>

