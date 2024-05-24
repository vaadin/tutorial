package com.example.application.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

public class LimitedSortedAppendOnlyList<T> {

    private final int limit;
    private final TreeSet<T> items;

    public LimitedSortedAppendOnlyList(int limit, Comparator<T> comparator) {
        this.limit = limit;
        this.items = new TreeSet<>(comparator);
    }

    public void add(T item) {
        items.add(item);
        if (items.size() > limit) {
            items.pollFirst();
        }
    }

    public void addAll(Collection<T> items) {
        items.forEach(this::add);
    }

    public Stream<T> stream() {
        return items.stream();
    }

    public Optional<T> getLast() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.getLast());
    }
}
