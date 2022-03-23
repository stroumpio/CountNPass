package com.example.countnpass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static android.view.GestureDetector.OnDoubleTapListener;
import static android.view.GestureDetector.OnGestureListener;

public class CountNPassGestureDetector implements View.OnTouchListener, OnGestureListener, OnDoubleTapListener {

    //Set the standards
    private static final int MINOR_THRESHOLD = 250;
    private static final int MAJOR_THRESHOLD = 400;
    private static final int MAXIMUM_DURATION = 350;
    private static final int MINIMUM_TAP_DURATION = 40;

    // Create a HapticLockGestureCallback object
    private final CountNPassGestureCallBack gestureCallback;
    // Create a GestureDetector(imported) object
    private final GestureDetector gestureDetector;
    // Create a boolean for the twoFingersDown interaction
    private boolean twoFingersDown;
    // Create a boolean for the twoFingerTapStart interaction
    private long twoFingerTapStart;

    // Constructor
    public CountNPassGestureDetector(Context context, CountNPassGestureCallBack callback) {
        // Create a HapticLockGestureCallback object
        gestureCallback = callback;
        // Create a GestureDetector(imported) object
        gestureDetector = new GestureDetector(context, this);
        // Set twoFingersDown condition false
        twoFingersDown = false;
    }

    // Lint should ignore the specified warnings for the annotated element
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Attach gestureDetector to motionEvent
        gestureDetector.onTouchEvent(motionEvent);

        // Check if motionEvent detects 2 fingers touching the screen
        // If it is true
        if (motionEvent.getPointerCount() == 2) {
            // If twoFingersDown is disabled, enable it and start to calculate the duration
            if (!twoFingersDown) {
                twoFingersDown = true;
                // Start counting the duration of twoFingersDown
                twoFingerTapStart = System.currentTimeMillis();
            }
            // If it is false
        } else {
            // And if twoFingersDown was already enabled
            if (twoFingersDown) {
                // Find the total duration of the motion
                long duration = System.currentTimeMillis() - twoFingerTapStart;

                // If duration was adequate
                if (duration >= MINIMUM_TAP_DURATION) {
                    // Execute twoFingerTap method
                    gestureCallback.twoFingerTap();
                }
            }

            // Set again twoFingersDown to false
            twoFingersDown = false;
        }

        return false;
    }

    // When motion equals to longPress, execute longPress method
    @Override
    public void onLongPress(MotionEvent e) {
        gestureCallback.longPress();
    }

    // When motion equals to doubleTap, execute doubleTap method
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        gestureCallback.doubleTap();
        return false;
    }

    // onFling: When the user swipes on the screen and lift his finger from it.
    // e1: The first down motion event that started the fling.
    // e2: The move motion event that triggered the current onFling.
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Calculate the duration of fling gesture
        long duration = e2.getEventTime() - e1.getEventTime();
        // Calculate the direction of the fling gesture
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        // If duration is acceptable (less than the maximum)
        if (duration <= MAXIMUM_DURATION) {
            // If horizontal gesture is between the limits of the acceptable Minor Threshold
            if (deltaX >= -MINOR_THRESHOLD && deltaX <= MINOR_THRESHOLD) {
                // If vertical gesture is less than the negative Major Threshold the execute swipeUp
                if (deltaY <= -MAJOR_THRESHOLD) {
                    gestureCallback.swipeUp();
                    // If vertical gesture is more than the Major Threshold the execute swipeDown
                } else if (deltaY >= MAJOR_THRESHOLD) {
                    gestureCallback.swipeDown();
                }
            }
            else if (deltaY >= -MINOR_THRESHOLD && deltaY <= MINOR_THRESHOLD) {
                // If vertical gesture is less than the negative Major Threshold the execute swipeLeft
                if (deltaX <= -MAJOR_THRESHOLD) {
                    gestureCallback.swipeLeft();
                    // If vertical gesture is more than the Major Threshold the execute swipeRight
                } else if (deltaX >= MAJOR_THRESHOLD) {
                    gestureCallback.swipeRight();
                }
            }
        }

        return false;
    }

    // CANCEL THESE METHODS
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
