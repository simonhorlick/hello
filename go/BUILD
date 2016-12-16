load("@bazel_tools//tools/build_defs/docker:docker.bzl", "docker_build")
load("@io_bazel_rules_go//go:def.bzl", "go_binary")

go_binary(
    name = "helloworld_gateway_server",
    srcs = ["server.go"],
    deps = [
        "//protos:helloworld_gateway",
        "@com_github_golang_glog//:go_default_library",
        "@com_github_grpc_ecosystem_grpc_gateway//runtime:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_x_net//context:go_default_library",
    ],
)

# Warning: This target will only build successfully on linux.
docker_build(
    name = "helloworld_gateway_server_container",
    base = "@com_github_tianon_docker_brew_debian//:jessie",
    data_path = "/",
    entrypoint = [
        "/go/helloworld_gateway_server",
    ],
    files = [
        ":helloworld_gateway_server",
    ],
)
