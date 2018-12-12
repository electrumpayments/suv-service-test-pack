package io.electrum.suv.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import io.dropwizard.jersey.validation.DropwizardConfiguredValidator;
import io.dropwizard.jersey.validation.HibernateValidationFeature;
import io.dropwizard.jersey.validation.Validators;
import io.electrum.suv.api.models.*;
import io.electrum.suv.server.SUVFormatViolationExceptionMapper;
import io.electrum.suv.server.SUVHibernateViolationExceptionMapper;
import io.electrum.suv.server.SUVUnrecognizedFieldViolationExceptionMapper;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;

public class SUVTestServer extends ResourceConfig {

   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

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

   private final ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers;// records state of existing vouchers

   public SUVTestServer() {
      packages(SUVTestServer.class.getPackage().getName());

      register(MyObjectMapperProvider.class);
      register(JacksonFeature.class);

      register(
            new HibernateValidationFeature(
                  new DropwizardConfiguredValidator(Validators.newValidatorFactory().getValidator())));
      register(new SUVHibernateViolationExceptionMapper());
      register(new SUVFormatViolationExceptionMapper());
      register(new SUVUnrecognizedFieldViolationExceptionMapper());

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

   public ConcurrentHashMap<String, VoucherState> getConfirmedExistingVouchers() {
      return confirmedExistingVouchers;
   }

   /** Represents the state of the voucher an assigns an ordering to the states */
   public enum VoucherState {
      CONFIRMED_PROVISIONED(0), REDEEMED(1), CONFIRMED_REDEEMED(2), REFUNDED(3);

      private final int value;

      VoucherState(int value) {
         this.value = value;
      }

      public int getValue() {
         return value;
      }
   }

   @Provider
   public static class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

      private final ObjectMapper mapper;
      private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

      public MyObjectMapperProvider() {
         mapper = new ObjectMapper();
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
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

}
