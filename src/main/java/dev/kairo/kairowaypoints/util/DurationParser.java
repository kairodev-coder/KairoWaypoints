package dev.kairo.kairowaypoints.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern PATTERN = Pattern.compile("^(\\d+)(s|m|h|d|w)$", Pattern.CASE_INSENSITIVE);

    private DurationParser() { }

    public static Duration parse(String input) {
        Matcher matcher = PATTERN.matcher(input.strip());
        if (!matcher.matches()) throw new IllegalArgumentException("duration");
        long amount = Long.parseLong(matcher.group(1));
        if (amount <= 0 || amount > 525600) throw new IllegalArgumentException("duration");
        return switch (matcher.group(2).toLowerCase()) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            case "w" -> Duration.ofDays(amount * 7);
            default -> throw new IllegalArgumentException("duration");
        };
    }
}
