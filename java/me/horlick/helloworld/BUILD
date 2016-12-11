package(default_visibility = ["//javatests/me/horlick/helloworld:__pkg__"])

java_library(
    name = "greeter_lib",
    srcs = [
        "GreeterServer.java",
        "GreeterService.java",
    ],
    deps = [
        "//protos:helloworld_java_grpc",
        "@grpc_java//:grpc-java",
        "@me_dinowernli_java_grpc_prometheus//jar",
    ],
)

java_binary(
    name = "greeter_bin",
    srcs = ["GreeterApplication.java"],
    main_class = "me.horlick.helloworld.GreeterApplication",
    runtime_deps = [
        "@grpc_java//:grpc-netty",
        "@io_prometheus_simpleclient//jar",
    ],
    deps = [":greeter_lib"],
)
