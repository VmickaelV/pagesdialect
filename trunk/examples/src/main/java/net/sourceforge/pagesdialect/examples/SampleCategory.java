package net.sourceforge.pagesdialect.examples;

/**
 * Category representation for examples.
 */
public class SampleCategory {
    
    private Integer id;
    private String name;

    public SampleCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public SampleCategory() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
