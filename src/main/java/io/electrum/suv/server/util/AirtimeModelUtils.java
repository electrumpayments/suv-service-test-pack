package io.electrum.suv.server.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.model.FormatError;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.Voucher;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicAdviceResponse;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.SlipData;
import io.electrum.vas.model.SlipLine;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

//TODO This probably needs to be changed to work with all the new suv things.
public class AirtimeModelUtils {
/*   protected static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   protected static List<String> redeemInstructions = new ArrayList<String>();
   protected static List<SlipLine> messageLines = new ArrayList<SlipLine>();

   static {
      redeemInstructions.add("To redeem your airtime");
      redeemInstructions.add("enter the USSD code below:");
      redeemInstructions.add("*999*<pin>#");
      messageLines.add(new SlipLine().text("For any queries please"));
      messageLines.add(new SlipLine().text("contact your network"));
      messageLines.add(new SlipLine().text("operator."));
   }

   public static ErrorDetail buildFormatErrorRsp(List<String> errors) {
      if (errors.size() == 0) {
         return null;
      }
      List<FormatError> formatErrors = new ArrayList<>(errors.size());
      for (String error : errors) {
         formatErrors.add(new FormatError().msg(error));
      }
      return new ErrorDetail().errorType(ErrorDetail.ErrorType.FORMAT_ERROR)
            .errorMessage("Bad formatting.")
            .detailMessage(new DetailMessage().formatErrors(formatErrors));
   }

   public static BasicAdviceResponse buildAdviceResponseFromAdvice(BasicAdvice basicAdvice) {
      return new BasicAdviceResponse().id(basicAdvice.getId())
            .requestId(basicAdvice.getRequestId())
            .time(basicAdvice.getTime())
            .transactionIdentifiers(basicAdvice.getThirdPartyIdentifiers());
   }

   protected static Voucher createRandomizedVoucher() {
      Voucher voucher = new Voucher();
      voucher.setPin(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setExpiryDate(new DateTime());
      voucher.setSerialNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setBatchNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setRedeemInstructions(redeemInstructions);
      return voucher;
   }

   protected static SlipData createRandomizedSlipData() {
      SlipData slipData = new SlipData();
      slipData.setMessageLines(messageLines);
      return slipData;
   }

   protected static void updateWithRandomizedIdentifiers(Transaction transaction) {
      List<ThirdPartyIdentifier> thirdPartyIds = transaction.getThirdPartyIdentifiers();
      Institution settlementEntity = transaction.getSettlementEntity();
      if (settlementEntity == null) {
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

   protected static Amounts createRandomizedAmounts() {
      return new Amounts().approvedAmount(createRandomizedAmount()).feeAmount(createRandomizedAmount());
   }

   private static LedgerAmount createRandomizedAmount() {
      return new LedgerAmount().currency("710")
            .amount(Long.parseLong(RandomData.random09((int) ((Math.random() * 2) + 1))));
   }

   public static Response buildIncorrectUsernameErrorResponse(
         String objectId,
         Institution client,
         String username,
         ErrorDetail.RequestType requestType) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Incorrect username",
                  "The HTTP Basic Authentication username (" + username
                        + ") is not the same as the value in the Client.Id field (" + client.getId() + ").",
                  null,
                  requestType,
                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

   public static ErrorDetail buildInconsistentIdErrorDetail(
         String pathId,
         String objectId,
         String originalMsgId,
         ErrorDetail.RequestType requestType) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "String inconsistent",
                  "The ID path parameter is not the same as the object's ID.",
                  originalMsgId,
                  requestType,
                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setPathId(pathId);

      return errorDetail;
   }

   public static ErrorDetail buildDuplicateErrorDetail(
         String objectId,
         String originalMsgId,
         ErrorDetail.RequestType requestType,
         Transaction transaction) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Duplicate UUID.",
                  "Request with String already processed with the associated fields.",
                  originalMsgId,
                  requestType,
                  ErrorDetail.ErrorType.DUPLICATE_RECORD);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setVoucherId(objectId);
      detailMessage.setRequestTime(transaction.getTime().toString());
      // detailMessage.setProduct(transaction.getProduct());
      detailMessage.setReceiver(transaction.getReceiver());

      return errorDetail;
   }

   public static ErrorDetail buildErrorDetail(
         String objectId,
         String errorMessage,
         String detailMessageFreeString,
         String originalMsgId,
         ErrorDetail.RequestType requestType,
         ErrorDetail.ErrorType errorType) {

      ErrorDetail errorDetail =
            new ErrorDetail().errorType(errorType)
                  .errorMessage(errorMessage)
                  .id(objectId)
                  .originalId(originalMsgId)
                  .requestType(requestType);

      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString(detailMessageFreeString);
      detailMessage.setReversalId(originalMsgId);

      errorDetail.setDetailMessage(detailMessage);

      return errorDetail;
   }

   public static boolean isUuidConsistent(String pathId, String serviceId) {
      return pathId.equals(serviceId);
   }*/

}
