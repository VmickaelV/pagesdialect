package org.thymeleaf.pagesdialect.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Product representation for examples.
 */
public class SampleProduct {
    
    private String name;
    private String category;
    private Integer stock;
    private Integer price;

    public SampleProduct() {
    }

    public SampleProduct(String name, String category, Integer stock, Integer price) {
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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
        return (int) (min + Math.round(Math.random() * (max - min)) + 1);
    }
    
    public static List<SampleProduct> loadAllProducts() {
        List<SampleProduct> products = new ArrayList<SampleProduct>();
        for (int i = 1; i <= 500; i++) {
            products.add(new SampleProduct("Sample product #" + i, "Apparels", random(0, 50), random(100, 200)));
        }
        return products;
    }
}
