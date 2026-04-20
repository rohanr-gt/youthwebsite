package com.example.demo.uno;

import java.util.Objects;

public class UnoCard {
    public int id;
    public String color;
    public String value;

    public UnoCard() {}

    public UnoCard(int id, String color, String value) {
        this.id = id;
        this.color = color;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnoCard unoCard = (UnoCard) o;
        return id == unoCard.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
