package io.electrum.suv.server.util;

import io.electrum.suv.api.RefundsResource;
import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class RefundModelUtils extends SUVModelUtils {
   public static RefundResponse refundRspFromReq(RefundRequest refundRequest) throws IOException {
       RefundResponse refundResponse =
               JsonUtil.deserialize(JsonUtil.serialize(refundRequest, RefundRequest.class), RefundResponse.class);

       updateWithRandomizedIdentifiers(refundResponse);
       refundResponse.setVoucher(createRandomizedVoucher());
       refundResponse.setSlipData(createRandomizedSlipData());

       //TODO Confirm not needed to populate more fields (optional)
       return refundResponse;
   }

   // TODO Reimplement all these for new values
   /*
    * 
    * private static ErrorDetail buildRequestNotFoundErrorDetail( String id, String requestId, ErrorDetail.RequestType
    * requestType) { return buildErrorDetail( id, "Original refund was not found.",
    * "No refundRequest located for given refund Identifier.", requestId, requestType,
    * ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD); }
    * 
    * public static Response buildNorefundRspFoundErrorResponse(String refundRequestId) { ErrorDetail errorDetail =
    * buildErrorDetail( null, "refund Response not found.", "No refund Response could be found with associated msg id.",
    * refundRequestId, ErrorDetail.RequestType.refund_STATUS_REQUEST, ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);
    * 
    * return Response.status(400).entity(errorDetail).build(); }
    * 
    * private static Response buildAlreadyReversedErrorResponse( String refundRequestId, RequestKey requestKey,
    * BasicReversal reversal) { ErrorDetail errorDetail = buildErrorDetail( refundRequestId, "refund Request reversed.",
    * "refund reversal with String already processed with the associated fields.", reversal.getId(),
    * ErrorDetail.RequestType.refund_REVERSAL, ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);
    * 
    * refundResponse rsp = getrefundResponseFromCache(requestKey); if (rsp != null) { DetailMessage detailMessage =
    * (DetailMessage) errorDetail.getDetailMessage(); detailMessage.setVoucher(rsp.getVoucher()); } return
    * Response.status(400).entity(errorDetail).build(); }
    * 
    * private static Response buildDuplicateErrorResponse( String refundRequestId, RequestKey requestKey, refundRequest
    * originalRequest) { ErrorDetail errorDetail = buildDuplicateUuidErrorDetail( refundRequestId, null,
    * ErrorDetail.RequestType.refund_REQUEST, originalRequest);
    * 
    * DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
    * detailMessage.setProduct(originalRequest.getProduct());
    * 
    * refundResponse rsp = getrefundResponseFromCache(requestKey); if (rsp != null) {
    * detailMessage.setVoucher(rsp.getVoucher()); } return Response.status(400).entity(errorDetail).build(); }
    */
   private static boolean isRefundRequestProvisioned(String refundRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
            SUVTestServerRunner.getTestServer().getRefundRequestRecords();
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
      log.debug(
            String.format(
                  "Searching for refund request provision record under following key: %s",
                  provisionKey.toString()));
      return refundRequestRecords.containsKey(provisionKey);
   }

   public static RefundRequest getRefundRequestFromCache(String refundRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
            SUVTestServerRunner.getTestServer().getRefundRequestRecords();
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
      log.debug(
            String.format(
                  "Searching for refund request provision record under following key: %s",
                  provisionKey.toString()));
      return refundRequestRecords.get(provisionKey);
   }

   private static BasicReversal getRefundReversalFromCache(String refundRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getRefundReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
      return reversalRecords.get(reversalKey);
   }

   private static BasicAdvice getRefundConfirmationFromCache(String refundRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, BasicAdvice> refundConfirmationRecords =
            SUVTestServerRunner.getTestServer().getRefundConfirmationRecords();
      RequestKey confirmKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
      return refundConfirmationRecords.get(confirmKey);
   }

   private static RefundResponse getRefundResponseFromCache(RequestKey refundRequestKey) {
      ConcurrentHashMap<RequestKey, RefundResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
      return responseRecords.get(refundRequestKey);
   }

   public static RefundResponse getrefundResponseFromCache(String refundRequestId, String username, String password) {
      RequestKey refundRequestKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
      ConcurrentHashMap<RequestKey, RefundResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
      return responseRecords.get(refundRequestKey);
   }

   /*
    * public static String getrefundIdWithPurchRefFromCache(String refundReference, String username, String password) {
    * RequestKey refundReferenceKey = new RequestKey(username, password, RequestKey.refund_REF_RESOURCE,
    * refundReference); ConcurrentHashMap<RequestKey, String> refundReferenceRecords =
    * SUVTestServerRunner.getTestServer().getrefundReferenceRecords(); return
    * refundReferenceRecords.get(refundReferenceKey); }
    * 
    * public static boolean containsrefundIdWithPurchRef(String refundReference, String username, String password) {
    * RequestKey refundReferenceKey = new RequestKey(username, password, RequestKey.refund_REF_RESOURCE,
    * refundReference); ConcurrentHashMap<RequestKey, String> refundReferenceRecords =
    * SUVTestServerRunner.getTestServer().getrefundReferenceRecords(); return
    * refundReferenceRecords.containsKey(refundReferenceKey); }
    */
   // </editor-fold>

}
