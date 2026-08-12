package com.miniorm.config;

import com.miniorm.exception.MiniORMException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection
{
    private static final HikariDataSource DATA_SOURCE;

    static
    {
        try
        {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(DBConfig.getUrl());
            config.setUsername(DBConfig.getUsername());
            config.setPassword(DBConfig.getPassword());
            config.setMaximumPoolSize(DBConfig.getMaxPoolSize());
            config.setMinimumIdle(DBConfig.getMinIdle());
            config.setPoolName("MiniORM-Pool");

            DATA_SOURCE =
                    new HikariDataSource(config);
        }
        catch (Exception e)
        {
            throw new MiniORMException(
                    "Failed to initialize database connection pool.",
                    e
            );
        }
    }

    private DBConnection()
    {
    }

    public static Connection getConnection()
    {
        if (DATA_SOURCE.isClosed())
        {
            throw new MiniORMException(
                    "Database connection pool is closed."
            );
        }

        try
        {
            return DATA_SOURCE.getConnection();
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to obtain database connection.",
                    e
            );
        }
    }

    public static void shutdown()
    {
        if (!DATA_SOURCE.isClosed())
        {
            DATA_SOURCE.close();
        }
    }
}