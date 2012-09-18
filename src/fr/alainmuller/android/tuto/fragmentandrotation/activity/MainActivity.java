package fr.alainmuller.android.tuto.fragmentandrotation.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import fr.alainmuller.android.tuto.fragmentandrotation.MyListFragment;
import fr.alainmuller.android.tuto.fragmentandrotation.R;

/**
 * Created with IntelliJ IDEA.
 * User: Alain Muller
 * Date: 18/09/12
 * Time: 14:27
 * Activity principale de l'application
 */
public class MainActivity extends FragmentActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Gestion du Fragment : s'il existe déjà, on le réattache à la vue
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, new MyListFragment())
                    .commit();
        }
    }
}
