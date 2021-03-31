package de.waldorfaugsburg.lessoncontrol.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.config.ServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class LessonControlServerApplication {

    public LessonControlServerApplication() {

    }

    public static void main(final String[] args) {
        SpringApplication.run(LessonControlServerApplication.class, args);
    }

    @Bean
    public Gson getGson() {
        return new GsonBuilder().create();
    }

    @Bean
    public ServerConfiguration getConfiguration(final Gson gson) throws IOException {
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("config.json")))) {
            return gson.fromJson(reader, ServerConfiguration.class);
        }
    }

    @Bean
    public DeviceConfiguration getDeviceConfiguration(final Gson gson) throws IOException {
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("devices.json")))) {
            return gson.fromJson(reader, DeviceConfiguration.class);
        }
    }
}
