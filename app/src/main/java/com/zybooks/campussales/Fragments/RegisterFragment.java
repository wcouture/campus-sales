package com.zybooks.campussales.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.R;

public class RegisterFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set exit transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_exit));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_enter));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState){
        super.onViewCreated(rootView, savedInstanceState);

        EditText username = rootView.findViewById(R.id.reg_username_field);
        EditText password = rootView.findViewById(R.id.reg_password_field);
        EditText email = rootView.findViewById(R.id.reg_email_field);

        Button register_button = rootView.findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountManager account = AccountManager.getInstance(rootView.getContext());
                account.register(username.getText().toString(), password.getText().toString(), email.getText().toString(), new AccountManager.RegisterResponseHandler() {
                    @Override
                    public void onResponseReceived(boolean successful) {
                        Toast.makeText(rootView.getContext(), "Successfully registered account", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(rootView).navigate(R.id.nav_login);
                    }

                    @Override
                    public void onRegisterFailure(String error) {
                        Toast.makeText(rootView.getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}