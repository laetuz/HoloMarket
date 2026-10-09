package id.neotica.holomarket.model;

/**
 * Created by ryomartin on 06/12/25.
 */

public class ItemModel {
    private String id;
    private String name;
    private double price;
    private int stock;
    private String description;
    private String imageUrl;
//    private String createdAt;

    public ItemModel(
            String id, String name, double price, int stock, String description, String imageUrl /*String createdAt*/
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
//        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getStock() { return stock; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
//    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() { return name; }

}