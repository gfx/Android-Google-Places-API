package com.github.gfx.googleplaces;

import java.util.LinkedHashMap;

class LruMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxEntries;

    public LruMap(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        return size() > maxEntries;
    }
}
