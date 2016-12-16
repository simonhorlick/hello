package main
import (
 "flag"
 "net/http"
 "strings"

 "github.com/golang/glog"
 "golang.org/x/net/context"
 "github.com/grpc-ecosystem/grpc-gateway/runtime"
 "google.golang.org/grpc"

 gw "github.com/simonhorlick/hello/protos/helloworld_gateway"
)

var (
 echoEndpoint = flag.String("echo_endpoint", "localhost:50051", "endpoint of Greeter")
)

// allowCORS allows Cross Origin Resoruce Sharing from any origin.
// Don't do this without consideration in production systems.
func allowCORS(h http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if origin := r.Header.Get("Origin"); origin != "" {
			w.Header().Set("Access-Control-Allow-Origin", origin)
			if r.Method == "OPTIONS" && r.Header.Get("Access-Control-Request-Method") != "" {
				preflightHandler(w, r)
				return
			}
		}
		h.ServeHTTP(w, r)
	})
}

func preflightHandler(w http.ResponseWriter, r *http.Request) {
	headers := []string{"Content-Type", "Accept"}
	w.Header().Set("Access-Control-Allow-Headers", strings.Join(headers, ","))
	methods := []string{"GET", "HEAD", "POST", "PUT", "DELETE"}
	w.Header().Set("Access-Control-Allow-Methods", strings.Join(methods, ","))
	glog.Infof("preflight request for %s", r.URL.Path)
	return
}

func run() error {
 ctx := context.Background()
 ctx, cancel := context.WithCancel(ctx)
 defer cancel()

 mux := runtime.NewServeMux()
 opts := []grpc.DialOption{grpc.WithInsecure()}
 err := gw.RegisterGreeterHandlerFromEndpoint(ctx, mux, *echoEndpoint, opts)
 if err != nil {
   return err
 }

 http.ListenAndServe(":50052", allowCORS(mux))
 return nil
}

func main() {
 flag.Parse()
 defer glog.Flush()

 if err := run(); err != nil {
   glog.Fatal(err)
 }
}
