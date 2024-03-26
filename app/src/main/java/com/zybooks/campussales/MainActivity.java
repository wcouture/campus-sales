package com.zybooks.campussales;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.accounts.Account;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static void refreshFragment(View v){
        int currentDestinationId = Navigation.findNavController(v).getCurrentDestination().getId();
        Navigation.findNavController(v).navigate(currentDestinationId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        AccountManager account = AccountManager.getInstance(this);
        if(account.stayLoggedIn()){
            SharedPreferences sp = getSharedPreferences("login_detail", MODE_PRIVATE);
            String username = sp.getString("username", "null");
            String password = sp.getString("password", "null");
            account.setUsername(username);
            account.setPassword(password);
            account.login(new AccountManager.LoginResponseHandler() {
                @Override
                public void onResponseReceived(boolean successful) {
                    System.out.println("Response" + String.valueOf(successful));
                }

                @Override
                public void onLoginFailure(String error) {
                    System.out.println(error);
                }
            });
        }

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(R.id.nav_dashboard, R.id.nav_posts, R.id.nav_inbox, R.id.nav_profile, R.id.nav_view).build();

            try{
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
            }catch(Exception e){
                e.printStackTrace();
            }

            NavigationUI.setupWithNavController(navView, navController);

        }
    }
}