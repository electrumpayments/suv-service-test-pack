package io.electrum.suv.server.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.Msisdn;
import io.electrum.airtime.api.model.Product;
import io.electrum.airtime.api.model.PurchaseConfirmation;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;

public class RefundModelUtils extends AirtimeModelUtils {

   // TODO Reimplement all these for new values
   //<editor-fold desc="error and validation checks on data storage">
  /* public static Response canPurchasePurchaseRequest(String purchaseRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            SUVTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey requestKey = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);

      // Check if request already provisioned
      PurchaseRequest originalRequest = purchaseRequestRecords.get(requestKey);
      if (originalRequest != null) {
         return buildDuplicateErrorResponse(purchaseRequestId, requestKey, originalRequest);
      }

      // Check if request has already been reversed
      BasicReversal reversal = getPurchaseReversalFromCache(purchaseRequestId, username, password);
      if (reversal != null) {
         return buildAlreadyReversedErrorResponse(purchaseRequestId, requestKey, reversal);
      }

      return null;
   }

   public static Response canReversePurchase(BasicReversal reversal, String username, String password) {
      if (!isPurchaseRequestProvisioned(reversal.getRequestId(), username, password)) {
         ErrorDetail errorDetail =
               buildRequestNotFoundErrorDetail(
                     reversal.getId(),
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_REVERSAL);
         return Response.status(404).entity(errorDetail).build();
      }

      // check that it's not confirmed
      PurchaseConfirmation confirmation = getPurchaseConfirmationFromCache(reversal.getRequestId(), username, password);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     reversal.getId(),
                     "Purchase Request confirmed already.",
                     "The purchase cannot be reversed as it has already been confirmed with the associated details.",
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_REVERSAL,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setConfirmationId(confirmation.getId());

         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static Response canConfirmPurchase(
         PurchaseConfirmation purchaseConfirmation,
         String username,
         String password) {
      // check if purchase request was provisioned
      if (!isPurchaseRequestProvisioned(purchaseConfirmation.getRequestId(), username, password)) {
         ErrorDetail errorDetail =
               buildRequestNotFoundErrorDetail(
                     purchaseConfirmation.getId(),
                     purchaseConfirmation.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_CONFIRMATION);
         return Response.status(404).entity(errorDetail).build();
      }

      // check that it's not reversed
      BasicReversal reversal = getPurchaseReversalFromCache(purchaseConfirmation.getRequestId(), username, password);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     reversal.getId(),
                     "Purchase Request reversed already.",
                     "The purchase cannot be confirmed as it has already been reversed with the associated details.",
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_CONFIRMATION,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static Response canPurchaseStatusWithMsgId(
         String originalPurchaseRequestId,
         String username,
         String password) {
      // check if purchase request was provisioned
      if (!isPurchaseRequestProvisioned(originalPurchaseRequestId, username, password)) {
         ErrorDetail errorDetail =
               buildRequestNotFoundErrorDetail(
                     null,
                     originalPurchaseRequestId,
                     ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST);
         return Response.status(404).entity(errorDetail).build();
      }

      // check that it's not reversed
      BasicReversal reversal = getPurchaseReversalFromCache(originalPurchaseRequestId, username, password);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     reversal.getId(),
                     "Purchase Request reversed already.",
                     "The purchase has already been reversed with the associated details.",
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static Response canPurchaseStatusWithPurchaseRef(
         String purchaseRef,
         String provider,
         String username,
         String password) {
      if (purchaseRef == null || provider == null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     null,
                     "The purchaseRef or provider is null",
                     "The purchaseRef and provider are both required when no originalMsgId is given",
                     purchaseRef,
                     ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST,
                     ErrorDetail.ErrorType.FORMAT_ERROR);
         return Response.status(400).entity(errorDetail).build();
      }

      // check if a purchase can be found with the given purchaseRef
      if (!containsPurchaseIdWithPurchRef(purchaseRef, username, password)) {
         ErrorDetail errorDetail =
               buildRequestNotFoundErrorDetail(null, purchaseRef, ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST);
         return Response.status(404).entity(errorDetail).build();
      }

      return null;
   }

   private static ErrorDetail buildRequestNotFoundErrorDetail(
         String id,
         String requestId,
         ErrorDetail.RequestType requestType) {
      return buildErrorDetail(
            id,
            "Original purchase was not found.",
            "No PurchaseRequest located for given purchase Identifier.",
            requestId,
            requestType,
            ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);
   }

   public static Response buildNoPurchaseRspFoundErrorResponse(String purchaseRequestId) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  null,
                  "Purchase Response not found.",
                  "No Purchase Response could be found with associated msg id.",
                  purchaseRequestId,
                  ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST,
                  ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);

      return Response.status(400).entity(errorDetail).build();
   }

   private static Response buildAlreadyReversedErrorResponse(
         String purchaseRequestId,
         RequestKey requestKey,
         BasicReversal reversal) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  purchaseRequestId,
                  "Purchase Request reversed.",
                  "Purchase reversal with String already processed with the associated fields.",
                  reversal.getId(),
                  ErrorDetail.RequestType.PURCHASE_REVERSAL,
                  ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);

      PurchaseResponse rsp = getPurchaseResponseFromCache(requestKey);
      if (rsp != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setVoucher(rsp.getVoucher());
      }
      return Response.status(400).entity(errorDetail).build();
   }

   private static Response buildDuplicateErrorResponse(
         String purchaseRequestId,
         RequestKey requestKey,
         PurchaseRequest originalRequest) {
      ErrorDetail errorDetail =
            buildDuplicateErrorDetail(
                  purchaseRequestId,
                  null,
                  ErrorDetail.RequestType.PURCHASE_REQUEST,
                  originalRequest);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setProduct(originalRequest.getProduct());

      PurchaseResponse rsp = getPurchaseResponseFromCache(requestKey);
      if (rsp != null) {
         detailMessage.setVoucher(rsp.getVoucher());
      }
      return Response.status(400).entity(errorDetail).build();
   }

   private static boolean isPurchaseRequestProvisioned(String purchaseRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            SUVTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey provisionKey =
            new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);
      log.debug(
            String.format(
                  "Searching for purchase request provision record under following key: %s",
                  provisionKey.toString()));
      return purchaseRequestRecords.containsKey(provisionKey);
   }

   public static PurchaseRequest getPurchaseRequestFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            SUVTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey provisionKey =
            new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);
      log.debug(
            String.format(
                  "Searching for purchase request provision record under following key: %s",
                  provisionKey.toString()));
      return purchaseRequestRecords.get(provisionKey);
   }

   private static PurchaseReversal getPurchaseReversalFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getPurchaseReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, PurchaseResource.ReversePurchase.REVERSE_PURCHASE, purchaseRequestId);
      return reversalRecords.get(reversalKey);
   }

   private static PurchaseConfirmation getPurchaseConfirmationFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseConfirmation> purchaseConfirmationRecords =
            SUVTestServerRunner.getTestServer().getPurchaseConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(
                  username,
                  password,
                  PurchaseResource.ConfirmPurchase.PURCHASE_CONFIRMATION,
                  purchaseRequestId);
      return purchaseConfirmationRecords.get(confirmKey);
   }

   private static PurchaseResponse getPurchaseResponseFromCache(RequestKey purchaseRequestKey) {
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getPurchaseResponseRecords();
      return responseRecords.get(purchaseRequestKey);
   }

   public static PurchaseResponse getPurchaseResponseFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      RequestKey purchaseRequestKey =
            new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            SUVTestServerRunner.getTestServer().getPurchaseResponseRecords();
      return responseRecords.get(purchaseRequestKey);
   }

   public static String getPurchaseIdWithPurchRefFromCache(String purchaseReference, String username, String password) {
      RequestKey purchaseReferenceKey =
            new RequestKey(username, password, RequestKey.PURCHASE_REF_RESOURCE, purchaseReference);
      ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords =
            SUVTestServerRunner.getTestServer().getPurchaseReferenceRecords();
      return purchaseReferenceRecords.get(purchaseReferenceKey);
   }

   public static boolean containsPurchaseIdWithPurchRef(String purchaseReference, String username, String password) {
      RequestKey purchaseReferenceKey =
            new RequestKey(username, password, RequestKey.PURCHASE_REF_RESOURCE, purchaseReference);
      ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords =
            SUVTestServerRunner.getTestServer().getPurchaseReferenceRecords();
      return purchaseReferenceRecords.containsKey(purchaseReferenceKey);
   }*/
   //</editor-fold>

}
