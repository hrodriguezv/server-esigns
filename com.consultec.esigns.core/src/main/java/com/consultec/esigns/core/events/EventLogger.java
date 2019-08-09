
package com.consultec.esigns.core.events;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

import com.consultec.esigns.core.util.SystemCommandExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class EventLogger.
 */
@Slf4j
public class EventLogger {

  /** The instance. */
  private static EventLogger instance;

  /** The mutex. */
  private static Object mutex = new Object();

  /** The id. */
  private AtomicInteger id;

  /** The Constant appName. */
  private static final String APP_NAME = "\"Consultec Listener\"";

  /**
   * The Enum Operation.
   */
  private enum Operation {

    /** The information. */
    INFORMATION,
    /** The error. */
    ERROR,
    /** The warning. */
    WARNING;

  }

  /**
   * The Enum Origin.
   */
  private enum Origin {

    /** The application. */
    APPLICATION,
    /** The system. */
    SYSTEM,
    /** The security. */
    SECURITY,
    /** The installation. */
    INSTALLATION;

  }

  /**
   * Instantiates a new Event Logger.
   */
  private EventLogger() {

  }

  /**
   * Gets the next id.
   *
   * @return the next id
   */
  private Integer getNextId() {

    synchronized (mutex) {
      instance.id.incrementAndGet();
    }

    return instance.id.get();

  }

  /**
   * Execute command.
   *
   * @param op the op
   * @param or the or
   * @param msg the msg
   */
  private static void executeCommand(Operation op, Origin or, String msg) {

    try {

      String[] cmd = {
          "cmd",
          "/c",
          "EventCreate",
          "/t",
          op.name(),
          "/id",
          instance.getNextId().toString(),
          "/l",
          or.name(),
          "/so",
          APP_NAME,
          "/d",
          " \"" + msg + "\""};

      SystemCommandExecutor commandExecutor = new SystemCommandExecutor(Arrays.asList(cmd));

      int exitValue = commandExecutor.executeCommand();

      log.debug("Process exited with value {}", exitValue);

    } catch (IOException e) {

      log.error("Error intentando enviar traza al stack de eventos de Windows", e);

    } catch (InterruptedException e) {

      log.error("Error intentando enviar traza al stack de eventos de Windows", e);

      Thread.currentThread().interrupt();

    }

  }

  /**
   * Gets the single instance of Event Logger.
   *
   * @return single instance of Event Logger
   */
  public static EventLogger getInstance() {

    EventLogger result = instance;

    if (result == null) {

      synchronized (mutex) {

        result = instance;

        if (result == null) {
          instance = result = new EventLogger();
        }

      }

    }

    return result;

  }

  /**
   * Inits the.
   */
  public void init() {

    String osName = System.getProperty("os.name").toUpperCase(Locale.ENGLISH);

    if (!osName.startsWith("WINDOWS")) {
      throw new RuntimeErrorException(null, "Not Windows");
    }

    instance.id = new AtomicInteger(1);

  }

  /**
   * Error.
   *
   * @param msg the msg
   */
  public void error(String msg) {

    executeCommand(Operation.ERROR, Origin.APPLICATION, msg);

  }

  /**
   * Info.
   *
   * @param msg the msg
   */
  public void info(String msg) {

    executeCommand(Operation.INFORMATION, Origin.APPLICATION, msg);

  }

  /**
   * Warn.
   *
   * @param msg the msg
   */
  public void warn(String msg) {

    executeCommand(Operation.WARNING, Origin.APPLICATION, msg);

  }

}
