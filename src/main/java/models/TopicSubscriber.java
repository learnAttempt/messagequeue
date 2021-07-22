package models;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@AllArgsConstructor
@Getter
public class TopicSubscriber {
  private final AtomicInteger offset;
  private final ISubscriber subscriber;
  private final int numRetries;
  private final boolean batchConsumption;

  public TopicSubscriber(
      @NonNull final ISubscriber subscriber, int numRetries, boolean batchConsumption) {
    this.subscriber = subscriber;
    this.offset = new AtomicInteger(0);
    this.numRetries = numRetries;
    this.batchConsumption = batchConsumption;
  }
}
