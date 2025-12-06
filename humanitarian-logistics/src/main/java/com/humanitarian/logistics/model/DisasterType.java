package com.humanitarian.logistics.model;

import java.io.Serializable;
import java.util.*;

public class DisasterType implements Serializable {
    private final String name;
    private final Set<String> aliases;

    public DisasterType(String name) {
        this.name = normalize(name);
        this.aliases = new HashSet<>();
        this.aliases.add(this.name);
    }

    public static String normalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.trim().toLowerCase().replaceAll("^#", "");
    }

    public void addAlias(String alias) {
        if (alias != null && !alias.isEmpty()) {
            this.aliases.add(normalize(alias));
        }
    }

    public boolean matches(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return this.aliases.contains(normalize(keyword));
    }

    public Set<String> getAliases() {
        return new HashSet<>(this.aliases);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisasterType that = (DisasterType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
