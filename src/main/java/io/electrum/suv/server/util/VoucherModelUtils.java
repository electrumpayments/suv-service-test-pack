package io.electrum.suv.server.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.api.models.*;
import io.electrum.suv.api.models.ErrorDetail.ErrorType;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.TenderAdvice;

public class VoucherModelUtils extends SUVModelUtils {
   private static final Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

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
         ErrorDetail errorDetail = buildDuplicateUuidErrorDetail(voucherId, null, originalRequest);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();

         // Check for a response for this request
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         // If a response is found, add it to the detailMessage
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build(); // Bad Request
      }

      // If voucher reversal request already received
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     voucherId,
                     "Voucher reversed.",
                     "Voucher reversal with UUID already processed with the associated fields.",
                     reversal.getId(),
                     ErrorType.VOUCHER_ALREADY_REVERSED);

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
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
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
         errorDetail.errorType(ErrorType.VOUCHER_ALREADY_CONFIRMED)
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

   /**
    * Determine whether a given Voucher Confirmation request can be completed.
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
    * @return Null if the confirmation can complete, a 404 response if a corresponding request is not found, and a 400
    *         response if the provision request has been reversed.
    */
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
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
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
         errorDetail.errorType(ErrorType.VOUCHER_ALREADY_REVERSED)
               .errorMessage("Voucher reversed.")
               .detailMessage(
                     new DetailMessage()
                           .freeString("Voucher provision has already been reversed with the associated details.")
                           .reversalId(reversal.getId()));

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   // TODO Ensure method is correct

   /**
    * Builds a 400 error response indicating the BasicAuth username is inconsistent with the username in the body of the
    * request.
    */
   public static Response buildIncorrectUsernameErrorResponse(String objectId, Institution client, String username) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Incorrect username",
                  "The HTTP Basic Authentication username (" + username
                        + ") is not the same as the value in the Client.Id field (" + client.getId() + ").",
                  null,

                  ErrorType.AUTHENTICATION_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

