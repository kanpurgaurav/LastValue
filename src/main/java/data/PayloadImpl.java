package data;

public class PayloadImpl implements Payload {

    private int price;

    public PayloadImpl(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.valueOf(price);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Payload){
            return ((PayloadImpl)obj).price == price;
        }
        return false;
    }
}
