package com.school.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class PromotionRow {
    private final Student student;
    private final BooleanProperty selected;

    public PromotionRow(Student student) {
        this.student = student;
        this.selected = new SimpleBooleanProperty(true); // Default to checked
    }

    public Student getStudent() { return student; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public BooleanProperty selectedProperty() { return selected; }
}