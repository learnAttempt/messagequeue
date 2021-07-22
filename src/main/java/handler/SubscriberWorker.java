package handler;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import models.Message;
import models.Topic;
import models.TopicSubscriber;

@Getter
public class SubscriberWorker implements Runnable {

  private final Topic topic;
  private final TopicSubscriber topicSubscriber;

  public SubscriberWorker(
      @NonNull final Topic topic, @NonNull final TopicSubscriber topicSubscriber) {
    this.topic = topic;
    this.topicSubscriber = topicSubscriber;
  }

  @SneakyThrows
  @Override
  public void run() {
    synchronized (topicSubscriber) {
      try {

        do {
          int curOffset = topicSubscriber.getOffset().get();
          while (curOffset >= topic.getMessages().size()) {
            topicSubscriber.wait();
          }
          if (topicSubscriber.isBatchConsumption()) consumeBatchMessages(topic, curOffset);
          else {
            Message message = topic.getMessages().get(curOffset);
            topicSubscriber.getSubscriber().consume(message);

            topicSubscriber.getOffset().compareAndSet(curOffset, curOffset + 1);
          }
        } while (true);
      } catch (InterruptedException ex) {

      }
    }
  }

  private void consumeBatchMessages(Topic topic, int curOffset) throws InterruptedException {
    int endIndex = topic.getMessages().size();
    int offset = curOffset;
    while (offset < endIndex) {
      Message message = topic.getMessages().get(curOffset);
      topicSubscriber.getSubscriber().consume(message);
      offset++;
    }
    topicSubscriber.getOffset().compareAndSet(curOffset, offset);
  }

  public synchronized void wakeUpIfNeeded() {
    synchronized (topicSubscriber) {
      topicSubscriber.notify();
    }
  }
}
