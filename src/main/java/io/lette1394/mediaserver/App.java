package io.lette1394.mediaserver;

public class App {
  public String getGreeting() {
    return "Hello world.";
  }

  public static void main(String[] args) {
    System.out.println(new App().getGreeting());
  }

  public void upload(String id, byte[] data) {}
}
