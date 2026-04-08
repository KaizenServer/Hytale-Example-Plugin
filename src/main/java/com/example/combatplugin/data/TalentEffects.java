package com.example.combatplugin.data;

import com.example.combatplugin.domain.model.TalentDefinition;
import com.example.combatplugin.domain.model.TalentEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry populated by all *Talents.java data classes.
 * Holds both TalentDefinition (metadata) and TalentEffect (runtime behaviour).
 *
 * Usage:
 *   TalentEffects registry = new TalentEffects();
 *   registry.registerAll();
 *   // then pass registry.getTalentMap() and registry.getEffectMap() to services.
 */
public final class TalentEffects {

    private final Map<String, TalentDefinition> talentMap = new HashMap<>();
    private final Map<String, TalentEffect>     effectMap = new HashMap<>();

    public void registerAll() {
        SwordMasterTalents.register(talentMap, effectMap);
        ElementalistTalents.register(talentMap, effectMap);
        TechnocratTalents.register(talentMap, effectMap);
        NecromancerTalents.register(talentMap, effectMap);
    }

    public Map<String, TalentDefinition> getTalentMap() { return talentMap; }
    public Map<String, TalentEffect>     getEffectMap() { return effectMap; }
}
