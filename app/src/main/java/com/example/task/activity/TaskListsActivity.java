package com.example.task.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.task.databinding.ActivityTaskListsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Objects;

public class TaskListsActivity extends Activity {
    private final static String APPLICATION_NAME = "Tasks";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private TextView mTextView;
    private ActivityTaskListsBinding binding;
    private GoogleSignInClient client;
    private GoogleSignInOptions signInOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mTextView = binding.text;

        mTextView.setText(Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)).getEmail());
    }
}