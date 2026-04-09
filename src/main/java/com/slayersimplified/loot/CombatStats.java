package com.slayersimplified.loot;

import lombok.Value;

/**
 * Combat statistics scraped from the OSRS Wiki infobox for a monster.
 */
@Value
public class CombatStats
{
    String combatLevel;
    String hitpoints;
    String maxHit;
    String attackStyle;
    String attribute;
    String elementalWeakness;
    String elementalWeaknessPercent;
    String immunePoison;
    String immuneVenom;
    String immuneCannon;
    String immuneThrall;
    String immuneBurn;
}
