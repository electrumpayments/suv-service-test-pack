package io.electrum.suv.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import io.electrum.suv.api.models.*;
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

   // private ConcurrentHashMap<RequestKey, VoucherRequest> provisionVoucherRecords;
   // private ConcurrentHashMap<RequestKey, VoucherResponse> voucherResponseRecords;
   // private ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords;
   // private ConcurrentHashMap<RequestKey, VoucherConfirmation> voucherConfirmationRecords;
   //
   // private ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords;
   // private ConcurrentHashMap<RequestKey, PurchaseReversal> purchaseReversalRecords;
   // private ConcurrentHashMap<RequestKey, PurchaseConfirmation> purchaseConfirmationRecords;
   // private ConcurrentHashMap<RequestKey, PurchaseResponse> purchaseResponseRecords;

   // TODO Fill these based on the operations specified in the docs (up to 9)
   // The value of the hashmap is a class in the models,
   // can be found (with its record name pair) in the docs under params/schema for each request type.
   private ConcurrentHashMap<RequestKey, RedemptionRequest> voucherRedemptionRecords;
   private ConcurrentHashMap<RequestKey, BasicAdvice> redemptionConfirmationRecourds;
   private ConcurrentHashMap<RequestKey, BasicReversal> redemptionReversalRecourds;
   private ConcurrentHashMap<RequestKey, RefundRequest> voucherRefundRecords;
   private ConcurrentHashMap<RequestKey, BasicAdvice> refundConfirmationRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> refundReversalRecords;
   private ConcurrentHashMap<RequestKey, ProvisionRequest> voucherProvisionRecords;
   private ConcurrentHashMap<RequestKey, TenderAdvice> voucherConfirmationRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords;

   // This hashmap stores the relationship between purchase references and purchase request id's so a purchase reference
   // can be used to retrieve the correlated purchase request id
   private ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords;

   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   public SUVTestServer() {
      packages(SUVTestServer.class.getPackage().getName());

      register(MyObjectMapperProvider.class);
      register(JacksonFeature.class);
      register(VouchersResourceImpl.class);

      //TODO Figure out what this does
//      register(
//            new HibernateValidationFeature(
//                  new DropwizardConfiguredValidator(Validators.newValidatorFactory().getValidator())));
//      register(new AirtimeViolationExceptionMapper());

      // TODO Convert these to the new hashmaps
      voucherRedemptionRecords = new ConcurrentHashMap<>();
      redemptionConfirmationRecourds = new ConcurrentHashMap<>();
      redemptionReversalRecourds = new ConcurrentHashMap<>();
      voucherRefundRecords = new ConcurrentHashMap<>();
      refundConfirmationRecords = new ConcurrentHashMap<>();
      refundReversalRecords = new ConcurrentHashMap<>();
      voucherProvisionRecords = new ConcurrentHashMap<>();
      voucherConfirmationRecords = new ConcurrentHashMap<>();
      voucherReversalRecords = new ConcurrentHashMap<>();
      purchaseReferenceRecords = new ConcurrentHashMap<>();

      log.debug("Initialising new TestServer");
   }

   // TODO Generate getters/setters

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
}
