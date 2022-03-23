package com.example.countnpass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends ComponentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityResultLauncher<String> getTaskResult;
    private EditText startBlockText;
    private EditText startTaskText;
    private ArrayList<Integer> trialIDs;
    private int participantNumber;
    private int currentTask;
    private int totalTasks;
    private int currentBlock;

    // Set the layout for this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get log file
        getTaskResult = registerForActivityResult(new TaskResultContract(), result -> {
            if (result != null) {
                Log.i(TAG, result);
                writeLogLineToFile(result);
                nextTask();
            } else {
                Log.e(TAG, "Null result received from Task Activity");
            }
        });

        startBlockText = findViewById(R.id.editTextStartBlock);
        startTaskText = findViewById(R.id.editTextStartTask);

        // Parse participant number to string
        final EditText participantNumberText = findViewById(R.id.editTextParticipantId);
        participantNumberText.setOnEditorActionListener((v, actionId, event) -> {
            String participantTextString = participantNumberText.getText().toString();
            participantNumber = Integer.parseInt(participantTextString);

            return true;
        });

        final Button startButton = findViewById(R.id.buttonStartExperiment);
        startButton.setOnClickListener(v -> {
            // Parse every text field to string
            String participantTextString = participantNumberText.getText().toString();
            String startBlockString = startBlockText.getText().toString();
            String startTaskString = startTaskText.getText().toString();

            if (participantTextString.length() == 0) {
                Snackbar.make(findViewById(R.id.mainConstraintLayout), "Enter participant number!", Snackbar.LENGTH_SHORT).show();
            } else {
                int participantNumber = Integer.parseInt(participantTextString);
                int startBlock = Integer.parseInt(startBlockString.length() == 0 ? "1" : startBlockString);
                int startTask = Integer.parseInt(startTaskString.length() == 0 ? "1" : startTaskString);

                // Start experiment
                initExperiment(participantNumber, startBlock, startTask);
                nextTask();
            }
        });

        // GENERATING LOG FILE
        Button logButton = findViewById(R.id.buttonShareLog);
        logButton.setOnClickListener(v -> {
            File file = new File(getBaseContext().getFilesDir(), getLogFilename());
            Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.countnpass.fileprovider", file);

            if (file.exists()) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.setType("text/plain");
                startActivity(Intent.createChooser(share, "Share log file"));
            } else {
                Snackbar.make(findViewById(R.id.mainConstraintLayout), "Unable to find " + getLogFilename(), Snackbar.LENGTH_SHORT).show();
            }
        });

        logButton.setOnLongClickListener(v -> {
            final String filename = getLogFilename();
            final File file = new File(getBaseContext().getFilesDir(), filename);

            if (file.exists()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle("Confirm Delete");
                dialogBuilder.setMessage("Delete " + filename +" ?");
                dialogBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (file.delete()) {
                        Snackbar.make(findViewById(R.id.mainConstraintLayout), "Deleted " + filename, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainConstraintLayout), "Unable to delete " + filename, Snackbar.LENGTH_SHORT).show();
                    }

                    dialog.dismiss();
                });
                dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
                dialogBuilder.create().show();
            }

            return true;
        });
    }

    /**
     * Initialise experiment variables.
     */
    private void initExperiment(int participant, int startBlock, int startTask) {
        participantNumber = participant;
        totalTasks = Experiment.N_PINS * Experiment.TRIALS_PER_PIN;
        currentTask = startTask - 1; // Needs to start at 0 because it'll be incremented by nextTask
        currentBlock = startBlock == 0 ? 1 : startBlock; // Needs to start at 1
        trialIDs = new ArrayList<>(totalTasks);

        // Generate list of trial IDs then shuffle
        for (int i = 0; i < Experiment.N_PINS; i++) {
            for (int j = 0; j < Experiment.TRIALS_PER_PIN; j++) {
                trialIDs.add(i + 1);
            }
        }

        Collections.shuffle(trialIDs, new Random(participantNumber));
    }

    /**
     * Moves to the next experiment task. Contains experiment logic for progressing through tasks
     * and blocks. When relevant, will also end the experiment.
     */
    private void nextTask() {
        currentTask++;

        if (currentTask > totalTasks) {
            currentBlock++;

            if (currentBlock > Experiment.CONDITIONS) {
                // Finished all tasks in all blocks, end of experiment
                Snackbar.make(findViewById(R.id.mainConstraintLayout), R.string.experiment_complete, Snackbar.LENGTH_LONG).show();
            } else {
                // Finished all tasks for first block, move to next block
                currentTask = 0;
                nextTask();
            }
        } else {
            // Start next task automatically
            startBlockText.setText(String.valueOf(currentBlock));
            startTaskText.setText(String.valueOf(currentTask));

            String targetPIN = getTrial(trialIDs.get(currentTask - 1));
            getTaskResult.launch(String.format(Locale.ENGLISH, "{participant: %d, condition: \"%s\", block: %d, blocks: %d, task: %d, tasks: %d, target: \"%s\"}", participantNumber, getCondition(participantNumber, currentBlock), currentBlock, Experiment.CONDITIONS, currentTask, totalTasks, targetPIN));
        }
    }

    /**
     * Utility function for getting the condition name for a given participant and block number.
     */
    private static String getCondition(int participantNumber, int currentBlock) {
        if (participantNumber % 2 == 0) {
            if (currentBlock % 2 == 0) {
                return Experiment.CONDITION_ZERO;
            } else {
                return Experiment.CONDITION_RANDOM;
            }
        } else {
            if (currentBlock % 2 == 0) {
                return Experiment.CONDITION_RANDOM;
            } else {
                return Experiment.CONDITION_ZERO;
            }
        }
    }

    /**
     * Utility function for accessing a PIN for a given trial ID.
     */
    private static String getTrial(int id) {
        switch (id) {
            case 1:
                return Experiment.TRIAL_1;
            case 2:
                return Experiment.TRIAL_2;
            case 3:
                return Experiment.TRIAL_3;
            case 4:
                return Experiment.TRIAL_4;
            case 5:
                return Experiment.TRIAL_5;
            case 6:
                return Experiment.TRIAL_6;
            default:
                return null;
        }
    }

    /**
     * Utility function for retrieving a log filename for the current participant.
     */
    private String getLogFilename() {
        return "P" + participantNumber + ".csv";
    }

    /**
     * Utility function for writing to the log file for the current participant.
     */
    private void writeLogLineToFile(String line) {
        try {
            try (FileOutputStream fos = MainActivity.this.openFileOutput(getLogFilename(), Context.MODE_PRIVATE | Context.MODE_APPEND)) {
                fos.write(line.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}