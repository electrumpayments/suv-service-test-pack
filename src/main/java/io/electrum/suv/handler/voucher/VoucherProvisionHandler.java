package io.electrum.suv.handler.voucher;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class VoucherProvisionHandler extends BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(VoucherProvisionHandler.class);

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
         if (!VoucherModelUtils.isUuidConsistent(provisionRequest.getId())) {
            return Response.status(400).entity((buildVoucherRequestErrorResponse(provisionRequest.getId()))).build();
         }

         if (!provisionRequest.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                  provisionRequest.getId(),
                  provisionRequest.getClient(),
                  username,
                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);
         }

         // TODO canProvisionVoucher?
         /*
          * rsp = VoucherModelUtils.canProvisionVoucher(voucherId, username, password); if(rsp!=null){ return rsp; }
          */

         // todo fill method
         RequestKey key = addVoucherRequestToCache(provisionRequest.getId(), provisionRequest);

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
    * Adds the voucher request to the VoucherProvisionRecords
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

   private void addVoucherResponseToCache(RequestKey key, ProvisionResponse provisionRsp) {
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            SUVTestServerRunner.getTestServer().getVoucherProvisionRecords();
      provisionRecords.put(key, request);
      return key;
   }
}
