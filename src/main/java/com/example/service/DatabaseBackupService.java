package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DatabaseBackupService {

    @Autowired
    private DataSource dataSource;

    public String createBackup() throws SQLException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupFileName = "socium_backup_" + timestamp + ".sql";
        String backupPath = "./backups/" + backupFileName;

        try {
            Path backupsDir = Paths.get("backups");
            if (!Files.exists(backupsDir)) {
                Files.createDirectories(backupsDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create backups directory", e);
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SCRIPT TO '" + new File(backupPath).getAbsolutePath() + "'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.execute();
            }
        }

        return backupFileName;
    }

    public void restoreBackup(String backupFileName) throws SQLException {
        String backupPath = "./backups/" + backupFileName;
        File backupFile = new File(backupPath);
        
        if (!backupFile.exists()) {
            throw new RuntimeException("Backup file not found: " + backupFileName);
        }

        // Restore backup using H2's RUNSCRIPT command
        try (Connection connection = dataSource.getConnection()) {
            // First, drop all existing tables
            String dropSql = "DROP ALL OBJECTS";
            try (PreparedStatement dropStatement = connection.prepareStatement(dropSql)) {
                dropStatement.execute();
            }

            // Then restore from backup
            String restoreSql = "RUNSCRIPT FROM '" + backupFile.getAbsolutePath() + "'";
            try (PreparedStatement restoreStatement = connection.prepareStatement(restoreSql)) {
                restoreStatement.execute();
            }
        }
    }

    public String[] listBackups() {
        File backupsDir = new File("./backups/");
        if (!backupsDir.exists()) {
            return new String[0];
        }

        File[] backupFiles = backupsDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (backupFiles == null) {
            return new String[0];
        }

        String[] backupNames = new String[backupFiles.length];
        for (int i = 0; i < backupFiles.length; i++) {
            backupNames[i] = backupFiles[i].getName();
        }
        return backupNames;
    }

    public long getDatabaseSize() {
        File dataDir = new File("./data/");
        if (!dataDir.exists()) {
            return 0;
        }

        long totalSize = 0;
        File[] files = dataDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith("socium_db")) {
                    totalSize += file.length();
                }
            }
        }
        return totalSize;
    }

    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
} 