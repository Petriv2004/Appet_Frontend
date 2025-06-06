// Generated by view binder compiler. Do not edit!
package co.unipiloto.appet.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import co.unipiloto.appet.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityCancionBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final Button btnCancion;

  @NonNull
  public final Button btnCancionParar;

  @NonNull
  public final LinearLayout main;

  private ActivityCancionBinding(@NonNull ScrollView rootView, @NonNull Button btnCancion,
      @NonNull Button btnCancionParar, @NonNull LinearLayout main) {
    this.rootView = rootView;
    this.btnCancion = btnCancion;
    this.btnCancionParar = btnCancionParar;
    this.main = main;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityCancionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityCancionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_cancion, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityCancionBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnCancion;
      Button btnCancion = ViewBindings.findChildViewById(rootView, id);
      if (btnCancion == null) {
        break missingId;
      }

      id = R.id.btnCancionParar;
      Button btnCancionParar = ViewBindings.findChildViewById(rootView, id);
      if (btnCancionParar == null) {
        break missingId;
      }

      id = R.id.main;
      LinearLayout main = ViewBindings.findChildViewById(rootView, id);
      if (main == null) {
        break missingId;
      }

      return new ActivityCancionBinding((ScrollView) rootView, btnCancion, btnCancionParar, main);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
