package com.dew.system.settingsvalue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SelectionValue extends Value<String> {
    private final List<String> options;
    private boolean expanded = false;

    public SelectionValue(String name, String defaultValue, String... options) {
        super(name, defaultValue);
        this.options = Arrays.asList(options);
        if (!this.options.contains(defaultValue)) {
            throw new IllegalArgumentException("Default value must be one of the options");
        }
    }

    public SelectionValue(String name, String defaultValue, Supplier<Boolean> visible, String... options) {
        super(name, defaultValue, visible);
        this.options = Arrays.asList(options);
        if (!this.options.contains(defaultValue)) {
            throw new IllegalArgumentException("Default value must be one of the options");
        }
    }

    public List<String> getOptions() {
        return options;
    }

    public void next() {
        int index = options.indexOf(get());
        if (index == -1 || index + 1 >= options.size()) {
            set(options.get(0));
        } else {
            set(options.get(index + 1));
        }
    }

    public void previous() {
        int index = options.indexOf(get());
        if (index == -1 || index == 0) {
            set(options.get(options.size() - 1));
        } else {
            set(options.get(index - 1));
        }
    }

    public void setSelected(String selected) {
        if (options.contains(selected)) {
            this.value = selected;
        }
    }

    public void toggleExpanded() {
        expanded = !expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
