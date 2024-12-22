package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class DatabaseConnection {


    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(getUrl());
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String getUrl() {
        String targetPath = Path.of(System.getProperty("user.dir")) + "\\database.db";
        URL sourceUrl = DatabaseConnection.class.getResource("/db/projectdb.db");
        assert sourceUrl != null;
        String url =  "jdbc:sqlite:".concat(targetPath);
        return url;
    }
    public static void ensureDBFile() throws IOException {


        String targetPath = Path.of(System.getProperty("user.dir")) + "\\database.db";
        File targetFile = new File(targetPath);
        if (targetFile.exists()) {
            return;
        }
        InputStream link = (DatabaseConnection.class.getResourceAsStream("/db/projectdb.db"));
        Files.copy(link, targetFile.getAbsoluteFile().toPath());

        //Path sourcePath = Path.of(sourceUrl.getPath());
        //Path tp = Path.of(targetPath);

        //Files.copy(sourcePath, tp, StandardCopyOption.REPLACE_EXISTING );
        /*
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/projectdb.db")) {
            if (in == null) {
                throw new IllegalStateException("Database file not found in resources!");
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
    }


}
