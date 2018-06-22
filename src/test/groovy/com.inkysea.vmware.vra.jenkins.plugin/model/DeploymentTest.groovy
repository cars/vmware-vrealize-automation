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
    private String cpu = "{ \"data\":{\"CentOS7\":{\"data\":{\"cpu\":2}}}}";
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
        Deployment request = new Deployment(logger, params)

        request.Create();
        //System.out.println("Machine List  :   "+request.getMachineList());
        this.deployments.add(request);

        System.out.println("Machine List : "+request.getOutputs());

    }


/*
    @Test
    public void testDestroy() {
        Deployment request = new Deployment(logger, params)

        request.Destroy("CentOS_7-09847286");
    }
*/

    @Test
    public void testJsonMerge() {


        JsonParser parser = new JsonParser();

        Deployment request = new Deployment(logger, params)

        JsonObject parent = request.bluePrintTemplate;

        JsonObject req = parser.parse(cpu);
        System.out.println("JSON to merge : "+req);


        String json = request.merge(parent, req ).toString()
        System.out.println("Merged JSON : "+json);

    }

}


