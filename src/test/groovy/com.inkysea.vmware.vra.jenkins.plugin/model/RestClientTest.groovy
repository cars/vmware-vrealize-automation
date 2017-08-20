package com.inkysea.vmware.vra.jenkins.plugin.model

import groovy.util.GroovyTestCase;
import groovy.util.ConfigSlurper;
import org.junit.Test;

/**
 * Created by kthieler on 2/23/16.
 */
class RestClientTest extends GroovyTestCase  {

    private PluginParam params;
    private PrintStream logger;
    private ConfigObject testConfig;

    RestClientTest() {

        this.logger = logger;
        System.out.println("starting rest client test");
      
        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            this.params = new PluginParam(testConfig.vRAURL,
                    testConfig.userName,
                    testConfig.password,
                    testConfig.tenant,
                    testConfig.bluePrintName,
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
    public void testAuth() {
        RestClient connect = new RestClient( params )
        def token = connect.token;
        logger.println(token)
    }
}

