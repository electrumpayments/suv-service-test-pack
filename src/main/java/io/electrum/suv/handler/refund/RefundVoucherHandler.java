package io.electrum.suv.handler.refund;

import io.electrum.suv.api.models.*;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RefundModelUtils;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class RefundVoucherHandler extends BaseHandler {
   private String uuid;

   public RefundVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(RefundRequest refundRequest, UriInfo uriInfo) {
      try {
         Response rsp;

         uuid = refundRequest.getId();
         if (!VoucherModelUtils.isValidUuid(uuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  uuid,
                  refundRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         // Confirm that the basicAuth ID matches clientID in message body
         if (!refundRequest.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                  uuid,
                  refundRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);
         }

         // Confirm voucher not already provisioned or reversed.
         rsp = VoucherModelUtils.canRefundVoucher(uuid, username, password);
         if (rsp != null) {
            return rsp;
         }

         // The voucher can be Refunded
         RequestKey key = addRefundRequestToCache(uuid, refundRequest);

         // TODO See Giftcard, should this all be done differently
         RefundResponse refundRsp = RefundModelUtils.refundRspFromReq(refundRequest);
         addRefundResponseToCache(key, refundRsp);
         rsp = Response.created(uriInfo.getRequestUri()).entity(refundRsp).build();
         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addRefundResponseToCache(RequestKey key, RefundResponse request) {
      ConcurrentHashMap<RequestKey, RefundResponse> refundResponseRecords =
            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
      refundResponseRecords.put(key, request);
   }

   private RequestKey addRefundRequestToCache(String voucherId, RefundRequest request) {
      RequestKey key = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, voucherId); // TODO are there
                                                                                                   // other resources?
      ConcurrentHashMap<RequestKey, RefundRequest> voucherRefundRecords =
            SUVTestServerRunner.getTestServer().getRefundRequestRecords();
      voucherRefundRecords.put(key, request);
      return key;
   }

   @Override
   protected String getRequestName() {
      return "Refund Voucher";
   }
}
