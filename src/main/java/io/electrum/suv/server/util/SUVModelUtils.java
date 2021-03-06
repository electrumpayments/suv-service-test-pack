package io.electrum.suv.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.api.models.Voucher;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.suv.server.model.FormatError;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.vas.model.*;

//import io.electrum.airtime.api.model.ErrorDetail;
//import io.electrum.airtime.api.model.Voucher;

@SuppressWarnings("Annotator")
public class SUVModelUtils {
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   private static final List<String> redeemInstructions = new ArrayList<>();
   private static final List<SlipLine> messageLines = new ArrayList<>();

   static {
      redeemInstructions.add("To redeem your voucher");
      redeemInstructions.add("enter the USSD code below:");
      redeemInstructions.add("*999*<pin>#");
      messageLines.add(new SlipLine().text("For any queries please"));
      messageLines.add(new SlipLine().text("contact your network"));
      messageLines.add(new SlipLine().text("operator."));
   }

   /** Builds list of format errors to be returned as part of a detail message in an errorDetail. */
   public static ErrorDetail buildFormatErrorRsp(List<String> errors) {
      List<FormatError> formatErrors = new ArrayList<>(errors.size());
      for (String error : errors) {
         formatErrors.add(new FormatError().msg(error));
      }
      return new ErrorDetail().errorType(ErrorDetail.ErrorType.FORMAT_ERROR)
            .errorMessage("Bad formatting.")
            .detailMessage(new DetailMessage().formatErrors(formatErrors));
   }

   /*
    * public static BasicAdviceResponse buildAdviceResponseFromAdvice(BasicAdvice basicAdvice) { return new
    * BasicAdviceResponse().id(basicAdvice.getId()) .requestId(basicAdvice.getRequestId()) .time(basicAdvice.getTime())
    * .transactionIdentifiers(basicAdvice.getThirdPartyIdentifiers()); }
    */

   /** Create a new voucher with randomized values for code, expiry date and instruction */
   static Voucher createRandomizedVoucher() {
      Voucher voucher = new Voucher();
      voucher.setCode(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setExpiryDate(new DateTime());
      // voucher.setSerialNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      // voucher.setBatchNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setRedeemInstructions(redeemInstructions);
      return voucher;
   }

   /** Returns a new randomized {@link SlipData} with message lines populated. */
   static SlipData createRandomizedSlipData() {
      SlipData slipData = new SlipData();
      slipData.setMessageLines(messageLines);
      return slipData;
   }

   /**
    * Updates a given {@link Transaction transaction} with randomized 3rd party IDs. Inserts placeholder settlement
    * entity if transaction does not include one.
    * 
    * @param transaction
    *           to be updated
    */
   static void updateWithRandomizedIdentifiers(Transaction transaction) {
      List<ThirdPartyIdentifier> thirdPartyIds = transaction.getThirdPartyIdentifiers();
      Institution settlementEntity = transaction.getSettlementEntity();
      if (settlementEntity == null) {
         log.debug("Settlement Entity not found, creating placeholder.");
         settlementEntity = new Institution();
         settlementEntity.setId("33333333");
         settlementEntity.setName("TransactionsRUs");
      }

      Institution receiver = transaction.getReceiver();
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(settlementEntity.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(receiver.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));

      transaction.setSettlementEntity(settlementEntity);
   }

   /** Builds an {@link ErrorDetail} for duplicate UUID errors */
   static ErrorDetail buildDuplicateUuidErrorDetail(
         String objectId,
         String originalMsgId,
         // ErrorDetail.RequestType requestType,
         Transaction transaction) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Duplicate UUID.",
                  "Request with ID already processed with the associated fields.",
                  originalMsgId,
                  // requestType,
                  ErrorDetail.ErrorType.DUPLICATE_RECORD);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setVoucherId(objectId);
      detailMessage.setRequestTime(transaction.getTime().toString()); //
      detailMessage.setReceiver(transaction.getReceiver());

      return errorDetail;
   }

   /** Builds a new error detail (including a detail message) from specific messages. */
   static ErrorDetail buildErrorDetail(
         String objectId,
         String errorMessage,
         String detailMessageFreeString,
         String originalMsgId,
         // ErrorDetail.RequestType requestType,
         ErrorDetail.ErrorType errorType) {

      ErrorDetail errorDetail =
            new ErrorDetail().errorType(errorType).errorMessage(errorMessage).id(objectId).originalId(originalMsgId);
      // .requestType(requestType);

      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString(detailMessageFreeString);
      detailMessage.setReversalId(originalMsgId);

      errorDetail.setDetailMessage(detailMessage);

      return errorDetail;
   }

   /**
    * Confirms UUID format valid using regex (8-4-4-4-12 hexadecimal)
    *
    * Throws a {@link FormatException} if it does not.
    */
   @SuppressWarnings("Annotator")
   public static void validateUuid(String uuid) {
      if (!uuid.matches("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}"))
         throw new FormatException(
               new FormatError().msg("UUID must conform to the format 8-4-4-4-12 hexadecimal values"));
   }

   /**
    * Checks that the ThirdPartyIdentifiers all contain a TransactionIdentifier.
    *
    * Throws a {@link FormatException} if any do not.
    * 
    * @param thirdPartyIdentifiers
    *           list of ThirdPartyIdentifiers to validate
    */
   public static void validateThirdPartyIdTransactionIds(List<ThirdPartyIdentifier> thirdPartyIdentifiers) {
      int cnt = 0;
      for (ThirdPartyIdentifier tpi : thirdPartyIdentifiers) {
         if (tpi.getTransactionIdentifier() == null)
            throw new FormatException(
                  new FormatError()
                        .msg(String.format("thirdPartyIdentifiers[%d].transactionIdentifier may not be null", cnt)));
         cnt++;
      }
   }

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

                  ErrorDetail.ErrorType.AUTHENTICATION_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

   static ValidationResponse confirmReversalNotReceived(
         String username,
         String password,
         String requestUuid,
         SUVTestServer testServer,
         String requestType,
         ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords,
         ErrorDetail.ErrorType errorType) {
      // Confirm reversal request did not arrive before this

      RequestKey requestKey =
            new RequestKey(username, password, RequestKey.ResourceType.REVERSALS_RESOURCE, requestUuid);
      BasicReversal reversal = reversalRecords.get(requestKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     requestUuid,
                     String.format("%s reversed.", requestType),
                     String.format("%s reversal with UUID already processed with the associated fields.", requestType),
                     reversal.getId(),
                     errorType);

         // Check for a response for this request, if found add to detailMessage
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, RefundResponse> responseRecords =
               testServer.getRecordStorageManager().getRefundResponseRecords();
         RefundResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return new ValidationResponse(Response.status(400).entity(errorDetail).build());
      }
      return new ValidationResponse(null);
   }
}
