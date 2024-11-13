package limiter;

public class FixedTokenBucketRateLimiter {

    private final long capacity;            // Max tokens the bucket can hold
    private final long refillAmount;        // Number of tokens to add during each refill
    private final long refillIntervalMillis; // Interval in milliseconds between refills
    private long availableTokens;           // Current number of tokens in the bucket
    private long lastRefillTimestamp;       // Timestamp of the last refill

    public FixedTokenBucketRateLimiter(long capacity, long refillAmount, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillAmount = refillAmount;
        this.refillIntervalMillis = refillIntervalMillis;
        this.availableTokens = capacity;  // Initially the bucket is full
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    // Allow request if there's at least one token available
    public synchronized boolean allowRequest() {
        refillTokens();  // Refill tokens if necessary

        if (availableTokens > 0) {
            availableTokens--;  // Consume a token for the request
            return true;         // Request allowed
        }

        return false;  // No tokens available, request denied
    }

    // Refill the tokens by the fixed amount based on elapsed time
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;

        // Check if enough time has passed to refill the tokens
        if (elapsedTime >= refillIntervalMillis) {
            // Calculate how many times we should refill based on elapsed time
            long refills = elapsedTime / refillIntervalMillis;
            for (long i = 0; i < refills; i++) {
                // Refill a fixed number of tokens up to the capacity
                availableTokens = Math.min(availableTokens + refillAmount, capacity);
                lastRefillTimestamp = now; // Update the last refill time
            }
        }
    }

    // Main method for testing the Token Bucket Rate Limiter
    public static void main(String[] args) throws InterruptedException {
        // Create a Token Bucket Rate Limiter with a capacity of 5 tokens, 1 token per second refill
        FixedTokenBucketRateLimiter limiter = new FixedTokenBucketRateLimiter(5, 1, 1000);

        // Simulate requests
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest()) {
                System.out.println("Request " + (i + 1) + " allowed");
            } else {
                System.out.println("Request " + (i + 1) + " denied");
            }
            Thread.sleep(1000); // Simulate a 1-second delay between requests
        }
    }
}
