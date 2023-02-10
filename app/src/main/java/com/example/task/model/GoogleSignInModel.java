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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.activity.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GoogleSignInModel {
    MainActivity activity;
    AccountManager accountManager;
    Account account;

    public GoogleSignInModel(MainActivity activity) {
        this.activity = activity;
        this.accountManager = AccountManager.get(activity);
        this.account = accountManager.getAccounts()[1];
    }

    public void signIn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose account");
        builder.setItems(getAccountNames(), (dialogInterface, i) -> {
            setAccount(i);
            activity.onAccountChanged();
        }).show();
    }

    private String[] getAccountNames() {
        Account[] accounts = accountManager.getAccounts();
        String[] names = new String[accounts.length];

        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }

        return names;
    }

    private void setAccount(int i) {
        account = accountManager.getAccounts()[i];
    }

    public Account getAccount() {
        return account;
    }
}
