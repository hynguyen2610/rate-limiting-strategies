package limiter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LeakyBucketRateLimiter {

    public static final int INTERVAL = 1_000;
    public static final int LEAK_INTERVAL1 = INTERVAL;
    public static final int LEAK_INTERVAL = LEAK_INTERVAL1;
    private final int capacity;               // Maximum capacity of the bucket
    private final int leakRate;               // Leak rate in requests per second (e.g., 1 request/sec)
    private int currentWaterLevel;            // Current "water level" in the bucket
    private long lastLeakTime;                // Last time the bucket leaked tokens

    public LeakyBucketRateLimiter(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;  // Leak rate in requests per second
        this.currentWaterLevel = 0;  // Initially, the bucket is empty
        this.lastLeakTime = System.currentTimeMillis();
    }

    // Method to leak tokens based on elapsed time
    private void leakTokens() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastLeakTime;

        // If time has passed, we calculate how many tokens should have leaked
        if (elapsedTime >= LEAK_INTERVAL) {
            int tokensToLeak = (int) (elapsedTime / 1000) * leakRate;
            currentWaterLevel = Math.max(0, currentWaterLevel - tokensToLeak);  // Prevent going negative
            lastLeakTime = now;  // Update the last leak time
        }
    }

    // Method to allow or deny a request
    public synchronized boolean allowRequest() {
        leakTokens();  // Ensure the bucket is leaking before accepting new requests

        // Check if there is space in the bucket (i.e., the current water level is less than capacity)
        if (currentWaterLevel < capacity) {
            currentWaterLevel++;  // Add the new request to the bucket (i.e., fill the bucket)
            return true;  // Request allowed
        }

        return false;  // Bucket is full, request denied
    }

    // Method to get the current space available in the bucket
    public int getAvailableSpace() {
        return capacity - currentWaterLevel;
    }

    // Main method for testing the Leaky Bucket Rate Limiter
    public static void main(String[] args) throws InterruptedException {
        // Create a Leaky Bucket rate limiter with a capacity of 5 requests and a leak rate of 1 request/sec
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(5, 1);

        // Simulate requests arriving at a rate of 1 request every 0.5 seconds
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest()) {
                System.out.println("Request " + (i + 1) + " allowed");
            } else {
                System.out.println("Request " + (i + 1) + " denied");
            }
            Thread.sleep(500); // Simulate a 0.5-second delay between requests
        }
    }
}
