package io.electrum.suv.handler.voucher;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class VoucherReversalHandler extends BaseHandler {
   public VoucherReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   private BasicReversal reversal;

   /**
    * Handle the response to a reverseVoucher request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#reverseVoucher">SUV
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
         String voucherId = reversal.getRequestId();
         ValidationResponse validationRsp;
         this.reversal = reversal;

         VoucherModelUtils.validateUuid(reversalUuid);
         VoucherModelUtils.validateUuid(voucherId);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(reversal.getThirdPartyIdentifiers());

         validationRsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalUuid, username, password);
         if (validationRsp.hasErrorResponse()) {
            if (validationRsp.getResponse().getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addVoucherReversalToCache();
            }
            return validationRsp.getResponse();
         }

         addVoucherReversalToCache();

         validationRsp.setResponse(Response.accepted((reversal)).build());

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addVoucherReversalToCache() {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, reversal.getRequestId());
      reversalRecords.put(reversalKey, reversal);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Reversal";
   }
}
