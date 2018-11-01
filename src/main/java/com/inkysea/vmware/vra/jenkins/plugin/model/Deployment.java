package com.inkysea.vmware.vra.jenkins.plugin.model;

import java.io.IOException;
import java.io.PrintStream;
//import java.io.StringReader;
import java.util.logging.Logger;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
//import com.google.gson.stream.JsonReader;
//import net.sf.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Created by kthieler on 2/24/16.
 */
public class Deployment {

    private static final Logger LOGGER = Logger.getLogger(Deployment.class.getName());
    private PluginParam params;
    private DestroyParam dParams;

    private Request request;
    private PrintStream logger;
    //private String DESTROY_TEMPLATE_URL;
    //private String DESTROY_URL;
    private String deploymentName;
    private String parentResourceID;
    private JsonObject deploymentResources;
    private String businessGroupId;
    private String tenantId;
    public JsonObject blueprintTemplate;
    private String catalogID;
    private String subtenantRef;

    private String jsonString = "{\"@type\":\"ResourceActionRequest\", \"resourceRef\":{\"id\":\"\"}, \"resourceActionRef\"\n" +
            ":{\"id\":\"\"}, \"organization\":{\"tenantRef\":\"\", \"tenantLabel\"\n" +
            ":\"\", \"subtenantRef\":\"\", \"subtenantLabel\":\"\"\n" +
            "}, \"state\":\"SUBMITTED\", \"requestNumber\":0, \"requestData\":{\"entries\":[]}}";

    private Set<String> componentSet = new HashSet<String>();

    private List<List<String>> machineList = new ArrayList<List<String>>();
    private ArrayList<String> machineDataList = new ArrayList<String>();



    private List<List<String>> loadBalancerList = new ArrayList<List<String>>();
    private ArrayList<String> loadBalancerDataList = new ArrayList<String>();


    public Deployment(PrintStream logger, PluginParam params) throws IOException {

        this.params = params;
        this.logger = logger;

        this.request  = new Request(logger, params);

    }

    public Deployment(PrintStream logger, DestroyParam params) throws IOException {

        this.dParams = params;
        this.logger = logger;

        this.request  = new Request(logger, params);

    }

    public boolean create() throws IOException, InterruptedException {
        LOGGER.entering(this.getClass().getSimpleName(),"create()");	
        boolean rcode = false;

        if ( params.getRequestTemplate()) {

            logger.println("Requesting Blueprint Template");
            //logger.debug("Requesting Blueprint Template");
            this.blueprintTemplate = this.request.getBlueprintTemplate();
            JsonParser parser = new JsonParser();

            for ( RequestParam option : params.getRequestParams()){
                if ( option.getJson() == null ){

                    logger.println("Request Parameter is null. skipping to next parameter");

                }else {
                    logger.println("Request Parameter : " + option.getJson());
                    //
                    System.out.println("BlueprintTemplate ="+ this.blueprintTemplate.toString());
                    logger.println("BlueprintTemplate ="+ this.blueprintTemplate.toString());
                    logger.println("Option ="+ option.getJson().toString());
                    this.blueprintTemplate = merge(this.blueprintTemplate.getAsJsonObject(),
                            parser.parse(option.getJson()).getAsJsonObject());
                }
            }
            request.provisionBlueprint(this.blueprintTemplate);

        }else{
            logger.println("_NOT_ Requesting Blueprint Template");        
            //logger.
            //logger.debug("_NOT_ Requesting Blueprint Template");
            JsonObject bpDetails = request.fetchBlueprint();

            JsonArray contentArray = bpDetails.getAsJsonArray("content");

            for (JsonElement content : contentArray ){

                if( content.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(params.getBlueprintName())){

                    this.catalogID= content.getAsJsonObject().get("catalogItemId").getAsString();

                    JsonArray orgArray = content.getAsJsonObject().getAsJsonArray("entitledOrganizations");

                    for ( JsonElement org : orgArray ){
                        if( org.getAsJsonObject().get("tenantLabel").getAsString() != null) {
                            this.subtenantRef= org.getAsJsonObject().get("subtenantRef").getAsString();
                            break;
                        }

                    }

                    break;
                }
            }

            if(this.catalogID == null ){
                throw new IOException("Did not find the catalogID value from the provided blueprint : "
                        +params.getBlueprintName()+"\nPlease validate blueprint name in vRA");
            }



            if(this.subtenantRef == null ){
                throw new IOException("Did not find the subtenantRef value from the provided tenant name : "
                                        +params.getTenant()+"\nPlease validate tenant name in vRA");
            }

            this.blueprintTemplate = requestCreateJSON();
            String json = this.blueprintTemplate.toString();
            logger.println("Requesting Blueprint with JSON body : " + json);
            request.postRequestJson(this.blueprintTemplate.toString());
        }


        if (this.params.isWaitExec()) {
            while (!request.isRequestComplete()) {
                System.out.println("Execution status : " + request.requestStatus().toString());
                Thread.sleep(10 * 1000);
            }

            //Need to decide whether to let PARTIALLY_SUCCESSFUL = Success or failure? 
            // right now it's a failure
            switch (request.requestStatus()) {
                case SUCCESSFUL:
                    if (request.requestJobState().equals("SUCCESSFUL")){
                        System.out.println("Request completed successfully");
                        deploymentResources();
                        rcode = true;
                        break;
                    } else {
                        System.out.println("Request execution marked [partially successfull| " +request.requestJobState()+ "] failing build");
                        rcode = false;
                        throw new IOException("Request execution failed(Partial Success|"+request.requestJobState()+"|: Please go to vRA for more details");
                    }
                case FAILED:
                    rcode = false;
                    throw new IOException("Request execution failed. Please go to vRA for more details");
                case REJECTED:
                    rcode = false;
                    throw new IOException("Request execution cancelled. Please go to vRA for more details");
            }
        }
        LOGGER.exiting(this.getClass().getSimpleName(),"create()");	
        return rcode;

    }

