package com.humanitarian.logistics.model;

import java.io.Serializable;
import java.util.Objects;

public class ReliefItem implements Serializable, Comparable<ReliefItem> {
    private static final long serialVersionUID = 1L;

    public enum Category {
        CASH("Cash Assistance"),
        MEDICAL("Medical Support"),
        SHELTER("Shelter"),
        FOOD("Food"),
        TRANSPORTATION("Transportation");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Category category;
    private final String description;
    private final int priority;

    public ReliefItem(Category category, String description, int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.priority = priority;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(ReliefItem other) {
        return Integer.compare(other.priority, this.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliefItem that = (ReliefItem) o;
        return priority == that.priority &&
               category == that.category &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, priority);
    }

    @Override
    public String toString() {
        return category.getDisplayName() + " (Priority: " + priority + ")";
    }
}
