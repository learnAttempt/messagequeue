import handler.CustomExecutor;
import handler.TopicProcessor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import models.ISubscriber;
import models.Message;
import models.Topic;
import models.TopicSubscriber;

@AllArgsConstructor
@Builder
public class CustomQueue {

  private final Map<String, TopicProcessor> topicProcessors;
  private final CustomExecutor<String> eventExecutor;
  private final CustomExecutor<String> deliverExecutor;

  private CustomQueue deadLetterQueue;

  CustomQueue(
      final CustomExecutor<String> deliverExecutor, final CustomExecutor<String> eventExecutor) {
    this.topicProcessors = new HashMap<>();
    this.deliverExecutor = deliverExecutor;
    this.eventExecutor = eventExecutor;
  }

  public CompletionStage<Void> publish(final String topic, @NonNull final Message message) {
    return eventExecutor.getThreadFor(topic, publishToQueue(topic, message));
  }

  public Topic createTopic(@NonNull final String topicName) {
    final Topic topic = new Topic(topicName, UUID.randomUUID().toString());
    TopicProcessor topicHandler = new TopicProcessor(topic, deliverExecutor);
    topicProcessors.put(topicName, topicHandler);
    System.out.println("Created topic: " + topic.getTopicName());
    return topic;
  }

  public CompletionStage<Void> publishToQueue(
      @NonNull String topicName, @NonNull final Message message) {
    if (!topicProcessors.containsKey(topicName)) createTopic(topicName);
    topicProcessors.get(topicName).getTopic().addMessage(message);
    return topicProcessors.get(topicName).notifySubscribers();
  }

  public CompletionStage<Void> subscribe(
      @NonNull final ISubscriber subscriber,
      @NonNull final String topicName,
      int numRetries,
      boolean batchconsumption) {
    return eventExecutor.getThreadFor(
        topicName + subscriber.getId(),
        () -> {
          if (!topicProcessors.containsKey(topicName))
            throw new IllegalArgumentException("topic dsnt exist to subscribe");
          topicProcessors
              .get(topicName)
              .getTopic()
              .addSubscriber(new TopicSubscriber(subscriber, numRetries, batchconsumption));

          System.out.println(subscriber.getId() + " subscribed to topic: " + topicName);
        });
  }

  public CompletionStage<Void> removeSubscriber(
      @NonNull final String  topicName, @NonNull final String subscriberId) {
    return eventExecutor.getThreadFor(
        topicName + subscriberId,
        () -> {
          topicProcessors.get(topicName).getTopic().removeSubscriber(subscriberId);

          System.out.println(subscriberId + " removed subscription to topic: " +topicName );
        });
  }
}