    //creates json request for provisioning blueprint? 
    private JsonObject requestCreateJSON() {
        LOGGER.entering(this.getClass().getSimpleName(),"requesCreateJSON()");	

        
        JsonObject requestJson = new JsonObject();
        // add a property 
        requestJson.addProperty("@type", "CatalogItemRequest");

        JsonObject catalogItemRef = new JsonObject();
        catalogItemRef.addProperty("id", this.catalogID);
        requestJson.add("catalogItemRef", catalogItemRef);

        JsonObject organization = new JsonObject();
        organization.addProperty("tenantRef", this.params.getTenant());
        organization.addProperty("subtenantRef", this.subtenantRef);
        requestJson.add("organization", organization);

        requestJson.addProperty("requestedFor", this.params.getUserName());
        requestJson.addProperty("state", "SUBMITTED");
        requestJson.addProperty("requestNumber", 0);

        JsonObject requestData = new JsonObject();

        JsonArray entriesArray = new JsonArray();
        JsonObject entries = new JsonObject();

        entries.addProperty("key", "requestedFor");

        JsonObject value = new JsonObject();
        value.addProperty("type", "string");
        value.addProperty("value", this.params.getUserName());

        entries.add("value", value);

        entriesArray.add(entries);

        for (RequestParam option : params.getRequestParams()) {

            if (option.getJson() == null) {

                logger.println("Request Parameter is null. skipping to next parameter");

            } else {
                System.out.println("Request Parameter : " + option.getJson().toString());
                //logger.println("Request Parameter : " + option.getJson().toString());
                JsonElement response = new JsonParser().parse(option.getJson()).getAsJsonObject();
                entriesArray.add(response);
            }

        }

        requestData.add("entries", entriesArray);
        requestJson.add("requestData", requestData);
        LOGGER.exiting(this.getClass().getSimpleName(),"requestCreateJSON()");	

        return requestJson;

    }


