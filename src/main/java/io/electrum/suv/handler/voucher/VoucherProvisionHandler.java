package io.electrum.suv.handler.voucher;

import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.handler.BaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class VoucherProvisionHandler extends BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(VoucherProvisionHandler.class);

   public VoucherProvisionHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Provision";
   }

   public Response handle(ProvisionRequest provisionRequest) {
      // TODO Actually implement this method

       //TODO Remove
       log.info("Called handle on VoucherProvisionHandler");

       return null;
   }
}
