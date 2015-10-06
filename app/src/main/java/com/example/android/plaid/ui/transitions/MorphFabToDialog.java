/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.plaid.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.plaid.R;
import com.example.android.plaid.ui.drawable.MorphDrawable;
import com.example.android.plaid.util.AnimUtils;

/**
 * A transition that morphs a circle into a rectangle, changing it's background color.
 */
public class MorphFabToDialog extends ChangeBounds {

    private static final String PROPERTY_COLOR = "plaid:circleMorph:color";
    private static final String PROPERTY_CORNER_RADIUS = "plaid:circleMorph:cornerRadius";
    private static final String[] TRANSITION_PROPERTIES = {
            PROPERTY_COLOR,
            PROPERTY_CORNER_RADIUS
    };
    private @ColorInt int startColor = Color.TRANSPARENT;

    public MorphFabToDialog(@ColorInt int startColor) {
        super();
        setStartColor(startColor);
    }

    public MorphFabToDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStartColor(@ColorInt int startColor) {
        this.startColor = startColor;
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPERTY_COLOR, startColor);
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, view.getHeight() / 2);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPERTY_COLOR,
                ContextCompat.getColor(view.getContext(), R.color.background_light));
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, view.getResources()
                .getDimensionPixelSize(R.dimen.dialog_corners));
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues) {
        Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
        if (startValues == null || endValues == null || changeBounds == null) {
            return null;
        }

        Integer startColor = (Integer) startValues.values.get(PROPERTY_COLOR);
        Integer startCornerRadius = (Integer) startValues.values.get(PROPERTY_CORNER_RADIUS);
        Integer endColor = (Integer) endValues.values.get(PROPERTY_COLOR);
        Integer endCornerRadius = (Integer) endValues.values.get(PROPERTY_CORNER_RADIUS);

        if (startColor == null || startCornerRadius == null || endColor == null ||
                endCornerRadius == null) {
            return null;
        }

        MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
        endValues.view.setBackground(background);

        Animator color = ObjectAnimator.ofArgb(background, background.COLOR, endColor);
        Animator corners = ObjectAnimator.ofFloat(background, background.CORNER_RADIUS,
                endCornerRadius);

        // ease in the dialog's child views
        if (endValues.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) endValues.view;
            int duration = 150;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setTranslationY(v.getHeight() / 2);
                v.setAlpha(0f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(duration)
                        .setStartDelay(150)
                        .setInterpolator(AnimUtils.getMaterialInterpolator(vg.getContext()));
                duration += 50;
            }
        }

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(changeBounds, corners, color);
        transition.setDuration(300);
        transition.setInterpolator(AnimUtils.getMaterialInterpolator(sceneRoot.getContext()));
        return transition;
    }

}