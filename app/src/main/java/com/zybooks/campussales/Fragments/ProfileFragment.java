package com.zybooks.campussales.Fragments;

import android.accounts.Account;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.R;

public class ProfileFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set exit transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_out));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_enter));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState){
        super.onViewCreated(rootView, savedInstanceState);

        if(AccountManager.getInstance(getContext()).isLoggedIn() == false){
            Navigation.findNavController(rootView).navigate(R.id.nav_login);
        }

        TextView username_label = rootView.findViewById(R.id.username_label);
        TextView email_label = rootView.findViewById(R.id.email_label);

        AccountManager account = AccountManager.getInstance(rootView.getContext());
        username_label.setText(account.getUsername());
        email_label.setText(account.getEmail());
    }
}