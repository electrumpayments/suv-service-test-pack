package io.electrum.suv.handler.redeem;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.RequestKey.ResourceType;
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

   /**
    * Handle the response to a redeemVoucher request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#provisionvoucher">SUV
    * Interface docs</a> for details.
    *
    * @param redemptionRequest
    *           from request body
    * @param uriInfo
    * @return a {@link ProvisionResponse} for this transaction or a 400 Error if there is a format error or the voucher
    *         is already redeemed or reversed.
    */
   public Response handle(RedemptionRequest redemptionRequest, UriInfo uriInfo) {
      try {
         ValidationResponse validationRsp;

         String uuid = redemptionRequest.getId();
         VoucherModelUtils.validateUuid(uuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(redemptionRequest.getThirdPartyIdentifiers());

         validationRsp = validateClientIdUsernameMatch(redemptionRequest);
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
         RedemptionResponse redemptionRsp = VoucherModelUtils.redemptionRspFromReq(redemptionRequest);
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
    * Check for a corresponding redemption request and update the voucher's state if it exists. Add the redemption to
    * the cache.
    * 
    * @param key
    *           the unique key of this response, the same the as the corresponding redemption request
    * @param redemptionRsp
    *           the {@link RedemptionResponse} for this request
    */
   private void addRedemptionResponseToCache(RequestKey key, RedemptionResponse redemptionRsp) {
      ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRedemptionResponseRecords();
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRedemptionReversalRecords();

      BasicReversal basicReversal = reversalRecords.get(key);
      responseRecords.put(key, redemptionRsp);
      if (basicReversal == null) {
         confirmedExistingVouchers.put(redemptionRsp.getVoucher().getCode(), VoucherState.REDEEMED);
      }
   }

   /**
    * Adds the redemption request to the RedemptionRequestRecords
    *
    * @param requestUuid
    *           the unique identifier for this request
    * @param request
    *           the redemption request to be recorded
    * @return the corresponding {@link RequestKey} for this entry.
    */
   private RequestKey addRedemptionRequestToCache(String requestUuid, RedemptionRequest request) {
      RequestKey key = new RequestKey(username, password, ResourceType.REDEMPTIONS_RESOURCE, requestUuid);
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRecords =
            SUVTestServerRunner.getTestServer().getBackend().getRedemptionRequestRecords();

      redemptionRecords.put(key, request);

      return key;
   }
}
