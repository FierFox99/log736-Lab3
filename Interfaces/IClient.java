package Interfaces;

import Classes.Transaction;

import java.io.IOException;

public interface IClient {
    public int init() throws IOException;
    public void sendTransaction() throws IOException;
}
