package com.king.dsp.neo4j;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Created by laeg on 18/08/2016.
 */
@Path("/service")
public class Service {

    private static IpService ipService;
    private final GraphDatabaseService graphDb;
    private ExecutorService executor;


    public Service(@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        // set up the ip service class and pass through the graphDb?
        ipService = new IpService(graphDb);

        this.executor = Executors.newWorkStealingPool();

    }

    @GET
    @Produces("application/json")
    @Path("/sayhello")
    public Response get() {
        JSONObject jObj = new JSONObject();
        jObj.put("online", 1);
        return Response.ok().entity(jObj.toString()).build();
    }

    @POST
    @Path("/write/rfbjson")
    public Response writeRfbs(@Context GraphDatabaseService db, String body) throws Exception{

        // Time the execution
        long startTime = System.nanoTime();

        // replace me
        String rfbInput = "{\"app\":{\"bundle\":\"com.zynga.wwf2.free\",\"cat\":[\"IAB1\",\"IAB9\",\"IAB9-30\",\"entertainment\",\"games\"],\"id\":\"1cb88d26e4c74248bf957e3bb9d0884d\",\"name\":\"Words With Friends 2 Android\",\"publisher\":{\"id\":\"eb99c87f16d44af380cca10c8691f91a\",\"name\":\"Zynga\"},\"ver\":\"3.852\"},\"at\":2,\"badv\":[\"apprope.com\",\"badoo.com\",\"classesusa.com\",\"cm.best-thing.eu\",\"dicewithbuddies.com\",\"etermax.com\",\"fanatee.com\",\"fogs.com\",\"ifunny\",\"ifunny.co\",\"maginteractive.com\",\"pg.com\",\"puzzlesocial.com\",\"scopely.com\",\"withbuddies\",\"withbuddies.com\",\"wordswithfriends\",\"yahtzeewithbuddies.com\",\"zynga\"],\"bcat\":[\"IAB25\",\"IAB25-2\",\"IAB26\",\"IAB7-39\",\"IAB9-9\"],\"device\":{\"connectiontype\":2,\"dnt\":0,\"geo\":{\"city\":\"Prestons\",\"country\":\"AUS\",\"region\":\"02\",\"zip\":\"2170\"},\"h\":1024,\"ifa\":\"769e3449-f174-4315-83aa-ba66933905f9\",\"ip\":\"121.217.126.194\",\"js\":1,\"language\":\"en\",\"make\":\"samsung\",\"model\":\"SM-T550\",\"os\":\"Android\",\"osv\":\"6.0.1\",\"ua\":\"Mozilla/5.0 (Linux; Android 6.0.1; SM-T550 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36\",\"w\":768},\"id\":\"8455da99-6cff-4ae1-9e84-e4ce9d00eeb6\",\"imp\":[{\"banner\":{\"api\":[3,5],\"battr\":[1,2,3,8,9,10,13,14,6],\"btype\":[4],\"h\":90,\"pos\":1,\"w\":728},\"bidfloor\":1.310,\"displaymanager\":\"mopub\",\"displaymanagerver\":\"4.5.1\",\"ext\":{\"brsrclk\":1,\"dlp\":1},\"id\":\"1\",\"instl\":0,\"secure\":0,\"tagid\":\"79394cf5180940d0a1828c2618e74ca5\"}],\"user\":{\"keywords\":\"z_sdkversion:4.7.1,z_impression_id:bab6d91b-188d-45bf-b55f-f72f35b12456,z_slot_name:MOB_WWF2_BAN,z_nexage:true,z_min_version:4.2.1\"}}";

        JSONObject rfbJsonObject = new JSONObject(rfbInput);

        String deviceId = rfbJsonObject.getString("id");

        JSONObject appJsonObj = (JSONObject) rfbJsonObject.get("app");

        JSONObject deviceJsonObj = (JSONObject) rfbJsonObject.get("device");

        handleDeviceJSON(db, deviceId, deviceJsonObj);
        handleAppJSON(db, appJsonObj);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("duration-ms", duration);

        return Response.ok().entity(jsonResponse.toString()).build();
    }

