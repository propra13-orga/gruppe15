package dungeon.server;

import dungeon.game.LogicHandler;
import dungeon.messages.Mailman;
import dungeon.messages.Message;
import dungeon.server.commands.CloseConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends messages to the client and injects all received messages into the server mailman.
 */
public class ClientConnection implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(ClientConnection.class.getName());

  private final Socket socket;

  private final Mailman mailman;

  private final ObjectInputStream inputStream;

  private final ObjectOutputStream outputStream;

  private final LogicHandler logicHandler;

  private final AtomicBoolean running = new AtomicBoolean(false);

  public ClientConnection (Socket socket, Mailman mailman, LogicHandler logicHandler) throws IOException {
    this.socket = socket;
    this.mailman = mailman;
    this.logicHandler = logicHandler;

    try {
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not create in/out streams", e);

      throw e;
    }
  }

  @Override
  public void run () {
    this.running.set(true);

    LOGGER.info("Send ID");
    this.send(this.logicHandler.nextId());

    LOGGER.info("Send world object");
    this.send(this.logicHandler.getWorld());

    while (this.running.get()) {
      Object received = null;

      try {
        received = this.read();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Connection is broken", e);
        this.stop();
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.WARNING, "Received object of unkown class", e);
      }

      if (received instanceof Message) {
        this.mailman.send((Message)received);
      } else if (received instanceof CloseConnection) {
        this.stop();
      }
    }
  }

  public void stop () {
    if (!this.running.get()) {
      return;
    }

    LOGGER.info("Shutdown client connection");

    this.running.set(false);

    try {
      this.inputStream.close();
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "Could not close stream", e);
    }

    try {
      this.outputStream.close();
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "Could not close stream", e);
    }

    try {
      this.socket.close();
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "Could not close socket", e);
    }
  }

  public void send (Object object) {
    try {
      this.outputStream.writeObject(object);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not send object " + object, e);
    }
  }

  private Object read () throws IOException, ClassNotFoundException {
    return this.inputStream.readObject();
  }
}
