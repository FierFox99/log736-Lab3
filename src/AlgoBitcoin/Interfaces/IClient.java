package AlgoBitcoin.Interfaces;

import java.io.IOException;

public interface IClient {
    public int init() throws IOException;
    public void sendTransaction() throws IOException;
}
