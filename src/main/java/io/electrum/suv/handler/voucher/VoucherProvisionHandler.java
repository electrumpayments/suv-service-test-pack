package io.electrum.suv.handler.voucher;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.RequestKey.ResourceType;
import io.electrum.suv.server.util.VoucherModelUtils;

public class VoucherProvisionHandler extends BaseHandler {

   public VoucherProvisionHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Provision";
   }

   /**
    * Handle the response to a provisionVoucher request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#provisionvoucher">SUV
    * Interface docs</a> for details.
    *
    * @param provisionRequest
    *           from request body
    * @param uriInfo
    * @return a {@link ProvisionResponse} for this transaction or a 400 Error if there is a format error or the voucher
    *         is already redeemed or reversed.
    */
   public Response handle(ProvisionRequest provisionRequest, UriInfo uriInfo) {
      try {
         ValidationResponse validationRsp;

         String uuid = provisionRequest.getId();
         VoucherModelUtils.validateUuid(uuid);

         VoucherModelUtils.validateThirdPartyIdTransactionIds(provisionRequest.getThirdPartyIdentifiers());

         // Confirm that the basicAuth ID matches clientID in message body
         validationRsp = validateClientIdUsernameMatch(provisionRequest);
         if (validationRsp.hasErrorResponse())
            return validationRsp.getResponse();

         // Confirm voucher not already provisioned or reversed.
         validationRsp = VoucherModelUtils.canProvisionVoucher(uuid, username, password);
         if (validationRsp.hasErrorResponse())
            return validationRsp.getResponse();

         // The voucher can be provisioned and stored.
         RequestKey key = addVoucherRequestToCache(uuid, provisionRequest);

         ProvisionResponse provisionRsp = VoucherModelUtils.voucherRspFromReq(provisionRequest);
         addVoucherResponseToCache(key, provisionRsp);
         validationRsp.setResponse(Response.created(uriInfo.getRequestUri()).entity(provisionRsp).build());
         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the voucher provision response to the VoucherResponseRecords
    * 
    * @param key
    *           The unique key of this response, the same key as the corresponding VoucherRequest
    * @param provisionRsp
    *           The {@link ProvisionResponse} for this request
    */
   private void addVoucherResponseToCache(RequestKey key, ProvisionResponse provisionRsp) {
      ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getProvisionResponseRecords();
      responseRecords.put(key, provisionRsp);
   }

   /**
    * Adds the voucher provision request to the VoucherProvisionRecords
    *
    * @param voucherId
    *           the unique identifier for this request
    * @param request
    *           the provision request to be recorded
    * @return the corresponding {@link RequestKey} for this entry.
    */
   private RequestKey addVoucherRequestToCache(String voucherId, ProvisionRequest request) {
      RequestKey key = new RequestKey(username, password, ResourceType.VOUCHERS_RESOURCE, voucherId);
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            SUVTestServerRunner.getTestServer().getVoucherProvisionRecords();
      provisionRecords.put(key, request);
      return key;
   }
}
