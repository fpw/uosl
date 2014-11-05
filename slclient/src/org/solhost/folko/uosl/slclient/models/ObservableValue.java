package org.solhost.folko.uosl.slclient.models;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class ObservableValue<ValueType> {
    private ValueType value;
    private final Set<BiConsumer<ValueType, ValueType>> observers;

    public ObservableValue(ValueType initialValue) {
        this.observers = new HashSet<>();
        this.value = initialValue;
    }

    public void addObserver(BiConsumer<ValueType, ValueType> observer) {
        observers.add(observer);
    }

    public void removeObserver(BiConsumer<ValueType, ValueType> observer) {
        observers.remove(observer);
    }

    public void setValue(ValueType newValue) {
        ValueType oldValue = this.value;
        if(oldValue == newValue) {
            return;
        }
        this.value = newValue;
        for(BiConsumer<ValueType, ValueType> observer : observers) {
            observer.accept(oldValue, value);
        }
    }

    public ValueType getValue() {
        return value;
    }
}
