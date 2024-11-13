package limiter;

import java.util.ArrayList;
import java.util.List;

public class FixedWindowRateLimiter {

    private final int maxRequestsCount;        // Max requests per window
    private final long windowDuration;   // Window duration in milliseconds (e.g., 10 seconds)
    private long windowStartTime;        // The start time of the current window
    private int requestCount;            // The current count of requests in the window
    private List<Request> requests;      // List to store processed requests

    public FixedWindowRateLimiter(int maxRequests, long windowDurationMillis) {
        this.maxRequestsCount = maxRequests;
        this.windowDuration = windowDurationMillis;
        this.windowStartTime = System.currentTimeMillis();
        this.requestCount = 0;
        this.requests = new ArrayList<>();
    }

    public synchronized boolean allowRequest(Integer orderNumber) {
        long now = System.currentTimeMillis();

        // If the current window has expired, reset the window and request count
        if (now - windowStartTime >= windowDuration) {
            windowStartTime = now; // Reset the window start time
            requestCount = 0;      // Reset the request count
            requests.clear();      // Clear the list of requests
        }

        // If the request count is less than the max allowed, allow the request
        if (requestCount < maxRequestsCount) {
            requestCount++;                       // Increment the request count
            Request request = new Request(orderNumber, now); // Create new request with ID and timestamp
            requests.add(request);                // Add the request to the list
            return true; // Request allowed
        }

        // Otherwise, deny the request as the rate limit is exceeded
        return false; // Request denied
    }

    public List<Request> getRequests() {
        return requests;
    }

    // Dummy Request class with an ID and timestamp


    public static void main(String[] args) throws InterruptedException {
        // Create a Fixed Window rate limiter with a limit of 3 requests per 10 seconds
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(3, 10000);

        // Simulate requests
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest(i)) {
                System.out.println("Request " + (i + 1) + " allowed");
            } else {
                System.out.println("Request " + (i + 1) + " denied");
            }
            Thread.sleep(2000); // Simulate a delay of 2 seconds between requests
        }

        // Output the details of the allowed requests (with ID and timestamp)
        System.out.println("Details of processed requests: ");
        for (Request request : limiter.getRequests()) {
            System.out.println(request);
        }
    }
}
