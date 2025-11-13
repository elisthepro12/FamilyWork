package com.example.familywork;

public class Item {
    private String id;
    private String name;
    private int quantity;

    public Item() {
        // נדרש ע"י Firebase
    }

    public Item(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