    private void handleDeviceJSON(GraphDatabaseService graphDb, String deviceId, JSONObject deviceJson){
        /*
        ""
        device: {
            carrier: "262-02",
            connectiontype: 3,
            devicetype: 4,
            dnt: 0,
            geo: {
            city: "Stuttgart",
            country: "DEU",
            region: "01",
            zip: "70176"
            },
            h: 1136,
            hwv: "iPhone 5s (GSM+CDMA)",
            ifa: "B08B77E7-196B-4001-908D-C81A5F1B3026",
            ip: "109.42.0.0",
            js: 1,
            language: "de",
            make: "Apple",
            model: "iPhone",
            os: "iOS",
            osv: "9.3.2",
            ua: "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69",
            w: 640
        },
        * */
        try (Transaction tx = graphDb.beginTx()){

            // Check nodes
            Node deviceNode = findOrCreateDeviceNode(graphDb, deviceId, deviceJson);

            Node ipNode = findOrCreateIpNode(graphDb, deviceJson.get("ip").toString());

            if (deviceNode != null && ipNode != null)
            {
                if (!deviceNode.hasRelationship(Relationship.HAS_USED_CONNECTION, Direction.OUTGOING))
                {
                    deviceNode.createRelationshipTo(ipNode, Relationship.HAS_USED_CONNECTION);
                }
            }

            tx.success();

        }

    }

    private Node handleAppJSON(GraphDatabaseService graphDb, JSONObject appJson)
    {
        /*
        app: {
            id: "9e898003b465446ebe80712312413243",
            name: "Trivia Crack iOS",
            storeurl: "https://itunes.apple.com/us/app/trivia-crack/id651510680?mt=8&uo=4",
            ver: "2.14",
            bundle: "651510680",
            cat: [
                "IAB1",
                "IAB9",
                "IAB9-30",
                "entertainment",
                "games"
            ],
            publisher: {
                id: "8cc575bd24ad48bcbb6a3b0e1a71d171",
                name: "Etermax"
            }
        },
        */
        Node appNode;

        try (Transaction tx = graphDb.beginTx()){
            appNode = findOrCreateAppNode(graphDb, appJson.getString("id"), appJson);

            handleAppCategories(graphDb, appJson.getJSONArray("cat"), appNode);

            Node publisherNode = findOrCreatePublisher(graphDb,  appJson.getJSONObject("publisher").getString("id"), appJson.getJSONObject("publisher"));

            publisherNode.createRelationshipTo(appNode, Relationship.OWNS_APPLICATION);

            tx.success();
        }

        return appNode;
    }

    private void handleAppCategories(GraphDatabaseService graphDb, JSONArray categories, Node appNode)
    {

        Iterator catit = categories.iterator();
        while (catit.hasNext())
        {
            String catName = (String) catit.next();
            if (catName.contains("IAB"))
            {
                Node catNode = findOrCreateCatNode(graphDb, catName);

                if (!appNode.hasRelationship(Relationship.APP_IN_CATEGORY, Direction.OUTGOING))
                {
                    appNode.createRelationshipTo(catNode, Relationship.APP_IN_CATEGORY);
                }

            }

        }
    }


    private Node findOrCreateCatNode(GraphDatabaseService graphDb, String catName)
    {
        Node catNode;

        try(Transaction tx = graphDb.beginTx())
        {
            catNode = graphDb.findNode(Labels.Category, "name", catName);
            if (catNode == null)
            {
                catNode = createCatNode(graphDb, catName);
            }

            tx.success();
        }

        return catNode;
    }

    private Node createCatNode(GraphDatabaseService graphDb, String catName)
    {
        Node catNode;
        try(Transaction tx = graphDb.beginTx())
        {
            catNode = graphDb.createNode(Labels.Category);

            if (catName != null)
            {
                catNode.setProperty("name", catName);
            }

            if (catNode.getAllProperties().size() > 0)
            {
                System.out.println("Category node created, properties added. \t Category Name:  " + catName);
            }

            tx.success();
        }

        return catNode;
    }


    private Node findOrCreateAppNode(GraphDatabaseService graphDb, String appId, JSONObject appJson)
    {
        Node appNode;

        try (Transaction tx = graphDb.beginTx())
        {
            appNode = graphDb.findNode(Labels.App, "appid", appId);

            if (appNode == null)
            {
                appNode = createAppNode(graphDb, appId, appJson);
            }

            tx.success();
        }

        return appNode;
    }

    private Node createAppNode(GraphDatabaseService graphDb, String appId, JSONObject appJson)
    {
        Node appNode;

        try (Transaction tx = graphDb.beginTx()){

            appNode = graphDb.createNode(Labels.App);

            if (appId != null)
            {
                appNode.setProperty("appid", appId);
                appNode.setProperty("name", appJson.getString("name"));
                appNode.setProperty("version", appJson.has("ver") ?  appJson.getString("ver")  : "");
                appNode.setProperty("storeurl", appJson.has("storeurl") ? appJson.getString("storeurl")  : "");

            }

            if (appNode.getAllProperties().size() > 0)
            {
                System.out.println("App node created, properties added. \t\t App ID: \t\t " + appId);
            }

            tx.success();
        }

        return appNode;
    }

