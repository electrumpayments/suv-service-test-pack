package io.electrum.suv.handler.redeem;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class RedeemConfirmationHandler extends BaseHandler {
   public RedeemConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

//   public Response handle(Redeem)

    @Override
    protected String getRequestName() {
        return "Redeem Confirmation";
    }
}
