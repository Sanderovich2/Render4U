package dev.render4u.module;

import dev.render4u.util.Lang;

public enum Category {
    COMBAT("category.combat"),
    MOVEMENT("category.movement"),
    RENDER("category.render"),
    PLAYER("category.player"),
    MISC("category.misc"),
    THEMES("category.themes");

    private final String translationKey;

    Category(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getDisplayName() {
        return Lang.tr(translationKey);
    }
}
