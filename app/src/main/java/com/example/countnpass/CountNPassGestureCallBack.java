package com.example.countnpass;

// This class the interaction methods for swipe and tap

public interface CountNPassGestureCallBack {
    /**
     * Swipe up gesture callback, to increase digit.
     */
    void swipeUp();

    /**
     * Swipe down gesture callback, to decrease digit.
     */
    void swipeDown();

    /**
     * Double tap gesture callback, to submit current digit.
     */
    void doubleTap();

    /**
     * Long press gesture callback, to indicate how many digits were entered.
     */
    void longPress();

    /**
     * Two finger tap gesture callback, to undo last PIN item entry.
     */
    void twoFingerTap();

    /**
     * Swipe left, to repeat the key.
     */
    void swipeLeft();

    /**
     * Swipe right, to repeat the key.
     */
    void swipeRight();
}