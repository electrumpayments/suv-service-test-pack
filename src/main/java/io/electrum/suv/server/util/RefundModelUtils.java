package io.electrum.suv.server.util;

import java.io.IOException;

import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.vas.JsonUtil;

public class RefundModelUtils extends SUVModelUtils {
   public static RefundResponse refundRspFromReq(RefundRequest refundRequest) throws IOException {
      RefundResponse refundResponse =
            JsonUtil.deserialize(JsonUtil.serialize(refundRequest, RefundRequest.class), RefundResponse.class);

      updateWithRandomizedIdentifiers(refundResponse);
      refundResponse.setSlipData(createRandomizedSlipData());

      // TODO Confirm not needed to populate more fields (optional)
      return refundResponse;
   }
//   private static boolean isRefundRequestProvisioned(String refundRequestId, String username, String password) {
//      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
//            SUVTestServerRunner.getTestServer().getRefundRequestRecords();
//      RequestKey provisionKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
//      log.debug(
//            String.format(
//                  "Searching for refund request provision record under following key: %s",
//                  provisionKey.toString()));
//      return refundRequestRecords.containsKey(provisionKey);
//   }
//
//   public static RefundRequest getRefundRequestFromCache(String refundRequestId, String username, String password) {
//      ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords =
//            SUVTestServerRunner.getTestServer().getRefundRequestRecords();
//      RequestKey provisionKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
//      log.debug(
//            String.format(
//                  "Searching for refund request provision record under following key: %s",
//                  provisionKey.toString()));
//      return refundRequestRecords.get(provisionKey);
//   }
//
//   private static BasicReversal getRefundReversalFromCache(String refundRequestId, String username, String password) {
//      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
//            SUVTestServerRunner.getTestServer().getRefundReversalRecords();
//      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
//      return reversalRecords.get(reversalKey);
//   }
//
//   private static BasicAdvice getRefundConfirmationFromCache(String refundRequestId, String username, String password) {
//      ConcurrentHashMap<RequestKey, BasicAdvice> refundConfirmationRecords =
//            SUVTestServerRunner.getTestServer().getRefundConfirmationRecords();
//      RequestKey confirmKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
//      return refundConfirmationRecords.get(confirmKey);
//   }
//
//   private static RefundResponse getRefundResponseFromCache(RequestKey refundRequestKey) {
//      ConcurrentHashMap<RequestKey, RefundResponse> responseRecords =
//            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
//      return responseRecords.get(refundRequestKey);
//   }
//
//   public static RefundResponse getrefundResponseFromCache(String refundRequestId, String username, String password) {
//      RequestKey refundRequestKey = new RequestKey(username, password, RequestKey.REFUNDS_RESOURCE, refundRequestId);
//      ConcurrentHashMap<RequestKey, RefundResponse> responseRecords =
//            SUVTestServerRunner.getTestServer().getRefundResponseRecords();
//      return responseRecords.get(refundRequestKey);
//   }
//
//
//   public static String getrefundIdWithPurchRefFromCache(String refundReference, String username, String password) {
//   RequestKey refundReferenceKey = new RequestKey(username, password, RequestKey.refund_REF_RESOURCE,
//   refundReference); ConcurrentHashMap<RequestKey, String> refundReferenceRecords =
//   SUVTestServerRunner.getTestServer().getrefundReferenceRecords(); return
//   refundReferenceRecords.get(refundReferenceKey); }
//
//   public static boolean containsrefundIdWithPurchRef(String refundReference, String username, String password) {
//   RequestKey refundReferenceKey = new RequestKey(username, password, RequestKey.refund_REF_RESOURCE,
//   refundReference); ConcurrentHashMap<RequestKey, String> refundReferenceRecords =
//   SUVTestServerRunner.getTestServer().getrefundReferenceRecords(); return
//   refundReferenceRecords.containsKey(refundReferenceKey); }
//

}
