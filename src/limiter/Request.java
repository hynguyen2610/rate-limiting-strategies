package limiter;

public class Request {
    private final Integer id;
    private final long timestamp;

    private Status status;

    public Request(Integer id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.status = Status.NEW;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Request{id='" + id + "', timestamp=" + timestamp + "}";
    }

    public Status getStatus() {
        return status;
    }

    public void process() {
        this.status = Status.PROCESSED;
    }

    public void rejected() {
        this.status = Status.REJECTED;
    }
}

enum Status {
    NEW,
    PROCESSED,
    REJECTED
}