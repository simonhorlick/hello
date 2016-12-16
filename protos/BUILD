load("@com_github_simonhorlick_base//tools/build_rules:genproto.bzl", "proto_library", "java_proto_library", "javanano_proto_library")
load("@com_github_simonhorlick_base//tools/build_rules:go_genproto.bzl", "go_grpc_gateway_library")

package(default_visibility = ["//visibility:public"])

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

javanano_proto_library(
    name = "helloworld_javanano_grpc",
    deps = [
        ":helloworld_proto",
    ],
)

go_grpc_gateway_library(
    name = "helloworld_gateway",
    src = "helloworld.proto",
)
