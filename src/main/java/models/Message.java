package models;

    import java.util.UUID;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class Message {

  private final String id;
  private final String message;
  private final long creationTime;

  public Message(String message, Long creationTime) {
    this.message = message;
    this.id = UUID.randomUUID().toString();
    this.creationTime = creationTime;
  }
}
