package com.example.task.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.activity.MainActivity;
import com.example.task.other.Consts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class GoogleSignInModel {
    MainActivity activity;
    AccountManager accountManager;
    GoogleSignInOptions googleSignInOptions;
    Account account;

    public GoogleSignInModel(MainActivity activity) {
        this.activity = activity;
        this.accountManager = AccountManager.get(activity);
        this.googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        this.account = getNewAccount();
    }

    public void signIn() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions);
        Intent intent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(intent, Consts.REQUEST_CODE_SING_IN);
    }

    public void singOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions);
        googleSignInClient.signOut();
    }

    public void onSignedResult (Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            task.getResult(ApiException.class);
        } catch (ApiException e) {
            activity.runOnUiThread(() -> Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show());
            return;
        }

        account = getNewAccount();
    }

    private Account getNewAccount() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (googleSignInAccount != null) {
            return googleSignInAccount.getAccount();
        }

        return null;
    }

    public Account getAccount() {
        return account;
    }
}
