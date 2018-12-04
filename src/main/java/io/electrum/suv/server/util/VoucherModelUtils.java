package io.electrum.suv.server.util;

import io.electrum.suv.api.models.*;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
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
    * Ensures voucher is not already provisioned or no reversal request has been received. No need to check if redeemed
    * or confirmed as those actions cannot occur on an unprovisioned voucher.
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

      // TODO I don't believe this can be tested for in PostMan
      // If voucher reversal request already received
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

         // TODO what is this here for, really don't see it being !null
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
    * @return A 404 Error response if a voucher corresponding to voucherUuid cannot be found (not provisioned), a 400
    *         Error if the voucher is already confirmed. Null if voucher can be reversed.
    */
   public static Response canReverseVoucher(String voucherUuid, String reversalUuid, String username, String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(voucherUuid);

      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      if (!isVoucherProvisioned(voucherUuid, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherUuid.")
                           .voucherId(voucherUuid));
         return Response.status(404).entity(errorDetail).build();
      } // TODO extract this to confirmVoucherRedeemed() returns response code or null

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords = testServer.getVoucherConfirmationRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherUuid);
      TenderAdvice confirmation = confirmationRecords.get(requestKey);
      if (confirmation != null) {
         errorDetail.errorType(ErrorDetail.ErrorType.VOUCHER_ALREADY_CONFIRMED)
               .errorMessage("Voucher confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(voucherUuid));
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static Response canConfirmVoucher(
         String voucherUuid,
         String confirmationUuid,
         String username,
         String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(voucherUuid);

      // TODO Extract method
      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher provisioned
      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      if (!isVoucherProvisioned(voucherUuid, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No voucher req.")
               .detailMessage(
                     new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                           .voucherId(voucherUuid));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherUuid);
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
    *           the uuid of the request to be checked
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

   /**
    * Determine whether a given {@link Voucher voucher} redemption request is able to be completed.
    * 
    * Ensures voucher is not already redeemed and has been confirmed.
    * 
    * 
    * @param voucherCode
    *           the unique identifier of this voucher
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @param requestUuid
    *           the unique identifier of this request
    * @return A 400 error response indicating the voucher could not be redeemed, null if able to redeem.
    */
   public static Response canRedeemVoucher(String voucherCode, String username, String password, String requestUuid) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ConcurrentHashMap<RequestKey, RedemptionRequest> requestRecords = testServer.getRedemptionRequestRecords();
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      // If voucher not yet confirmed
      // Use the mapping from the voucher code to a request key (which corresponds to a confirmationsRecord)
      // ConcurrentHashMap<String, RequestKey> voucherCodeRequestKey =
      // testServer.getVoucherCodeRequestKeyConfirmationRecords();
      // ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords = testServer.getVoucherConfirmationRecords();
      RequestKey confirmationKey = new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, requestUuid);
      if (confirmedExistingVouchers.get(voucherCode) == null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     requestUuid,
                     "Voucher not confirmed.",
                     String.format(
                           "Voucher confirmation for Voucher Code:%s has not been processed. Cannot redeem unconfirmed vouchers.",
                           voucherCode),
                     null,
                     ErrorDetail.ErrorType.VOUCHER_NOT_REDEEMABLE);
         return Response.status(400).entity(errorDetail).build();
      }

      // If no redemptionRequests have been recorded, the voucher cannot fail the test of having already been redeemed.
      if (requestRecords.size() != 0) {
         // TODO Could make requestKey part of method args
         // final ConcurrentHashMap<String, RequestKey> voucherCodeRequestKeyRedemptionRecords =
         // SUVTestServerRunner.getTestServer().getVoucherCodeRequestKeyRedemptionRecords();

         RequestKey requestKey = new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, requestUuid);
         // RedemptionRequest originalRequest = requestRecords.get(requestKey);

         // If Voucher already redeemed
         if (confirmedExistingVouchers.get(voucherCode).getValue() >= VoucherState.REDEEMED.getValue()) {
            ErrorDetail errorDetail =
                  buildErrorDetail(
                        requestUuid,
                        "Duplicate Redemption Requests",
                        "A Redemption Request for that voucher has already been received.",
                        null, // TODO Reintroduce mapping to retrieve this
                        ErrorDetail.ErrorType.VOUCHER_ALREADY_REDEEMED);

            DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();

            // Check for a response for this request
            ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
                  testServer.getRedemptionResponseRecords();
            RedemptionResponse rsp = responseRecords.get(requestKey);
            if (rsp != null) {
               detailMessage.setVoucher(rsp.getVoucher()); // TODO confirm this is !null
            }
            return Response.status(400).entity(errorDetail).build();
         }
      }

      return null;
   }

   public static RedemptionResponse redemptionRspFromReq(RedemptionRequest redemptionRequest) throws IOException {
      RedemptionResponse redemptionResponse =
            JsonUtil.deserialize(
                  JsonUtil.serialize(redemptionRequest, RedemptionRequest.class),
                  RedemptionResponse.class);

      updateWithRandomizedIdentifiers(redemptionResponse);
      redemptionResponse.setVoucher(redemptionRequest.getVoucher());
      redemptionResponse.setSlipData(createRandomizedSlipData());
      // redemptionResponse.setResponseProduct(req.getProduct().name("TalkALot").type(Product.ProductType.AIRTIME_FIXED));
      return redemptionResponse;
   }

   private static boolean canRedeemVoucher() {
      return false;
   }

   public static Response canRefundVoucher(String voucherId, String username, String password) {
      // todo implement method
      return null;
   }

   /**
    * Determine whether the corresponding {@link RedemptionRequest} can be reversed.
    *
    * Ensures the corresponding voucher has been redeemed and the redemption is not yet confirmed. The redemption
    * request referred to by the reversal must have been received and a redemption confirmation request must not have
    * been received yet. have been received
    * 
    * @param redemptionUuid
    *           the unique identifier of the redemptionRequest to be reversed
    * @param reversalUuid
    *           the unique identifier of this request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return A 404 Error response if a voucher corresponding to redemptionUuid cannot be found (not redeemed), a 400
    *         Error if the voucher redemtion is already confirmed. Null if redemption can be reversed.
    */
   public static Response canReverseRedemption(
         String redemptionUuid,
         String reversalUuid,
         String username,
         String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(redemptionUuid);

      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher redeemed
      ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords =
            testServer.getRedemptionRequestRecords();
      if (!isVoucherRedeemed(redemptionUuid, redemptionRequestRecords, username, password)) {
         errorDetail.errorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No redemption req.")
               .detailMessage(
                     new DetailMessage().freeString("No RedemptionRequest located for given redemptionUuid.")
                           .voucherId(redemptionUuid));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords = testServer.getRedemptionConfirmationRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, redemptionUuid);
      BasicAdvice confirmation = confirmationRecords.get(requestKey);

      if (confirmation != null) {
         errorDetail.errorType(ErrorDetail.ErrorType.VOUCHER_ALREADY_CONFIRMED)
               .errorMessage("Voucher confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(redemptionUuid)
                           .reversalId(reversalUuid));
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   /**
    * Determine whether a voucher had been redeemed or not.
    *
    * @param redemptionRequestUuid
    *           the uuid of the request to be checked
    * @param provisionRecords
    *           to be searched
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @return whether voucher with this UUID has been provisioned.
    */
   public static boolean isVoucherRedeemed( // TODO Refactor naming
         String redemptionRequestUuid,
         ConcurrentHashMap<RequestKey, RedemptionRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey =
            new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, redemptionRequestUuid);
      log.debug(
            String.format("Searching for redemptionRequest record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }
}
