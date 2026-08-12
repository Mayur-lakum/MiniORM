package com.miniorm.entity;

import com.miniorm.annotations.Column;
import com.miniorm.annotations.Entity;
import com.miniorm.annotations.Id;
import com.miniorm.annotations.Table;

import java.util.Objects;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    // Required for reflection-based entity creation.
    public User()
    {
    }

    public User(int id, String name, String email)
    {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (!(object instanceof User user))
        {
            return false;
        }

        return id == user.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}