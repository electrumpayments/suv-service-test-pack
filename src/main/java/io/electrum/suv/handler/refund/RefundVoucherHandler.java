package io.electrum.suv.handler.refund;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RefundModelUtils;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;

public class RefundVoucherHandler extends BaseHandler {
   private String voucherCode;

   public RefundVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   /**
    * Handle the response to a provisionVoucher request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#provisionvoucher">SUV
    * Interface docs</a> for details.
    *
    * @param refundRequest
    *           from request body
    * @param uriInfo
    * @return a {@link RefundResponse} for this transaction or a 400 Error if there is a format error or the voucher is
    *         already redeemed or reversed.
    */
   public Response handle(RefundRequest refundRequest, UriInfo uriInfo) {
      try {
         ValidationResponse validationRsp;
         String refundUuid = refundRequest.getId();
         voucherCode = refundRequest.getVoucher().getCode();

         // Validate uuid format in code until it can be ported to hibernate in the interface
         VoucherModelUtils.validateUuid(refundUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(refundRequest.getThirdPartyIdentifiers());

         // Confirm that the basicAuth ID matches clientID in message body
         validationRsp = validateClientIdUsernameMatch(refundRequest);
         if (validationRsp.hasErrorResponse())
            return validationRsp.getResponse();

         // Confirm voucher not already provisioned or reversed.
         validationRsp = VoucherModelUtils.canRefundVoucher(refundUuid, username, password, voucherCode);
         if (validationRsp.hasErrorResponse()) {
            return validationRsp.getResponse();
         }

         // The voucher can be Refunded
         RequestKey key = addRefundRequestToCache(refundUuid, refundRequest);

         RefundResponse refundRsp = RefundModelUtils.refundRspFromReq(refundRequest);
         addRefundResponseToCache(key, refundRsp);
         validationRsp.setResponse(Response.created(uriInfo.getRequestUri()).entity(refundRsp).build());

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the refund response to the RefundResponseRecords
    *
    * @param key
    *           The unique key of this response, the same key as the corresponding RefundRequest
    * @param refundResponse
    *           The {@link RefundResponse} for this refundResponse
    */
   private void addRefundResponseToCache(RequestKey key, RefundResponse refundResponse) {
      ConcurrentHashMap<RequestKey, RefundResponse> refundResponseRecords =
            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      refundResponseRecords.put(key, refundResponse);
      confirmedExistingVouchers.put(voucherCode, SUVTestServer.VoucherState.REFUNDED);

   }

   /**
    * Adds the voucher refund request to the RefundRequestRecords
    *
    * @param voucherId
    *           the unique identifier for this request
    * @param request
    *           the refund request to be recorded
    * @return the corresponding {@link RequestKey} for this entry.
    */
   private RequestKey addRefundRequestToCache(String voucherId, RefundRequest request) {
      RequestKey key = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, voucherId);

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
