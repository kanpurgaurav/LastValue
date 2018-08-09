package data;

import java.util.Date;

public class PriceImpl implements Price {

    private String id;

    private Date asOf;

    private Payload payload;

    public PriceImpl(String id, Date asOf, Payload payload) {
        this.id = id;
        this.asOf = asOf;
        this.payload = payload;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getAsOf() {
        return asOf;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return String.format("id = %s, asOf = %s, payload=%s",String.valueOf(id), asOf.toString(), payload);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PriceImpl){
            PriceImpl price = (PriceImpl) obj;
            return price.id.equals(id) && price.asOf.equals(asOf) && price.payload.equals(payload);
        }
        return false;
    }
}
