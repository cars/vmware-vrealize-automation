package com.inkysea.vmware.vra.jenkins.plugin.model

import org.junit.Test
import java.util.logging.Logger;
import com.inkysea.vmware.vra.jenkins.plugin.model.ExecutionStatus;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import groovy.util.ConfigSlurper;

/**
 * Created by kthieler on 2/23/16.
 */
class RequestTest extends GroovyTestCase {

    private PluginParam params;
    private PrintStream logger;
    private ConfigObject testConfig;


    RequestTest() {

        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            this.params = new PluginParam(testConfig.vRAURL,
                    testConfig.userName,
                    testConfig.password,
                    testConfig.tenant,
                    testConfig.catalogItemName,
                    testConfig.waitExec,
                    false,
                    null
            )                             

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
 
        }
    }


    @Test
    public void testfetchBlueprint() {
        Request request = new Request(logger, params);
        def token = request.fetchBlueprint();
        logger.println(token)
    }

    @Test
    public void testGetBlueprintTemplate() {
        Request request = new Request(logger, params)
        def token = request.GetBlueprintTemplate();
        logger.println(token)
    }

    @Test
    public void testProvisionBlueprint() {
        Request request = new Request(logger, params)
        def token = request.ProvisionBlueprint();

        while (!request.IsRequestComplete()) {
            System.out.println("Execution status : " + request.RequestStatus().toString());
            Thread.sleep(10 * 1000);
        }

        switch (request.RequestStatus().toString()) {
            case "SUCCESSFUL":
                System.out.println("Requested complete successfully");
                break;
            case "FAILED":
                System.out.println("Request execution failed");
                throw new IOException("Request execution failed. Please go to vRA for more details");
            case "REJECTED":
                throw new IOException("Request execution cancelled. Please go to vRA for more details");
        }

        System.out.println("Resource View :"+request.GetResourceView().toString());

    }


}


