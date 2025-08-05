package com.nipun.system.document.exceptions;

public class NoSharedDocumentException extends RuntimeException {
  public NoSharedDocumentException() {
    super("No such document shard with user");
  }
}
