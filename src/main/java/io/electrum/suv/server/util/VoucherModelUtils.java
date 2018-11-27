package io.electrum.suv.server.util;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.Institution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class VoucherModelUtils extends SUVModelUtils {
   static Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

   // TODO Implement class

   public static boolean isUuidConsistent(String id) {
      // TODO validate uuid

      return true;
   }

   // TODO Ensure method is correct
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

   // TODO Ensure method actually does what it should do
   public static ErrorDetail buildErrorDetail(
         String objectId,
         String errorMessage,
         String detailMessageFreeString,
         String originalMsgId,
         ErrorDetail.ErrorType errorType) {

      ErrorDetail errorDetail =
            new ErrorDetail().errorType(errorType).errorMessage(errorMessage).id(objectId).originalId(originalMsgId);

      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString(detailMessageFreeString);
      detailMessage.setReversalId(originalMsgId);

      errorDetail.setDetailMessage(detailMessage);

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
      // TODO Voucher.amounts never appears to be set from null
      return voucherResponse;
   }
}
