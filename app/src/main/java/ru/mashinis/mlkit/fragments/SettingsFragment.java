package ru.mashinis.mlkit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import ru.mashinis.mlkit.MainActivity;
import ru.mashinis.mlkit.R;

public class SettingsFragment extends Fragment {

    private EditText kilometerEditText;
    private EditText pathNumberEditText;
    private Button toggleButton;
    private Button saveButton;
    private boolean isPositive = true;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        kilometerEditText = view.findViewById(R.id.editTextKilometer);
        pathNumberEditText = view.findViewById(R.id.editTextPathNumber);
        toggleButton = view.findViewById(R.id.toggleButton);
        saveButton = view.findViewById(R.id.saveButton);

        toggleButton.setOnClickListener(v -> {
            isPositive = !isPositive;
            toggleButton.setText(isPositive ? "четный" : "нечетный");
        });

        saveButton.setOnClickListener(v -> {
            int currentKilometer = Integer.parseInt(kilometerEditText.getText().toString());
            String pathNumber = pathNumberEditText.getText().toString();

            ((MainActivity) requireActivity()).showDataFragment(currentKilometer, pathNumber, isPositive);
        });

        return view;
    }
}

