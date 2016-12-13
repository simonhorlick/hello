# metrics

A server that exposes [Prometheus](https://prometheus.io) metrics over HTTP.

## Example

A MetricsServer binds a minimal HTTP server to the given port and exposes the contents of a CollectorRegistry in the standard Prometheus format.

```java
MetricsServer metrics = null;

try {
  metrics = new MetricsServer(CollectorRegistry.defaultRegistry, 9090);
  metrics.start();

  ApplicationServer server = new ApplicationServer();
  server.start();
  server.blockUntilShutdown();
} finally{
  if(metrics != null) {
    metrics.shutdown();
  }
}
```