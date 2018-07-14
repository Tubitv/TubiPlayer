package com.tubitv.media.fsm.concrete.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.AdPlayingState;
import com.tubitv.media.fsm.concrete.FetchCuePointState;
import com.tubitv.media.fsm.concrete.FinishState;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MakingPrerollAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.ReceiveAdState;
import com.tubitv.media.fsm.concrete.VastAdInteractionSandBoxState;
import com.tubitv.media.fsm.concrete.VpaidState;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by allensun on 7/31/17.
 * A factory class that is in charge of initializing {@link State} for {@link com.tubitv.media.fsm.state_machine.FsmPlayer}.
 * To reuse state instance, we have a caching mechanism to only create one instance of each {@link State},
 * reuse that instance from cached map.
 * The default instance of {@link com.tubitv.media.fsm.BaseState} should be created using the below class.
 * * <p>
 * {@link com.tubitv.media.fsm.concrete.FetchCuePointState},
 * {@link com.tubitv.media.fsm.concrete.MakingPrerollAdCallState},
 * {@link com.tubitv.media.fsm.concrete.MakingAdCallState},
 * {@link com.tubitv.media.fsm.concrete.MoviePlayingState}
 * {@link com.tubitv.media.fsm.concrete.FinishState}
 * {@link com.tubitv.media.fsm.concrete.ReceiveAdState}
 * {@link com.tubitv.media.fsm.concrete.AdPlayingState}
 * {@link com.tubitv.media.fsm.concrete.VpaidState}
 * {@link com.tubitv.media.fsm.concrete.VastAdInteractionSandBoxState}
 */
public final class StateFactory {

    /**
     * map store singleton instance of every types of {@link State},
     */
    private final Map<Class, State> stateInstance = new HashMap<>();

    /**
     * the key is the default state, and value is the custom state.
     */
    private final Map<Class, Class> customStateType = new HashMap<>();

    @Nullable
    private synchronized State getCacheInstance(@NonNull Class type) {
        return stateInstance.get(type);
    }

    private synchronized void setCacheInstance(@NonNull Class type, @NonNull State instance) {
        stateInstance.put(type, instance);
    }

    /**
     * convert the default {@link State} into custom State
     *
     * @param cla default state type
     * @return the custom state type
     */
    @Nullable
    private Class convertToCustomClass(@NonNull Class cla) {
        return customStateType.get(cla);
    }

    /**
     * Method should only be called right after initialization, before {@link #createState} ever been called for max state predictability.
     *
     * @param subClass must be the subclass of {@link com.tubitv.media.fsm.BaseState} to swap original to subclass
     *                 Must be called before any createState method being called.
     */
    public void overrideStateCreation(@NonNull Class subClass) {

        if (MakingAdCallState.class.isAssignableFrom(subClass)) {
            customStateType.put(MakingAdCallState.class, subClass);

        } else if (MoviePlayingState.class.isAssignableFrom(subClass)) {
            customStateType.put(MoviePlayingState.class, subClass);

        } else if (FinishState.class.isAssignableFrom(subClass)) {
            customStateType.put(FinishState.class, subClass);

        } else if (ReceiveAdState.class.isAssignableFrom(subClass)) {
            customStateType.put(ReceiveAdState.class, subClass);

        } else if (AdPlayingState.class.isAssignableFrom(subClass)) {
            customStateType.put(AdPlayingState.class, subClass);

        } else if (VpaidState.class.isAssignableFrom(subClass)) {
            customStateType.put(VpaidState.class, subClass);

        } else if (VastAdInteractionSandBoxState.class.isAssignableFrom(subClass)) {
            customStateType.put(VastAdInteractionSandBoxState.class, subClass);

        } else if (FetchCuePointState.class.isAssignableFrom(subClass)) {
            customStateType.put(FetchCuePointState.class, subClass);

        } else if (MakingPrerollAdCallState.class.isAssignableFrom(subClass)) {
            customStateType.put(MakingPrerollAdCallState.class, subClass);

        } else {
            throw new IllegalStateException(
                    String.valueOf(subClass.getName() + "is not a base class of default State class "));
        }
    }

    @NonNull
    public State createState(@NonNull Class classType) {

        if (!State.class.isAssignableFrom(classType)) {
            throw new IllegalStateException(
                    String.valueOf(classType.getName() + "is not a base class of default State class "));
        }

        // null check if there is any custom state class. if there is,
        Class finalClassType = convertToCustomClass(classType);

        if (finalClassType == null) {
            finalClassType = classType;
        }

        State buildState = getCacheInstance(finalClassType);

        if (buildState == null) {
            try {

                Constructor<?> ctor = finalClassType.getConstructor();
                buildState = (State) ctor.newInstance();

                setCacheInstance(finalClassType, buildState);
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
