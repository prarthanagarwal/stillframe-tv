package com.gathin.tvhome;
import java.util.*;
/** Small validation boundary for saved user choices; seven slots fit the existing remote grid. */
final class DockSelection {
    static List<String> parse(String value) { return parse(value, 7); }
    static List<String> parse(String value, int limit) {
        List<String> out = new ArrayList<>();
        if (value == null) return out;
        for (String p : value.split(",")) {
            p = p.trim();
            if (p.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+") && !out.contains(p) && out.size() < limit) out.add(p);
        }
        return out;
    }
}
