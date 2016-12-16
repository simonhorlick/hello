load("@io_bazel_rules_go//go:def.bzl", "go_binary")

go_binary(
    name = "server",
    srcs = ["server.go"],
    deps = [
        "//protos:helloworld_gateway",
        "@com_github_golang_glog//:go_default_library",
        "@com_github_grpc_ecosystem_grpc_gateway//runtime:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_x_net//context:go_default_library",
    ],
)
