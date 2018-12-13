package io.electrum.suv.server.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ErrorDetail.ErrorType;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.models.RedemptionResponse;
import io.electrum.suv.api.models.Voucher;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

public class RedemptionModelUtils {
   /**
    * Determine whether a given {@link Voucher voucher} redemption request is able to proceed.
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
   public static ValidationResponse canRedeemVoucher(
         String voucherCode,
         String username,
         String password,
         String requestUuid) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();
      ConcurrentHashMap<RequestKey, RedemptionRequest> requestRecords =
            testServer.getRecordStorageManager().getRedemptionRequestRecords();
      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getConfirmedExistingVouchers();

      // If voucher provision not yet confirmed
      if (confirmedExistingVouchers.get(voucherCode) == null) {
         ErrorDetail errorDetail =
               SUVModelUtils.buildErrorDetail(
                     requestUuid,
                     "Voucher not confirmed.",
                     "Voucher confirmation for this voucher has not been processed. Cannot redeem unconfirmed vouchers.",
                     null,
                     ErrorType.VOUCHER_NOT_REDEEMABLE);
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }

      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            testServer.getRecordStorageManager().getRedemptionReversalRecords();
      ValidationResponse reversalRsp =
            SUVModelUtils.confirmReversalNotReceived(
                  username,
                  password,
                  requestUuid,
                  testServer,
                  "Redemption",
                  reversalRecords,
                  ErrorType.REDEMPTION_ALREADY_REVERSED);
      if (reversalRsp.hasErrorResponse())
         return reversalRsp;

      // If no redemptionRequests have been recorded, the voucher cannot fail the test of having already been redeemed.
      if (requestRecords.size() != 0) {
         RequestKey key = new RequestKey(username, password, RequestKey.ResourceType.REDEMPTIONS_RESOURCE, requestUuid);
         // RedemptionRequest originalRequest = requestRecords.get(requestKey);

         // If Voucher already redeemed
         if (confirmedExistingVouchers.get(voucherCode).getValue() >= SUVTestServer.VoucherState.REDEEMED.getValue()) {
            ErrorDetail errorDetail =
                  SUVModelUtils.buildErrorDetail(
                        requestUuid,
                        "Duplicate Redemption Requests",
                        "A Redemption Request for that voucher has already been received.",
                        null,
                        ErrorType.VOUCHER_ALREADY_REDEEMED);

            DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();

            // Check for a response for this request
            ConcurrentHashMap<RequestKey, RedemptionResponse> responseRecords =
                  testServer.getRecordStorageManager().getRedemptionResponseRecords();
            RedemptionResponse rsp = responseRecords.get(key);
            if (rsp != null) {
               detailMessage.setVoucher(rsp.getVoucher());
            }
            return new ValidationResponse(Response.status(400).entity(errorDetail).build());
         }
      }

      return new ValidationResponse(null);
   }

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
    *         corresponding voucher is not in a redeemed state, null if it is in a redeemed state and the confirmation
    *         can proceed.
    */
   public static ValidationResponse canConfirmRedemption(
         String redemptionUuid,
         String confirmationUuid,
         String voucherCode) {

      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(redemptionUuid);

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getConfirmedExistingVouchers();

      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Confirmation refers does not exist."));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      if (confirmedExistingVouchers.get(voucherCode) != SUVTestServer.VoucherState.REDEEMED) {
         errorDetail.errorType(ErrorType.VOUCHER_NOT_REDEEMED)
               .errorMessage("Redemption Confirmation not performed")
               .detailMessage(
                     new DetailMessage().freeString(
                           String.format(
                                 "The voucher referenced in the Redemption Request to which this Redemption Confirmation pertains is not currently in the redeemed state. "
                                       + "The Voucher is currently in a %s state.",
                                 confirmedExistingVouchers.get(voucherCode).name())));

         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }

      return new ValidationResponse(null);
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
    * @return A {@link ValidationResponse} containing a 404 Error response if the redemption request was not received or
    *         the voucher could not be found, a 400 Error response if the voucher redemption is already confirmed. The
    *         validation response will have no error response if redemption can be reversed.
    */
   public static ValidationResponse canReverseRedemption(
         String redemptionUuid,
         String reversalUuid,
         String username,
         String password,
         String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();
      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(redemptionUuid);
      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getRecordStorageManager().getConfirmedExistingVouchers();

      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Reversal refers does not exist."));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      switch (confirmedExistingVouchers.get(voucherCode)) {
      case REDEEMED:
         return new ValidationResponse(null);
      case CONFIRMED_REDEEMED:
         ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords =
               testServer.getRecordStorageManager().getRedemptionConfirmationRecords();
         RequestKey requestKey =
               new RequestKey(username, password, RequestKey.ResourceType.CONFIRMATIONS_RESOURCE, redemptionUuid);
         BasicAdvice confirmation = confirmationRecords.get(requestKey);

         errorDetail.errorType(ErrorType.REDEMPTION_ALREADY_CONFIRMED)
               .errorMessage("Redemption confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Voucher Redemption cannot be reversed as it has already been confirmed with the associated details.")
                           .confirmationId(confirmation.getId())
                           .voucherId(redemptionUuid)
                           .reversalId(reversalUuid));
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      default:
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Redemption Request")
               .detailMessage(
                     new DetailMessage().freeString(
                           "The Redemption Request to which this Redemption Reversal refers does not exist."));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }
   }

   /**
    * Builds a redemption response for a given request.
    *
    * Populates the response with random identifiefers and slip data as well as the correct voucher details.
    *
    * @param redemptionRequest
    *           the request to generate a response for
    * @return a new RedemptionResponse for this request
    * @throws IOException
    */
   public static RedemptionResponse redemptionRspFromReq(RedemptionRequest redemptionRequest) throws IOException {
      RedemptionResponse redemptionResponse =
            JsonUtil.deserialize(
                  JsonUtil.serialize(redemptionRequest, RedemptionRequest.class),
                  RedemptionResponse.class);

      SUVModelUtils.updateWithRandomizedIdentifiers(redemptionResponse);
      redemptionResponse.setVoucher(redemptionRequest.getVoucher());
      redemptionResponse.setSlipData(SUVModelUtils.createRandomizedSlipData());
      return redemptionResponse;
   }
}
