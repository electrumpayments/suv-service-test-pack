package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IVouchersResource;
import io.electrum.suv.api.VouchersResource;
import io.electrum.suv.api.utils.Paths;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

//TODO Are PATHS necessary
@Path(Paths.BASE_PATH + Paths.PROVISION_VOUCHER)
@Api(description = "the Airtime API", authorizations = { @Authorization("httpBasic") })
public class VouchersResourceImpl extends VouchersResource {
   static IVouchersResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   @Override
   protected IVouchersResource getResourceImplementation() {
      if (instance == null) {
         instance = new IVouchersResourceImpl();
      }
      return instance;
   }

}
