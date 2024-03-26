package com.zybooks.campussales.Fragments;

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
import android.widget.TextView;
import android.widget.Toast;

import com.zybooks.campussales.AccountManager;
import com.zybooks.campussales.R;

public class LoginFragment extends Fragment{

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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState){
        super.onViewCreated(rootView, savedInstanceState);

        EditText username_field = rootView.findViewById(R.id.username_field);
        EditText password_field = rootView.findViewById(R.id.password_field);

        Button login_button = rootView.findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountManager account = AccountManager.getInstance(rootView.getContext());
                account.setUsername(username_field.getText().toString());
                account.setPassword(password_field.getText().toString());
                account.setStayLoggedIn(true);

                account.login(new AccountManager.LoginResponseHandler() {
                    @Override
                    public void onResponseReceived(boolean successful) {
                        if(successful){
                            Toast.makeText(rootView.getContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(rootView).navigate(R.id.nav_profile);
                        }
                        else{
                            Toast.makeText(rootView.getContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onLoginFailure(String error) {
                        Toast.makeText(rootView.getContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        TextView register_button = rootView.findViewById(R.id.register_label);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(rootView).navigate(R.id.nav_register);
            }
        });
    }

}