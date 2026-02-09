package com.example.familywork;

public class Item {

    private String id;
    private String name;
    private int quantity;
    private String imageBase64;
    private long deletedAt; // <-- חובה להיסטוריה

    // חובה ל-Firebase
    public Item() {}

    public Item(String id, String name, int quantity, String imageBase64) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.imageBase64 = imageBase64;
        this.deletedAt = 0;
    }

    public Item(String id, String name, int quantity) {
        this(id, name, quantity, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
