package ru.mashinis.mlkit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import ru.mashinis.mlkit.fragments.DataFragment;
import ru.mashinis.mlkit.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .commit();
        }
    }

    public void showDataFragment(int currentKilometer, String pathNumber, boolean isPositive) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DataFragment.newInstance(currentKilometer, pathNumber, isPositive))
                .addToBackStack(null)
                .commit();
    }
}
