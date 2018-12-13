package io.electrum.suv.handler.redeem;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RedemptionModelUtils;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.RequestKey.ResourceType;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class RedeemReversalHandler extends BaseHandler {

   private BasicReversal reversal;

   public RedeemReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   /**
    * Handle the response to a reverseRedeem request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#reverseRedeem">SUV
    * Interface docs</a> for details.
    *
    * @param reversal
    *           from request body
    * @return a {@link BasicReversal} for this transaction, a 400 error if the request is incorrectly formatted of a 404
    *         error if the request referenced by the reversal cannot be found.
    */
   public Response handle(BasicReversal reversal) {
      try {
         // The UUID of this request
         String reversalUuid = reversal.getId();
         // The UUID identifying the request that this reversal relates to
         String redemptionUuid = reversal.getRequestId();
         ValidationResponse validationRsp;
         this.reversal = reversal;

         VoucherModelUtils.validateUuid(reversalUuid);
         VoucherModelUtils.validateUuid(redemptionUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(reversal.getThirdPartyIdentifiers());

         RedemptionResponse redemptionRsp =
               SUVTestServerRunner.getTestServer()
                     .getRecordStorageManager()
                     .getRedemptionResponseRecords()
                     .get(new RequestKey(username, password, ResourceType.REDEMPTIONS_RESOURCE, redemptionUuid));

         String voucherCode;
         if (redemptionRsp == null)
            voucherCode = null;
         else
            voucherCode = redemptionRsp.getVoucher().getCode();

         validationRsp =
               RedemptionModelUtils.canReverseRedemption(redemptionUuid, reversalUuid, username, password, voucherCode);
         if (validationRsp.hasErrorResponse()) {
            if (validationRsp.getResponse().getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addRedemptionReversalToCache();
            }
            return validationRsp.getResponse();
         }

         addRedemptionReversalToCache();

         validationRsp.setResponse(Response.accepted((reversal)).build());

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Check for a corresponding redemption request and update the voucher's state if it exists. Add the reversal to the
    * cache.
    */
   private void addRedemptionReversalToCache() {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getRedemptionReversalRecords();
      RequestKey key = new RequestKey(username, password, ResourceType.REDEMPTIONS_RESOURCE, reversal.getRequestId());

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getRedemptionRequestRecords();

      RedemptionRequest redemptionRequest = redemptionRequestRecords.get(key);
      key.setResourceType(ResourceType.REVERSALS_RESOURCE);
      reversalRecords.put(key, reversal);
      if (redemptionRequest != null) {
         confirmedExistingVouchers
               .put(redemptionRequest.getVoucher().getCode(), SUVTestServer.VoucherState.CONFIRMED_PROVISIONED);
      }
   }

   @Override
   protected String getRequestName() {
      return "Redeem Reversal";
   }
}
