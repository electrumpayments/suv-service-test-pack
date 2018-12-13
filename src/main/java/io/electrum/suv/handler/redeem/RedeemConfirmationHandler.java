package io.electrum.suv.handler.redeem;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.RequestKey.ResourceType;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicAdvice;

public class RedeemConfirmationHandler extends BaseHandler {

   private String voucherCode;

   public RedeemConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   /**
    * Handle the response to a confirmRedeem request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#confirmRedeem">SUV
    * Interface docs</a> for details.
    *
    * @param confirmation
    *           from request body
    * @return a {@link BasicAdvice} for this transaction or a 400 Error if there is a format error or the voucher is
    *         already redeemed or reversed.
    */
   public Response handle(BasicAdvice confirmation) {
      try {
         ValidationResponse validationRsp;

         // THe UUID of this request
         String confirmationUuid = confirmation.getId();
         // The UUID identifying the request that this confirmation relates to
         String redemptionUuid = confirmation.getRequestId();

         // Validate uuid format in code until it can be ported to hibernate in the interface
         VoucherModelUtils.validateUuid(confirmationUuid);
         VoucherModelUtils.validateUuid(redemptionUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(confirmation.getThirdPartyIdentifiers());

         RedemptionResponse redemptionRsp =
               SUVTestServerRunner.getTestServer().getBackend()
                     .getRedemptionResponseRecords()
                     .get(new RequestKey(username, password, ResourceType.REDEMPTIONS_RESOURCE, redemptionUuid));
         if (redemptionRsp == null)
            voucherCode = null;
         else
            voucherCode = redemptionRsp.getVoucher().getCode();

         validationRsp = VoucherModelUtils.canConfirmRedemption(redemptionUuid, confirmationUuid, voucherCode);
         if (validationRsp.hasErrorResponse()) {
            return validationRsp.getResponse();
         }

         addRedemptionConfirmationToCache(confirmation);

         validationRsp.setResponse(Response.accepted((confirmation)).build());

         return validationRsp.getResponse();
      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the redemption confirmation request to the cache and updates the voucher state in the list of existing
    * vouchers.
    */
   private void addRedemptionConfirmationToCache(BasicAdvice confirmation) {
      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRedemptionConfirmationRecords();

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();

      RequestKey confirmationsKey =
            new RequestKey(username, password, ResourceType.CONFIRMATIONS_RESOURCE, confirmation.getRequestId());
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);

      confirmedExistingVouchers.put(voucherCode, SUVTestServer.VoucherState.CONFIRMED_REDEEMED);
   }

   @Override
   protected String getRequestName() {
      return "Redeem Confirmation";
   }
}
