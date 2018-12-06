package io.electrum.suv.handler.refund;

import io.electrum.suv.api.models.*;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class RefundReversalHandler extends BaseHandler {
   public RefundReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   private String voucherCode;

   public Response handle(BasicReversal reversal, UriInfo uriInfo) {
      try {
         Response rsp;

         // The UUID of this request
         String reversalUuid = reversal.getId();
         // The UUID identifying the request that this reversal relates to
         String refundUuid = reversal.getRequestId();

         if (!VoucherModelUtils.validateUuid(reversalUuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                    reversalUuid,
                    null, // TODO Could overload method
                    username,
                    ErrorDetail.ErrorType.FORMAT_ERROR);
         } else if (!VoucherModelUtils.validateUuid(refundUuid)) {
            return VoucherModelUtils
                    .buildInvalidUuidErrorResponse(refundUuid, null, username, ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         RefundResponse refundRsp =
                 SUVTestServerRunner.getTestServer()
                         .getRefundResponseRecords()
                         .get(new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundUuid));

         if (refundRsp == null)
            voucherCode = null;
         else
            voucherCode = refundRsp.getVoucher().getCode();

         // TODO check this in airtime
         rsp = VoucherModelUtils.canReverseRefund(refundUuid, reversalUuid, username, password, voucherCode);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addRefundReversalToCache(reversal);
            }
            return rsp;
         }

         addRefundReversalToCache(reversal);

         rsp = Response.accepted((reversal)).build(); // TODO Ask Casey if this is ok

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addRefundReversalToCache(BasicReversal basicReversal) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
              SUVTestServerRunner.getTestServer().getRefundReversalRecords();
      RequestKey key =
              new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, basicReversal.getRequestId());

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
              SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
              SUVTestServerRunner.getTestServer().getRefundRequestRecords();

      RefundRequest refundRequest = refundRequestRecords.get(key);
      key.setResourceType(RequestKey.REVERSALS_RESOURCE);
      reversalRecords.put(key, basicReversal);
      if (refundRequest != null) {
         confirmedExistingVouchers.put(refundRequest.getVoucher().getCode(), SUVTestServer.VoucherState.CONFIRMED_REDEEMED  );
      }
   }

   @Override
   protected String getRequestName() {
      return "Refund Reversal";
   }
}
