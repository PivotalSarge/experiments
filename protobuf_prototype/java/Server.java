import io.pivotal.message.MessageProtocol.ConnectReply;
import io.pivotal.message.MessageProtocol.ConnectRequest;
import io.pivotal.message.MessageProtocol.DisconnectReply;
import io.pivotal.message.MessageProtocol.DisconnectRequest;
import io.pivotal.message.MessageProtocol.DestroyReply;
import io.pivotal.message.MessageProtocol.DestroyRequest;
import io.pivotal.message.MessageProtocol.GetReply;
import io.pivotal.message.MessageProtocol.GetRequest;
import io.pivotal.message.MessageProtocol.Header;
import io.pivotal.message.MessageProtocol.InvalidateReply;
import io.pivotal.message.MessageProtocol.InvalidateRequest;
import io.pivotal.message.MessageProtocol.Pair;
import io.pivotal.message.MessageProtocol.PutReply;
import io.pivotal.message.MessageProtocol.PutRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Server {
  private class ClientTask implements Runnable {
    private static final int headerSize = 9;

    private final int clientId;

    private final Socket clientSocket;

    private void dump(String caption, byte[] buf)
    {
      if (!caption.isEmpty()) {
        System.out.print(caption + ": ");
      }
      for (int z = 0; z < buf.length; ++z) {
        if (0 < z) {
          System.out.print(" ");
        }
        System.out.print(String.format("%02X", buf[z]));
      }
      System.out.println();
    }

    private ClientTask(int clientId, Socket clientSocket)
    {
      this.clientId = clientId;
      this.clientSocket = clientSocket;
      if (debugLogging) {
        System.out.println("Got a client (" + this.clientId + ")! :)");
      }
    }

    @Override public void run()
    {
      while (true) {
        try {
          com.google.protobuf.Message message = receiveMessage();
          if (message != null) {
            Header.MessageType messageType = getMessageType(message);
            if (Header.MessageType.CONNECT_REQUEST == messageType) {
              ConnectReply.Builder connectReply = ConnectReply.newBuilder();
              connectReply.setId(clientId);
              sendMessage(connectReply.build());
            }
            else if (Header.MessageType.DISCONNECT_REQUEST == messageType) {
              DisconnectReply.Builder disconnectReply = DisconnectReply.newBuilder();
              disconnectReply.setId(clientId);
              sendMessage(disconnectReply.build());
              try {
                Thread.currentThread().sleep(500);
              }
              catch (InterruptedException e) {
                // NOP
              }
              break;
            }
            else if (Header.MessageType.PUT_REQUEST == messageType) {
              PutRequest putRequest = (PutRequest)message;
              Region region = null;
              if (regions.containsKey(putRequest.getRegion())) {
                region = regions.get(putRequest.getRegion());
              }
              else {
                region = new Region(putRequest.getRegion());
                regions.put(putRequest.getRegion(), region);
              }

              PutReply.Builder putReply = PutReply.newBuilder();
              for (int i = 0; i < putRequest.getPairCount(); ++i) {
                Pair pair = putRequest.getPair(i);
                region.put(pair.getKey(), pair.getValue());
                if (debugLogging) {
                  System.out.println(pair.getKey() + "=" + pair.getValue());
                }
                putReply.setCount(putReply.getCount() + 1);
              }

              sendMessage(putReply.build());

              System.out.println();
              System.out.println(region.toString());
            }
            else if (Header.MessageType.GET_REQUEST == messageType) {
              GetRequest getRequest = (GetRequest)message;
              Region region = null;
              if (regions.containsKey(getRequest.getRegion())) {
                region = regions.get(getRequest.getRegion());

                System.out.println();
                System.out.println(region.toString());

                GetReply.Builder getReply = GetReply.newBuilder();
                for (int i = 0; i < getRequest.getKeyCount(); ++i) {
                  String key = getRequest.getKey(i);
                  if (region.containsKey(key)) {
                    Pair.Builder pair = Pair.newBuilder();
                    pair.setKey(key);
                    pair.setValue(region.get(key));
                    if (debugLogging) {
                      System.out.println(pair.getKey() + "=" + pair.getValue());
                    }
                    getReply.addPair(pair.build());
                  }
                }

                sendMessage(getReply.build());
              }
            }
            else if (Header.MessageType.INVALIDATE_REQUEST == messageType) {
              InvalidateRequest invalidateRequest = (InvalidateRequest)message;
              Region region = null;
              if (regions.containsKey(invalidateRequest.getRegion())) {
                region = regions.get(invalidateRequest.getRegion());

                InvalidateReply.Builder invalidateReply = InvalidateReply.newBuilder();
                for (int i = 0; i < invalidateRequest.getKeyCount(); ++i) {
                  String key = invalidateRequest.getKey(i);
                  if (region.containsKey(key)) {
                    region.invalidate(key);
                    invalidateReply.setCount(invalidateReply.getCount() + 1);
                  }
                }

                System.out.println();
                System.out.println(region.toString());

                sendMessage(invalidateReply.build());
              }
            }
            else if (Header.MessageType.DESTROY_REQUEST == messageType) {
              DestroyRequest destroyRequest = (DestroyRequest)message;
              Region region = null;
              if (regions.containsKey(destroyRequest.getRegion())) {
                region = regions.get(destroyRequest.getRegion());

                DestroyReply.Builder destroyReply = DestroyReply.newBuilder();
                for (int i = 0; i < destroyRequest.getKeyCount(); ++i) {
                  String key = destroyRequest.getKey(i);
                  if (region.containsKey(key)) {
                    region.destroy(key);
                    destroyReply.setCount(destroyReply.getCount() + 1);
                  }
                }

                System.out.println();
                System.out.println(region.toString());

                sendMessage(destroyReply.build());
              }
            }
            else {
              System.err.println("Unhandled message type: " + messageType);
              try {
                Thread.currentThread().sleep(500);
              }
              catch (InterruptedException e) {
                // NOP
              }
              break;
            }
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      try {
        Thread.currentThread().sleep(500);
      }
      catch (InterruptedException e) {
        // NOP
      }
      if (debugLogging) {
        System.out.println("DONE! :)");
      }
      try {
        clientSocket.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    void sendMessage(com.google.protobuf.GeneratedMessageV3 message) throws IOException
    {
      ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
      message.writeTo(messageStream);
      byte[] messageBytes = messageStream.toByteArray();
      if (debugLogging) {
        dump("SND[MSG]", messageBytes);
      }
      Header.Builder header = Header.newBuilder();
      header.setMessageType(getMessageType(message));
      header.setMessageSize(messageBytes.length);
      header.setOk(true);
      if (debugLogging) {
        System.out.println("SND " + header.getMessageType());
      }
      ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
      header.build().writeTo(headerStream);
      byte[] headerBytes = headerStream.toByteArray();
      if (debugLogging) {
        dump("SND[HDR]", headerBytes);
      }
      OutputStream outputStream = clientSocket.getOutputStream();
      outputStream.write(headerBytes);
      outputStream.write(messageBytes);
      outputStream.flush();
    }

    com.google.protobuf.Message receiveMessage() throws IOException
    {
      InputStream inputStream = clientSocket.getInputStream();
      while (inputStream.available() < headerSize) {
        try {
          Thread.currentThread().sleep(100);
        }
        catch (InterruptedException e) {
          // NOP
        }
      }
      byte[] headerBytes = new byte[headerSize];
      int headerCount = inputStream.read(headerBytes);
      if (headerSize == headerCount) {
        if (debugLogging) {
          dump("RCV[HDR]", headerBytes);
        }
        Header.Builder header = Header.newBuilder();
        header.mergeFrom(new ByteArrayInputStream(headerBytes));
        if (debugLogging) {
          System.out.println("RCV " + header.getMessageType());
        }
        while (inputStream.available() < header.getMessageSize()) {
          try {
            Thread.currentThread().sleep(100);
          }
          catch (InterruptedException e) {
            // NOP
          }
        }
        byte[] messageBytes = new byte[header.getMessageSize()];
        int messageCount = inputStream.read(messageBytes);
        if (header.getMessageSize() == messageCount) {
          if (debugLogging) {
            dump("RCV[MSG]", messageBytes);
          }
          if (debugLogging) {
            System.out.println("Read " + messageCount + " message bytes! :)");
          }
          com.google.protobuf.GeneratedMessageV3.Builder message =
              getMessageBuilder(header.getMessageType());
          message.mergeFrom(new ByteArrayInputStream(messageBytes));
          return message.build();
        }
      }
      else {
        System.err.println("Unable to read header. :(");
      }
      return null;
    }

    Header.MessageType getMessageType(com.google.protobuf.Message message)
    {
      if (message instanceof ConnectRequest) {
        return Header.MessageType.CONNECT_REQUEST;
      }
      else if (message instanceof ConnectReply) {
        return Header.MessageType.CONNECT_REPLY;
      }
      else if (message instanceof DisconnectRequest) {
        return Header.MessageType.DISCONNECT_REQUEST;
      }
      else if (message instanceof DisconnectReply) {
        return Header.MessageType.DISCONNECT_REPLY;
      }
      else if (message instanceof PutRequest) {
        return Header.MessageType.PUT_REQUEST;
      }
      else if (message instanceof PutReply) {
        return Header.MessageType.PUT_REPLY;
      }
      else if (message instanceof GetRequest) {
        return Header.MessageType.GET_REQUEST;
      }
      else if (message instanceof GetReply) {
        return Header.MessageType.GET_REPLY;
      }
      else if (message instanceof InvalidateRequest) {
        return Header.MessageType.INVALIDATE_REQUEST;
      }
      else if (message instanceof InvalidateReply) {
        return Header.MessageType.INVALIDATE_REPLY;
      }
      else if (message instanceof DestroyRequest) {
        return Header.MessageType.DESTROY_REQUEST;
      }
      else if (message instanceof DestroyReply) {
        return Header.MessageType.DESTROY_REPLY;
      }
      return Header.MessageType.UNSPECIFIED_TYPE;
    }

    com.google.protobuf.GeneratedMessageV3.Builder getMessageBuilder(Header.MessageType messageType)
    {
      if (Header.MessageType.CONNECT_REQUEST == messageType) {
        return ConnectRequest.newBuilder();
      }
      else if (Header.MessageType.CONNECT_REPLY == messageType) {
        return ConnectReply.newBuilder();
      }
      else if (Header.MessageType.DISCONNECT_REQUEST == messageType) {
        return DisconnectRequest.newBuilder();
      }
      else if (Header.MessageType.DISCONNECT_REPLY == messageType) {
        return DisconnectReply.newBuilder();
      }
      else if (Header.MessageType.PUT_REQUEST == messageType) {
        return PutRequest.newBuilder();
      }
      else if (Header.MessageType.PUT_REPLY == messageType) {
        return PutReply.newBuilder();
      }
      else if (Header.MessageType.GET_REQUEST == messageType) {
        return GetRequest.newBuilder();
      }
      else if (Header.MessageType.GET_REPLY == messageType) {
        return GetReply.newBuilder();
      }
      else if (Header.MessageType.INVALIDATE_REQUEST == messageType) {
        return InvalidateRequest.newBuilder();
      }
      else if (Header.MessageType.INVALIDATE_REPLY == messageType) {
        return InvalidateReply.newBuilder();
      }
      else if (Header.MessageType.DESTROY_REQUEST == messageType) {
        return DestroyRequest.newBuilder();
      }
      else if (Header.MessageType.DESTROY_REPLY == messageType) {
        return DestroyReply.newBuilder();
      }
      return null;
    }
  }

  static boolean debugLogging = Boolean.parseBoolean(System.getenv("DEBUG_LOGGING"));

  private int lastId = 0;

  private Map<String, Region> regions;

  public static void main(String[] args) throws Exception
  {
    Server server = new Server();
    server.startServer();
  }

  public Server()
  {
    this.lastId = 0;
    this.regions = new HashMap<String, Region>();
  }

  public void startServer()
  {
    final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    Runnable serverTask = new Runnable() {
      @Override public void run()
      {
        try {
          ServerSocket serverSocket = new ServerSocket(8000);
          if (debugLogging) {
            System.out.println("Waiting for clients to connect...");
          }
          while (true) {
            Socket clientSocket = serverSocket.accept();
            clientProcessingPool.submit(new ClientTask(++lastId, clientSocket));
          }
        }
        catch (IOException e) {
          System.err.println("Unable to process client request. :(");
          e.printStackTrace();
        }
      }
    };
    Thread serverThread = new Thread(serverTask);
    serverThread.start();
  }
}
