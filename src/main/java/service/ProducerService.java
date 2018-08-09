package service;

import data.Chunk;
import exception.UnsupportedOperationException;

public interface ProducerService {

    public boolean startProduction(String batchid) throws UnsupportedOperationException;

    public boolean cancelBatch(String batchId)throws UnsupportedOperationException;;

    public boolean completeBatch(String batchId) throws UnsupportedOperationException;

    public boolean submitChunk(Chunk chunk) throws UnsupportedOperationException;
}
