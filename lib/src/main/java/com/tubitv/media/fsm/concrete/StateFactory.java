package com.tubitv.media.fsm.concrete;

import android.support.annotation.Nullable;

import com.tubitv.media.fsm.State;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by allensun on 7/31/17.
 */
public class StateFactory {

    private static final Map<Class, State> stateInstance = new HashMap<>();

    @Nullable
    private synchronized State getCacheInstance(Class type) {
        return stateInstance.get(type);
    }

    private synchronized void setCacheInstance(Class type, State instance) {
        stateInstance.put(type, instance);
    }


    public State createState(Class classType) {

        State buildState = getCacheInstance(classType);

        if (buildState == null) {
            try {

                Constructor<?> ctor = classType.getConstructor();
                buildState = (State) ctor.newInstance();

                setCacheInstance(classType, buildState);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return buildState;
    }
}
