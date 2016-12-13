package(default_visibility = ["//visibility:public"])

load("@bazel_tools//tools/build_defs/docker:docker.bzl", "docker_build")

# The docker layer containing a Java runtime and all necessary system libs.
docker_build(
  name = "jre",
  base = "@com_github_tianon_docker_brew_debian//:jessie",
  debs = [
    "@glib2//file", # for jvm network
    "@ffi//file", # also needed for jvm
    "@openjdk_8_jre_headless//file",
    # These are both required for netty-tcnative
    "@libssl_linux_deb//file",
    "@libapr1_linux_deb//file",
    "@ca_certificates//file",
  ],
)
