package net.sourceforge.pagesdialect.examples;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Product representation for examples.
 */
public class SampleProduct {
    
    private String name;
    private SampleCategory category;
    private Integer stock;
    private Integer price;
    private Date offerExpiration;

    private final static List<SampleProduct> products;
    
    static {
        // List of random sample products.
        SampleCategory[] categories = {new SampleCategory(101, "Apparels"),
            new SampleCategory(102, "Electronics"), new SampleCategory(103, "Furniture"),
            new SampleCategory(104, "Hardware"), new SampleCategory(105, "Media")};
        Calendar cal = Calendar.getInstance();
        products = new ArrayList<SampleProduct>();
        for (int i = 1; i <= 500; i++) {
            String name = "Sample product #" + i;
            cal.add(Calendar.DAY_OF_YEAR, random(0, 50));
            Date offerExpiration = cal.getTime();
            products.add(new SampleProduct(name, categories[random(0, 4)], random(0, 50), random(100, 200), offerExpiration));
        }
    }
    
    public SampleProduct() {
    }

    public SampleProduct(String name, SampleCategory category, Integer stock, Integer price, Date offerExpiration) {
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.price = price;
        this.offerExpiration = offerExpiration;
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

    public Date getOfferExpiration() {
        return offerExpiration;
    }

    public void setOfferExpiration(Date offerExpiration) {
        this.offerExpiration = offerExpiration;
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
