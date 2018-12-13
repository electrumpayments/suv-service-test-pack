package io.electrum.suv.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import io.electrum.suv.server.model.Backend;
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
   private Backend backend;

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

      backend = new Backend();
   }

   public Backend getBackend() {
      return backend;
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
