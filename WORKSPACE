workspace(name = "com_github_simonhorlick_hello")

local_repository(
    name = "com_github_simonhorlick_base",
    path = "/Users/simon/projects/base",
)

# This needs to come before rpc repositories.
git_repository(
    name = "io_bazel_rules_go",
    remote = "https://github.com/bazelbuild/rules_go.git",
    tag = "0.2.0",
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories", "new_go_repository")

go_repositories()

load("@com_github_simonhorlick_base//:java_base_repositories.bzl", "java_base_repositories")
load("@com_github_simonhorlick_base//:java_test_repositories.bzl", "java_test_repositories")
load("@com_github_simonhorlick_base//:rpc_repositories.bzl", "rpc_repositories")
load("@com_github_simonhorlick_base//:docker_base_repositories.bzl", "docker_base_repositories")

java_base_repositories()

java_test_repositories()

rpc_repositories()

docker_base_repositories()

android_sdk_repository(
    name = "androidsdk",
    api_level = 23,
    build_tools_version = "23.0.3",
    path = "/Users/simon/Library/Android/sdk",
)

new_git_repository(
    name = "io_vitess",
    build_file = "third_party/vitess.BUILD",
    remote = "https://github.com/youtube/vitess.git",
    tag = "v2.1.0-alpha.1",
)

maven_jar(
    name = "org_apache_commons_commons_collections4",
    artifact = "org.apache.commons:commons-collections4:4.1",
    sha1 = "a4cf4688fe1c7e3a63aa636cc96d013af537768e",
)

maven_jar(
    name = "org_joda_joda_time",
    artifact = "joda-time:joda-time:2.9.6",
    sha1 = "e370a92153bf66da17549ecc78c69ec6c6ec9f41",
)
