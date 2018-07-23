**Based on the throughput graph, what do you think traffic looks like to this service? Is it relatively constant or bursty? Regular or irregular?**

The graph appears to show a major spike in throughput from 12:23 to 12:30. After 12:30, the throughput graph implies a bursty traffic pattern on a predictable regular periodic cadence. Approximately, every minute the graph implies that there is a 4x spike in throughput.

However, a review of the latency graph shows that peaks in the max latency corresponds with bursts in the throughput. This opens up the possibility that these events are related or correlated. If this was the case, the graphs may be showing an inaccurate representation of true traffic behavior.   

**What kind of latency distribution might lead to a situation where percentile values (even those as high as the 99th) are generally lower than the mean?**

A distribution with a few extreme outliers could result in a mean higher than upper percentiles. A[ Long Tail Distribution](https://en.wikipedia.org/wiki/Long_tail) like this could likely occur in a latency distribution. If the vast majority of requests have a small latency but, one or a few requests occur with a large latency the overall mean of that distribution could be significantly higher than the 99th percentile. 

An analysis of the latency graph shows that this is likely what is occurring in the graphs. The latency graph utilizes a logarithmic scale.  The maximum latency is as high as 100ms and stays well above 1ms. However, the 75th, 95th, and even the 99th percentile remain below 1ms. This likely would result in the mean climbing above the 99th percentile. This is exactly what is happening for a significant portion of the graph.

**What kinds of conditions (either resource or semantic) might cause such a distribution in a Java application?**

A Java application may cause a latency distribution to result in a higher mean than the 99th percentile when a small set of requests have an extreme increase in latency. These maximum latency requests may be caused by a wide variety of issues including networking delays, scheduling or garbage collection hiccups, occasionally computationally expensive code paths or even cache misses that require additional processing. 

The latency and throughput graphs shown in the example appear to have seasonal or periodic throughput and latency spikes. This might be the result of a form of coordinated omission where one or a few requests exhaust an application’s resources preventing it from taking on any new requests. As the application works through the expensive requests that are exhausting system resources a queue of new requests might build up. Because the application is unable to process these requests the ‘waiting time’ is not included in the time measurement. When the exhaustive requests finish, the application quickly works through the requests in the queue and includes them in the throughput calculations. This possible flurry in requests might be the result of the spikes in the throughput graph. 

This application behavior could occur when an application’s request processing threads are easily exhausted preventing the application from having the ability to process new requests. Although this could occur in many different setups it is easy to imagine in an application that is using Spring Webflux because Webflux intentionally utilizes a small number of threads. A misuse of blocking I/O or a computationally expensive task could starve the application from processing other requests. 

**How might you design and train an anomaly detection algorithm for this distribution?**

There are a wide variety of anomaly detection algorithms that could be used on this distribution. The choice of anomaly detection algorithm would depend on what anomalous behavior is intended to be detected. 

Because anomaly testing is typically a form of unsupervised learning an algorithm needs to be able to detect when time series values are significant deviations from past behavior. To accomplish this an algorithm should first model the normal behavior of the distribution so that it can forecast future values. With the forecasted value and the actual value, a statistical test should be performed to determine if the value is anomalous. 

A very basic anomaly detection algorithm might ignore the seasonality in the distribution and calculate a ‘forecast’ based on a rolling window of past samples. This could be accomplished by treating the past window as a Gaussian distribution and flagging any sample with a predetermined number of standard deviations away from the mean as an anomalous outlier. Unfortunately, this simplistic approach is based on the assumption that the time series latency data is a normal distribution and could be overly influenced by outliers. 

An approach more resistant to outliers would be to utilize the Median Absolute Deviation (MAD) to detect outliers. The MAD is the median of the absolute value of each sample from the median of the set of samples. The algorithm would then calculate if a sample was anomalous by determining if it fell within a predetermined number of normalized MAD deviations. 

Unfortunately, both of these approaches are unable to detect anomalies relative to seasonal patterns or long-term trends. A more sophisticated algorithm would need to be used that was capable of forecasting seasonal patterns and trends. One possible technique is a[ seasonal trend decomposition algorithm](https://en.wikipedia.org/wiki/Decomposition_of_time_series) that could be used to decompose the recent time series into a seasonal component, a trend, and a noise component. These learned components would be able to forecast new values with knowledge of the seasonality and the overall trend. The algorithm could flag samples as anomalous if they were outside a predetermined tolerance for the forecasted value.

**Build a simple application and load test that exhibits a distribution like you’ve predicted, and plot its throughput and latency.**

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

