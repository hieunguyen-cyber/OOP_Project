package com.humanitarian.logistics.model;

import java.util.*;

/**
 * Manages disaster types (e.g., yagi, matmo, etc.)
 * Normalizes hashtags and keywords to disaster types
 * - #yagi → yagi
 * - yagi → yagi
 * - #matmo → matmo
 * - matmo → matmo
 */
public class DisasterType {
    private final String name;
    private final Set<String> aliases; // Keywords and hashtags that map to this disaster type

    public DisasterType(String name) {
        this.name = normalize(name);
        this.aliases = new HashSet<>();
        this.aliases.add(this.name);
    }

    /**
     * Normalize: Remove # prefix and convert to lowercase
     */
    public static String normalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.trim().toLowerCase().replaceAll("^#", "");
    }

    /**
     * Add an alias (keyword or hashtag) that maps to this disaster type
     */
    public void addAlias(String alias) {
        if (alias != null && !alias.isEmpty()) {
            this.aliases.add(normalize(alias));
        }
    }

    /**
     * Check if a keyword/hashtag matches this disaster type
     */
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return this.aliases.contains(normalize(keyword));
    }

    /**
     * Get all aliases for this disaster type
     */
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
