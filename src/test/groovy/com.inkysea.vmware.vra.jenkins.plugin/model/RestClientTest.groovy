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
    //private String tmpPassword;
    private List<RequestParam> requestParam = new ArrayList<RequestParam>();
    RestClientTest() {

        this.logger = logger;
        System.out.println("starting rest client test");
      
        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            System.out.println("vRAURL = "+ testConfig.vRAURL);
            System.out.println("userName = "+ testConfig.userName);
            System.out.println("password = "+ testConfig.password);
            System.out.println("tenant = "+ testConfig.tenant);            
            System.out.println("catalogItemName = "+ testConfig.catalogItemName);
            System.out.println("waitExec= "+ testConfig.waitExec);     
            System.out.println("requestParam= "+ testConfig.requestParam);     
            //String tmpPassword = testConfig.password;
            this.params = new PluginParam(testConfig.vRAURL,
                    testConfig.userName,
                    testConfig.password,
                    testConfig.tenant,
                    testConfig.catalogItemName,
                    testConfig.waitExec,
                    false,
                    requestParam
            )
            
  
            System.out.println("Params Next= ");     
            System.out.println("Params= "+ this.params);     
        } catch (IOException ex) {
                        System.out.println("error");     
            ex.printStackTrace();
        } finally{
 
        }
    }

    @Test
    public void testAuth2(){
        System.out.println("\n\n************___testAuth2___*************");        
        System.out.println("In TesTAuth2")
        System.out.println("Server:"+params.serverUrl)
        System.out.println("CatalogItem:"+params.blueprintName)
        System.out.println("User:"+params.userName)
          logger.println(params)
        RestClient connect = new RestClient( testConfig.vRAURL, testConfig.userName, "!QAZ2wsx", testConfig.tenant )
        def token = connect.token;
        logger.println(token)

    }

    @Test
    public void testAuth() {
        System.out.println("\n\n************___testAuth___*************");        
        System.out.println("In TesTAuth")
        System.out.println(params)
          logger.println(params)
        RestClient connect = new RestClient( params )
        def token = connect.token;
        logger.println(token)
    }
    
}

