package com.inkysea.vmware.vra.jenkins.plugin.model

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import groovy.util.GroovyTestCase;

/**
 * Created by kthieler on 2/23/16.
 */
class BlueprintTest extends GroovyTestCase {

    private BlueprintParam params;
    private PrintStream logger;
    private ConfigObject testConfig;

    protected List<Deployment> deployments = new ArrayList<Deployment>();
    private String cpu = "{ \"data\":{\"WIN\":{\"data\":{\"cpu\":2}}}}";
    private List<RequestParam> requestParam = new ArrayList<RequestParam>();


    BlueprintTest() {

        //Properties prop = new Properties();
        //InputStream input = null;

        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            System.out.println("Blueprint Source PATH = " + testConfig.blueprintPath)
            System.out.println("Catalog Service = " + testConfig.blueprintServiceCategory)
            System.out.println("vRA Server = " + testConfig.vRAURL)
            System.out.println("vRA Tenant = " + testConfig.tenant)            
            this.params = new BlueprintParam(testConfig.vRAURL,
                    testConfig.userName,
                    testConfig.password,
                    testConfig.tenant,
                    false,  //packageBlueprint
                    testConfig.blueprintPath, //blueprintPath
                    true, //overWrite
                    true, //publishBlueprint
                    testConfig.blueprintServiceCategory, //) //serviceCategory
                    testConfig.blueprintTemplateName,
                    false)  //reassign blueprint

        } catch (IOException ex) {
            System.out.println("***___In Catch___*****")            
            ex.printStackTrace();
        } finally{
            System.out.println("***___In Finally___*****")            
        }
    }


    @Test
    public void testBlueprintCreate() {
        System.out.println("\n\n************___testBlueprintCreate___*************");        
        System.out.println("Skipping Blueprint Create Test for now")
        //Blueprint blueprint = new Blueprint(logger, params);

        //blueprint.Create();


    }



    
    @Test
    public void testBlueprintDestroy() {
        System.out.println("\n\n************___testBlueprintDestroy___*************");        
        System.out.println("Skipping Blueprint Destroy Test");

    }

}


