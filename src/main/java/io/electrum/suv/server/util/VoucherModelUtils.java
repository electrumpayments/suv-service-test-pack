package io.electrum.suv.server.util;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class VoucherModelUtils extends SUVModelUtils {
   static Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

   // TODO Implement class

   // TODO complete method
   // TODO confirm correct function
   // TODO documentation
   public static Response canProvisionVoucher(String voucherId, String username, String password) {
      final SUVTestServer testServer = SUVTestServerRunner.getTestServer();

      ConcurrentHashMap<RequestKey, ProvisionRequest> provisionRecords = testServer.getVoucherProvisionRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
      ProvisionRequest originalRequest = provisionRecords.get(requestKey); // check the voucher is not already
                                                                           // provisioned
      // If Voucher already provisioned
      if (originalRequest != null) {
         ErrorDetail errorDetail =
               buildDuplicateErrorDetail(
                     voucherId,
                     null,
                     /* ErrorDetail.RequestType.VOUCHER_REQUEST, */ originalRequest);

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

      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords = testServer.getVoucherReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId);
      BasicReversal reversal = reversalRecords.get(reversalKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     voucherId,
                     "Voucher reversed.",
                     "Voucher reversal with String already processed with the associated fields.",
                     reversal.getId(),
                     // ErrorDetail.RequestType.VOUCHER_REQUEST,
                     ErrorDetail.ErrorType.GENERAL_ERROR); // TODO Pick a better ErrorType

          // Check for a response for this request, if found add to detailMessage
          DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, ProvisionResponse> responseRecords = testServer.getVoucherResponseRecords();
         ProvisionResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }

      // TODO Validate Voucher not Redeemed
      // TODO Validate Voucher not provisioned?
      return null;
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
