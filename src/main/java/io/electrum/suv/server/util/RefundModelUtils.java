package io.electrum.suv.server.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ErrorDetail.ErrorType;
import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.api.models.Voucher;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

public class RefundModelUtils extends SUVModelUtils {
   public static RefundResponse refundRspFromReq(RefundRequest refundRequest) throws IOException {
      RefundResponse refundResponse =
            JsonUtil.deserialize(JsonUtil.serialize(refundRequest, RefundRequest.class), RefundResponse.class);

      updateWithRandomizedIdentifiers(refundResponse);
      refundResponse.setSlipData(createRandomizedSlipData());

      // TODO Confirm not needed to populate more fields (optional)
      return refundResponse;
   }

   /**
    * Determine whether a given {@link Voucher voucher} refund request is able to proceed.
    *
    * Ensures the voucher provision has not already been reversed and that the voucher is currently in a redemption
    * confirmed state.
    *
    * @param refundUuid
    *           the unique identifier of this request.
    * @param username
    * @param password
    * @param voucherCode
    * @return
    */
   public static ValidationResponse canRefundVoucher(
         String refundUuid,
         String username,
         String password,
         String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getBackend().getRefundReversalRecords();
      ValidationResponse reversalRsp =
            SUVModelUtils.confirmReversalNotReceived(
                  username,
                  password,
                  refundUuid,
                  testServer,
                  "Refund",
                  reversalRecords,
                  ErrorType.REFUND_ALREADY_REVERSED);
      if (reversalRsp.hasErrorResponse())
         return reversalRsp;

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();

      ErrorDetail errorDetail;
      switch (confirmedExistingVouchers.get(voucherCode)) {
      case CONFIRMED_PROVISIONED:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher not refundable",
                     "The voucher is currently in a non-refundable state. Only vouchers for which "
                           + "redemption confirmation message have been received may be refunded.",
                     null,
                     ErrorType.VOUCHER_NOT_REDEEMED);
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());

      case REDEEMED:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher Redemption unconfirmed",
                     "There is a current redemption request for this voucher pending confirmation. Cannot refund in the current state.",
                     null,
                     ErrorType.REDEMPTION_NOT_CONFIRMED);
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());

      // case CONFIRMED_REDEEMED: Handled by default so as not to duplicate code

      case REFUNDED:
         errorDetail =
               buildErrorDetail(
                     refundUuid,
                     "Voucher refund pending",
                     "There is a current refund request for this voucher pending confirmation. Cannot refund in the current state.",
                     null,
                     ErrorType.VOUCHER_ALREADY_REFUNDED);
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      default:
         return new ValidationResponse(null);
      }
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
    * @return a 404 error response if the referenced request was not found, a 400 error response if the corresponding
    *         voucher is not in a redeemed state, null if it is in a redeemed state and the confirmation can proceed.
    */
   public static ValidationResponse canConfirmRefund(String refundUuid, String confirmationUuid, String voucherCode) {
      ErrorDetail errorDetail = new ErrorDetail().id(confirmationUuid).originalId(refundUuid);

      // Confirm Voucher in Refunded state.
      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();

      // No corresponding request
      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Refund Request")
               .detailMessage(
                     new DetailMessage()
                           .freeString("The Refund Request to which this Refund Confirmation refers does not exist."));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      if (confirmedExistingVouchers.get(voucherCode) != SUVTestServer.VoucherState.REFUNDED) {
         errorDetail.errorType(ErrorType.VOUCHER_NOT_REFUNDED)
               .errorMessage("Refund Confirmation not performed")
               .detailMessage(
                     new DetailMessage().freeString(
                           String.format(
                                 "The voucher referenced in the Refund Request to which this Refund Confirmation pertains is not currently in the refunded state. "
                                       + "The Voucher is currently in a %s state.",
                                 confirmedExistingVouchers.get(voucherCode).name())));
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }

      return new ValidationResponse(null);
   }

   /**
    * Determine whether the corresponding {@link RefundRequest} can be reversed.
    *
    * Checks to ensure that the corresponding Refund Request exists. If it does, the relevant vouchers state is checked
    * to determine whether this reversal can proceed. If the voucher is not currently in a refunded state, an
    * appropriate error response is returned.
    *
    *
    * @param refundUuid
    *           the unique identifier of the redemptionRequest to be reversed
    * @param reversalUuid
    *           the unique identifier of this request
    * @param username
    *           from BasicAuth
    * @param password
    *           from BasicAuth
    * @param voucherCode
    *           the unique code for the voucher, used to identify it in the cache
    * @return A {@link ValidationResponse} containing a 404 Error response if the refund request was not received or the
    *         voucher could not be found, a 400 Error response if the voucher refund is already confirmed or a refund
    *         has already been processed. The validationResponse will have no error response if the refund can be
    *         reversed.
    */
   public static ValidationResponse canReverseRefund(
         String refundUuid,
         String reversalUuid,
         String username,
         String password,
         String voucherCode) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ErrorDetail errorDetail = new ErrorDetail().id(reversalUuid).originalId(refundUuid);

      ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getBackend().getConfirmedExistingVouchers();

      ConcurrentHashMap<RequestKey, BasicAdvice> confirmationRecords =
            testServer.getBackend().getRedemptionConfirmationRecords();
      RequestKey requestKey =
            new RequestKey(username, password, RequestKey.ResourceType.CONFIRMATIONS_RESOURCE, refundUuid);
      BasicAdvice confirmation = confirmationRecords.get(requestKey);

      // No corresponding refund request
      if (voucherCode == null) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD)
               .errorMessage("No Refund Request")
               .detailMessage(new DetailMessage().freeString("No Refund Request located for given Refund UUID."));
         return new ValidationResponse(Response.status(404).entity(errorDetail).build());
      }

      switch (confirmedExistingVouchers.get(voucherCode)) {

      case CONFIRMED_PROVISIONED:
         errorDetail.errorType(ErrorType.REFUND_ALREADY_CONFIRMED)
               .errorMessage("Refund confirmed")
               .detailMessage(
                     new DetailMessage()
                           .freeString("The voucher related to this Refund Reversal has already been refunded.")
                           .reversalId(reversalUuid));
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());

      case CONFIRMED_REDEEMED:
         errorDetail.errorType(ErrorType.REDEMPTION_ALREADY_CONFIRMED)
               .errorMessage("Redemption confirmed.")
               .detailMessage(
                     new DetailMessage().freeString(
                           "This Refund Reversal cannot be processed as the relevant voucher is not in a refunded state.")
                           .confirmationId(confirmation.getId())
                           .voucherId(refundUuid)
                           .reversalId(reversalUuid));
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      case REFUNDED:
      default:
         return new ValidationResponse(null);

      }

   }
}
