package io.electrum.suv.handler.redeem;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

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
         ValidationResponse validationRsp = new ValidationResponse(null);

         String uuid = redemptionRequest.getId();
         VoucherModelUtils.validateUuid(uuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(redemptionRequest.getThirdPartyIdentifiers());

         // Confirm that the basicAuth ID matches clientID in message body
         validationRsp = validateClientIdUsernameMatch(redemptionRequest, uuid);
         if (validationRsp.hasErrorResponse())
            return validationRsp.getResponse();

         // Confirm voucher not already provisioned or reversed.
         String voucherCode = redemptionRequest.getVoucher().getCode();
         validationRsp = VoucherModelUtils.canRedeemVoucher(voucherCode, username, password, uuid);
         if (validationRsp.hasErrorResponse()) {
            return validationRsp.getResponse();
         }

         // The voucher can be redeemed and stored.
         RequestKey key = addRedemptionRequestToCache(uuid, redemptionRequest);
         // TODO See Giftcard, should this all be done differently
         RedemptionResponse redemptionRsp = VoucherModelUtils.redemptionRspFromReq(redemptionRequest); // TODO could
                                                                                                       // change this to
                                                                                                       // user
                                                                                                       // voucherCode...?
         addRedemptionResponseToCache(key, redemptionRsp);
         validationRsp.setResponse(Response.created(uriInfo.getRequestUri()).entity(redemptionRsp).build());

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }

   }

   /**
    * Adds the voucher redemption response to the cache and update the entry to the voucher in the list of existing
    * vouchers //TODO documentation
    * 
    * Must check for corresponding ReverseRedemptionRequest before Updating state of voucher so as to maintain validity
    * of state and requests, don't update if it exists.
    */
   private void addRedemptionResponseToCache(RequestKey key, RedemptionResponse redemptionRsp) {
      ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getRedemptionResponseRecords();
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getRedemptionReversalRecords();

      BasicReversal basicReversal = reversalRecords.get(key);
      responseRecords.put(key, redemptionRsp);
      if (basicReversal == null) {
         confirmedExistingVouchers.put(redemptionRsp.getVoucher().getCode(), VoucherState.REDEEMED);
      }
   }

   // TODO generalise addVoucherToCache(String uuid, Transaction request, ConcurrentHashMap<Object,Object> records)?
   private RequestKey addRedemptionRequestToCache(String requestUuid, RedemptionRequest request) {
      RequestKey key = new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, requestUuid);
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRecords =
            SUVTestServerRunner.getTestServer().getRedemptionRequestRecords();

      redemptionRecords.put(key, request);

      return key;
   }
}
