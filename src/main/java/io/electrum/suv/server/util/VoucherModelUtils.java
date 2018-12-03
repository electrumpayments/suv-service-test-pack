package io.electrum.suv.server.util;

import io.electrum.suv.api.models.*;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.TenderAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class VoucherModelUtils extends SUVModelUtils {
   static Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

   // TODO confirm correct function
   // TODO documentation

   /**
    * Determine whether a given {@link Voucher voucher} provision request is able to be completed.
    * 
    * Ensures voucher is not already provisioned. No need to check if redeemed, reversed or confirmed as those actions
    * cannot occur on an unprovisioned voucher.
    * 
    * 
    * @param voucherId
    *           the unique identifier of this voucher
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return A 400 error response indicating the voucher could not be provisioned, null if able to provision.
    */
   public static Response canProvisionVoucher(String voucherId, String username, String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      // TODO Could make requestKey part of method args
      RequestKey requestKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
      ProvisionRequest originalRequest = provisionRecords.get(requestKey);

      // If Voucher already provisioned
      if (originalRequest != null) {
         ErrorDetail errorDetail = buildDuplicateErrorDetail(voucherId, null, originalRequest);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         // detailMessage.setProduct(originalRequest.getProduct()); TODO This doesn't exist...can ignore?

         // Check for a response for this request
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         // If a response is found, add it to the detailMessage
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build(); // Bad Request
      }

      //TODO I don't believe this can be tested for in PostMan
      // If voucher reverssal request already received
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     voucherId,
                     "Voucher reversed.",
                     "Voucher reversal with String already processed with the associated fields.",
                     reversal.getId(),
                     ErrorDetail.ErrorType.GENERAL_ERROR); // TODO Pick a better ErrorType

         // TODO what is this here for
         // Check for a response for this request, if found add to detailMessage
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }

      return null; // Voucher can be provisioned
   }

   /**
    * Determine whether a given {@link Voucher voucher} reversal request can be completed.
    *
    * Ensures voucher is already provisioned and is not yet confirmed.
    * 
    * @param voucherId
    *           the unique identifier of the voucher to be reversed
    * @param reversalId
    *           the unique identifier of this request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return A 404 Error response if a voucher corresponding to voucherId cannot be found (not provisioned), a 400
    *         Error if the voucher is already confirmed. Null if voucher can be reversed.
    */
   public static Response canReverseVoucher(String voucherId, String reversalId, String username, String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(reversalId).originalId(voucherId);

      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                           .voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      } // TODO extract this to confirmVoucherRedeemed() returns response code or null

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords = testServer.getVoucherConfirmationRecords();
      RequestKey requestKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId);
      TenderAdvice confirmation = confirmationRecords.get(requestKey);
      if (confirmation != null) {
         errorDetail.errorType(ErrorDetail.ErrorType.VOUCHER_ALREADY_CONFIRMED)
               .errorMessage("Voucher confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(voucherId));
         return Response.status(400).entity(errorDetail).build();
      }

      // TODO Check that it is not redeemed? Probably not needed
      return null;
   }

   public static Response canConfirmVoucher(
         String voucherId,
         String confirmationUuid,
         String username,
         String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(voucherId);

      // TODO Extract method
      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                           .voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         errorDetail.errorType(ErrorDetail.ErrorType.VOUCHER_ALREADY_REVERSED)
               .errorMessage("Voucher reversed.")
               .detailMessage(
                     new DetailMessage()
                           .freeString("Voucher provision has already been reversed with the associated details.")
                           .reversalId(reversal.getId()));
         // TODO Pick a better ErrorType

         // TODO what is this here for
         // Check for a response for this request, if found add to detailMessage
         // DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         // ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         // ProvisionResponse rsp = responseRecords.get(requestKey);
         // if (rsp != null) {
         // detailMessage.setVoucher(rsp.getVoucher());
         // }
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   // TODO Ensure method is correct

   /**
    * Builds a 400 error response indicating the BasicAuth username is inconsistent with the username in the body of the
    * request.
    */
   public static Response buildIncorrectUsernameErrorResponse(
         String objectId,
         Institution client,
         String username,
         ErrorDetail.ErrorType requestType) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Incorrect username",
                  "The HTTP Basic Authentication username (" + username
                        + ") is not the same as the value in the Client.Id field (" + client.getId() + ").",
                  null,

                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

   /** Build a 400 error response indicating the UUID format is invalid. Provides details of expected format. */
   public static Response buildInvalidUuidErrorResponse(
         String objectId,
         Institution client,
         String username, // TODO remove redundant param
         ErrorDetail.ErrorType requestType) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Invalid UUID",
                  "The UUID in the request body is not a vail UUID format."
                        + "\nUUID must conform to the format 8-4-4-4-12 hexedecimal values."
                        + "\nExample: 58D5E212-165B-4CA0-909B-C86B9CEE0111",
                  null,

                  requestType);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

   // TODO remove redundant method
   public static ErrorDetail buildInconsistentIdErrorDetail(String pathId, String objectId, String originalMsgId
   /* ErrorDetail.RequestType requestType */) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "String inconsistent",
                  "The ID path parameter is not the same as the object's ID.",
                  originalMsgId,
                  // requestType,
                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setPathId(pathId);

      return errorDetail;
   }

   // TODO Ensure method actually does what it should do
   /** Creates a corresponding ProvisionResponse from a ProvisionRequest */
   public static ProvisionResponse voucherRspFromReq(ProvisionRequest req) throws IOException {
      ProvisionResponse voucherResponse =
            JsonUtil.deserialize(JsonUtil.serialize(req, ProvisionRequest.class), ProvisionResponse.class);

      updateWithRandomizedIdentifiers(voucherResponse);
      voucherResponse.setVoucher(createRandomizedVoucher());
      voucherResponse.setSlipData(createRandomizedSlipData());
      // voucherResponse.setResponseProduct(req.getProduct().name("TalkALot").type(Product.ProductType.AIRTIME_FIXED));
      return voucherResponse;
   }

   /**
    * Determine whether a voucher had been provisioned or not.
    * 
    * @param voucherId
    *           the uuid of the voucher to be returned
    * @param provisionRecords
    *           to be searched
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return whether voucher with this UUID has been provisioned.
    */
   public static boolean isVoucherProvisioned( // TODO Refactor naming
         String voucherId,
         ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
      log.debug(String.format("Searching for provision record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }

   public static Response canRefundVoucher(String uuid, String username, String password) {
      return null;
   }
}
