package io.electrum.suv.handler.refund;

import io.electrum.suv.handler.BaseHandler;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class RefundReversalHandler extends BaseHandler {
   public RefundReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle() {
      // TODO Implement handle method
      throw new NotImplementedException("Handle not yet implemented in " + this.getRequestName());
   }

   @Override
   protected String getRequestName() {
      return "Refund Reversal";
   }
}
