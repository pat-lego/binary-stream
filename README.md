# AEM Binary Stream 

This project allows users to stream binaries out of the repository and include them in other parts of their application. This was built because [1] the Content disposition filter is unsupported in AEMaaCS. The reason for this is because binaries are served out of the repository as URL's representing their location in the blob store.


[1] https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/assets/assets-cloud-changes.html?lang=en


## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.content: contains sample content using the components from the ui.apps
* ui.config: contains runmode specific OSGi configs for the project
* all: a single content package that embeds all of the compiled modules (bundles and content packages) including any vendor dependencies

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

To build all the modules and deploy the `all` package to a local instance of AEM, run in the project root directory the following command:

## How to use

Install the all package in AEM and then use the service as follows

    http://localhost:4502/content/binary/stream.txt/content/dam/html/sample.html?cd=inline

The suffix in the URL represents the binary we want to stream out of the repository and the cd represents the content disposition filter we want to set on the binary. If there is no cd set on the request the default is taken from the OSGi configuration.

