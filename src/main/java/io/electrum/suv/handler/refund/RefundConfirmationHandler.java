package io.electrum.suv.handler.refund;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicAdvice;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class RefundConfirmationHandler extends BaseHandler {
   private String voucherCode;

   public RefundConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicAdvice confirmation, UriInfo uriInfo) {
      try {
         Response rsp;

         // THe UUID of this request
         String confirmationUuid = confirmation.getId();
         // The UUID identifying the request that this confirmation relates to
         String refundUuid = confirmation.getRequestId();

         // Validate uuid format in code until it can be ported to hibernate in the interface
         if (!VoucherModelUtils.validateUuid(confirmationUuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  confirmationUuid,
                  null, // TODO Could overload method
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         } else if (!VoucherModelUtils.validateUuid(refundUuid)) {
            return VoucherModelUtils
                  .buildInvalidUuidErrorResponse(refundUuid, null, username, ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         // Check that there is actually a corresponding refund request
         RefundResponse refundRsp =
               SUVTestServerRunner.getTestServer()
                     .getRefundResponseRecords()
                     .get(new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundUuid));

         if (refundRsp == null)
            voucherCode = null;
         else
            voucherCode = refundRsp.getVoucher().getCode();

         rsp = VoucherModelUtils.canConfirmRefund(refundUuid, confirmationUuid, username, password, voucherCode);
         if (rsp != null) {
            return rsp;
         }

         addRefundConfirmationToCache(confirmation);

         rsp = Response.accepted((confirmation)).build(); // TODO Ask Casey if this is ok

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addRefundConfirmationToCache(BasicAdvice confirmation) {
      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getRefundConfirmationRecords();

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      RequestKey confirmationsKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, confirmation.getRequestId());
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);

      // Reset the voucher state to before it was redeemed
      confirmedExistingVouchers.put(voucherCode, SUVTestServer.VoucherState.CONFIRMED_PROVISIONED);
   }

   @Override
   protected String getRequestName() {
      return "Refund Confirmation";
   }
}
