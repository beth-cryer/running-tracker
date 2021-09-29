package com.example.psybc5_mdp_cw2.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.psybc5_mdp_cw2.NavigationActivity;
import com.example.psybc5_mdp_cw2.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    EditText et_delay, et_distance, et_step;
    Switch s_map;

    Button submit;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        et_delay = root.findViewById(R.id.et_delay);
        et_distance = root.findViewById(R.id.et_distance);
        et_step = root.findViewById(R.id.et_step_threshold);
        s_map = root.findViewById(R.id.s_map);

        submit = root.findViewById(R.id.btnSettings);
        submit.setOnClickListener(onclick);

        //Set initial values from currently-stored sharedpreferences settings:
        et_delay.setText(Integer.toString(NavigationActivity.S_DELAY));
        et_distance.setText(Integer.toString(NavigationActivity.S_DISTANCE));
        et_step.setText(Integer.toString(NavigationActivity.S_STEP_THRESH));
        s_map.setChecked(NavigationActivity.S_MAP_ENABLED);

        return root;
    }

    private String checkError(TextView v) {
        String s = v.getText().toString();
        if (s.equals("")) {
            v.setError("Cannot be empty!");
        }
        return s;
    }

    View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String delay = checkError(et_delay);
            String dist = checkError(et_distance);
            String step = checkError(et_step);

            if (delay.equals("") || dist.equals("") || step.equals("")) return;

            NavigationActivity.S_DELAY = Integer.parseInt(delay);
            NavigationActivity.S_DISTANCE = Integer.parseInt(dist);
            NavigationActivity.S_STEP_THRESH = Integer.parseInt(step);
            NavigationActivity.S_MAP_ENABLED = s_map.isChecked();

            Toast.makeText(getActivity(), "Settings updated successfully!", Toast.LENGTH_LONG).show();
        }
    };
}