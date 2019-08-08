package com.consultec.esigns.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.io.source.ByteArrayOutputStream;

public class StreamHelper {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(StreamHelper.class);

  /**
   * Instantiates a new stream helper.
   */
  private StreamHelper() {}

  public static <T> byte[] toStream(T obj) {

    // Reference for stream of bytes
    byte[] stream = null;

    // ObjectOutputStream is used to convert a Java object into OutputStream
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);) {

      oos.writeObject(obj);

      stream = baos.toByteArray();

    } catch (IOException e) {
      logger.error("Error serializing the object", e);
    }

    return stream;

  }

  @SuppressWarnings("unchecked")
  public static <T> T fromStream(byte[] stream) {

    T stu = null;

    try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
        ObjectInputStream ois = new ObjectInputStream(bais);) {

      stu = (T) ois.readObject();

    } catch (IOException | ClassNotFoundException e) {

      logger.error("Error deserializing the object", e);

    }

    return stu;
  }

}
