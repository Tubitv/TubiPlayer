package com.tubitv.media.interfaces;

// TODO alternative for Rx consumer, remove when we decide to integrate Rx in tubi_player

public interface PlayerConsumer<T> {
    /**
     * Consume the given value.
     *
     * @param t the value
     */
    void accept(T t);
}
