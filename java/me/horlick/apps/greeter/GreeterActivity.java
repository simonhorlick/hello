package me.horlick.apps.greeter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class GreeterActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.greeter_activity);

    // Set up the toolbar.
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    if (null == savedInstanceState) {
      FragmentManager fragmentManager = getFragmentManager();
      fragmentManager
          .beginTransaction()
          .replace(R.id.greeter_fragment, NameEntryFragment.newInstance())
          .commit();
    }
  }

  public void navigateToGreetingDisplayFragment() {
    FragmentManager fragmentManager = getFragmentManager();

    // Replace the existing fragment and add it to the back stack.
    fragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.animator.slide_from_right,
            R.animator.slide_to_slight_left,
            R.animator.slide_from_slight_left,
            R.animator.slide_to_right)
        .replace(R.id.greeter_fragment, new GreetingDisplayFragment())
        .addToBackStack(null)
        .commit();
  }
}
