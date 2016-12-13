package me.horlick.apps.greeter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import me.horlick.helloworld.nano.GreeterGrpc;
import me.horlick.helloworld.nano.HelloReply;
import me.horlick.helloworld.nano.HelloRequest;

public class GreeterActivity extends Activity {

  GreeterGrpc.GreeterFutureStub stub;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.greeter_activity);

    // Set up the toolbar.
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // FIXME(simon): Inject these.
    String host = "10.0.2.2";
    int port = 50051;

    ManagedChannel channel =
        OkHttpChannelBuilder.forAddress(host, port).usePlaintext(BuildConfig.DEBUG).build();
    stub = GreeterGrpc.newFutureStub(channel);

    if (null == savedInstanceState) {
      FragmentManager fragmentManager = getFragmentManager();
      fragmentManager
          .beginTransaction()
          .replace(R.id.greeter_fragment, NameEntryFragment.newInstance(stub))
          .commit();
    }
  }

  public void navigateToGreetingDisplayFragment(String s) {
    HelloRequest request = new HelloRequest();
    request.name = s;
    ListenableFuture<HelloReply> responseFuture = stub.sayHello(request);

    FragmentManager fragmentManager = getFragmentManager();

    // Replace the existing fragment and add it to the back stack.
    fragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.animator.slide_from_right,
            R.animator.slide_to_slight_left,
            R.animator.slide_from_slight_left,
            R.animator.slide_to_right)
        .replace(R.id.greeter_fragment, GreetingDisplayFragment.newInstance(responseFuture))
        .addToBackStack(null)
        .commit();
  }
}
