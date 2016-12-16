java_test(
    name = "greeter_test",
    size = "small",
    srcs = ["GreeterServiceTest.java"],
    test_class = "me.horlick.helloworld.GreeterServiceTest",
    # junit, etc
    deps = [
        "//java/me/horlick/helloworld:greeter_lib",
        "//protos:helloworld_java_grpc",
        "@grpc_java//:stub",
        "@guava//jar",
        "@hamcrest_core//jar",
        "@junit//jar",
        "@org_mockito_mockito//jar",
    ],
)
