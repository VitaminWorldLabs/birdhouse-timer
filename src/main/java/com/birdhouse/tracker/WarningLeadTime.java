package com.birdhouse.tracker;

public enum WarningLeadTime
{
    OFF("Off", 0),
    FIVE("5 minutes", 5),
    TEN("10 minutes", 10),
    FIFTEEN("15 minutes", 15);

    private final String label;
    final int minutes;

    WarningLeadTime(String label, int minutes)
    {
        this.label = label;
        this.minutes = minutes;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
