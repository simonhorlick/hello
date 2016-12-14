package(default_visibility = ["//visibility:public"])

load("@com_github_simonhorlick_base//tools/build_rules:genproto.bzl", "proto_library", "java_proto_library")

licenses(["unencumbered"])  # BSD 3-Clause

proto_library(
    name = "client_protos",
    srcs = [
        "proto/query.proto",
        "proto/topodata.proto",
        "proto/vtgate.proto",
        "proto/vtgateservice.proto",
        "proto/vtrpc.proto",
        "proto/vttest.proto",
    ],
    # FIXME(simon): This is very hacky.
    imports = ["external/io_vitess/proto"],
)

java_proto_library(
    name = "client_protos_java",
    deps = [
        ":client_protos",
    ],
)

java_library(
    name = "client",
    srcs = glob([
        "java/client/src/main/**/*.java",
        "java/grpc-client/src/main/**/*.java",
    ]),
    deps = [
        ":client_protos_java",
        "@grpc_java//:grpc-java",
        "@grpc_java//:grpc-netty",
        "@gson//jar",
        "@guava//jar",
        "@com_google_code_findbugs_jsr305//jar",
        "@org_apache_commons_commons_collections4//jar",
        "@org_joda_joda_time//jar",
        "@protobuf//:protobuf_java",
    ],
)
