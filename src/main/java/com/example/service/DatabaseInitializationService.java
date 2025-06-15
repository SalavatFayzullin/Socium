package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DatabaseInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create data directory for H2 database files
        createDataDirectory();
        
        // Create uploads directory for avatar files
        createUploadsDirectory();
        
        // Print database information
        printDatabaseInfo();
    }

    private void createDataDirectory() {
        try {
            Path dataPath = Paths.get("data");
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                System.out.println("✓ Created data directory for H2 database files");
            } else {
                System.out.println("✓ Data directory already exists");
            }
        } catch (Exception e) {
            System.err.println("✗ Error creating data directory: " + e.getMessage());
        }
    }

    private void createUploadsDirectory() {
        try {
            Path uploadsPath = Paths.get("uploads/avatars");
            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
                System.out.println("✓ Created uploads directory for avatar files");
            } else {
                System.out.println("✓ Uploads directory already exists");
            }
        } catch (Exception e) {
            System.err.println("✗ Error creating uploads directory: " + e.getMessage());
        }
    }

    private void printDatabaseInfo() {
        System.out.println("\n=== SOCIUM DATABASE CONFIGURATION ===");
        System.out.println("Database Type: H2 File-based Database");
        System.out.println("Database Files: ./data/socium_db.*");
        System.out.println("Avatar Uploads: ./uploads/avatars/");
        System.out.println("H2 Console: http://localhost:8080/h2-console");
        System.out.println("Database URL: jdbc:h2:file:./data/socium_db");
        System.out.println("Username: sa");
        System.out.println("Password: password");
        System.out.println("Persistence: All data saved to files");
        System.out.println("=====================================\n");
    }
} 