package io.electrum.suv.server.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ErrorDetail.ErrorType;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.api.models.Voucher;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey.ResourceType;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;

public class VoucherModelUtils extends SUVModelUtils {
   private static final Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

   /**
    * Determine whether a given {@link Voucher voucher} provision request is able to proceed.
    * 
    * Ensures voucher is not already provisioned or no reversal request has been received. No need to check if redeemed
    * or confirmed as those actions cannot occur on an unprovisioned voucher.
    * 
    * 
    * @param voucherId
    *           the unique identifier of this voucher (The provision request uuid serves as the initial unique value for
    *           a voucher until it is returned to the vendor)
    * @param requestKey
    * @return A {@Link ValidationResponse} set to no error response if the request can continue or a 400 error response
    *         indicating the voucher could not be provisioned.
    */
   public static ValidationResponse canProvisionVoucher(String voucherId, RequestKey requestKey) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            testServer.getRecordStorageManager().getVoucherProvisionRecords();
      ProvisionRequest originalRequest = provisionRecords.get(requestKey);

      // If Voucher already provisioned
      if (originalRequest != null) {
         ErrorDetail errorDetail = buildDuplicateUuidErrorDetail(voucherId, null, originalRequest);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();

         // Check for a response for this request
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords =
               testServer.getRecordStorageManager().getProvisionResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         // If a response is found, add it to the detailMessage
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }

         return new ValidationResponse(Response.status(400).entity(errorDetail).build()); // Bad Request
      }

      // If voucher reversal request already received
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            testServer.getRecordStorageManager().getVoucherReversalRecords();
      requestKey.setResourceType(ResourceType.REVERSALS_RESOURCE);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     voucherId,
                     "Voucher reversed.",
                     "Voucher reversal with UUID already processed with the associated fields.",
                     reversal.getId(),
                     ErrorType.VOUCHER_ALREADY_REVERSED);

         // This occurs in the case that a provisionRequest is received, reversed and then sent again. Not tested for in
         // Postman Tests
         // Check for a response for a request with this uuid, if found add to detailMessage
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords =
               testServer.getRecordStorageManager().getProvisionResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }

      return new ValidationResponse(null); // Voucher can be provisioned
   }

   /**
    * Determine whether a given Voucher Confirmation request can proceed.
    *
    * Checks that there is a corresponding voucher provision request and that the provision request is not reversed.
    *
    * @param voucherUuid
    *           identifies the request to which this confirmation refers
    * @param confirmationUuid
    *           uniquely identifies this confirmation request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return A {@Link ValidationResponse} set to no error response if the confirmation can complete, a 404 response if
    *         a corresponding request is not found, and a 400 response if the provision request has been confirmed.
    */
   public static ValidationResponse canConfirmVoucher(
         String voucherUuid,
         String confirmationUuid,
         String username,
         String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(voucherUuid);
      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            testServer.getRecordStorageManager().getVoucherProvisionRecords();

      if (!isVoucherProvisioned(voucherUuid, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                           .voucherId(voucherUuid));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            testServer.getRecordStorageManager().getVoucherReversalRecords();
      RequestKey requestKey = new RequestKey(username, password, ResourceType.REVERSALS_RESOURCE, voucherUuid);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         errorDetail.errorType(ErrorType.VOUCHER_ALREADY_REVERSED)
               .errorMessage("Voucher reversed.")
               .detailMessage(
                     new DetailMessage()
                           .freeString("Voucher provision has already been reversed with the associated details.")
                           .reversalId(reversal.getId()));

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords =
               testServer.getRecordStorageManager().getProvisionResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }
      return new ValidationResponse(null);
   }

   /**
    * Determine whether the corresponding {@link ProvisionRequest} can be reversed.
    *
    * Ensures the corresponding voucher is already provisioned and is not yet confirmed. The provision request referred
    * to by the reversal must have been received and a provision confirmation request must not have been received yet.
    * 
    * @param voucherUuid
    *           the unique identifier of the voucherRequest to be reversed
    * @param reversalUuid
    *           the unique identifier of this request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return A {@Link ValidationResponse} set to no error response if the request can complete, a 404 Error response if
    *         a voucher corresponding to voucherUuid cannot be found (not provisioned) and a 400 Error if the voucher is
    *         already confirmed.
    */
   public static ValidationResponse canReverseVoucher(
         String voucherUuid,
         String reversalUuid,
         String username,
         String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(voucherUuid);

      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords =
            testServer.getRecordStorageManager().getVoucherProvisionRecords();
      if (!isVoucherProvisioned(voucherUuid, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherUuid.")
                           .voucherId(voucherUuid));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords =
            testServer.getRecordStorageManager().getVoucherConfirmationRecords();
      RequestKey requestKey = new RequestKey(username, password, ResourceType.CONFIRMATIONS_RESOURCE, voucherUuid);
      TenderAdvice confirmation = confirmationRecords.get(requestKey);
      if (confirmation != null) {
         errorDetail.errorType(ErrorType.VOUCHER_ALREADY_CONFIRMED)
               .errorMessage("Voucher confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(voucherUuid));
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }

      return new ValidationResponse(null);
   }

   /** Creates a corresponding ProvisionResponse from a ProvisionRequest. Populates the provision */
   public static ProvisionResponse voucherRspFromReq(ProvisionRequest req) throws IOException {
      ProvisionResponse voucherResponse =
            JsonUtil.deserialize(JsonUtil.serialize(req, ProvisionRequest.class), ProvisionResponse.class);

      updateWithRandomizedIdentifiers(voucherResponse);
      if (req.getVoucher() == null)
         voucherResponse.setVoucher(createRandomizedVoucher());
      voucherResponse.setSlipData(createRandomizedSlipData());
      return voucherResponse;
   }

   /**
    * Determine whether a voucher had been provisioned or not.
    * 
    * @param voucherId
    *           the uuid of the request to be checked
    * @param provisionRecords
    *           to be searched
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return whether voucher with this UUID has been provisioned.
    */
   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
   private static boolean isVoucherProvisioned(
         String voucherId,
         ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey = new RequestKey(username, password, ResourceType.VOUCHERS_RESOURCE, voucherId);
      log.debug(String.format("Searching for provision record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }

}
