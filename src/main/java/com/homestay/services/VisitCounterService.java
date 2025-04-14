package com.homestay.services;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@WebListener
public class VisitCounterService implements HttpSessionListener {

    private static final String FILE_PATH = "visit-count.txt";
    private AtomicInteger totalVisits = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line = reader.readLine();
            if (line != null) {
                totalVisits.set(Integer.parseInt(line.trim()));
            }
        } catch (IOException e) {
            totalVisits.set(0); 
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int count = totalVisits.incrementAndGet();
        saveCountToFile(count);
    }

    private void saveCountToFile(int count) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(count));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTotalVisits() {
        return totalVisits.get();
    }
}
