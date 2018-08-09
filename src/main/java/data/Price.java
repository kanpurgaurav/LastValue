package data;

import java.util.Date;

public interface Price {

    public String getId();

    public Date getAsOf();

    public Payload getPayload();

}
