package br.com.loja_online;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LojaOnlineApplication {

    public static void main(String[] args) {
        loadEnvironmentFile();
        SpringApplication.run(LojaOnlineApplication.class, args);
    }

    private static void loadEnvironmentFile() {
        Path envFile = findEnvironmentFile();

        if (envFile == null) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);

            for (String line : lines) {
                loadProperty(line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo .env", exception);
        }
    }

    private static Path findEnvironmentFile() {
        Path rootEnvFile = Paths.get(".env");
        if (Files.exists(rootEnvFile)) {
            return rootEnvFile;
        }

        Path srcEnvFile = Paths.get("src", ".env");
        if (Files.exists(srcEnvFile)) {
            return srcEnvFile;
        }

        return null;
    }

    private static void loadProperty(String rawLine) {
        String line = rawLine.trim();

        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        if (line.startsWith("export ")) {
            line = line.substring(7).trim();
        }

        int separatorIndex = line.indexOf('=');

        if (separatorIndex <= 0) {
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();

        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }

        if (!key.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

}