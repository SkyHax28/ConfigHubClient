package com.dew.system.settingsvalue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MultiSelectionValue extends Value<List<String>> {
    private final List<String> options;

    public MultiSelectionValue(String name, List<String> defaultSelected, String... options) {
        super(name, new ArrayList<>(defaultSelected));
        this.options = Arrays.asList(options);
        validateDefaultValues(defaultSelected);
    }

    public MultiSelectionValue(String name, List<String> defaultSelected, Supplier<Boolean> visible, String... options) {
        super(name, new ArrayList<>(defaultSelected), visible);
        this.options = Arrays.asList(options);
        validateDefaultValues(defaultSelected);
    }

    private void validateDefaultValues(List<String> defaultSelected) {
        for (String val : defaultSelected) {
            if (!options.contains(val)) {
                throw new IllegalArgumentException("Default value '" + val + "' is not in the options list");
            }
        }
    }

    public List<String> getOptions() {
        return options;
    }

    public void select(String item) {
        if (options.contains(item) && !value.contains(item)) {
            value.add(item);
        }
    }

    public void deselect(String item) {
        value.remove(item);
    }

    public boolean isSelected(String item) {
        return value.contains(item);
    }

    public void toggle(String item) {
        if (isSelected(item)) {
            deselect(item);
        } else {
            select(item);
        }
    }

    private boolean expanded = false;

    public void toggleExpanded() { expanded = !expanded; }
    public boolean isExpanded() { return expanded; }
}