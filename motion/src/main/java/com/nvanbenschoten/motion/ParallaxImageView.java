package com.nvanbenschoten.motion;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * Copyright 2014 Nathan VanBenschoten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ParallaxImageView extends ImageView {

    private float mIntensity;
    private int mXTranslation;
    private int mYTranslation;

    public ParallaxImageView(Context context) {
        super(context);
        init();
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    public void setIntensity(float intensity) {
        if (intensity <= 0) return;

        mIntensity = intensity;
        configureMatrix();
    }

    private void configureMatrix() {
        if (getDrawable() == null) return;
        int dWidth = getDrawable().getIntrinsicWidth();
        int dHeight = getDrawable().getIntrinsicHeight();
        int vWidth = getWidth();
        int vHeight = getHeight();

        float scale;
        float dx = 0, dy = 0;

        if (dWidth * vHeight > vWidth * dHeight) {
            scale = (float) vHeight / (float) dHeight;
            dx = (vWidth - dWidth * scale * mIntensity) * 0.5f;
        } else {
            scale = (float) vWidth / (float) dWidth;
            dy = (vHeight - dHeight * scale * mIntensity) * 0.5f;
        }

        float xTranslate = dx + mXTranslation;
        float yTranslate = dy + mYTranslation;

        Matrix matrix = new Matrix();
        matrix.set(getImageMatrix());
        matrix.setScale(mIntensity * scale, mIntensity * scale);
        matrix.postTranslate(dx + mXTranslation, dy + mYTranslation);
        setImageMatrix(matrix);
    }

}
