package com.inkysea.vmware.vra.jenkins.plugin.model

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import groovy.util.GroovyTestCase;

/**
 * Created by kthieler on 2/23/16.
 */
class BlueprintParamTest extends GroovyTestCase {

    private BlueprintParam params;
    private PrintStream logger;
    private ConfigObject testConfig;

    //protected List<Deployment> deployments = new ArrayList<Deployment>();
    //private String cpu = "{ \"data\":{\"CentOS7\":{\"data\":{\"cpu\":2}}}}";
    //private List<RequestParam> requestParam = new ArrayList<RequestParam>();


    BlueprintParamTest() {

        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            System.out.println("BlueprintPATH = " + testConfig.blueprintPath)
            System.out.println("blueprintTemplateName = " + testConfig.blueprintTemplateName)
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
            ex.printStackTrace();
        } finally{

        }
    }

    @Test
    public void testBlueprintValidation() {
        System.out.println("Validating args...");
        params.validate();
        System.out.println("Validated?");
    }

    @Test
    public void testBlueprintValidation2() {
        //Test second constructor
        System.out.println("Validating args...");
        System.out.println("Placeholder")
        //BlueprintParam params2 = new BlueprintParam(params);

        //params2.validate();
        System.out.println("Validated?");
    }



}


