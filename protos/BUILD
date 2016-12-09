load("@com_github_simonhorlick_base//tools/build_rules:genproto.bzl", "proto_library", "java_proto_library")

proto_library(
    name = "helloworld_proto",
    srcs = ["helloworld.proto"],
)

java_proto_library(
    name = "helloworld_java_grpc",
    deps = [
        ":helloworld_proto",
    ],
)
