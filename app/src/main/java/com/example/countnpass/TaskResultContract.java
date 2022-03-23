package com.example.countnpass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.countnpass.TaskActivity.EXTRA_RESULT_STRING;

public class TaskResultContract extends ActivityResultContract<String, String> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String input) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(TaskActivity.EXTRA_SETUP_STRING, input);

        return intent;
    }

    @Override
    public String parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            return null;
        }

        return result.getStringExtra(EXTRA_RESULT_STRING);
    }
}