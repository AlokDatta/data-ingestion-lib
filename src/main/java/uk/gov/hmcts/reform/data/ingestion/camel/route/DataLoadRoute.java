package uk.gov.hmcts.reform.data.ingestion.camel.route;

import static org.apache.commons.lang.WordUtils.uncapitalize;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.DIRECT_ROUTE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.IS_FILE_STALE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.MAPPING_METHOD;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.language.SimpleExpression;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.FileReadProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;

/**
 * This class is Judicial User Profile Router Triggers Orchestrated data loading.
 */
@Component
public class DataLoadRoute {

    @Autowired
    FileReadProcessor fileReadProcessor;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Environment environment;

    @Autowired
    SpringTransactionPolicy springTransactionPolicy;

    @Autowired
    ExceptionProcessor exceptionProcessor;

    @Autowired
    CamelContext camelContext;

    @Autowired
    HeaderValidationProcessor headerValidationProcessor;

    @Transactional("txManager")
    public void startRoute(String startRoute, List<String> routesToExecute) throws FailedToCreateRouteException {

        List<RouteProperties> routePropertiesList = getRouteProperties(routesToExecute);

        try {
            camelContext.addRoutes(
                    new SpringRouteBuilder() {
                        @Override
                        public void configure() throws Exception {

                            onException(Exception.class)
                                    .handled(true)
                                    .process(exceptionProcessor)
                                    .markRollbackOnly()
                                    .end();

                            String[] multiCastRoute = createDirectRoutesForMulticast(routesToExecute);

                            //Started direct route with multi-cast all the configured routes with
                            //Transaction propagation required eg.application-jrd-router.yaml(rd-judicial-data-load)
                            from(startRoute)
                                    .transacted()
                                    .policy(springTransactionPolicy)
                                    .multicast()
                                    .stopOnException()
                                    .to(multiCastRoute).end();


                            for (RouteProperties route : routePropertiesList) {

                                Expression exp = new SimpleExpression(route.getBlobPath());

                                from(DIRECT_ROUTE + route.getRouteName()).id(DIRECT_ROUTE + route.getRouteName())
                                        .transacted()
                                        .policy(springTransactionPolicy)
                                        .setHeader(MappingConstants.ROUTE_DETAILS, () -> route)
                                        .setProperty(MappingConstants.BLOBPATH, exp)
                                        .process(fileReadProcessor)
                                        .choice()
                                            .when(header(IS_FILE_STALE).isEqualTo(false))
                                                .process(headerValidationProcessor)
                                                .split(body()).unmarshal().bindy(BindyType.Csv,
                                                applicationContext.getBean(route.getBinder()).getClass())
                                                .to(route.getTruncateSql())
                                                .process((Processor) applicationContext.getBean(route.getProcessor()))
                                                .split().body()
                                                .streaming()
                                                .bean(applicationContext.getBean(route.getMapper()), MAPPING_METHOD)
                                                .to(route.getSql())
                                        .end();
                            }
                        }
                    });
        } catch (Exception ex) {
            throw new FailedToCreateRouteException(" Data Load - failed to start for route ", startRoute,
                startRoute, ex);
        }
    }

    private String[] createDirectRoutesForMulticast(List<String> routeList) {
        int index = 0;
        String[] directRouteNameList = new String[routeList.size()];
        for (String child : routeList) {
            directRouteNameList[index] = (DIRECT_ROUTE).concat(child);
            index++;
        }
        return directRouteNameList;
    }

    /**
     * Sets Route Properties.
     *
     * @param routes routes
     * @return List RouteProperties.
     */
    private List<RouteProperties> getRouteProperties(List<String> routes) {
        List<RouteProperties> routePropertiesList = new LinkedList<>();
        int index = 0;
        for (String routeName : routes) {
            RouteProperties properties = new RouteProperties();
            properties.setRouteName(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.ID));
            properties.setSql(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.INSERT_SQL));
            properties.setTruncateSql(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TRUNCATE_SQL)
                    == null ? "log:test" : environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TRUNCATE_SQL));
            properties.setBlobPath(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.BLOBPATH));
            properties.setMapper(uncapitalize(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.MAPPER)));
            properties.setBinder(uncapitalize(environment.getProperty(MappingConstants.ROUTE + "." + routeName + "."
                    + MappingConstants.CSVBINDER)));
            properties.setProcessor(uncapitalize(environment.getProperty(MappingConstants.ROUTE + "." + routeName + "."
                    + MappingConstants.PROCESSOR)));
            properties.setFileName(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.FILE_NAME));
            properties.setTableName(environment.getProperty(
                    MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TABLE_NAME));
            routePropertiesList.add(index, properties);
            index++;
        }
        return routePropertiesList;
    }
}
