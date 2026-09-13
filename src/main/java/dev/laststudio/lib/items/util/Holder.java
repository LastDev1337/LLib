package dev.laststudio.lib.items.util;

public class Holder<T> {
    private final T wrapped;

    public Holder(T wrapped) {
        this.wrapped = wrapped;
    }

    public T get() {
        return wrapped;
    }

    public boolean has(){
        return wrapped != null;
    }
}
