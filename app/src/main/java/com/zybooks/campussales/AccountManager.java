package com.zybooks.campussales;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.navigation.Navigation;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountManager {

    public interface LoginResponseHandler{
        public void onResponseReceived(boolean successful);
        public void onLoginFailure(String error);
    }
    public interface RegisterResponseHandler{
        void onResponseReceived(boolean successful);
        void onRegisterFailure(String error);
    }
    private static AccountManager instance;
    private Context context;
    private RequestQueue requestQueue;
    private boolean loggedIn = false;
    private boolean stayLoggedIn = false;
    private final String BASE_URL = "https://fo-stats.willc-dev.net/sales/";//https://campussalesserver.willcouture.repl.co/";
    private String username = "null";
    private String password = "null";
    private String email = "null";
    private long auth_id = -1;

    public static AccountManager getInstance(Context context){
        if(instance == null){
            instance = new AccountManager(context);
        }
        return instance;
    }

    private AccountManager(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        if(stayLoggedIn && false) {
            SharedPreferences sp = context.getSharedPreferences("login_details", Context.MODE_PRIVATE);
            username = sp.getString("username", "null");
            password = sp.getString("password", "null");
            if (username != "null" && password != "null") {
                login(new LoginResponseHandler() {
                    @Override
                    public void onResponseReceived(boolean successful) {

                    }

                    @Override
                    public void onLoginFailure(String error) {
                        System.out.println(error);
                    }
                });

            }
            else{
                System.out.println("null details");
            }
        }
    }

    public void setStayLoggedIn(boolean stayloggedIn){
        this.stayLoggedIn = stayloggedIn;
        SharedPreferences sp = context.getSharedPreferences("login_details", Context.MODE_PRIVATE);
        if(stayloggedIn){
            sp.edit().putString("username", username).putString("password", password).apply();
        }
        else{
            sp.edit().putString("username", "null").putString("password", "null").apply();
        }
    }

    public void setUsername(String u_name){
        username = u_name;
    }
    public String getUsername() { return username; }

    public void setPassword(String p_word){
        password = p_word;
    }

    public String getEmail() { return email; }

    public void login(LoginResponseHandler LRH){
        if (password.equals("null") || username.equals("null")){
            LRH.onLoginFailure("Enter username and password");
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("user", username);
            data.put("pass", password);
        }catch(Exception e){
            LRH.onLoginFailure(e.toString());
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL + "login", data , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("Response: " + response);
                try {
                    boolean result = response.getBoolean("result");
                    LRH.onResponseReceived(result);
                    if(result){
                        auth_id = response.getLong("auth_id");
                        email = response.getString("email");
                        loggedIn = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LRH.onLoginFailure(error.toString());
            }
        });

        requestQueue.add(request);

    }

    public void register(String username, String password, String email, RegisterResponseHandler handler){
        if(username.equals("null") || password.equals("null") || email.indexOf("@fsu.edu") == -1){
            handler.onRegisterFailure("Input a username, password, and official FSU email");
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("user", username);
            data.put("pass", password);
            data.put("email", email);
        }catch(Exception e){
            handler.onRegisterFailure(e.toString());
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL + "register", data , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("Response: " + response);
                try {
                    boolean result = response.getBoolean("result");
                    handler.onResponseReceived(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.onRegisterFailure(error.toString());
            }
        });

        requestQueue.add(request);

    }

    public long getAuth_id(){
        return auth_id;
    }

    public boolean isLoggedIn(){
        return loggedIn;
    }

    public boolean stayLoggedIn(){
        return stayLoggedIn;
    }
}
