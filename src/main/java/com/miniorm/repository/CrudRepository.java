package com.miniorm.repository;

import com.miniorm.core.QueryOperator;

import java.util.List;

public interface CrudRepository<T, ID>
{
    void save(T entity);

    void saveAll(List<T> entities);

    T findById(ID id);

    List<T> findAll();

    List<T> findByColumn(
            String column,
            Object value
    );

    List<T> findWhere(
            String column,
            QueryOperator operator,
            Object value
    );

    void update(T entity);

    void delete(ID id);
}