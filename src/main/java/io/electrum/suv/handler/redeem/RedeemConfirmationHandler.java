package io.electrum.suv.handler.redeem;

import io.electrum.suv.handler.BaseHandler;
import io.electrum.vas.model.BasicAdvice;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class RedeemConfirmationHandler extends BaseHandler {
   public RedeemConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicAdvice body, UriInfo uriInfo) {
      // TODO Implement handle method
      throw new NotImplementedException("Handle not yet implemented in " + this.getRequestName());
   }

    @Override
    protected String getRequestName() {
        return "Redeem Confirmation";
    }
}
