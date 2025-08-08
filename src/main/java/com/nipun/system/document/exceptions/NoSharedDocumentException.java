package com.nipun.system.document.exceptions;

public class NoSharedDocumentException extends RuntimeException {
  public NoSharedDocumentException() {
    super("The requested document version could not be found or is not accessible to you.");
  }
}
