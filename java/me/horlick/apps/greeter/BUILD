genrule(
    name = "build-config-genrule",
    outs = ["BuildConfig.java"],
    cmd = "echo 'package me.horlick.apps.greeter;" +
          "public class BuildConfig {" +
          "public static final boolean DEBUG = true;" +
          "}' > $(@)",
)

android_binary(
    name = "greeter_bin",
    srcs = glob(["**/*.java"]) + [":build-config-genrule"],
    manifest = "AndroidManifest.xml",
    resource_files = glob([
        "res/**",
    ]),
    deps = [
        "//protos:helloworld_javanano_grpc",
        "@com_google_code_findbugs_jsr305//jar",
        "@grpc_java//:core",
        "@grpc_java//:grpc-okhttp",
        "@guava//jar",
    ],
)
