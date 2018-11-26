package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRedemptionsResource;
import io.electrum.suv.api.RedemptionsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

//TODO Are PATHS necessary
//@Path(Paths.BASE_PATH + Paths.REDEEM_VOUCHER)
@Api(description = "the Single Use Voucher API", authorizations = { @Authorization("httpBasic") })
public class RedemptionsResourceImpl extends RedemptionsResource {
   static RedemptionsResourceInterfaceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(RedemptionsResourceImpl.class);

   @Override
   protected IRedemptionsResource getResourceImplementation() {
      if (instance == null) {
         instance = new RedemptionsResourceInterfaceImpl();
      }
      return instance;
   }

}
