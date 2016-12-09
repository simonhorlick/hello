workspace(name = "com_github_simonhorlick_hello")

local_repository(
    name = "com_github_simonhorlick_base",
    path = "/Users/simon/projects/base"
)

load("@com_github_simonhorlick_base//:java_base_repositories.bzl", "java_base_repositories")
load("@com_github_simonhorlick_base//:java_test_repositories.bzl", "java_test_repositories")
load("@com_github_simonhorlick_base//:rpc_repositories.bzl", "rpc_repositories")
load("@com_github_simonhorlick_base//:docker_base_repositories.bzl", "docker_base_repositories")

java_base_repositories()

java_test_repositories()

rpc_repositories()

docker_base_repositories()
