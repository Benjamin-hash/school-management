package com.school.models;

public class FeeStructure {
    private final String level;
    private final int term;
    private final double amount;

    public FeeStructure(String level, int term, double amount) {
        this.level = level;
        this.term = term;
        this.amount = amount;
    }

    public String getLevel() { return level; }
    public int getTerm() { return term; }
    public double getAmount() { return amount; }
}