package com.tubitv.media.fsm.concrete.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.fsm.State;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by allensun on 7/31/17.
 * To reuse state instance, we have a caching mechanism to only create one instance of each {@link State},
 * reuse that instance from cached map.
 */
public class StateFactory {

    private static final Map<Class, State> stateInstance = new HashMap<>();

    @Nullable
    private synchronized State getCacheInstance(@NonNull Class type) {
        return stateInstance.get(type);
    }

    private synchronized void setCacheInstance(@NonNull Class type, @NonNull State instance) {
        stateInstance.put(type, instance);
    }


    public State createState(@NonNull Class classType) {

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
