package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 18-2-27.
 */

public class AnimationState {
    boolean one_state;
    boolean two_state;

    public AnimationState() {
    }

    public AnimationState(boolean one_state, boolean two_state) {
        this.one_state = one_state;
        this.two_state = two_state;
    }

    public boolean isOne_state() {
        return one_state;
    }

    public void setOne_state(boolean one_state) {
        this.one_state = one_state;
    }

    public boolean isTwo_state() {
        return two_state;
    }

    public void setTwo_state(boolean two_state) {
        this.two_state = two_state;
    }
}