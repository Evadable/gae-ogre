package org.jogre.server.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jogre.common.MessageBus;
import org.jogre.common.PayloadBuilder;
import org.jogre.server.data.ProtoSchema.OutgoingMessage;

import com.appenginefan.toolkit.persistence.Persistence;

public class StoreBasedMessageBusFactory implements MessageBusFactory {
  
  private static final Pattern META_PATTERN = 
      Pattern.compile("(\\d+)\\.(\\d+)\\.(.+)");
  
  private static final String sign(String secret, String handle, String lastAckToken) {
    try {
      String signThis = secret + "--sign-this-with-SHA-1--" + handle;
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      digest.update(signThis.getBytes("iso-8859-1"), 0, signThis.length());
      return new BigInteger(digest.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      return "NOOP";
    } catch (UnsupportedEncodingException e) {
      return "NOENC";
    }
  }
  
  private final Persistence<byte[]> binaryStore;
  
  public StoreBasedMessageBusFactory(Persistence<byte[]> binaryStore) {
    this.binaryStore = binaryStore;
  }

  @Override
  public MessageBus fromHandle(String handle) {
    //TODO: cache in request
    return new StoreBasedMessageBus(handle, binaryStore);
  }

  @Override
  public MessageBus loadMessageBus(
      HttpServletRequest request, String meta) {
    //TODO: cache in request
    
    // Null or malformed meta?
    if (meta == null) {
      return null;
    }
    Matcher matcher = META_PATTERN.matcher(meta);
    if (!matcher.matches()) {
      return null;
    }
    
    // Incorrectly signed meta?
    String handle = matcher.group(1);
    String lastAckToken = matcher.group(2);
    StoreBasedMessageBus bus = (StoreBasedMessageBus) fromHandle(handle);
    if (!sign(bus.getSecret(), handle, lastAckToken).equals(matcher.group(3))) {
      return null;
    }
    
    // Kick acknowledged messages out of the store
    bus.ack(Integer.parseInt(lastAckToken));

    // Done
    return bus;
  }

  @Override
  public MessageBus newMessageBus(HttpServletRequest request) {
    //TODO: cache in request
    return new StoreBasedMessageBus(null, binaryStore);
  }

  @Override
  public String toHandle(MessageBus bus) {
    return ((StoreBasedMessageBus) bus).getHandle();
  }

  @Override
  public void writeState(MessageBus bus,
      HttpServletResponse response) throws IOException {
    
    // Persist the store to get to a consistent state
    final StoreBasedMessageBus storeBus = (StoreBasedMessageBus) bus;
    storeBus.save();
    List<OutgoingMessage> messages = 
        storeBus.getLastKnownState().getMessageQueueList();
    
    // Create the meta
    final String handle = storeBus.getHandle();
    final String lastAckToken = 
      messages.isEmpty() 
        ? "0" 
        : String.valueOf(messages.get(messages.size() - 1).getAckToken());
    final String signature = sign(storeBus.getSecret(), handle, lastAckToken);
    final String meta = String.format("%s.%s.%s", handle, lastAckToken, signature);
    
    // Now, add all the outgoing messages
    PayloadBuilder payload = PayloadBuilder.payload(meta);
    for (OutgoingMessage message : messages) {
      payload.addChildNoparse(message.getPayload());
    }
    
    // Now, we can write the data to the outgoing stream
    response.getOutputStream().println(payload.toString());
  }

  @Override
  public void commit(HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void rollback(HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }
}
