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
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class RedeemReversalHandler extends BaseHandler {

   public RedeemReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicReversal reversal) {
      try {
         ValidationResponse validatioRsp;

         // The UUID of this request
         String reversalUuid = reversal.getId();
         // The UUID identifying the request that this reversal relates to
         String redemptionUuid = reversal.getRequestId();

         VoucherModelUtils.validateUuid(reversalUuid);
         VoucherModelUtils.validateUuid(redemptionUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(reversal.getThirdPartyIdentifiers());

         RedemptionResponse redemptionRsp =
               SUVTestServerRunner.getTestServer()
                     .getRedemptionResponseRecords()
                     .get(new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, redemptionUuid));

         String voucherCode;
         if (redemptionRsp == null)
            voucherCode = null;
         else
            voucherCode = redemptionRsp.getVoucher().getCode();

         // TODO check this in airtime
         validatioRsp = VoucherModelUtils.canReverseRedemption(redemptionUuid, reversalUuid, username, password, voucherCode);
         if (validatioRsp.hasErrorResponse()) {
            if (validatioRsp.getResponse().getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addRedemptionReversalToCache(reversal);
            }
            return validatioRsp.getResponse();
         }

         addRedemptionReversalToCache(reversal);

         validatioRsp.setResponse(Response.accepted((reversal)).build()); // TODO Ask Casey if this is ok

         return validatioRsp.getResponse();
      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Must check for a corresponding redemption request to get voucher from so that vouchers state may be updated. If no
    * corresponding redemption exists, that voucher must still exist in provision_confirmed state (this is fine) //TODO
    * Docs
    */
   private void addRedemptionReversalToCache(BasicReversal basicReversal) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getRedemptionReversalRecords();
      RequestKey key =
            new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, basicReversal.getRequestId());

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords =
            SUVTestServerRunner.getTestServer().getRedemptionRequestRecords();

      RedemptionRequest redemptionRequest = redemptionRequestRecords.get(key);
      key.setResourceType(RequestKey.REVERSALS_RESOURCE);
      reversalRecords.put(key, basicReversal);
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
