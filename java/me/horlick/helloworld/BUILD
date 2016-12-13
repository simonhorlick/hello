package(default_visibility = ["//javatests/me/horlick/helloworld:__pkg__"])

java_library(
    name = "greeter_lib",
    srcs = [
        "GreeterServer.java",
        "GreeterService.java",
    ],
    deps = [
        "//java/me/horlick/metrics",
        "//protos:helloworld_java_grpc",
        "@grpc_java//:grpc-java",
        "@io_prometheus_simpleclient//jar",
        "@me_dinowernli_java_grpc_prometheus//src/main/java/me/dinowernli/grpc/prometheus",
        "@org_slf4j_slf4j_api//jar",
    ],
)

java_binary(
    name = "greeter_bin",
    srcs = ["GreeterApplication.java"],
    main_class = "me.horlick.helloworld.GreeterApplication",
    runtime_deps = [
        "@grpc_java//:grpc-netty",
    ],
    deps = [
        ":greeter_lib",
        "//java/me/horlick/metrics",
        "@io_prometheus_simpleclient//jar",
        "@org_slf4j_slf4j_simple//jar",
    ],
)

docker_build(
  name = "greeter_container",
  base = "//docker:jre",
  data_path = "/",
  files = [
    ":greeter_bin_deploy.jar",
  ],
  entrypoint = ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "/java/me/horlick/helloworld/greeter_bin_deploy.jar"],
)