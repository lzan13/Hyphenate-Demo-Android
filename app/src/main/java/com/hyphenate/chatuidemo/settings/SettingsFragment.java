package com.hyphenate.chatuidemo.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hyphenate.chatuidemo.R;

/**
 * Created by lzan13 on 2016/10/11.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_settings, container, false);
        init();
        return view;
    }

    private void init() {

        // Display the fragment as the main content.
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SettingsPreference())
                .commit();
    }

}
