package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRefundsResource;
import io.electrum.suv.api.RefundsResource;
import io.electrum.suv.api.utils.Paths;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

//TODO Can this not be generalized across the resource implementations
//TODO Are PATHS necessary
@Path(Paths.BASE_PATH + Paths.REFUND_VOUCHER)
@Api(description = "the Single Use Voucher API", authorizations = { @Authorization("httpBasic") })
public class RefundsResourceImpl extends RefundsResource {
   static IRefundsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   @Override
   protected IRefundsResource getResourceImplementation() {
      if (instance == null) {
         instance = new IRefundsResourceImpl();
      }
      return instance;
   }

}
