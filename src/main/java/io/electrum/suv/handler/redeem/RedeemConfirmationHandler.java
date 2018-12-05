package io.electrum.suv.handler.redeem;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.TenderAdvice;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class RedeemConfirmationHandler extends BaseHandler {

   private String voucherCode;

   public RedeemConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicAdvice confirmation, UriInfo uriInfo) {
      try {
         Response rsp;

         // THe UUID of this request
         String confirmationUuid = confirmation.getId();
         // The UUID identifying the request that this confirmation relates to
         String redemptionUuid = confirmation.getRequestId();

         // TODO !!!Either remove validation or write postman test for confirmation!!!
         if (!VoucherModelUtils.isValidUuid(confirmationUuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  confirmationUuid,
                  null, // TODO Could overload method
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         } else if (!VoucherModelUtils.isValidUuid(redemptionUuid)) {
            return VoucherModelUtils
                  .buildInvalidUuidErrorResponse(redemptionUuid, null, username, ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         RedemptionResponse redemptionRsp =
               SUVTestServerRunner.getTestServer()
                     .getRedemptionResponseRecords()
                     .get(new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, redemptionUuid));

         if (redemptionRsp == null)
            voucherCode = null;
         else
            voucherCode = redemptionRsp.getVoucher().getCode();

         rsp = VoucherModelUtils.canConfirmRedemption(redemptionUuid, confirmationUuid, username, password, voucherCode);
         if (rsp != null) {
            return rsp;
         }

         addRedemptionConfirmationToCache(confirmation);

         rsp = Response.accepted((confirmation)).build(); // TODO Ask Casey if this is ok

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the voucher confirmation request to the cache and stores an entry to the voucher in the list of existing
    * vouchers //TODO documentation
    * 
    * @param confirmation
    */
   private void addRedemptionConfirmationToCache(BasicAdvice confirmation) {
      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getRedemptionConfirmationRecords();

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      RequestKey confirmationsKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, confirmation.getRequestId());
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);

      confirmedExistingVouchers.put(voucherCode, SUVTestServer.VoucherState.CONFIRMED_REDEEMED);
   }

   @Override
   protected String getRequestName() {
      return "Redeem Confirmation";
   }
}
