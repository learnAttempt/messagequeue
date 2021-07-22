package models;


import exception.UnSubscribeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@AllArgsConstructor
@Getter
public class Topic {

  private final String topicName;
  private final String topicId;
  private final List<Message> messages;
  private final ConcurrentHashMap<String, TopicSubscriber> subscribers;

  public Topic(@NonNull final String topicName, @NonNull final String topicId) {
    this.topicName = topicName;
    this.topicId = topicId;
    this.messages = new ArrayList<>();
    this.subscribers = new ConcurrentHashMap<>();
  }

  public synchronized void addMessage(@NonNull final Message message) {
    messages.add(message);
  }

  public synchronized void addSubscriber(@NonNull final TopicSubscriber subscriber) {
    subscribers.put(subscriber.getSubscriber().getId(),subscriber);
  }

  public synchronized void removeSubscriber(@NonNull final String subscriberId) {
    if(!subscribers.containsKey(subscriberId))
      throw new UnSubscribeException( subscriberId +" is not subscribed");
    subscribers.remove(subscriberId);
  }

}
