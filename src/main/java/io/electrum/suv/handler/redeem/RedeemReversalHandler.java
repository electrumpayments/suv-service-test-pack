package io.electrum.suv.handler.redeem;

import io.electrum.suv.handler.BaseHandler;
import io.electrum.vas.model.BasicReversal;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class RedeemReversalHandler extends BaseHandler {
   public RedeemReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicReversal body, UriInfo uriInfo) {
      // TODO Implement handle method
      throw new NotImplementedException("Handle not yet implemented in " + this.getRequestName());
   }

   @Override
   protected String getRequestName() {
      return "Redeem Reversal";
   }
}
