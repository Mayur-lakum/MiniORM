package com.miniorm.repository;

import com.miniorm.core.EntityManager;
import com.miniorm.core.QueryOperator;

import java.util.List;

public class GenericRepository<T, ID>
        implements CrudRepository<T, ID>
{
    private final Class<T> entityClass;

    public GenericRepository(Class<T> entityClass)
    {
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity)
    {
        EntityManager.save(entity);
    }

    @Override
    public void saveAll(List<T> entities)
    {
        EntityManager.saveAll(entities);
    }

    @Override
    public T findById(ID id)
    {
        return EntityManager.findById(
                entityClass,
                id
        );
    }

    @Override
    public List<T> findAll()
    {
        return EntityManager.findAll(entityClass);
    }

    @Override
    public List<T> findByColumn(
            String column,
            Object value)
    {
        return EntityManager.findByColumn(
                entityClass,
                column,
                value
        );
    }

    @Override
    public List<T> findWhere(
            String column,
            QueryOperator operator,
            Object value)
    {
        return EntityManager.findWhere(
                entityClass,
                column,
                operator,
                value
        );
    }

    @Override
    public void update(T entity)
    {
        EntityManager.update(entity);
    }

    @Override
    public void delete(ID id)
    {
        EntityManager.delete(
                entityClass,
                id
        );
    }
}