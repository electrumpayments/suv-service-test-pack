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
@Path("/suv/v1/vouchers")
@Api(description = "the SUV API", authorizations = { @Authorization("httpBasic") })
public class VouchersResourceImpl extends VouchersResource {
   static VouchersResourceInterfaceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(VouchersResourceImpl.class);

   @Override
   protected IVouchersResource getResourceImplementation() {
      if (instance == null) {
         instance = new VouchersResourceInterfaceImpl();
      }
      return instance;
   }

}
