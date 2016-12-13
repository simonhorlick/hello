android_binary(
    name = "greeter_bin",
    srcs = glob(["**/*.java"]),
    manifest = "AndroidManifest.xml",
    resource_files = glob([
        "res/**",
    ]),
    deps = [
        "//protos:helloworld_javanano_grpc",
        "@grpc_java//:core",
        "@grpc_java//:grpc-okhttp",
    ],
)
