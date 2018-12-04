package io.electrum.suv.handler.voucher;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.TenderAdvice;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

import static io.electrum.suv.resource.impl.SUVTestServer.VoucherState.*;

public class VoucherConfirmationHandler extends BaseHandler {
   /** The UUID of this request */
   private String confirmationUuid;
   /** The UUID identifying the request that this confirmation relates to */
   private String voucherId;

   public VoucherConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(TenderAdvice confirmation, UriInfo uriInfo) {
      try {
         Response rsp;

         // TODO Should we enforce requirements from the docs (TenderAdvice)
         // ArrayList<String> tempErrorList = new ArrayList<>();
         // tempErrorList.add("tenders may not be null");
         // if (confirmation.getTenders() == null)
         // return Response.status(Response.Status.BAD_REQUEST)
         // .entity(SUVModelUtils.buildFormatErrorRsp(tempErrorList))
         // .build();

         confirmationUuid = confirmation.getId();
         voucherId = confirmation.getRequestId();
         // TODO !!!Either remove validation or write postman test for confirmation!!!
         if (!VoucherModelUtils.isValidUuid(confirmationUuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  confirmationUuid,
                  null, // TODO Could overload method
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         } else if (!VoucherModelUtils.isValidUuid(voucherId)) {
            return VoucherModelUtils
                  .buildInvalidUuidErrorResponse(voucherId, null, username, ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         rsp = VoucherModelUtils.canConfirmVoucher(voucherId, confirmationUuid, username, password);
         if (rsp != null) {
            return rsp;
         }

         addVoucherConfirmationToCache(confirmation);

         rsp = Response.accepted((confirmation)).build(); // TODO Ask Casey if this is ok

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the voucher confirmation request to the cache and stores an entry to the voucher in the list of existing
    * vouchers //TODO documentation
    */
   private void addVoucherConfirmationToCache(TenderAdvice confirmation) {
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getVoucherConfirmationRecords();
      // ConcurrentHashMap<String, RequestKey> voucherCodeRequestKeyRecords =
      // SUVTestServerRunner.getTestServer().getVoucherCodeRequestKeyConfirmationRecords();
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      ProvisionResponse provisionRsp =
            SUVTestServerRunner.getTestServer()
                  .getVoucherResponseRecords()
                  .get(new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId));

      String voucherCode = provisionRsp.getVoucher().getCode();

      RequestKey confirmationsKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId);
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);
      // voucherCodeRequestKeyRecords.put(voucherCode, confirmationsKey);
      confirmedExistingVouchers.put(voucherCode, VoucherState.CONFIRMED_PROVISIONED);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Confirmation";
   }
}
