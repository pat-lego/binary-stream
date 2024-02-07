package com.adobe.aem.support.binary.stream.core.servlets;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.support.binary.stream.core.servlets.config.BinaryStreamConfig;
import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.Rendition;

import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(resourceTypes = "binary-stream/components/stream", methods = HttpConstants.METHOD_GET, extensions = "txt")
@ServiceDescription("Binary Servlet Resolver")
@Designate(ocd = BinaryStreamConfig.class)
public class BinaryStreamServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final BinaryStreamConfig config;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Activate
    public BinaryStreamServlet(BinaryStreamConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        ResourceResolver resolver = req.getResourceResolver();
        Optional<String> suffix = Optional.ofNullable(req.getRequestPathInfo().getSuffix());

        if (!suffix.isPresent()) {
            logger.error("The suffix cannot be empty");
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<String> results = allowedPaths(suffix);
        if (results.isEmpty()) {
            logger.error("The requested path {} is not a sub path of the allowed configurations", suffix.get());
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Resource resource = resolver.resolve(suffix.get());
        Asset asset = resource.adaptTo(Asset.class);
        Optional<String> contentType = Optional
                .of(asset.getChild("jcr:content/metadata").getValueMap().get("dc:format", String.class));
        if (logger.isDebugEnabled() && contentType.isPresent()) {
            logger.debug("The asset {} content type is set to {}", suffix.get(), contentType);
        }

        if (!contentType.isPresent()) {
            logger.error("The requested path {} does not have a configured mime type", suffix.get());
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        List<String> mimeTypes = allowedMimeTypes(contentType);
        if (mimeTypes.isEmpty()) {
            logger.error("The requested path {} does not an accepted mime type to stream", suffix.get());
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Optional<Rendition> rendition = Optional.ofNullable(asset.getRendition("original"));
        if (!rendition.isPresent()) {
            logger.error("The requested path {} does not contain an original rendition", suffix.get());
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try (InputStream assetBinary = rendition.get().getStream()) {
            resp.setContentType(contentType.get());
            Optional<String> cd = Optional.ofNullable(req.getParameter("cd"));
            if (cd.isPresent()) {
                resp.setHeader("Content-Disposition", cd.get().toLowerCase());
            } else {
                resp.setHeader("Content-Disposition", this.config.contentDisposition().toLowerCase() + ";filename=" + suffix.get().substring(suffix.get().lastIndexOf("/") + 1));
            }
            
            IOUtils.copy(assetBinary, resp.getOutputStream());
            resp.setStatus(SlingHttpServletResponse.SC_OK);
        }
    }

    public List<String> allowedPaths(Optional<String> suffix) {
        return Stream.of(this.config.rootPaths()).filter(r -> suffix.isPresent() && suffix.get().startsWith(r))
                .collect(Collectors.toList());
    }

    public List<String> allowedMimeTypes(Optional<String> suffix) {
        return Stream.of(this.config.mimeTypes()).filter(r -> suffix.isPresent() && suffix.get().startsWith(r))
                .collect(Collectors.toList());
    }
}
