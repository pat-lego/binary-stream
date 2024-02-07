package com.adobe.aem.support.binary.stream.core.servlets.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Binary Stream Config", description = "Configurations to retrieve binaries from AEM")
public @interface BinaryStreamConfig {

    @AttributeDefinition(name = "Allowed Root Paths", type = AttributeType.STRING)
    String[] rootPaths();

    @AttributeDefinition(name = "Allowed Mime Types", type = AttributeType.STRING)
    String[] mimeTypes();

    @AttributeDefinition(name = "Content Disposition Filter Default", description = "Value used if no option provided in the query string parameters", options = {
        @Option(label = "INLINE", value = "INLINE"),
        @Option(label = "ATTACHMENT", value = "ATTACHMENT")
    })
    String contentDisposition() default "INLINE";

}
