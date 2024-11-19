package Interfaces;

public interface IBlock {
    void setNounce(int nounce);

    String calculateBlockHash();

    void calculateRoot();
}
