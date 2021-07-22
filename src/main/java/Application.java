import handler.CustomExecutor;
import models.Message;
import models.Topic;
import util.Timer;

public class Application {

  public static void main(String [] args){
    CustomExecutor<String> eventExecutor = new CustomExecutor<>(16);
    CustomExecutor<String> broadcastExecutor = new CustomExecutor<>(16);
    Timer t =new Timer();
    final CustomQueue queue = new CustomQueue(eventExecutor,broadcastExecutor);
    Topic t1=queue.createTopic("t1");
    Topic t2= queue.createTopic("t2");
    final SubscriberImp sub1 = new SubscriberImp("sub1", 10000);
    final SubscriberImp sub2 = new SubscriberImp("sub2", 10000);
    queue.subscribe(sub1, "t1",5,false);
    queue.subscribe(sub2, "t1",5,true);

    final SubscriberImp sub3 = new SubscriberImp("sub3", 5000);
    queue.subscribe(sub3, "t2",2,false);
    queue.publish("t1", new Message("m1",t.getCurrentTime()));
    queue.publish("t2", new Message("m3",t.getCurrentTime()));
    queue.publish("t1", new Message("m2",t.getCurrentTime()));

    queue.publish("t2", new Message("m4",t.getCurrentTime()));

    try{
      Thread.sleep(15000);
    }catch (Exception ex){
      System.out.println("exception in sleep");
    }

    queue.publish("t2", new Message("m5",t.getCurrentTime()));
    queue.publish("t1", new Message("m6",t.getCurrentTime()));
    queue.removeSubscriber("t1",sub1.getId());
    queue.publish("t1", new Message("m7",t.getCurrentTime()));
  }
}
