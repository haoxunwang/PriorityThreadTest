package com.nelson.prioritythreadtest;

import android.renderscript.RenderScript.Priority;

/**
 * Created by Nelson on 2017/12/4.
 */

public interface PriorityProvider<T> extends Comparable<T> {

    Priority getPriority();
}

