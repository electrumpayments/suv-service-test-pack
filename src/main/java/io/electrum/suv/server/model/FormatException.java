package io.electrum.suv.server.model;

public class FormatException extends RuntimeException {
   FormatError formatError;

   public FormatException(FormatError formatError) {
      this.formatError = formatError;
   }

   public FormatError getFormatError() {
      return formatError;
   }
}
