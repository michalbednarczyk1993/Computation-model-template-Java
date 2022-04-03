package com.setcom.computation.datamodel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


class PinConfigurationTest {

    @Test
    public void testConstructor() {
        JSONObject jsonFile;
        Resource resource = new ClassPathResource(
                "src\\test\\resources\\pin_configs_test.json",
                PinConfiguration.class.getClassLoader());
    }

    // https://www.baeldung.com/spring-boot-json-properties
    // https://www.baeldung.com/spring-classpath-file-access
    // https://stackoverflow.com/questions/58703834/how-to-read-json-file-from-resources-in-spring-boot
    // https://www.google.com/search?q=spring+jsonobject+from+file+resources+to+object&rlz=1C1CHBF_enPL862PL862&sxsrf=APq-WBuz3Mz67xkb2xPccgbSjy4jDR_-Bw%3A1648538640528&ei=ELRCYuLtH4377_UPlqOOsAo&oq=spring+jsonobject+from+file+resou&gs_lcp=Cgdnd3Mtd2l6EAMYADIICCEQFhAdEB46CAgAEIAEELEDOgQIABBDOgsIABCABBCxAxCDAToOCC4QgAQQsQMQxwEQowI6DgguEIAEELEDEMcBENEDOgsILhCABBDHARCvAToECCMQJzoGCCMQJxATOgcIABCxAxBDOgoIABCxAxCDARBDOggILhCABBCxAzoFCAAQgAQ6BQgAEMsBOgYIABAWEB46CAgAEBYQChAeOgUIIRCgAToECCEQFToHCCEQChCgAUoECEEYAEoECEYYAFAAWJ5HYJBWaABwAXgAgAF2iAHREJIBBDI2LjGYAQCgAQHAAQE&sclient=gws-wiz
    
}
