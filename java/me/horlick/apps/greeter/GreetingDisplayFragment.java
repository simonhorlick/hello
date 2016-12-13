package me.horlick.apps.greeter;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nullable;
import me.horlick.helloworld.nano.HelloReply;

public class GreetingDisplayFragment extends Fragment {

  private TextView greeting;
  private ListenableFuture<HelloReply> responseFuture;

  public static Fragment newInstance(ListenableFuture<HelloReply> responseFuture) {
    GreetingDisplayFragment fragment = new GreetingDisplayFragment();
    fragment.setResponseFuture(responseFuture);
    return fragment;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.greeting_display_fragment, container, false);

    setRetainInstance(true);

    greeting = (TextView) root.findViewById(R.id.greeting);

    if (responseFuture != null) {
      Futures.addCallback(
          responseFuture,
          new FutureCallback<HelloReply>() {
            @Override
            public void onSuccess(@Nullable HelloReply helloReply) {
              if (helloReply != null) {
                displayMessage(helloReply.message);
              }
            }

            @Override
            public void onFailure(Throwable throwable) {
              displayMessage(throwable.getLocalizedMessage());
            }
          });
    }

    return root;
  }

  private void displayMessage(final String message) {
    getActivity()
        .runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                greeting.setText(message);
              }
            });
  }

  public void setResponseFuture(ListenableFuture<HelloReply> responseFuture) {
    this.responseFuture = responseFuture;
  }
}