    private Node findOrCreatePublisher(GraphDatabaseService graphDb, String publisherId, JSONObject publisherJson)
    {
        Node publisherNode;

        try (Transaction tx = graphDb.beginTx())
        {
            publisherNode = graphDb.findNode(Labels.Publisher, "publisherid", publisherId);

            if (publisherNode == null)
            {
                publisherNode = createPublisherNode(graphDb, publisherId, publisherJson);
            }

            tx.success();
        }

        return publisherNode;
    }

    private Node createPublisherNode(GraphDatabaseService graphDb, String publisherId, JSONObject publisherJson)
    {
        Node publisherNode;

        try (Transaction tx = graphDb.beginTx()){

            publisherNode = graphDb.createNode(Labels.Publisher);

            if (publisherId != null)
            {
                publisherNode.setProperty("publisherid", publisherId);
                publisherNode.setProperty("name", publisherJson.getString("name"));
            }

            if (publisherNode.getAllProperties().size() > 0)
            {
                System.out.println("Publisher node created, properties added. \t Publisher ID: \t " + publisherId);
            }

            tx.success();
        }

        return publisherNode;
    }

    private Node findOrCreateDeviceNode(GraphDatabaseService graphDb, String deviceId, JSONObject deviceJson)
    {

        Node deviceNode;

        try (Transaction tx = graphDb.beginTx())
        {
            deviceNode = graphDb.findNode(Labels.UserDevice, "deviceid", deviceId);

            if (deviceNode == null)
            {
                deviceNode = createDeviceNode(graphDb, deviceId, deviceJson);
            }

            tx.success();
        }

        return deviceNode;
    }

    private Node createDeviceNode(GraphDatabaseService graphDb, String deviceId, JSONObject deviceJson)
    {

        Node deviceNode;

        try (Transaction tx = graphDb.beginTx()){

            deviceNode = graphDb.createNode(Labels.UserDevice);

            if (deviceId != null)
            {
                deviceNode.setProperty("deviceid", deviceId);
            }

            for (String prop : deviceJson.keySet())
            {
                String value = deviceJson.get(prop).toString();
                deviceNode.setProperty(prop, value);
            }

            if (deviceNode.getAllProperties().size() > 0)
            {
                System.out.println("Device node created, properties added. \t\t Device ID:  \t " + deviceId);
            }

            tx.success();
        }

        return deviceNode;
    }

    private Node findOrCreateIpNode(GraphDatabaseService graphDb, String ip)
    {
        Node ipNode;

        try (Transaction tx = graphDb.beginTx())
        {
            ipNode = graphDb.findNode(Labels.Ip, "address", ip);

            if (ipNode == null)
            {
                ipNode = createIpNode(graphDb, ip);
            }

            tx.success();
        }

        return ipNode;
    }

    private Node createIpNode(GraphDatabaseService graphDb, String ip)
    {
        Node ipNode;


        try (Transaction tx = graphDb.beginTx())
        {
            ipNode = graphDb.createNode(Labels.Ip);
            ipNode.setProperty("address", ip);

            if (ipNode.getAllProperties().size() > 0)
            {
                System.out.println("Ip node created, properties added. \t\t\t IP Address: \t " + ip);

                JSONObject ipInformation = new JSONObject(getIpAdditionalInformation(ip));

                ipNode.setProperty("org", ipInformation.getString("isp"));
                ipNode.setProperty("isp", ipInformation.getString("isp"));
                ipNode.setProperty("asn", ipInformation.getString("as"));
                ipNode.setProperty("lat", Float.parseFloat( ipInformation.get("lat").toString()));
                ipNode.setProperty("lon", Float.parseFloat( ipInformation.get("lon").toString()));
                ipNode.setProperty("timezone", ipInformation.getString("timezone"));

                // create nodes to link back the ip information
                Node countryNode = findOrCreateCountryNode(graphDb, ipInformation.getString("country"), ipInformation.getString("countryCode"));
                Node regionNode = findOrCreateRegion(graphDb, ipInformation.getString("regionName"), ipInformation.getString("region"));
                Node cityNode = findOrCreateCity(graphDb, ipInformation.getString("city"));
                Node zipNode = findOrCreateZip(graphDb, ipInformation.getString("zip"));

                ipNode.createRelationshipTo(zipNode, Relationship.IP_FROM_ZIP_CODE);
                zipNode.createRelationshipTo(cityNode, Relationship.ZIP_IN_CITY);
                cityNode.createRelationshipTo(regionNode, Relationship.CITY_IN_REGION);
                regionNode.createRelationshipTo(countryNode, Relationship.REGION_IN_COUNTY);

            }

            tx.success();
        }

        return ipNode;
    }

