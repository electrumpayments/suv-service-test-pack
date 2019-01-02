package io.electrum.suv.server.model;

import io.electrum.suv.server.SUVFormatViolationExceptionMapper;

/**
 * Custom Exception which can be thrown for custom formatting validation.
 * 
 * @see SUVFormatViolationExceptionMapper
 */
public class FormatException extends RuntimeException {
   private final FormatError formatError;

   public FormatException(FormatError formatError) {
      this.formatError = formatError;
   }

   public FormatError getFormatError() {
      return formatError;
   }
}
