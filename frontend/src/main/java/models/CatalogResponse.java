package models;

public class CatalogResponse {
    Integer bookNumber;
    String bookName;
    String topic;
    Integer cost;
    Integer count;

    public Integer getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(Integer bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CatalogResponse{" +
                "bookNumber=" + bookNumber +
                ", bookName='" + bookName + '\'' +
                ", topic='" + topic + '\'' +
                ", cost=" + cost +
                ", count=" + count +
                '}';
    }
}