// --Commented out by Inspection START (2018/12/07, 09:35):
//   /** Build a 400 error response indicating the UUID format is invalid. Provides details of expected format. */
//   public static Response buildInvalidUuidErrorResponse(String objectId, Institution client, ErrorType requestType) {
//
//      ErrorDetail errorDetail =
//            buildErrorDetail(
//                  objectId,
//                  "Invalid UUID",
//                  "The UUID in the request body is not a vail UUID format."
//                        + "\nUUID must conform to the format 8-4-4-4-12 hexedecimal values."
//                        + "\nExample: 58D5E212-165B-4CA0-909B-C86B9CEE0111",
//                  null,
//
//                  requestType);
//
//      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
//      detailMessage.setClient(client);
//
//      return Response.status(400).entity(errorDetail).build();
//   }
// --Commented out by Inspection STOP (2018/12/07, 09:35)

   // --Commented out by Inspection START (2018/12/06, 18:31):
   // // TODO remove redundant method
   // public static ErrorDetail buildInconsistentIdErrorDetail(String pathId, String objectId, String originalMsgId
   // /* ErrorDetail.RequestType requestType */) {
   //
   // ErrorDetail errorDetail =
   // buildErrorDetail(
   // objectId,
   // "String inconsistent",
   // "The ID path parameter is not the same as the object's ID.",
   // originalMsgId,
   // // requestType,
   // ErrorType.FORMAT_ERROR);
   //
   // DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
   // detailMessage.setPathId(pathId);
   //
   // return errorDetail;
   // }
   // --Commented out by Inspection STOP (2018/12/06, 18:31)

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
   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
   private static boolean isVoucherProvisioned( // TODO Refactor naming
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

      // If voucher provision not yet confirmed
      if (confirmedExistingVouchers.get(voucherCode) == null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     requestUuid,
                     "Voucher not confirmed.",
                     "Voucher confirmation for this voucher has not been processed. Cannot redeem unconfirmed vouchers.",
                     null,
                     ErrorType.VOUCHER_NOT_REDEEMABLE);
         return Response.status(400).entity(errorDetail).build();
      }

      // Confirm reversal request did not arrive before this
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getRedemptionReversalRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, requestUuid);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     requestUuid,
                     "Redemption reversed.",
                     "Redemption reversal with UUID already processed with the associated fields.",
                     reversal.getId(),
                     ErrorType.REDEMPTION_ALREADY_REVERSED);

         // Check for a response for this request, if found add to detailMessage
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, RefundResponse> responseRecords = testServer.getRefundResponseRecords();
         RefundResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }

      // If no redemptionRequests have been recorded, the voucher cannot fail the test of having already been redeemed.
      if (requestRecords.size() != 0) {
         // TODO Could make requestKey part of method args

         RequestKey key = new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, requestUuid);
         // RedemptionRequest originalRequest = requestRecords.get(requestKey);

         // If Voucher already redeemed
         if (confirmedExistingVouchers.get(voucherCode).getValue() >= VoucherState.REDEEMED.getValue()) {
            ErrorDetail errorDetail =
                  buildErrorDetail(
                        requestUuid,
                        "Duplicate Redemption Requests",
                        "A Redemption Request for that voucher has already been received.",
                        null, // TODO Reintroduce mapping to retrieve this
                        ErrorType.VOUCHER_ALREADY_REDEEMED);

            DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();

            // Check for a response for this request
            ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
                  testServer.getRedemptionResponseRecords();
            RedemptionResponse rsp = responseRecords.get(key);
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
      return redemptionResponse;
   }

   /** Make sure the voucher is either in a redemptionConfiromed state */
   public static Response canRefundVoucher(String refundUuid, String username, String password, String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      // Confirm reversal request did not arrive before this
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getRefundReversalRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, refundUuid);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Refund reversed.",
                     "Refund reversal with UUID already processed with the associated fields.",
                     reversal.getId(),
                     ErrorType.REFUND_ALREADY_REVERSED);

         // Check for a response for this request, if found add to detailMessage
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, RefundResponse> responseRecords = testServer.getRefundResponseRecords();
         RefundResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }

      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      ErrorDetail errorDetail;
      switch (confirmedExistingVouchers.get(voucherCode).getValue()) {
      case 0:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher not refundable",
                     "The voucher is currently in a non-refundable state. Only vouchers for which "
                           + "redemption confirmation message have been received may be refunded.",
                     null, // TODO update message
                     ErrorType.VOUCHER_NOT_REDEEMED);
         return Response.status(400).entity(errorDetail).build();

      case 1:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher Redemption unconfirmed",
                     "There is a current redemption request for this voucher pending confirmation. Cannot refund in the current state.",
                     null,
                     ErrorType.REDEMPTION_NOT_CONFIRMED);
         return Response.status(400).entity(errorDetail).build();

      case 2:
         return null;
      case 3:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher refund pending",
                     "There is a current refund request for this voucher pending confirmation. Cannot refund in the current state.",
                     null,
                     ErrorType.VOUCHER_ALREADY_REFUNDED);
         return Response.status(400).entity(errorDetail).build();
      }

      // // If it is not redeemed at least
      // if (confirmedExistingVouchers.get(voucherCode).getValue() != 1) {
      // ErrorDetail errorDetail =
      // buildErrorDetail(
      // refundUuid,
      // "Voucher not refundable",
      // "The voucher is currently in a non-refundable state. Only vouchers for which "
      // + "redemption confirmation message have been received may be refunded.",
      // null,
      // ErrorType.VOUCHER_NOT_REDEEMED);
      // return Response.status(400).entity(errorDetail).build();
      // }

      return null;
   }

   /**
    * Determine whether the corresponding {@link RedemptionRequest} can be reversed.
    *
    * Checks to ensure that the corresponding Redemption Request exists. If it does, the relevant vouchers state is
    * checked to determine whether this reversal can proceed. If the voucher is not currently in a redeemed state, an
    * appropriate error response is returned.
    *
    * 
    * @param redemptionUuid
    *           the unique identifier of the redemptionRequest to be reversed
    * @param reversalUuid
    *           the unique identifier of this request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @param voucherCode
    *           the unique code for the voucher, used to identify it in the cache
    * @return A 404 Error response if the redemption request was not received or the voucher, a 400 Error response if
    *         the voucher redemption is already confirmed. Null if redemption can be reversed.
    */
   public static Response canReverseRedemption(
         String redemptionUuid,
         String reversalUuid,
         String username,
         String password,
         String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();
      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(redemptionUuid);
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Reversal refers does not exist."));
         return Response.status(404).entity(errorDetail).build();
      }

      switch (confirmedExistingVouchers.get(voucherCode)) {
      case REDEEMED:
         return null;
      case CONFIRMED_REDEEMED:
         ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords = testServer.getRedemptionConfirmationRecords();
         RequestKey requestKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, redemptionUuid);
         BasicAdvice confirmation = confirmationRecords.get(requestKey);

         errorDetail.errorType(ErrorType.REDEMPTION_ALREADY_CONFIRMED)
               .errorMessage("Redemption confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Voucher Redemption cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(redemptionUuid)
                           .reversalId(reversalUuid));
         return Response.status(400).entity(errorDetail).build();
      default:
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Reversal refers does not exist."));
         return Response.status(404).entity(errorDetail).build();
      }
   }

   // --Commented out by Inspection START (2018/12/07, 07:28):
   // /**
   // * Determine whether a voucher had been redeemed or not.
   // *
   // * @param redemptionRequestUuid
   // * the uuid of the request to be checked
   // * @param provisionRecords
   // * to be searched
   // * @param username
   // * from BasicAuth
   // * @param password
   // * from BasicAuth
   // * @return whether voucher with this UUID has been provisioned.
   // */
   // public static boolean isVoucherRedeemed( // TODO Refactor naming
   // String redemptionRequestUuid,
   // ConcurrentHashMap<RequestKey, RedemptionRequest> provisionRecords,
   // String username,
   // String password) {
   // RequestKey provisionKey =
   // new RequestKey(username, password, RequestKey.REDEMPTIONS_RESOURCE, redemptionRequestUuid);
   // log.debug(
   // String.format("Searching for redemptionRequest record under following key: %s", provisionKey.toString()));
   // return provisionRecords.get(provisionKey) != null;
   // }
   // --Commented out by Inspection STOP (2018/12/07, 07:28)

   /**
    * Determine whether this Redemption Confirmation can proceed.
    *
    * Requires the Voucher corresponding to the Redemption Request referenced in this Redemption Confirmation to be in a
    * redeemed state. If he voucher is in an unredeemed state or the Redemption Request cannot be found, an appropriate
    * error response will be returned.
    * 
    * @param redemptionUuid
    *           identifies the request to which this confirmation refers
    * @param confirmationUuid
    *           uniquely identifies this confirmation request
    * @param voucherCode
    *           the code for the corresponding voucher. Null indicates that the request referenced by this confirmation
    *           was not found.
    * @return a 404 error response if the referenced redemption request was not found, a 400 error response if the
    *         corresponding voucher is not in a redeemed state, null if it is in a redeemed state and the confirmation can
    *         proceed.
    */
   public static Response canConfirmRedemption(String redemptionUuid, String confirmationUuid, String voucherCode) {

      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(redemptionUuid);

      // TODO Extract method
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Confirmation refers does not exist."));
         return Response.status(404).entity(errorDetail).build();
      }

      if (confirmedExistingVouchers.get(voucherCode) != VoucherState.REDEEMED) {
         errorDetail.errorType(ErrorType.VOUCHER_NOT_REDEEMED)
               .errorMessage("Redemption Confirmation not performed")
               .detailMessage(
                     new DetailMessage().freeString(
                           String.format(
                                 "The voucher referenced in the Redemption Request to which this Redemption Confirmation pertains is not currently in the redeemed state. "
                                       + "The Voucher is currently in a %s state.",
                                 confirmedExistingVouchers.get(voucherCode).name())));

         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not reversed
      // ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      // RequestKey requestKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, redemptionUuid);
      // BasicReversal reversal = reversalRecords.get(requestKey);
      // if()
      // if (reversal != null) {
      // errorDetail.errorType(ErrorDetail.ErrorType.VOUCHER_ALREADY_REVERSED)
      // .errorMessage("Voucher reversed.")
      // .detailMessage(
      // new DetailMessage()
      // .freeString("Voucher provision has already been reversed with the associated details.")
      // .reversalId(reversal.getId()));
      // // TODO Pick a better ErrorType
      //
      // // TODO what is this here for
      // // Check for a response for this request, if found add to detailMessage
      // // DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      // // ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
      // // ProvisionResponse rsp = responseRecords.get(requestKey);
      // // if (rsp != null) {
      // // detailMessage.setVoucher(rsp.getVoucher());
      // // }
      // return Response.status(400).entity(errorDetail).build();
      // }
      return null;
   }

   /**
    * Determine whether this Refund Confirmation can proceed.
    *
    * Requires the Voucher corresponding to the Refund Request referenced in this Refund Confirmation to be in a
    * refunded state. If he voucher is in an unrefunded state or the Refund Request cannot be found, an appropriate
    * error response will be returned.
    *
    * @param refundUuid
    *           identifies the request to which this confirmation refers
    * @param confirmationUuid
    *           uniquely identifies this confirmation request
    * @param voucherCode
    *           the code for the corresponding voucher. Null indicates that the request referenced by this confirmation
    *           was not found.
    * @return a 404 error response if the referenced request was not found, a 400 error response if the
    *         corresponding voucher is not in a redeemed state, null if it is in a redeemed state and the confirmation can
    *         proceed.
    */
   public static Response canConfirmRefund(String refundUuid, String confirmationUuid, String voucherCode) {
      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(refundUuid);

      // TODO Extract method
      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher in Refunded state.
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      // No corresponding request
      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Refund Request")
               .detailMessage(
                     new DetailMessage()
                           .freeString("The Refund Request to which this Refund Confirmation refers does not exist."));
         return Response.status(404).entity(errorDetail).build();
      }

      if (confirmedExistingVouchers.get(voucherCode) != VoucherState.REFUNDED) {
         errorDetail.errorType(ErrorType.VOUCHER_NOT_REFUNDED)
               .errorMessage("Refund Confirmation not performed")
               .detailMessage(
                     new DetailMessage().freeString(
                           String.format(
                                 "The voucher referenced in the Refund Request to which this Refund Confirmation pertains is not currently in the refunded state. "
                                       + "The Voucher is currently in a %s state.",
                                 confirmedExistingVouchers.get(voucherCode).name())));
         // .voucherId(redemptionUuid)); TODO Removed this, didn't make sense to duplicate information
         return Response.status(400).entity(errorDetail).build(); // TODO Error codes ok?
      }

      return null;
   }

   public static Response canReverseRefund(
         String refundUuid,
         String reversalUuid,
         String username,
         String password,
         String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      // TODO Convert to switch-cases
      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(refundUuid);

      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      // ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords =
      // testServer.getRedemptionRequestRecords();

      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords = testServer.getRedemptionConfirmationRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, refundUuid);
      BasicAdvice confirmation = confirmationRecords.get(requestKey);

      // No corresponding refund request
      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Refund Request")
               .detailMessage(new DetailMessage().freeString("No Refund Request located for given Refund UUID."));
         return Response.status(404).entity(errorDetail).build();
      }

      // Happy days
      if (confirmedExistingVouchers.get(voucherCode) == VoucherState.REFUNDED)
         return null;

      // Reversal already processed
      if (confirmedExistingVouchers.get(voucherCode) == VoucherState.CONFIRMED_REDEEMED) {
         errorDetail.errorType(ErrorType.REDEMPTION_ALREADY_CONFIRMED)
               .errorMessage("Redemption confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "This Refund Reversal cannot be processed as the relevant voucher is not in a refunded state.")
                           .confirmationId(confirmation.getId())
                           .voucherId(refundUuid)
                           .reversalId(reversalUuid));
         return Response.status(400).entity(errorDetail).build();
      }

      // TODO Normalise these validation methods to be more similar (this)
      // Confirm Voucher redeemed

      if (confirmedExistingVouchers.get(voucherCode) == VoucherState.CONFIRMED_PROVISIONED) {
         errorDetail.errorType(ErrorType.REFUND_ALREADY_CONFIRMED)
               .errorMessage("Refund confirmed")
               .detailMessage(
                     new DetailMessage()
                           .freeString("The voucher related to this Refund Reversal has already been refunded.")
                           .reversalId(reversalUuid));
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

}
