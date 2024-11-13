package limiter.multithread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ModernTokenBucketRateLimiter {

    private final long capacity;              // Max tokens the bucket can hold
    private final long refillAmount;          // Fixed number of tokens added at each refill
    private final long refillIntervalMillis;  // Interval in milliseconds between refills
    private final AtomicLong availableTokens; // Current number of tokens in the bucket (Atomic for thread-safety)
    private long lastRefillTimestamp;         // Timestamp of the last refill

    private final ScheduledExecutorService scheduler; // Executor for refilling tokens periodically

    public ModernTokenBucketRateLimiter(long capacity, long refillAmount, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillAmount = refillAmount;
        this.refillIntervalMillis = refillIntervalMillis;
        this.availableTokens = new AtomicLong(capacity);  // Initially the bucket is full
        this.lastRefillTimestamp = System.currentTimeMillis();

        // Create a ScheduledExecutorService for refilling tokens at fixed intervals
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refillTokens, 0, refillIntervalMillis, TimeUnit.MILLISECONDS);
    }

    // Refills the bucket with tokens at the configured interval
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;

        if (elapsedTime >= refillIntervalMillis) {
            // Calculate how many times the bucket should be refilled based on elapsed time
            long refills = elapsedTime / refillIntervalMillis;
            for (long i = 0; i < refills; i++) {
                // Refill tokens up to the capacity
                availableTokens.set(Math.min(availableTokens.get() + refillAmount, capacity));
                lastRefillTimestamp = now;  // Update the last refill time
            }
        }
    }

    // Allow a request if there's at least one token available
    public boolean allowRequest() {
        refillTokens();  // Refill tokens first

        if (availableTokens.get() > 0) {
            availableTokens.decrementAndGet();  // Consume a token for the request
            return true;  // Request allowed
        }

        return false;  // No tokens available, request denied
    }

    public long getAvailableTokens() {
        return availableTokens.get();
    }

    // Main method for testing the Token Bucket Rate Limiter
    public static void main(String[] args) throws InterruptedException {
        // Create a Token Bucket Rate Limiter with a capacity of 5 tokens, 1 token refilled every second
        ModernTokenBucketRateLimiter limiter = new ModernTokenBucketRateLimiter(5, 1, 6000);

        // Simulate requests
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest()) {
                System.out.println("Request " + (i + 1) + " allowed");
            } else {
                System.out.println("Request " + (i + 1) + " denied");
            }
            Thread.sleep(1000); // Simulate a 1-second delay between requests
        }

        // Optionally, we could shutdown the scheduler after use
        limiter.scheduler.shutdown();
    }
}
