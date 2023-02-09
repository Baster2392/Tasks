package com.example.task.model;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.task.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleSignInModel {
    Activity activity;
    GoogleSignInOptions signInOptions;
    GoogleSignInClient client;
    GoogleSignInAccount account;

    public GoogleSignInModel(Activity activity) {
        this.activity = activity;
        this.signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(activity.getString(R.string.cliend_api_key))
                .requestEmail()
                .build();
        this.client = GoogleSignIn.getClient(activity, signInOptions);
        this.account = GoogleSignIn.getLastSignedInAccount(activity);
    }

    public void signIn() {
        Intent signInIntent = client.getSignInIntent();
        activity.startActivityForResult(signInIntent, 1);
    }

    public void signOut() {
        client.signOut();
    }

    public void onLogResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            task.getResult(ApiException.class);
        } catch (ApiException e) {
            Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show();
            return;
        }

        account = GoogleSignIn.getLastSignedInAccount(activity);
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }
}
