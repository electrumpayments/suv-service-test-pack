package io.electrum.suv.handler.refund;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class RefundReversalHandler extends BaseHandler {
   public RefundReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   @Override
   protected String getRequestName() {
      return "Refund Reversal";
   }
}
