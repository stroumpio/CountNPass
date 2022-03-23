package com.example.countnpass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class TaskActivity extends ComponentActivity implements CountNPassGestureCallBack {
    public static final String EXTRA_SETUP_STRING = "TASK_SETUP";
    public static final String EXTRA_RESULT_STRING = "TASK_RESULT";

    private HapticManager hapticManager;
    private String pinEntry;
    private String pinTarget;
    private String condition;
    private int participant;
    private int currentDigit;
    private int currentKey;
    private int attempts;
    private int taskNumber;
    private long startTime;
    private boolean newTask;
    private Toast myToast;

    // Set the layout for this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Enable Haptic Manager
        hapticManager = new HapticManager(this);

        // Enable gesture detector
        CountNPassGestureDetector gestureListener = new CountNPassGestureDetector(this, this);

        // Attach task_views element to the view fullscreenView
        View fullscreenView = findViewById(R.id.task_views);
        // Adjust fullscreenView
        fullscreenView.setClickable(true);
        fullscreenView.setFocusable(true);
        fullscreenView.setOnTouchListener(gestureListener);

        // Get the data from the intent that started the activity
        String setup =  getIntent().getStringExtra(EXTRA_SETUP_STRING);
        JSONObject setupOptions;
        int blockNumber = -1;
        int blockTotal = -1;
        int taskTotal = -1;

        try {
            setupOptions = new JSONObject(setup);

            pinTarget = setupOptions.getString("target");
            condition = setupOptions.getString("condition");
            participant = setupOptions.getInt("participant");
            blockNumber = setupOptions.getInt("block");
            blockTotal = setupOptions.getInt("blocks");
            taskNumber = setupOptions.getInt("task");
            taskTotal = setupOptions.getInt("tasks");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Set the starting condition for the user
        pinEntry = "";
        attempts = 0;
        // If condition is 1 then number will be 0, and if condition is 2 then number will be random
        currentDigit = condition.equals("random") ? getRandomNumber(0, 9) : 0;

        // Start countdown for each task block
        startTime = 0;
        newTask = true;

        // Generate a random key
        currentKey = getRandomNumber(0, 9);

        // Display current details to the user
        Snackbar.make(findViewById(R.id.taskConstraintLayout), String.format(Locale.ENGLISH, "Block %d of %d Task %d of %d: Enter PIN %s", blockNumber, blockTotal, taskNumber, taskTotal, pinTarget), Snackbar.LENGTH_INDEFINITE).show();
    }

    /**
     * Increase digit.
     */
    @Override
    public void swipeUp() {
        currentDigit++;

        if (currentDigit >= 10) {
            currentDigit = 0;
        }

        hapticManager.playHapticPattern(currentDigit, true);

        // Training Mode
        if (participant == 0) {
            //Display current digit
            displayToast(String.format(Locale.ENGLISH, "Current digit: %d ", currentDigit));
        }

        // If it is a new task block and the first time the user asks for the key, start counting
        if (newTask) {
            startTime = System.currentTimeMillis();
            newTask = false;
        }

    }

    /**
     * Decrease digit.
     */
    @Override
    public void swipeDown() {
        currentDigit--;

        if (currentDigit < 0) {
            currentDigit = 9;
        }

        hapticManager.playHapticPattern(currentDigit, true);

        // Training Mode
        if (participant == 0) {
            //Display current digit
            displayToast(String.format(Locale.ENGLISH, "Current digit: %d ", currentDigit));
        }

        // If it is a new task block and the first time the user asks for the key, start counting
        if (newTask) {
            startTime = System.currentTimeMillis();
            newTask = false;
        }
    }

    /**
     * Confirm digit and move to next digit.
     */
    @Override
    public void doubleTap() {

        // Calculate true digit by subtracting the current Key
        int trueDigit = currentDigit - currentKey;
        // When true digit has negative value, add 10 to find the true distance in range (0-9)
        if (trueDigit < 0) {
            trueDigit += 10;
        }

        // Add current digit to the pinEntry
        pinEntry += trueDigit;

        // Training Mode
        if (participant == 0) {
            //Display PIN Entry
            displayToast(String.format(Locale.ENGLISH, "You entered: %s ", currentDigit));
        }


        // If pinEntry has the desirable length, count it as an attempt
        if (pinEntry.length() == pinTarget.length()) {
            attempts++;

            if (pinEntry.equals(pinTarget)) {
                hapticManager.playConfirmPattern();

                // Security Experiment
                displayToast("Correct!");


                long duration = System.currentTimeMillis() - startTime;
                String resultString = getLogLine(participant, taskNumber, condition, pinTarget, attempts, duration);

                Intent result = new Intent();
                result.putExtra(EXTRA_RESULT_STRING, resultString);

                setResult(Activity.RESULT_OK, result);
                finish();
            } else {
                hapticManager.playErrorPattern();
            }
        }
    }

    /**
     * Undoes the most recently entered digit.
     */
    @Override
    public void twoFingerTap() {
        if (pinEntry.length() > 1) {
            pinEntry = pinEntry.substring(0, pinEntry.length() - 1);

            // Training Mode
            if (participant == 0) {
                // Display relevant message
                displayToast("Removed last entry");
            }
        }
        else if (pinEntry.length() == 1) {
            pinEntry = "";

            // Training Mode
            if (participant == 0) {
                // Display relevant message
                displayToast("Removed last entry");
            }
        }
        else {
            // Training Mode
            if (participant == 0) {
                // Display relevant message
                displayToast("No entry to remove");
            }
        }

        hapticManager.playConfirmPattern();
    }

    /**
     * Plays a simple pattern to indicate how many digits have been entered.
     */
    @Override
    public void longPress() {

        // Training Mode
        if (participant == 0) {
            // Display the key length
            displayToast(String.format(Locale.ENGLISH, "Number of digits entered: %d ", pinEntry.length()));
        }

        hapticManager.playHapticPattern(pinEntry.length(), true);
    }

    /**
     * Repeats the key.
     */
    @Override
    public void swipeLeft() {
        // Training Mode
        if (participant == 0) {
            // Display the key
            displayToast(String.format(Locale.ENGLISH, "Key: %d", currentKey));
        }

        // If it is a new task block and the first time the user asks for the key, start counting
        if (newTask) {
            startTime = System.currentTimeMillis();
            newTask = false;
        }

        // Play the key
        hapticManager.playHapticPattern(currentKey, true);
    }

    @Override
    public void swipeRight() {
        // Generate a random key
        currentKey = getRandomNumber(0, 9);

        // Training Mode
        if (participant == 0) {
            // Display the key
            displayToast(String.format(Locale.ENGLISH, "New Key: %d", currentKey));
        }

        // If it is a new task block and the first time the user asks for the key, start counting
        if (newTask) {
            startTime = System.currentTimeMillis();
            newTask = false;
        }

        // Play the key
        hapticManager.playHapticPattern(currentKey, true);
    }

    /**
     * Utility function for getting a random number.
     */
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * Utility function for generating a log file line with the task results.
     */
    public static String getLogLine(int participant, int task, String condition, String target, int attempts, long duration) {
        return String.format(Locale.ENGLISH, "%d,%d,%s,%s,\"%s\",%d,%d\n", participant, task, condition, target.length() == 4 ? "four" : "six", target, attempts, duration);
    }

    /**
     * Utility function for displaying a toast message as visual feedback in Training mode.
     */
    void displayToast(String text)
    {
        // Cancel previously generated toast
        if(myToast != null)
        {
            myToast.cancel();
        }
        myToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        myToast.show();
    }
}
