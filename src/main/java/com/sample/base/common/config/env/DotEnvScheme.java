package com.sample.base.common.config.env;

public enum DotEnvScheme {
    DB_URL,
    DB_USERNAME,
    DB_PASSWORD,

    REDIS_HOST,
    REDIS_PORT,
    REDIS_PASSWORD,

    JWT_SECRETKEY,
    JWT_EXPIRATION,
    JWT_SALT,
    JWT_NUM,

    KAFKA_BOOTSTRAP_SERVERS,

    SMTP_HOST,
    SMTP_PORT,
    SMTP_USERNAME,
    SMTP_PASSWORD,
    SMTP_FROM_MAIL;
}
