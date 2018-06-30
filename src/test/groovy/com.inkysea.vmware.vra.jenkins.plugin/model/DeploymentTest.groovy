package com.inkysea.vmware.vra.jenkins.plugin.model

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import groovy.util.ConfigSlurper;

/**
 * Created by kthieler on 2/23/16.
 */
class DeploymentTest extends GroovyTestCase {

    private PluginParam params;
    private RequestParam rParams;

    private PrintStream logger;
    private ConfigObject testConfig;

    protected List<Deployment> deployments = new ArrayList<Deployment>();
    private String cpu = "{ \"data\":{\"WIN\":{\"data\":{\"cpu\":2}}}}";
    private List<RequestParam> requestParam = new ArrayList<RequestParam>();



    DeploymentTest() {

        
        try {

            testConfig = new ConfigSlurper().parse(new File('src/test/resources/config.properties').toURL());
            this.rParams = new RequestParam(testConfig.requestParam)
            this.requestParam.add(rParams)
            this.params = new PluginParam(testConfig.vRAURL,
                    testConfig.userName,
                    testConfig.password,
                    testConfig.tenant,
                    testConfig.catalogItemName,
                    testConfig.waitExec,
                    false,
                    this.requestParam
            )


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{

        }
    }


    @Test
    public void testCreateAndDestroy() {
        System.out.println("\n\n************___testCreateAndDestroy___*************\n");        
        System.out.println("Skipping this test for now unsure of purpose");
        /*Deployment request = new Deployment(logger, params)

        request.create();
        //System.out.println("Machine List  :   "+request.getMachineList());
        this.deployments.add(request);

        System.out.println("DeploymentTest-*-Machine List : "+request.getOutputs());
    */
    }
    


/*
    @Test
    public void testDestroy() {
        System.out.println("\n\n************___testDestroy___*************\n");        
        Deployment request = new Deployment(logger, params)

        request.destroy("CentOS_7-09847286");
    }
*/

    @Test
    public void testJsonMerge() {
        System.out.println("\n\n************___testJsonMerge___*************\n");        
        System.out.println("Skipping this test for now unsure of purpose");
        /*
        JsonParser parser = new JsonParser();

        JsonObject req = parser.parse(cpu);
        System.out.println("DeploymentTest-*-JSON to merge : "+req);

        Deployment request = new Deployment(logger, params)
        System.out.println("DeploymentTest-*- New deployment completed"+ request.toString());

        request.create();
        System.out.println("DeploymentTest-*-Request created");

        JsonObject parent = request.blueprintTemplate;
        System.out.println("DeploymentTest-*-parent created");

        System.out.println("DeploymentTest-*-Getting ready to merge \n["+parent.toString()+"]\n&\n["+req.toString()+"]");
        String json = request.merge(parent,req ).toString()
        System.out.println("DeploymentTest-*-Merged JSON : "+json);
        */

    }

}


