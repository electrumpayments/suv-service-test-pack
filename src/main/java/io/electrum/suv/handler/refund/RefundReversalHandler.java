package io.electrum.suv.handler.refund;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.RequestKey.ResourceType;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class RefundReversalHandler extends BaseHandler {
   private BasicReversal reversal;

   public RefundReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   /**
    * Handle the response to a reverseRefund request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#reverseRefund">SUV
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
         String refundUuid = reversal.getRequestId();
         ValidationResponse validationRsp;
         this.reversal = reversal;

         VoucherModelUtils.validateUuid(reversalUuid);
         VoucherModelUtils.validateUuid(refundUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(reversal.getThirdPartyIdentifiers());

         RefundResponse refundRsp =
               SUVTestServerRunner.getTestServer()
                     .getBackend()
                     .getRefundResponseRecords()
                     .get(new RequestKey(username, password, ResourceType.REFUNDS_RESOURCE, refundUuid));

         String voucherCode;
         if (refundRsp == null)
            voucherCode = null;
         else
            voucherCode = refundRsp.getVoucher().getCode();

         validationRsp = VoucherModelUtils.canReverseRefund(refundUuid, reversalUuid, username, password, voucherCode);
         if (validationRsp.hasErrorResponse()) {
            if (validationRsp.getResponse().getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addRefundReversalToCache();
            }
            return validationRsp.getResponse();
         }

         addRefundReversalToCache();

         validationRsp.setResponse(Response.accepted((reversal)).build());

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Check for a corresponding refund request and update the voucher's state if it exists. Add the reversal to the
    * cache.
    */
   private void addRefundReversalToCache() {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRefundReversalRecords();
      RequestKey key = new RequestKey(username, password, ResourceType.REFUNDS_RESOURCE, reversal.getRequestId());

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRefundRequestRecords();

      RefundRequest refundRequest = refundRequestRecords.get(key);
      key.setResourceType(ResourceType.REVERSALS_RESOURCE);
      reversalRecords.put(key, reversal);
      if (refundRequest != null) {
         confirmedExistingVouchers
               .put(refundRequest.getVoucher().getCode(), SUVTestServer.VoucherState.CONFIRMED_REDEEMED);
      }
   }

   @Override
   protected String getRequestName() {
      return "Refund Reversal";
   }
}
