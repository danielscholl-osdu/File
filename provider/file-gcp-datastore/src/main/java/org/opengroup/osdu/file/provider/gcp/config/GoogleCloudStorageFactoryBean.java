package org.opengroup.osdu.file.provider.gcp.config;

import org.opengroup.osdu.core.gcp.multitenancy.GcsMultiTenantAccess;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class GoogleCloudStorageFactoryBean extends AbstractFactoryBean<GcsMultiTenantAccess> {

    @Override
    protected GcsMultiTenantAccess createInstance() throws Exception {

        return new GcsMultiTenantAccess();
    }


    @Override
    public Class<?> getObjectType() {
        return GcsMultiTenantAccess.class;
    }
}
