package models;

public interface ISubscriber {

  String getId();
  /*void setRetries(int val);
  void setBatchConsumption(boolean val);*/
  void consume(Message message) throws InterruptedException;
}
