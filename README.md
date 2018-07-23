#### Reliability Engineering Exercise

This is a simple application for the Spring Reliability Engineering exercise. To see my answers to the questions, head over to the [Questions](/Questions.md).


### Running this Application

This application uses [DataDog](https://www.datadoghq.com/) as a monitoring solution and a Datadog API key is required to run. However, it will be easy to switch to any monitoring solution supported by [Micrometer](https://micrometer.io/docs).

##### Running Locally
```bash

# If you don't have redis
brew install redis

export DATADOG_API_KEY=YOUR_API_KEY

./gradlew bootRun

# To view all available metrics
curl http://localhost:8081/actuator/metrics
```



### Background

This repo is an example of a simple Spring application and load test that exhibits a specific latency and throughput behavior.

These behaviors are:

* A small subset of requests are extreme outliers in the latency distribution. This long tail distribution of latencies results in in a higher mean in latency than the 99th percentile.

* Periodic and regular spikes in throughput and latency.

In order to recreate these behaviors a Spring Webflux app is used that has a simulated caching layer. The vast majority or requests do not need the cache or use a value found in the cache. A small subset of requests are ‘cache misses’ that require additional processing or an expensive network lookup.

To simulate the regular ‘cache miss’ a spring caching layer with a Redis backend is utilized. A time to live (ttl) on the cache is set to 1 minute. During the load test values will regularly expire in the cache and require ‘cache miss’ lookups. A 20 second `Thread.sleep` is added to the the code path that is executed when a lookup is required. This thread sleep obviously delays the response of the request that hits the ‘cache miss’. However, it also pauses the the thread implications for other requests.

Spring Webflux utilizes an intentionally small number of threads and achieves powerful concurrency by relying on the reactive paradigm and nonblocking I/O. The thread sleep is a blocking operation that prevents the thread from processing new or existing requests. In this simulated setup a queue of requests begins to build up. In a real Webflux application, blocking network calls, database lookups, or even computationally expensive operations could achieve the same result and completely exhaust a thread.

When the cache miss and thread sleep is over the thread can process the backed up requests. Because the application was unable to process these requests while they were waiting the ‘waiting time’ is not included in the time measurement. The application quickly works through the requests in the queue and includes them in the throughput calculations. This flurry of requests may result in the spikes in throughput. This is evident in the sample graphs shown below generated from a simplistic load test. Notice the periodic spikes in throughput correlated with the spikes in max latency.

**The Load Test**

For simplicity and explainability, load tests were constructed in JMeter. In the future, this project will likely include Gatling load tests as well.  The load tests executed two thread groups, consistently polling the ‘/cache-lookup’ endpoint that occasionally triggers a cache miss as well as the ‘/no-lookup’ endpoint that consistently responds with low latency.

### Todos

Setup docker-compose to enable ease of running locally.
