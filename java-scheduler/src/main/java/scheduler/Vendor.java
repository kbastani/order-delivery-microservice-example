package scheduler;

public class Vendor<T> {

    private Long id;
    private T source;

    public Vendor(Long id, T source) {
        this.id = id;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public T getSource() {
        return source;
    }
}
