package io.electrum.suv.handler.voucher;

import io.dropwizard.jersey.validation.JerseyViolationException;
import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.SUVModelUtils;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.Tender;
import io.electrum.vas.model.TenderAdvice;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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

   private void addVoucherConfirmationToCache(TenderAdvice confirmation) {
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getVoucherConfirmationRecords();

      ProvisionResponse provisionRsp =
            SUVTestServerRunner.getTestServer()
                  .getVoucherResponseRecords()
                  .get(new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId));

      String voucherCode = provisionRsp.getVoucher().getCode();

      RequestKey confirmationsKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherCode);
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Confirmation";
   }
}
