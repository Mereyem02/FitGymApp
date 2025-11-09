package com.example.fitgym.ui.admin;

import com.example.fitgym.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class DashboardFragmentAdmin extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // on lie le XML du dashboard
        return inflater.inflate(R.layout.fragment_dashboard_admin, container, false);
    }
}
