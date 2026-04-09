package com.slayersimplified.loot;

import java.util.Map;

/**
 * Represents a section of a drop table (e.g. "Weapons and armour", "Runes").
 * Ported from loot-lookup-plugin.
 */
public class DropTableSection
{
    private String header;
    private Map<String, WikiItem[]> table;

    public DropTableSection()
    {
    }

    public DropTableSection(String header, Map<String, WikiItem[]> table)
    {
        this.header = header;
        this.table = table;
    }

    public void setHeader(String newHeader) { this.header = newHeader; }
    public void setTable(Map<String, WikiItem[]> newTable) { this.table = newTable; }
    public String getHeader() { return header; }
    public Map<String, WikiItem[]> getTable() { return table; }
}