    private void getMachineList() {
        LOGGER.entering(this.getClass().getSimpleName(),"getMachineList()");
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        for (JsonElement content : contentArray) {
            ArrayList<String> tmpmachineDataList = new ArrayList<String>();
            LOGGER.finer("This resourceType is ["+ content.getAsJsonObject().get("resourceType").getAsString()+"]");
            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Virtual")) {
                //LOGGER.finest("looking at "+ content.toString());
                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");
                JsonArray  networkArray = jsonData.getAsJsonArray("NETWORK_LIST");
                LOGGER.finest("Adding Component ["+jsonData.getAsJsonObject().get("Component").getAsString()+"]");
                componentSet.add(jsonData.getAsJsonObject().get("Component").getAsString());

                for (JsonElement e : networkArray) {
                    JsonElement jsonNetworkData = e.getAsJsonObject().get("data");
                    LOGGER.finest("Processing networkArray elements for this resource");
                    tmpmachineDataList.add(content.getAsJsonObject().get("resourceType").getAsString());
                    tmpmachineDataList.add(jsonData.getAsJsonObject().get("Component").getAsString());
                    tmpmachineDataList.add(content.getAsJsonObject().get("name").getAsString());
                    tmpmachineDataList.add(jsonNetworkData.getAsJsonObject().get("NETWORK_NAME").getAsString());
                    LOGGER.finest("MAC Address is ["+jsonNetworkData.getAsJsonObject().get("NETWORK_MAC_ADDRESS").getAsString()+"]");
                    tmpmachineDataList.add(jsonNetworkData.getAsJsonObject().get("NETWORK_MAC_ADDRESS").getAsString());
                                        //Based on personal experience may have situation in which NIC is present on a network but doesn't 
                    // have an IP assigned, ex: DHCP
                    //
                    LOGGER.finest("Trying to check NETWORK_ADDRESS");
                    if (jsonNetworkData.getAsJsonObject().has("NETWORK_ADDRESS")){
                        LOGGER.finest("NETWORK_ADDRESS seems to exist");
                        LOGGER.finest("Network_address value is ["+ jsonNetworkData.getAsJsonObject().get("NETWORK_ADDRESS").getAsString()+"]");
                        tmpmachineDataList.add(jsonNetworkData.getAsJsonObject().get("NETWORK_ADDRESS").getAsString());
                    } else {
                        LOGGER.finest("NETWORK_ADDRESS does _NOT_ seem to exist");
                        tmpmachineDataList.add("AddressUnset");
                    } 
                    LOGGER.finest("Doing machineList.add");
                    machineList.add(tmpmachineDataList);
                    LOGGER.finest("Back from machineList.add");

                }
            }
        }
        LOGGER.exiting(this.getClass().getSimpleName(),"getMachineList()");	

    }

    private void getLoadBalancerList() {
        LOGGER.entering(this.getClass().getSimpleName(),"getLoadBalancerList()");	

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Network.LoadBalancer")) {

                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");

                loadBalancerDataList.add(content.getAsJsonObject().get("resourceType").getAsString());
                loadBalancerDataList.add(jsonData.getAsJsonObject().get("Name").getAsString());
                loadBalancerDataList.add(jsonData.getAsJsonObject().get("LoadBalancerInfo").getAsString());

                loadBalancerList.add(loadBalancerDataList);

                }

            }

        LOGGER.exiting(this.getClass().getSimpleName(),"getLoadBalancerList()");	
    }

    public Map <String, String> getMachineHashMap() throws IOException {
        LOGGER.entering(this.getClass().getSimpleName(),"getMachineHashMap()");	
        Map machineMap = null;

        getMachineList();


        for( List machine : this.machineList ){
            //creat map named group__machine_name__network_name :  network address
            for ( Object data : machine ){
                LOGGER.finest("Building Map? "+data.toString());
                System.out.println(data.toString());
            }

        }

        LOGGER.exiting(this.getClass().getSimpleName(),"getMachineHashMap()");	
        return machineMap;
    }

    public Map<String, String> getDeploymentComponents(String count) {
        LOGGER.entering(this.getClass().getSimpleName(),"getDeploymentComponents()");	
        // Prefix outputs with stack name to prevent collisions with other stacks created in the same build.
        LOGGER.finer("Got "+ count.toString()+" components");
        HashMap<String, String> map = new HashMap<String, String>();

        String deploymentName = getDeploymentName();
        String prefixDep = "VRADEP_"+count+"_";
        map.put( prefixDep+"NAME", deploymentName);
        map.put( prefixDep+"TENANT", params.getTenant());


        getMachineList();

        int componentCounter=1;
        for ( String component : componentSet ) {
            LOGGER.finest("ComponentSet: Deployment Component ["+component.toUpperCase()+"]");
            LOGGER.finest("counter =["+componentCounter+"]");
            String componentPrefix = prefixDep + "COMPONENT" + componentCounter;
            String componentMachinePrefix = "";

            //VRADEP_PB_1_COMPONENT#_NAME=
            map.put(componentPrefix + "_NAME", component.toUpperCase());

            Set<String> machineSet = new HashSet<String>();

            for (List machine : this.machineList) {
                LOGGER.finest("MachineList:Machine.get(1).toString()="+machine.get(1).toString());
                if (machine.get(1).toString().equalsIgnoreCase(component)) {
                    LOGGER.finest("Adding ["+ machine.get(2).toString()+"] to machineSet");
                    machineSet.add(machine.get(2).toString());
                }
            }

            int machineCounter = 1;
            Set<String> networkSet = new HashSet<String>();

            for (String machines : machineSet) {
                LOGGER.finest("MachineSet:"+ machines);
                for (List machine : this.machineList) {
                    LOGGER.finest("In For list machine: this.machinelist");
                    String get1 = machine.get(1).toString();
                    String get2 = machine.get(2).toString();
                    String get3 = machine.get(3).toString();
                    String get4 = machine.get(4).toString();
                    if (machine.get(1).toString().equalsIgnoreCase(component)
                            && machine.get(2).toString().equalsIgnoreCase(machines)) {
                        LOGGER.finest("building: componentMachinePRefix= "+componentMachinePrefix + "_NAME");
                        componentMachinePrefix = componentPrefix + "_MACHINE" + machineCounter;
                        
                        //VRADEP_PB_1_COMPONENT#_MACHINE#_NAME=
                        LOGGER.finest("Putting ["+componentMachinePrefix + "_NAME,"+ machine.get(2).toString().toUpperCase()+"] in map");
                        map.put(componentMachinePrefix + "_NAME", machine.get(2).toString().toUpperCase());
                        LOGGER.finest("Adding ["+machine.get(3).toString()+"] to networkset");
                        networkSet.add(machine.get(3).toString());
                        break;
                    }
                }

                for (String network : networkSet) {
                    int networkCounter = 1;
                    for (List machine : this.machineList) {
                        int ipCounter = 1;

                        if (machine.get(1).toString().equalsIgnoreCase(component)
                                && machine.get(3).toString().equalsIgnoreCase(network)) {

                            String networkMachinePrefix = componentMachinePrefix + "_NETWORK" + networkCounter;

                            //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_NAME=

                            map.put(networkMachinePrefix + "_NAME", machine.get(3).toString().toUpperCase());

                            //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_IP#=
                            map.put(networkMachinePrefix + "_MAC" + ipCounter, machine.get(4).toString().toUpperCase());        
                            map.put(networkMachinePrefix + "_IP" + ipCounter, machine.get(5).toString().toUpperCase());
                            ipCounter++;
                            networkCounter++;
                            break;
                        }
                    }

                }
                LOGGER.finest("Incrementing machinecounter");
                machineCounter++;

            }
            componentCounter++;
        }


        getLoadBalancerList();

        for( List loadbalancer : this.loadBalancerList ){

            int lbCounter = 1;
            for ( Object data : loadbalancer ){
                String lbPrefix = prefixDep+"LB"+lbCounter+"_";
                //VRADEP_PB_1_LBNAME
                map.put(lbPrefix + "NAME",loadbalancer.get(1).toString().toUpperCase());
                map.put(lbPrefix + "SERVICES",loadbalancer.get(2).toString());

            }

        }


        LOGGER.exiting(this.getClass().getSimpleName(),"getDeploymentComponents()");	
        return map;
    }

    public String getDeploymentName(){
        LOGGER.entering(this.getClass().getSimpleName(),"getDeploymentName()");	
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().equals("composition.resource.type.deployment")) {

                this.deploymentName = content.getAsJsonObject().get("name").getAsString();
                System.out.println("Name :" + this.deploymentName );

            }
        }
        String depName = this.deploymentName;
        LOGGER.exiting(this.getClass().getSimpleName(),"getDeploymentName()");	
        return depName;
    }

    public void deploymentResources() throws IOException{

            this.deploymentResources  = request.getRequestResourceView();

    }


    public void getParentResourceID() throws IOException{
        LOGGER.entering(this.getClass().getSimpleName(),"getParentResourceID()");	
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().equals("composition.resource.type.deployment")) {

                this.parentResourceID = content.getAsJsonObject().get("resourceId").getAsString();
            }
        }
        LOGGER.exiting(this.getClass().getSimpleName(),"getParentResourceID()");	
    }



    public void getParentResourceID(String name) throws IOException{
        LOGGER.entering(this.getClass().getSimpleName(),"getParentResourceID("+name+")");	
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {
            System.out.println("Content :" + content.getAsJsonObject().get("name").getAsString() );

            if (content.getAsJsonObject().get("name").getAsString().equals(name)) {
                this.parentResourceID = content.getAsJsonObject().get("resourceId").getAsString();
                System.out.println("ParentID :" + this.parentResourceID );
                break;
            }
        }
        LOGGER.exiting(this.getClass().getSimpleName(),"getParentResourceID("+name+")");	
    }

    public String getDestroyURL() throws IOException {

        String URL = "";

        return URL;
    }

    public String getDestroyAction() throws IOException {

        this.getParentResourceID();

        JsonObject actions = this.request.getResourceActions(this.parentResourceID);

        JsonArray contentArray = actions.getAsJsonArray("content");

        String actionID = "";

        for (JsonElement content : contentArray) {
                System.out.println(content.getAsJsonObject().get("name").getAsString());
            if (content.getAsJsonObject().get("name").getAsString().equals("Destroy")) {
                 actionID = content.getAsJsonObject().get("id").getAsString();
                 System.out.println(actionID);
                 break;
            }
        }
        return actionID;

    }

    private void getTenant(){

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {
            if (content.getAsJsonObject().get("resourceId").getAsString().equals(this.parentResourceID)) {
                this.tenantId = content.getAsJsonObject().get("tenantId").getAsString();
                System.out.println("tenantID :" + this.tenantId );
                break;
            }
        }
    }

    private void getBusinessGroup(){
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {
            if (content.getAsJsonObject().get("resourceId").getAsString().equals(this.parentResourceID)) {
                this.businessGroupId = content.getAsJsonObject().get("businessGroupId").getAsString();
                System.out.println("tenantID :" + this.businessGroupId );
                break;
            }
        }
    }

    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            }
            else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    //((ObjectNode) mainNode).put(fieldName, value);
                    ((ObjectNode) mainNode).replace(fieldName, value);
                }
            }

        }

        return mainNode;
    }

    public static JsonObject merge(JsonObject mainJson, JsonObject updateJson) throws IOException {

        JsonParser parser = new JsonParser();
        JsonObject returnJSON;

        ObjectMapper mapper = new ObjectMapper();
        if (mainJson == null){
             System.out.println("mainJson is null");
        } else { 
            System.out.println("Original BP request : " + mainJson.toString());
        }

        System.out.println("JSON to merge : " + updateJson.toString());


        String json1 = mainJson.toString();
        String json2 = updateJson.toString();

        System.out.println("Original BP request : " + json1);
        System.out.println("JSON to merge : " + json2);


        JsonNode mainNode = mapper.readTree(json1);
        returnJSON = parser.parse(mainNode.toString()).getAsJsonObject();
        JsonNode updateNode = mapper.readTree(json2);

        returnJSON = parser.parse(merge(mainNode,updateNode).toString()).getAsJsonObject();
       
        return returnJSON;

    }


    public boolean destroy( String DeploymentName ) throws IOException {

        logger.println("Destroying Deployment "+DeploymentName);

        // Get ResrouceView to find parentID from name
        this.deploymentResources = this.request.getResourceView(DeploymentName);
        System.out.println("JSON Obj "+this.deploymentResources);

        this.getParentResourceID(DeploymentName);
        // Get actionID for destroy
        return this.destroy();

    }

    public boolean destroy() throws IOException {

        if( this.parentResourceID == null ) {
            System.out.println("Destroying Deployment");

            deploymentResources();
            this.getParentResourceID();
        }

        String actionID = this.getDestroyAction();
        getBusinessGroup();
        getTenant();
/*
        JsonObject json = request.getResourceActionsRequestTemplate(parentResourceID, actionID);

        json.addProperty("description", "test");
        JsonObject jsonData = json.getAsJsonObject("data");
        jsonData.addProperty("description", "test");
        jsonData.addProperty("reasons", "test");

        System.out.println(json);
        request.ResourceActionsRequest(parentResourceID, actionID, json);
*/

        System.out.println("JSON Destroy "+ jsonString);


        JsonElement jsonDestroyElement = new JsonParser().parse(jsonString);
        JsonObject jsonDestroyObject = jsonDestroyElement.getAsJsonObject();

        JsonObject jsonResourceReb = jsonDestroyObject.getAsJsonObject("resourceRef");
        jsonResourceReb.addProperty("id", this.parentResourceID);

        JsonObject jsonResourceAction = jsonDestroyObject.getAsJsonObject("resourceActionRef");
        jsonResourceAction.addProperty("id", actionID);

        JsonObject jsonOrganizationAction = jsonDestroyObject.getAsJsonObject("organization");
        jsonOrganizationAction.addProperty("tenantRef", this.tenantId);
        jsonOrganizationAction.addProperty("tenantLabel", this.tenantId);
        jsonOrganizationAction.addProperty("subtenantRef", this.businessGroupId);





        System.out.println("JSON Destroy "+jsonDestroyObject.toString());

        request.postRequest(jsonDestroyObject.toString());

        return true;

    }

}
