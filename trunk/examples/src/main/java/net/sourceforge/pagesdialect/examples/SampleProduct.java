package net.sourceforge.pagesdialect.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Product representation for examples.
 */
public class SampleProduct {
    
    private String name;
    private SampleCategory category;
    private Integer stock;
    private Integer price;

    private final static List<SampleProduct> products;
    
    static {
        // List of random sample products.
        SampleCategory[] categories = {new SampleCategory(101, "Apparels"),
            new SampleCategory(102, "Electronics"), new SampleCategory(103, "Furniture"),
            new SampleCategory(104, "Hardware"), new SampleCategory(105, "Media")};
        products = new ArrayList<SampleProduct>();
        for (int i = 1; i <= 500; i++) {
            products.add(new SampleProduct("Sample product #" + i, categories[random(0, 4)], random(0, 50), random(100, 200)));
        }
    }
    
    public SampleProduct() {
    }

    public SampleProduct(String name, SampleCategory category, Integer stock, Integer price) {
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.price = price;
    }

    public SampleCategory getCategory() {
        return category;
    }

    public void setCategory(SampleCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }
    
    public String getFormattedPrice() {
        return price + " €";
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    private static Integer random(int min, int max) {
        return (int) (min + Math.round(Math.random() * (max - min)));
    }
    
    /**
     * Load a list of random sample products.
     */
    public static List<SampleProduct> loadAllProducts() {
        return products;
    }
}
