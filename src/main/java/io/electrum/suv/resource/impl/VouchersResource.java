package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IVouchersResource;
import io.electrum.suv.api.utils.Paths;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

//TODO THIS IS ACTUALLY WORKING (PATH)
@Path(Paths.BASE_PATH)
@Api(description = "the SUV API", authorizations = { @Authorization("httpBasic") })
public class VouchersResource extends io.electrum.suv.api.VouchersResource {
   static VouchersResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(VouchersResource.class);

   @Override
   protected IVouchersResource getResourceImplementation() {
      if (instance == null) {
         instance = new VouchersResourceImpl();
      }
      return instance;
   }

}
