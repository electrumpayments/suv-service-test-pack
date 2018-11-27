package io.electrum.suv.handler.voucher;

import io.dropwizard.jersey.validation.JerseyViolationException;
import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class VoucherProvisionHandler extends BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(VoucherProvisionHandler.class);

   @Null
   String uuid;

   public VoucherProvisionHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Provision";
   }

   public Response handle(ProvisionRequest provisionRequest, UriInfo uriInfo) {
      // TODO Remove
      log.info("Called handle on VoucherProvisionHandler");

      try {
         Response rsp;
         // TODO Actually implement this method
         // TODO Validate parameters are consistent and correct
         //TODO Actually vvalidate these
         if (provisionRequest.getId().equals("somethingstupid"))
            throw new JerseyViolationException(null, null);
         uuid = provisionRequest.getId();

         if (!VoucherModelUtils.isUuidConsistent(uuid)) {
            return Response.status(400).entity((buildVoucherRequestErrorResponse(uuid))).build();
         }

         if (!provisionRequest.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                    uuid,
                  provisionRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);
         }

         // Check voucher
         // TODO canProvisionVoucher?
         rsp = VoucherModelUtils.canProvisionVoucher(uuid, username, password);
         if (rsp != null) {
            return rsp;
         }

         // todo fill method
         RequestKey key = addVoucherRequestToCache(uuid, provisionRequest);

         // TODO See Giftcard, should this all be done differently
         ProvisionResponse provisionRsp = VoucherModelUtils.voucherRspFromReq(provisionRequest);

         addVoucherResponseToCache(key, provisionRsp);

         rsp = Response.created(uriInfo.getRequestUri()).entity(provisionRsp).build();

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private ErrorDetail buildVoucherRequestErrorResponse(String voucherId) {
      // TODO Implement method
      ErrorDetail errorDetail = new ErrorDetail();
      return errorDetail;
   }

   // Todo confirm correct function of method
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
      RequestKey key = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            SUVTestServerRunner.getTestServer().getVoucherProvisionRecords();
      provisionRecords.put(key, request);
      return key;
   }

   /**
    * Adds the voucher provision response to the VoucherResponseRecords
    * 
    * @param key
    *           The unique key of this response, the same key as the corresponding VoucherRequest
    * @param provisionRsp
    *           //TODO Fill params
    */
   private void addVoucherResponseToCache(RequestKey key, ProvisionResponse provisionRsp) {
      ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getVoucherResponseRecords();
      responseRecords.put(key, provisionRsp);
   }
}
