package io.electrum.suv.handler.refund;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.util.RefundModelUtils;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;

public class RefundVoucherHandler extends BaseHandler {
   private String refundUuid;
   private String voucherCode;

   public RefundVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(RefundRequest refundRequest, UriInfo uriInfo) {
      try {
         Response rsp;
         refundUuid = refundRequest.getId();
         voucherCode = refundRequest.getVoucher().getCode();

         // Validate uuid format in code until it can be ported to hibernate in the interface
         VoucherModelUtils.validateUuid(refundUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(refundRequest.getThirdPartyIdentifiers());

         // Confirm that the basicAuth ID matches clientID in message body
         if (!refundRequest.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                  refundUuid,
                  refundRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);
         }

         // Confirm voucher not already provisioned or reversed.
         rsp = VoucherModelUtils.canRefundVoucher(refundUuid, username, password, voucherCode);
         if (rsp != null) {
            return rsp;
         }

         // The voucher can be Refunded
         RequestKey key = addRefundRequestToCache(refundUuid, refundRequest);

         // TODO See Giftcard, should this all be done differently
         RefundResponse refundRsp = RefundModelUtils.refundRspFromReq(refundRequest);
         addRefundResponseToCache(key, refundRsp);
         rsp = Response.created(uriInfo.getRequestUri()).entity(refundRsp).build();
         return rsp;
      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addRefundResponseToCache(RequestKey key, RefundResponse request) {
      ConcurrentHashMap<RequestKey, RefundResponse> refundResponseRecords =
            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      refundResponseRecords.put(key, request);
      confirmedExistingVouchers.put(voucherCode, SUVTestServer.VoucherState.REFUNDED);

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
