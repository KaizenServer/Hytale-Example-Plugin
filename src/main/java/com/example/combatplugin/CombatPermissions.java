package com.example.combatplugin;

/**
 * Permission node constants for the CombatPlugin.
 *
 * INTEGRATION POINT: Wire these into PermissionsModule checks once the API is confirmed.
 * In v1, all commands are open to any player (stubs return true).
 */
public final class CombatPermissions {

    private CombatPermissions() {}

    /** Required to use /xp add */
    public static final String ADMIN_XP = "combatplugin.admin.xp";

    /** Required to use /level set */
    public static final String ADMIN_LEVEL = "combatplugin.admin.level";

    /** Required to use /level get on other players */
    public static final String ADMIN_LEVEL_OTHERS = "combatplugin.admin.level.others";
}
