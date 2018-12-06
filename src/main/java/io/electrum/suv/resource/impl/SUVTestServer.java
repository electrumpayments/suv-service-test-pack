package io.electrum.suv.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import io.dropwizard.jersey.validation.DropwizardConfiguredValidator;
import io.dropwizard.jersey.validation.HibernateValidationFeature;
import io.dropwizard.jersey.validation.Validators;
import io.electrum.suv.api.models.*;
import io.electrum.suv.server.SUVViolationExceptionMapper;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.TenderAdvice;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import io.electrum.suv.server.util.RequestKey;
import io.electrum.vas.model.BasicReversal;

public class SUVTestServer extends ResourceConfig {

   // The value of the hashmap is a class in the models,
   // can be found (with its record name pair) in the docs under params/schema for each request type.
   private ConcurrentHashMap<RequestKey, ProvisionRequest> voucherProvisionRecords;
   private ConcurrentHashMap<RequestKey, ProvisionResponse> voucherResponseRecords; // Holds response returned to vendor
                                                                                    // after voucher is provisioned.
   private ConcurrentHashMap<RequestKey, TenderAdvice> voucherConfirmationRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords;

   private ConcurrentHashMap<RequestKey, BasicAdvice> redemptionConfirmationRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> redemptionReversalRecords;
   private ConcurrentHashMap<RequestKey, RedemptionRequest> redemptionRequestRecords;
   private ConcurrentHashMap<RequestKey, RedemptionResponse> redemptionResponseRecords;

   private ConcurrentHashMap<RequestKey, BasicAdvice> refundConfirmationRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> refundReversalRecords;
   private ConcurrentHashMap<RequestKey, RefundRequest> refundRequestRecords;
   private ConcurrentHashMap<RequestKey, RefundResponse> refundResponseRecords;

   // This hashmap stores the relationship between purchase references and purchase request id's so a purchase reference
   // can be used to retrieve the correlated purchase request id
   private ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords;

   /** This hashmap stores the mapping between a voucher code and the request key to access its {@link SUVTestServer#voucherConfirmationRecords} entry. */
//   private ConcurrentHashMap<String, RequestKey> voucherCodeRequestKeyConfirmationRecords;
   /** This hashmap stores the mapping between a voucher code and the request key to access its {@link SUVTestServer#redemptionRequestRecords} entry. */
//   private ConcurrentHashMap<String, RequestKey> voucherCodeRequestKeyRedemptionRecords;

   private ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers;


   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   public SUVTestServer() {
      packages(SUVTestServer.class.getPackage().getName());

      register(MyObjectMapperProvider.class);
      register(JacksonFeature.class);

      register(
            new HibernateValidationFeature(
                  new DropwizardConfiguredValidator(Validators.newValidatorFactory().getValidator())));
      register(new SUVViolationExceptionMapper());

      voucherProvisionRecords = new ConcurrentHashMap<>();
      voucherResponseRecords = new ConcurrentHashMap<>();
      voucherConfirmationRecords = new ConcurrentHashMap<>();
      voucherReversalRecords = new ConcurrentHashMap<>();

      redemptionConfirmationRecords = new ConcurrentHashMap<>();
      redemptionReversalRecords = new ConcurrentHashMap<>();
      redemptionRequestRecords = new ConcurrentHashMap<>();
      redemptionResponseRecords = new ConcurrentHashMap<>();

      refundConfirmationRecords = new ConcurrentHashMap<>();
      refundReversalRecords = new ConcurrentHashMap<>();
      refundRequestRecords = new ConcurrentHashMap<>();
      refundResponseRecords = new ConcurrentHashMap<>();

      purchaseReferenceRecords = new ConcurrentHashMap<>();

//      voucherCodeRequestKeyConfirmationRecords = new ConcurrentHashMap<>();
//      voucherCodeRequestKeyRedemptionRecords = new ConcurrentHashMap<>();

      confirmedExistingVouchers = new ConcurrentHashMap<>();

      log.debug("Initialising new TestServer");
   }

   // TODO CodeStyle reorder methods
   // TODO Generate setters as needed

   public ConcurrentHashMap<RequestKey, ProvisionResponse> getVoucherResponseRecords() {
      return voucherResponseRecords;
   }



   public ConcurrentHashMap<RequestKey, BasicAdvice> getRedemptionConfirmationRecords() {
      return redemptionConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getRedemptionReversalRecords() {
      return redemptionReversalRecords;
   }



   public ConcurrentHashMap<RequestKey, BasicAdvice> getRefundConfirmationRecords() {
      return refundConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getRefundReversalRecords() {
      return refundReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, ProvisionRequest> getVoucherProvisionRecords() {
      return voucherProvisionRecords;
   }

   public ConcurrentHashMap<RequestKey, TenderAdvice> getVoucherConfirmationRecords() {
      return voucherConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getVoucherReversalRecords() {
      return voucherReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, String> getPurchaseReferenceRecords() {
      return purchaseReferenceRecords;
   }

   public ConcurrentHashMap<RequestKey, RedemptionRequest> getRedemptionRequestRecords() {
      return redemptionRequestRecords;
   }

   public ConcurrentHashMap<RequestKey, RefundRequest> getRefundRequestRecords() {
      return refundRequestRecords;
   }

   public ConcurrentHashMap<RequestKey, RedemptionResponse> getRedemptionResponseRecords() {
      return redemptionResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, RefundResponse> getRefundResponseRecords() {
      return refundResponseRecords;
   }

//    public ConcurrentHashMap<String, RequestKey> getVoucherCodeRequestKeyConfirmationRecords() {
//        return voucherCodeRequestKeyConfirmationRecords;
//    }

//   public ConcurrentHashMap<String, RequestKey> getVoucherCodeRequestKeyRedemptionRecords() {
//      return voucherCodeRequestKeyRedemptionRecords;
//   }

    public ConcurrentHashMap<String, VoucherState> getConfirmedExistingVouchers() {
        return confirmedExistingVouchers;
    }


    @Provider
   public static class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

      private final ObjectMapper mapper;
      private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

      public MyObjectMapperProvider() {
         mapper = new ObjectMapper();
         // mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         // mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         // mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         // mapper.setPropertyNamingStrategy(LOWER_CASE_WITH_HYPHEN_STRATEGY);
         // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         // DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
         // mapper.setDateFormat(DATE_FORMAT);
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
         mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
         mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
         mapper.registerModule(new JodaModule());
         DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
         mapper.setDateFormat(DATE_FORMAT);
      }

      @Override
      public ObjectMapper getContext(Class<?> type) {
         return mapper;
      }
   }

   @SuppressWarnings("serial")
   private static class LowerCaseWitHyphenStrategy extends PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy {
      @Override
      public String translate(String input) {
         String output = super.translate(input);
         return output == null ? null : output.replace('_', '-');
      }
   }

   //TODO Refactor
   public enum VoucherState{
       CONFIRMED_PROVISIONED(0),
       REDEEMED(1),
       CONFIRMED_REDEEMED(2),
       REFUNDED(3);

       private int value;

       VoucherState(int value){
           this.value = value;
       }

       public int getValue(){
           return value;
       }
   }
}
