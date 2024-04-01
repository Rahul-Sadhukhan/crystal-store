package com.walmart.realestate.crystal.storereview.util;

import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import com.walmart.realestate.crystal.storereview.properties.ServiceProviderProperties;
import com.walmart.realestate.crystal.storereview.properties.ServiceRegistryProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.ObjectStreamException;
import java.nio.charset.StandardCharsets;
import java.security.KeyRep;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ServiceRegistryUtil {

    private final ServiceRegistryProperties serviceRegistry;

    public Map<String, String> generateRoutingHeaders(ServiceProviderProperties serviceProvider) {
        long inTimestamp = Instant.now().toEpochMilli();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("wm_consumer.id", serviceRegistry.getConsumerId());
        map.put("wm_consumer.intimestamp", Long.toString(inTimestamp));
        map.put("wm_sec.key_version", serviceRegistry.getPrivateKeyVersion());

        String signature = generateSignature(serviceRegistry.getPrivateKey(), canonicalize(map));

        Map<String, String> routingHeaders = new LinkedHashMap<>();
        routingHeaders.put("wm_svc.name", serviceProvider.getName());
        routingHeaders.put("wm_svc.env", serviceProvider.getEnvironment());
        routingHeaders.putAll(map);
        routingHeaders.put("wm_sec.auth_signature", signature);
        routingHeaders.put("x-tenant", TenantContext.getCurrentTenant());

        return routingHeaders;
    }

    private static String canonicalize(Map<String, String> headersToSign) {
        return headersToSign.keySet().stream()
                .map(headersToSign::get)
                .map(String::trim)
                .collect(Collectors.joining("\n", "", "\n"));
    }

    @SneakyThrows
    private static String generateSignature(String key, String stringToSign) {
        Signature signatureInstance = Signature.getInstance("SHA256WithRSA");

        ServiceKeyRep keyRep = new ServiceKeyRep(KeyRep.Type.PRIVATE, "RSA", "PKCS#8", Base64.getDecoder().decode(key));
        PrivateKey resolvedPrivateKey = (PrivateKey) keyRep.readResolve();
        signatureInstance.initSign(resolvedPrivateKey);

        byte[] bytesToSign = stringToSign.getBytes(StandardCharsets.UTF_8);
        signatureInstance.update(bytesToSign);
        byte[] signatureBytes = signatureInstance.sign();

        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private static class ServiceKeyRep extends KeyRep {

        public ServiceKeyRep(Type type, String algorithm, String format, byte[] encoded) {
            super(type, algorithm, format, encoded);
        }

        @Override
        public Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }

    }

}
