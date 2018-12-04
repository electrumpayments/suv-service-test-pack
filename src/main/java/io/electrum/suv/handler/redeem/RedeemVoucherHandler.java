package io.electrum.suv.handler.redeem;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class RedeemVoucherHandler extends BaseHandler {
   public RedeemVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   @Override
   protected String getRequestName() {
      return "Redeem Voucher";
   }

   public Response handle(RedemptionRequest redemptionRequest, UriInfo uriInfo) {
      try {
         Response rsp = null;

         String uuid = redemptionRequest.getId();
         if (!VoucherModelUtils.isValidUuid(uuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  uuid,
                  redemptionRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         } else

         // Confirm that the basicAuth ID matches clientID in message body
         if (!redemptionRequest.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                  uuid,
                  redemptionRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);
         }

         String voucherCode = redemptionRequest.getVoucher().getCode();
         // Confirm voucher not already provisioned or reversed.
         rsp = VoucherModelUtils.canRedeemVoucher(voucherCode, username, password, uuid);
         if (rsp != null) {
            return rsp;
         }

         // The voucher can be redeemed and stored.
         RequestKey key = addRedemptionRequestToCache(uuid, redemptionRequest);
         // TODO See Giftcard, should this all be done differently
         RedemptionResponse redemptionRsp = VoucherModelUtils.redemptionRspFromReq(redemptionRequest); // TODO could
                                                                                                       // change this to
                                                                                                       // user
                                                                                                       // voucherCode...?
         addRedemptionResponseToCache(key, redemptionRsp);
         rsp = Response.created(uriInfo.getRequestUri()).entity(redemptionRsp).build();

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }

   }

   private void addRedemptionResponseToCache(RequestKey key, RedemptionResponse redemptionRsp) {
      ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getRedemptionResponseRecords();
      responseRecords.put(key, redemptionRsp);
   }

   // TODO generalise addVoucherToCache(String uuid, Transaction request, ConcurrentHashMap<Object,Object> records)?
   private RequestKey addRedemptionRequestToCache(String requestUuid, RedemptionRequest request) {
      RequestKey key = new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, requestUuid);
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRecords =
            SUVTestServerRunner.getTestServer().getRedemptionRequestRecords();
      ConcurrentHashMap<String, RequestKey> voucherCodeRequestKeyRedemptionRecords =
            SUVTestServerRunner.getTestServer().getVoucherCodeRequestKeyRedemptionRecords();

      String voucherCode = request.getVoucher().getCode();

      redemptionRecords.put(key, request);
      voucherCodeRequestKeyRedemptionRecords.put(voucherCode, key);

      return key;
   }
}
