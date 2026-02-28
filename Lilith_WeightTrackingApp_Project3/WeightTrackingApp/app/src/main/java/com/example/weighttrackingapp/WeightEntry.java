package com.example.weighttrackingapp;

/** Simple model used by RecyclerView. */
public class WeightEntry {
    public final long id;
    public final String date;
    public final double weight;

    public WeightEntry(long id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }
}
