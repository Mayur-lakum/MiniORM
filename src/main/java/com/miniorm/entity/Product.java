package com.miniorm.entity;

import com.miniorm.annotations.Column;
import com.miniorm.annotations.Entity;
import com.miniorm.annotations.Id;
import com.miniorm.annotations.Table;

import java.util.Objects;

@Entity
@Table(name = "products")
public class Product
{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private double price;

    // Required for reflection-based entity creation.
    public Product()
    {
    }

    public Product(int id, String name, double price)
    {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    @Override
    public String toString()
    {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (!(object instanceof Product product))
        {
            return false;
        }

        return id == product.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}