package limiter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// This strategy also be called Sliding Window Limiter
// For Sliding Windo is a better name, since there is no actually the period end frame here to make a window like the Fixed one.
// It's more like having a moving Bar (of now()) to remove old requests (before now()) when the total of current requests pool
// still not exceed the max limit.
public class MovingWindowRateLimiter {

    private final int maxRequests;
    private final long windowDuration;
    private Deque<Request> requests;

    public MovingWindowRateLimiter(int maxRequests, long windowDurationMillis) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDurationMillis;
        this.requests = new ArrayDeque<>();
    }

    public synchronized boolean allowRequest(Integer id) {
        long now = System.currentTimeMillis();

        // Remove requests that are outside the moving window
        cleanupOldRequests(now);

        if (requests.size() < maxRequests) {
            // Add the new request (with its timestamp and unique ID)
            Request request = new Request(id, now);
            requests.add(request);
            return true; //
        }

        return false; // Request denied
    }

    private void cleanupOldRequests(long now) {
        // Remove requests older than the window duration
        while (!requests.isEmpty() && (now - requests.peekFirst().getTimestamp()) > windowDuration) {
            requests.pollFirst(); // Remove the oldest request
        }
    }

    public List<Request> getRequests() {
        return new ArrayList<>(requests);  // Return a snapshot of requests
    }
}

