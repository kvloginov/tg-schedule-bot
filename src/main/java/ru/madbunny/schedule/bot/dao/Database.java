package ru.madbunny.schedule.bot.dao;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class Database {

    private static final Map<String, String> ENV = System.getenv();

    private static final String DATABASE_URL_VAR_NAME = "SCHEDULE_DATABASE_URL";
    private static final String DATABASE_USERNAME_VAR_NAME = "SCHEDULE_DATABASE_USERNAME";
    private static final String DATABASE_PASSWORD_VAR_NAME = "SCHEDULE_DATABASE_PASSWORD";

    public static PreparedStatement prepareStatement(String query) throws SQLException {
        var url = "jdbc:postgresql://ec2-54-74-156-137.eu-west-1.compute.amazonaws.com:5432/df4mfs3vqcs9qv";
//        var url = ENV.get(DATABASE_URL_VAR_NAME);
        var username = ENV.get(DATABASE_USERNAME_VAR_NAME);
        var pass = ENV.get(DATABASE_PASSWORD_VAR_NAME);
        var con = DriverManager.getConnection(url, username, pass);

        return con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    }
}
