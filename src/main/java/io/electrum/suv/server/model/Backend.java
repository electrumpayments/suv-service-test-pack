package io.electrum.suv.server.model;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import io.electrum.suv.api.models.*;
import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;

//TODO could be abstract
public class Backend {
   private final ConcurrentHashMap<RequestKey, ProvisionRequest> voucherProvisionRecords;
   private final ConcurrentHashMap<RequestKey, ProvisionResponse> provisionResponseRecords;
   private final ConcurrentHashMap<RequestKey, TenderAdvice> voucherConfirmationRecords;
   private final ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords;

   private final ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords;
   private final ConcurrentHashMap<RequestKey, RedemptionResponse> redemptionResponseRecords;
   private final ConcurrentHashMap<RequestKey, BasicAdvice> redemptionConfirmationRecords;
   private final ConcurrentHashMap<RequestKey, BasicReversal> redemptionReversalRecords;

   private final ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords;
   private final ConcurrentHashMap<RequestKey, RefundResponse> refundResponseRecords;
   private final ConcurrentHashMap<RequestKey, BasicAdvice> refundConfirmationRecords;
   private final ConcurrentHashMap<RequestKey, BasicReversal> refundReversalRecords;

   private final ConcurrentHashMap<String, SUVTestServer.VoucherState> confirmedExistingVouchers;// records state of
                                                                                                 // existing vouchers

   public Backend() {
      voucherProvisionRecords = new ConcurrentHashMap<>();
      provisionResponseRecords = new ConcurrentHashMap<>();
      voucherConfirmationRecords = new ConcurrentHashMap<>();
      voucherReversalRecords = new ConcurrentHashMap<>();

      redemptionRequestRecords = new ConcurrentHashMap<>();
      redemptionResponseRecords = new ConcurrentHashMap<>();
      redemptionConfirmationRecords = new ConcurrentHashMap<>();
      redemptionReversalRecords = new ConcurrentHashMap<>();

      refundRequestRecords = new ConcurrentHashMap<>();
      refundResponseRecords = new ConcurrentHashMap<>();
      refundConfirmationRecords = new ConcurrentHashMap<>();
      refundReversalRecords = new ConcurrentHashMap<>();

      confirmedExistingVouchers = new ConcurrentHashMap<>();
   }

   public ConcurrentHashMap<RequestKey, ProvisionRequest> getVoucherProvisionRecords() {
      return voucherProvisionRecords;
   }

   public ConcurrentHashMap<RequestKey, ProvisionResponse> getProvisionResponseRecords() {
      return provisionResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, TenderAdvice> getVoucherConfirmationRecords() {
      return voucherConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getVoucherReversalRecords() {
      return voucherReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, RedemptionRequest> getRedemptionRequestRecords() {
      return redemptionRequestRecords;
   }

   public ConcurrentHashMap<RequestKey, RedemptionResponse> getRedemptionResponseRecords() {
      return redemptionResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicAdvice> getRedemptionConfirmationRecords() {
      return redemptionConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getRedemptionReversalRecords() {
      return redemptionReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, RefundRequest> getRefundRequestRecords() {
      return refundRequestRecords;
   }

   public ConcurrentHashMap<RequestKey, RefundResponse> getRefundResponseRecords() {
      return refundResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicAdvice> getRefundConfirmationRecords() {
      return refundConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getRefundReversalRecords() {
      return refundReversalRecords;
   }

   public ConcurrentHashMap<String, SUVTestServer.VoucherState> getConfirmedExistingVouchers() {
      return confirmedExistingVouchers;
   }

   private ConcurrentHashMap[] getRecords() {
      return new ConcurrentHashMap[] { voucherProvisionRecords, provisionResponseRecords, voucherConfirmationRecords,
            voucherReversalRecords, redemptionRequestRecords, redemptionResponseRecords, redemptionConfirmationRecords,
            redemptionReversalRecords, refundRequestRecords, refundResponseRecords, refundConfirmationRecords,
            refundReversalRecords, confirmedExistingVouchers };
   }

   /**
    * Determines whether the username, password pair is present in any records on the server.
    * 
    * @param username
    * @param password
    * @return true if any record of this username and password is present, false otherwise.
    */
   public boolean doRecordsForUserExist(String username, String password) {
      for (ConcurrentHashMap<RequestKey, Object> map : getRecords()) {
         Enumeration<RequestKey> e = map.keys();
         while (e.hasMoreElements()) {
            RequestKey key = e.nextElement();
            if (key.getUsername().equals(username) && key.getPassword().equals(password))
               return true;
         }
      }
      return false;
   }

   /**
    * Removes all records, requests, or repsonses; which reference this username, password pair. <em>This cannot be
    * undone</em>
    * 
    * @param username
    * @param password
    */
   public void reset(String username, String password) {
      for (ConcurrentHashMap<RequestKey, Object> map : getRecords()) {
         Enumeration<RequestKey> e = map.keys();
         while (e.hasMoreElements()) {
            RequestKey key = e.nextElement();
            if (key.getUsername().equals(username) && key.getPassword().equals(password)) {
               Object record = map.get(key);
               if (record instanceof ProvisionResponse) {
                  ProvisionResponse rsp = ((ProvisionResponse) map.get(key));
                  confirmedExistingVouchers.remove(rsp.getVoucher().getCode());
               }
               map.remove(key);

            }
         }
      }
   }
}
