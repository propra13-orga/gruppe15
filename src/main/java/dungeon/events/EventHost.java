package dungeon.events;

import dungeon.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Publishes events to all registered consumers.
 *
 * And every consumer runs in it's own thread.
 */
public final class EventHost {
  private final ExecutorService executor;

  private final Collection<EventConsumer> eventConsumers;

  private final BlockingQueue<Event> eventQueue;

  private boolean running;

  public EventHost () {
    executor = Executors.newCachedThreadPool();
    eventConsumers = new ArrayList<EventConsumer>();
    eventQueue = new LinkedBlockingQueue<Event>();

    publish(LifecycleEvent.INITIALIZE);
  }

  /**
   * Add an event handler.
   *
   * This may only be called before calling #run().
   */
  public void addHandler (EventHandler eventHandler) {
    eventConsumers.add(new EventQueueConsumer(eventHandler));
  }

  public void addConsumer (EventConsumer eventConsumer) {
    eventConsumers.add(eventConsumer);
  }

  /**
   * Runs the event host until a LifecycleEvent.SHUTDOWN event is received or it's thread is interrupted.
   */
  public void run () {
    for (EventConsumer eventConsumer : eventConsumers) {
      executor.execute(eventConsumer);
    }

    running = true;

    try {
      while (running) {
        waitForNextEvent();
      }
    } catch (InterruptedException e) {
      Log.notice("The event host has been interrupted", e);
    } finally {
      Log.notice("The event host is shutting down");

      shutdown();
    }
  }

  /**
   * Publish an event.
   *
   * This method should be called from the handlers, that want to publish their own events.
   *
   * @param event Event to be published
   * @return true on success, otherwise false
   */
  public boolean publish (Event event) {
    if (event == null) {
      return false;
    }

    try {
      eventQueue.put(event);

      return true;
    } catch (InterruptedException e) {
      Log.notice("A thread has been interrupted while sending an event", e);

      return false;
    }
  }

  /**
   * Shuts down the host, the executor and all eventConsumers.
   *
   * This does not call executor.shutdownNow, because this will force eventConsumers to end and will have bad side effects.
   */
  private void shutdown () {
    running = false;

    executor.shutdown();

    for (EventConsumer eventConsumer : eventConsumers) {
      eventConsumer.shutdown();
    }
  }

  private void waitForNextEvent () throws InterruptedException {
    Event event = eventQueue.take();

    handleEvent(event);
  }

  private void handleEvent (Event event) {
    publishEvent(event);

    if (event == LifecycleEvent.SHUTDOWN) {
      shutdown();
    }
  }

  private void publishEvent (Event event) {
    for (EventConsumer eventConsumer : eventConsumers) {
      eventConsumer.onEvent(event);
    }
  }
}
