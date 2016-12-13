package me.horlick.apps.greeter;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class NameEntryFragment extends Fragment {

  public static Fragment newInstance() {
    return new NameEntryFragment();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.name_entry_fragment, container, false);

    setRetainInstance(true);

    final TextView name = (TextView) root.findViewById(R.id.name);

    Button signUpButton = (Button) root.findViewById(R.id.okButton);
    signUpButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.i("NameEntryFragment", name.getText().toString());
          }
        });

    return root;
  }
}
