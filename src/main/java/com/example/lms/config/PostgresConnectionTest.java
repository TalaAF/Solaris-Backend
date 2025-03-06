package com.example.lms.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
@Profile("postgres-test")
public class PostgresConnectionTest {

    @Bean
    public CommandLineRunner testPostgresConnection(DataSource dataSource) {
        return args -> {
            System.out.println("\n\n==================================================");
            System.out.println("TESTING POSTGRESQL CONNECTION TO UNIVERSITY DATABASE");
            System.out.println("==================================================");
            
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("✅ Connection established successfully!");
                System.out.println("Connection URL: " + connection.getMetaData().getURL());
                System.out.println("Database product: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("Database version: " + connection.getMetaData().getDatabaseProductVersion());
                
                // Test a simple query
                try (Statement stmt = connection.createStatement()) {
                    System.out.println("\n📋 Listing all tables in the database:");
                    ResultSet rs = stmt.executeQuery(
                        "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' ORDER BY table_name");
                    
                    boolean tablesFound = false;
                    while (rs.next()) {
                        tablesFound = true;
                        System.out.println("  - " + rs.getString("table_name"));
                    }
                    
                    if (!tablesFound) {
                        System.out.println("  (No tables found in the public schema)");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ DATABASE CONNECTION FAILED");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("==================================================\n");
        };
    }
}