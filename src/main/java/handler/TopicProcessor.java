package handler;


import exception.RetryLimitExceededException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import models.Topic;
import models.TopicSubscriber;

@Getter
public class TopicProcessor {
  private final Topic topic;
  private final Map<String, SubscriberWorker> subscriberWorkers;
  private final CustomExecutor<String> broadcastExecutor;

  public TopicProcessor(@NonNull final Topic topic,final CustomExecutor<String> executor) {
    this.topic = topic;
    this.subscriberWorkers = new HashMap<>();
    this.broadcastExecutor=executor;
  }

  public CompletionStage<Void> notifySubscribers() {
    return  CompletableFuture.allOf(topic.getSubscribers().values()
        .stream()
        .map(subscription -> startSubscriberWorker(subscription))
        .toArray(CompletableFuture[]::new));
  }

  public CompletionStage<Void> startSubscriberWorker(@NonNull final TopicSubscriber topicSubscriber) {
    final String subscriberId = topicSubscriber.getSubscriber().getId();
    if (!subscriberWorkers.containsKey(subscriberId)) {
      final SubscriberWorker subscriberWorker = new SubscriberWorker(topic, topicSubscriber);
      subscriberWorkers.put(subscriberId, subscriberWorker);

    }
    final SubscriberWorker subscriberWorker=subscriberWorkers.get(subscriberId);
    subscriberWorker.wakeUpIfNeeded();
    return broadcastExecutor.getThreadFor(topic.getTopicName() + topicSubscriber.getSubscriber().getId(),dowithRetry(subscriberWorker,topicSubscriber,topicSubscriber.getNumRetries()));
  }

  public CompletionStage<Void> dowithRetry(SubscriberWorker subscriberWorker,TopicSubscriber topicSubscriber,int numOfRetries){
    return CompletableFuture.runAsync(subscriberWorker)
        .handle((aVoid, throwable) ->{ if (throwable != null) {
      if (numOfRetries== 1) {
        throw new RetryLimitExceededException(throwable);
      }
      try {
        Thread.sleep(100000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return dowithRetry(subscriberWorker,topicSubscriber, numOfRetries-1);
    } else {
          subscriberWorker.wakeUpIfNeeded();
      return CompletableFuture.completedFuture((Void) null);
    }
  }).thenCompose(Function.identity());
  }
}