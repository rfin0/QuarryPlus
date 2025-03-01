package com.yogpc.qp.machine;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class PickIterator<T> implements Iterator<T> {
    protected T lastReturned = null;

    protected abstract T update();

    @Override
    public final T next() {
        return lastReturned = update();
    }

    @Nullable
    public T getLastReturned() {
        return lastReturned;
    }

    public void setLastReturned(T lastReturned) {
        this.lastReturned = lastReturned;
    }

    public static final class Single<T> extends PickIterator<T> {
        private final T value;

        public Single(T value) {
            this.value = value;
        }

        @Override
        protected T update() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in single iterator");
            }
            return value;
        }

        @Override
        public boolean hasNext() {
            return getLastReturned() == null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> PickIterator<T> empty() {
        return (PickIterator<T>) Empty.INSTANCE;
    }

    static final class Empty<T> extends PickIterator<T> {
        private static final Empty<Object> INSTANCE = new Empty<>();

        @Override
        protected T update() {
            throw new NoSuchElementException("No element in empty iterator");
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public void setLastReturned(T lastReturned) {
        }

        @Override
        public T getLastReturned() {
            return null;
        }
    }
}
