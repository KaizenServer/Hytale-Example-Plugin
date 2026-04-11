package com.example.combatplugin.config;

/**
 * Plugin-wide configuration loaded from combat_config.json at startup.
 *
 * All fields have sensible defaults so the plugin works out-of-the-box
 * even if the JSON file is absent or incomplete.
 */
public final class CombatConfig {

    private final int maxLevel;
    private final double xpCurveBase;
    private final double xpCurveExponent;
    private final int talentPointsPerLevel;
    private final boolean debugLogging;
    private final boolean xpDebugMessages;

    /** Defaults used when no config file is found. */
    public static final CombatConfig DEFAULT = new CombatConfig(30, 100.0, 1.5, 1, false, true);

    public CombatConfig(int maxLevel, double xpCurveBase, double xpCurveExponent,
                        int talentPointsPerLevel, boolean debugLogging, boolean xpDebugMessages) {
        this.maxLevel = maxLevel;
        this.xpCurveBase = xpCurveBase;
        this.xpCurveExponent = xpCurveExponent;
        this.talentPointsPerLevel = talentPointsPerLevel;
        this.debugLogging = debugLogging;
        this.xpDebugMessages = xpDebugMessages;
    }

    public int maxLevel()               { return maxLevel; }
    public double xpCurveBase()         { return xpCurveBase; }
    public double xpCurveExponent()     { return xpCurveExponent; }
    public int talentPointsPerLevel()   { return talentPointsPerLevel; }
    public boolean debugLogging()       { return debugLogging; }
    /** If true, players see "+50 XP" and "Level Up!" chat messages on XP gain. */
    public boolean xpDebugMessages()   { return xpDebugMessages; }

    @Override
    public String toString() {
        return "CombatConfig{maxLevel=" + maxLevel + ", xpCurveBase=" + xpCurveBase
                + ", exponent=" + xpCurveExponent + ", talentPtsPerLevel=" + talentPointsPerLevel + "}";
    }
}
