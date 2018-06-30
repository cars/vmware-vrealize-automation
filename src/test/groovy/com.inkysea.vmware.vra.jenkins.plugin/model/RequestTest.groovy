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
        System.out.println("\n\n************___testfetchBlueprint___*************");
        Request request = new Request(logger, params);
        def token = request.fetchBlueprint();
        logger.println(token)
    }

    @Test
    public void testGetBlueprintTemplate() {
        System.out.println("\n\n************___testGetBlueprintTemplate___*************");
        Request request = new Request(logger, params)
        def token = request.getBlueprintTemplate();
        System.out.println("Token is :" + token)
        logger.println(token)
    }

    @Test
    public void testProvisionBlueprint() {
        System.out.println("\n\n************___testProvisionBlueprint___*************");        
        Request request = new Request(logger, params)
        def token = request.provisionBlueprint();

        while (!request.isRequestComplete()) {
            System.out.println("Execution status : " + request.requestStatus().toString());
            Thread.sleep(10 * 1000);
        }

        switch (request.requestStatus().toString()) {
            case "SUCCESSFUL":
                System.out.println("Requested complete successfully");
                break;
            case "FAILED":
                System.out.println("Request execution failed");
                throw new IOException("Request execution failed. Please go to vRA for more details");
            case "REJECTED":
                throw new IOException("Request execution cancelled. Please go to vRA for more details");
        }

        System.out.println("Resource View :"+request.getResourceView().toString());

    }


}


