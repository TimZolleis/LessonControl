package de.waldorfaugsburg.lessoncontrol.common.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.function.Consumer;

public final class EventDistributor {

    private final Multimap<Class<? extends Listener>, Listener> listenerMultimap = HashMultimap.create();

    public <T extends Listener> void call(final Class<T> clazz, final Consumer<T> consumer) {
        listenerMultimap.get(clazz).forEach(l -> consumer.accept((T) l));
    }

    public <T extends Listener> void addListener(final Class<T> clazz, final T listener) {
        listenerMultimap.put(clazz, listener);
    }
}