    private Node findOrCreateCountryNode(GraphDatabaseService graphDb, String countryName, String countryCode)
    {
        Node countryNode;

        try (Transaction tx = graphDb.beginTx())
        {
            countryNode = graphDb.findNode(Labels.Country, "name", countryName);

            if (countryNode == null)
            {
                countryNode = createCountryNode(graphDb, countryName, countryCode);
            }

            tx.success();
        }

        return countryNode;
    }

    private Node createCountryNode(GraphDatabaseService graphDb, String countryName, String countryCode)
    {
        Node countryNode;

        try (Transaction tx = graphDb.beginTx())
        {
            countryNode = graphDb.createNode(Labels.Country);
            if (countryName != null)
            {
                countryNode.setProperty("name", countryName);
                countryNode.setProperty("code", countryCode);
            }

            if (countryNode.getAllProperties().size() > 0)
            {
                System.out.println("Country node created, properties added. \t Country Name: \t " + countryName);
            }

            tx.success();
        }

        return countryNode;

    }

    private Node findOrCreateRegion(GraphDatabaseService graphDb, String regionName, String regionCode)
    {
        Node regionNode;

        try (Transaction tx = graphDb.beginTx())
        {
            regionNode = graphDb.findNode(Labels.Region, "name", regionName);

            if (regionNode == null)
            {
                regionNode = createRegionNode(graphDb, regionName, regionCode);
            }

            tx.success();
        }

        return regionNode;
    }

    private Node createRegionNode(GraphDatabaseService graphDb, String regionName, String regionCode)
    {
        Node regionNode;

        try (Transaction tx = graphDb.beginTx())
        {
            regionNode = graphDb.createNode(Labels.Region);
            if (regionName != null)
            {
                regionNode.setProperty("name", regionName);
                regionNode.setProperty("code", regionCode);
            }

            if (regionNode.getAllProperties().size() > 0)
            {
                System.out.println("Region node created, properties added. \t\t Region Name: \t " + regionName);
            }

            tx.success();
        }

        return regionNode;
    }

    private Node findOrCreateCity(GraphDatabaseService graphDb, String cityName)
    {
        Node cityNode;

        try (Transaction tx = graphDb.beginTx())
        {
            cityNode = graphDb.findNode(Labels.City, "name", cityName);

            if (cityNode == null)
            {
                cityNode = createCityNode(graphDb, cityName);
            }

            tx.success();
        }

        return cityNode;
    }

    private Node createCityNode(GraphDatabaseService graphDb, String cityName)
    {
        Node cityNode;

        try (Transaction tx = graphDb.beginTx())
        {
            cityNode = graphDb.createNode(Labels.City);
            if (cityName != null)
            {
                cityNode.setProperty("name", cityName);
            }

            if (cityNode.getAllProperties().size() > 0)
            {
                System.out.println("City node created, properties added. \t\t City Name: \t " + cityName);
            }

            tx.success();
        }

        return cityNode;
    }

    private Node findOrCreateZip(GraphDatabaseService graphDb, String zip)
    {
        Node zipNode;

        try (Transaction tx = graphDb.beginTx())
        {
            zipNode = graphDb.findNode(Labels.Zip, "code", zip);

            if (zipNode == null)
            {
                zipNode = createZipNode(graphDb, zip);
            }

            tx.success();
        }

        return zipNode;
    }

    private Node createZipNode(GraphDatabaseService graphDb, String zip)
    {
        Node zipNode;

        try (Transaction tx = graphDb.beginTx())
        {
            zipNode = graphDb.createNode(Labels.Zip);

            if (zip != null)
            {
                zipNode.setProperty("code", zip);
            }

            if (zipNode.getAllProperties().size() > 0)
            {
                System.out.println("Zip node created, properties added. \t\t Zip Code: \t\t " + zip);
            }

            tx.success();
        }

        return zipNode;
    }

    private String getIpAdditionalInformation(String ipAddress)
    {
        String baseIp = "http://ip-api.com/json/";
        String requestAddress = baseIp + ipAddress;

        return callURL(requestAddress);
    }


    public static String callURL(String myURL)
    {
        System.out.println("Requesting more information: \t\t\t\t URL: \t\t\t " + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:" + myURL, e);
        }

        return sb.toString();
    }


}
