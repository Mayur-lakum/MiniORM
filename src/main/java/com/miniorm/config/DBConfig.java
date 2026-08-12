package com.miniorm.config;

import com.miniorm.exception.MiniORMException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DBConfig
{
    private static final Properties PROPERTIES =
            new Properties();

    static
    {
        loadProperties();
    }

    private DBConfig()
    {
    }

    public static String getUrl()
    {
        return resolve(
                "DB_URL",
                "db.url"
        );
    }

    public static String getUsername()
    {
        return resolve(
                "DB_USERNAME",
                "db.username"
        );
    }

    public static String getPassword()
    {
        return resolve(
                "DB_PASSWORD",
                "db.password"
        );
    }

    public static int getMaxPoolSize()
    {
        return getPositiveInteger(
                "DB_POOL_MAX_SIZE",
                "db.pool.maxSize",
                10
        );
    }

    public static int getMinIdle()
    {
        return getPositiveInteger(
                "DB_POOL_MIN_IDLE",
                "db.pool.minIdle",
                2
        );
    }

    private static void loadProperties()
    {
        try (InputStream input =
                     DBConfig.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "db.properties"
                             ))
        {
            if (input != null)
            {
                PROPERTIES.load(input);
            }
        }
        catch (IOException e)
        {
            throw new MiniORMException(
                    "Failed to load database configuration.",
                    e
            );
        }
    }

    private static String resolve(
            String environmentKey,
            String propertyKey)
    {
        String environmentValue =
                System.getenv(environmentKey);

        if (environmentValue != null
                && !environmentValue.isBlank())
        {
            return environmentValue;
        }

        return PROPERTIES.getProperty(propertyKey);
    }

    private static int getPositiveInteger(
            String environmentKey,
            String propertyKey,
            int defaultValue)
    {
        String value =
                resolve(
                        environmentKey,
                        propertyKey
                );

        if (value == null || value.isBlank())
        {
            return defaultValue;
        }

        try
        {
            int result =
                    Integer.parseInt(value);

            if (result <= 0)
            {
                throw new MiniORMException(
                        "Configuration value "
                                + propertyKey
                                + " must be greater than zero."
                );
            }

            return result;
        }
        catch (NumberFormatException e)
        {
            throw new MiniORMException(
                    "Invalid integer value for "
                            + propertyKey
                            + ": "
                            + value,
                    e
            );
        }
    }
}