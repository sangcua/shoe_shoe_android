package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.myapplication.models.Size;
import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable{
    private String _id;
    private String name;
    private String category;
    private List<Size> sizes;
    private int price;
    private String description;
    private String image;
    private String slug;

    public Product(String _id, String name, String category, int price, String description, String image, String slug, List<Size> sizes) {
        this._id = _id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.image = image;
        this.slug = slug;
        this.sizes = sizes != null ? sizes : new ArrayList<>();
    }

    // Constructor cho việc tạo sản phẩm mới
    public Product(String name, String category, int price, String description, String image, List<Size> sizes) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.image = image;
        this.sizes = sizes != null ? sizes : new ArrayList<>();
    }

     protected Product(Parcel in) {
         _id = in.readString();
         name = in.readString();
         category = in.readString();
         price = in.readInt();
         description = in.readString();
         image = in.readString();
         slug = in.readString();
         sizes = new ArrayList<>();
         in.readTypedList(sizes, Size.CREATOR);
     }

     public static final Creator<Product> CREATOR = new Creator<Product>() {
         @Override
         public Product createFromParcel(Parcel in) {
             return new Product(in);
         }

         @Override
         public Product[] newArray(int size) {
             return new Product[size];
         }
     };

     public String getId() {
         return _id;
     }

     public String getName() {
         return name;
     }

     public String getCategory() {
         return category;
     }

     public List<Size> getSizes() {
         return sizes;
     }

     public int getPrice() {
         return price;
     }

     public String getDescription() {
         return description;
     }

     public String getImage() {
         return image;
     }

     public String getSlug() {
         return slug;
     }

     // Format price to Vietnamese currency
     public String getFormattedPrice() {
         return String.format("%,d₫", price);
     }

     public int getTotalQuantity() {
         int total = 0;
         for (Size size : sizes) {
             total += size.getQuantity();
         }
         return total;
     }

     @Override
     public int describeContents() {
         return 0;
     }

     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(_id);
         dest.writeString(name);
         dest.writeString(category);
         dest.writeInt(price);
         dest.writeString(description);
         dest.writeString(image);
         dest.writeString(slug);
         dest.writeTypedList(sizes);
     }

     public static class Size implements Parcelable {
         private int size;
         private int quantity;
         private String _id;

         public Size(int size, int quantity, String _id) {
             this.size = size;
             this.quantity = quantity;
             this._id = _id;
         }

         protected Size(Parcel in) {
             size = in.readInt();
             quantity = in.readInt();
             _id = in.readString();
         }

         public static final Creator<Size> CREATOR = new Creator<Size>() {
             @Override
             public Size createFromParcel(Parcel in) {
                 return new Size(in);
             }

             @Override
             public Size[] newArray(int size) {
                 return new Size[size];
             }
         };

         public int getSize() {
             return size;
         }

         public int getQuantity() {
             return quantity;
         }

         public String getId() {
             return _id;
         }

         @Override
         public int describeContents() {
             return 0;
         }

         @Override
         public void writeToParcel(Parcel dest, int flags) {
             dest.writeInt(size);
             dest.writeInt(quantity);
             dest.writeString(_id);
         }
     }
} 