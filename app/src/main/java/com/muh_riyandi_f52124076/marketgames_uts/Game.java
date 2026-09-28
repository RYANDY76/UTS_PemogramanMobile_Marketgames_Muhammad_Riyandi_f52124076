package com.muh_riyandi_f52124076.marketgames_uts;

import java.util.Locale;

public class Game {
    String title, genre, publisher, rating, price, discount, coverUrl, description, aliases;
    boolean isFavorite = false;

    Game(String title, String genre, String publisher, String rating, String price,
         String discount, String coverUrl, String description, String aliases) {
        this.title = title;
        this.genre = genre;
        this.publisher = publisher;
        this.rating = rating;
        this.price = price;
        this.discount = discount;
        this.coverUrl = coverUrl;
        this.description = description;
        this.aliases = aliases != null ? aliases : "";
    }

    public int getPriceValue() {
        try {
            String digits = price.replaceAll("[^0-9]", "");
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getRatingValue() {
        try {
            return Double.parseDouble(rating);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean matches(String query, String category, boolean favoriteOnly) {
        // Favorite filter check
        if (favoriteOnly && !isFavorite) {
            return false;
        }

        // Category check
        if (category != null && !category.equalsIgnoreCase("Semua") && !category.isEmpty()) {
            String cat = category.toLowerCase(Locale.ROOT);
            if (!genre.toLowerCase(Locale.ROOT).contains(cat)) {
                return false;
            }
        }

        // Search query check
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String q = query.toLowerCase(Locale.ROOT).trim();
        return title.toLowerCase(Locale.ROOT).contains(q)
            || genre.toLowerCase(Locale.ROOT).contains(q)
            || publisher.toLowerCase(Locale.ROOT).contains(q)
            || description.toLowerCase(Locale.ROOT).contains(q)
            || aliases.toLowerCase(Locale.ROOT).contains(q);
    }
}
