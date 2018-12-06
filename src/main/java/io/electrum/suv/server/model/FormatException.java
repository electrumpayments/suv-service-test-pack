package io.electrum.suv.server.model;

import java.text.Format;

public class FormatException extends RuntimeException {
    public FormatError getFormatError() {
        return formatError;
    }

    FormatError formatError;

   public FormatException(FormatError formatError) {
      this.formatError = formatError;
   }
}
