package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseStartupValidator implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseStartupValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                System.out.println("✅ Database connection successful on startup.");
            } else {
                System.err.println("❌ Database connection is not valid on startup.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to the database on startup: " + e.getMessage());
            System.exit(1);
        }
    }
}
