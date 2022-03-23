package com.example.countnpass;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class HapticManager {
    public static final long PULSE_DURATION = 100;
    public static final long PULSE_BREAK_DURATION = 80;
    public static final long GROUP_BREAK_DURATION = 160;

    private final Vibrator haptics;

    public HapticManager(Context context) {
        haptics = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Inform the user if haptic feedback is not supported and exit
        if (haptics == null || !haptics.hasVibrator()) {
            Log.e(HapticManager.class.getSimpleName(), "Haptic feedback not available on this device");
            System.exit(-1);
        }
    }

    // Stop all haptics
    public void stop() {
        haptics.cancel();
    }

    // The duration of a Pulse
    private static long pulse() {
        return PULSE_DURATION;
    }

    // The duration of a Pulse Break
    private static long pulseBreak() {
        return PULSE_BREAK_DURATION;
    }

    // The duration of a Group Break
    private static long groupBreak() {
        return GROUP_BREAK_DURATION;
    }

    /**
     * Plays a simple confirmatory haptic pattern.
     */
    public void playConfirmPattern() {
        haptics.vibrate(VibrationEffect.createOneShot(PULSE_DURATION, 255));
    }

    /**
     * Plays a simple error haptic pattern.
     */
    public void playErrorPattern() {
        haptics.vibrate(VibrationEffect.createOneShot(PULSE_DURATION * 12, 255));
    }

    /**
     * Generates and plays a haptic feedback pattern for the given number n.
     *
     * @param n Digit to represent using vibration.
     * @param grouped If true, use grouped vibration, otherwise play n pulses.
     */
    public void playHapticPattern(int n, boolean grouped) {
        stop();

        long[] pattern;

        if (grouped) {
            // Generates a grouped haptic pattern for the given digit n.
            pattern = patternFromDigit(n);
        } else {
            // Generates a haptic pattern consisting of n equally spaced pulses.
            pattern = patternFromSize(n);
        }

        if (pattern.length > 0) {
            haptics.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    /**
     * Generates a haptic pattern consisting of n equally spaced pulses.
     */
    private static long[] patternFromSize(int n) {
        if (n == 0) {
            return new long[] {};
        }

        long[] pattern = new long[n * 2];

        for (int i = 0; i < pattern.length; i += 2) {
            pattern[i] = groupBreak();
            pattern[i + 1] = pulse();
        }

        return pattern;
    }

    /**
     * Generates a grouped haptic pattern for the given digit n.
     */
    private static long[] patternFromDigit(int n) {
        if (n < 0 || n > 9) {
            Log.e(HapticManager.class.getSimpleName(), "Unable to generate grouped pattern for digit " + n);
            System.exit(-1);
        }

        long[] pattern = emptyPulsePattern();
        //long[] pattern = new long[18];

        switch (n) {
            case 0:
                break;
            case 1:
                pattern[1] = pulse();
                break;
            case 2:
                pattern[1] = pulse();
                pattern[3] = pulse();
                break;
            case 3:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                break;
            case 4:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                break;
            case 5:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                pattern[9] = pulse();
                break;
            case 6:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                pattern[9] = pulse();
                pattern[11] = pulse();
                break;
            case 7:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                pattern[9] = pulse();
                pattern[11] = pulse();
                pattern[12] = groupBreak();
                pattern[13] = pulse();
                break;
            case 8:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                pattern[9] = pulse();
                pattern[11] = pulse();
                pattern[12] = groupBreak();
                pattern[13] = pulse();
                pattern[15] = pulse();
                break;
            case 9:
                pattern[1] = pulse();
                pattern[3] = pulse();
                pattern[5] = pulse();
                pattern[6] = groupBreak();
                pattern[7] = pulse();
                pattern[9] = pulse();
                pattern[11] = pulse();
                pattern[12] = groupBreak();
                pattern[13] = pulse();
                pattern[15] = pulse();
                pattern[17] = pulse();
                break;
        }

        return pattern;
    }

    /**
     * Utility function that generates an array consisting entirely of pulse breaks.
     */
    private static long[] emptyPulsePattern() {
        long[] pattern = new long[18];

        pattern[0] = 0;

        for (int i = 2; i < pattern.length; i+=2) {
            pattern[i] = pulseBreak();
        }

        return pattern;
    }
}
