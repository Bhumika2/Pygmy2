package models;

public class OrderRequest {
    Integer bookNumber;

    public OrderRequest(Integer bookNumber) {
        this.bookNumber = bookNumber;
    }

    public Integer getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(Integer bookNumber) {
        this.bookNumber = bookNumber;
    }
}
