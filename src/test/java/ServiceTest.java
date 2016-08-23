import com.king.dsp.neo4j.Labels;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by laeg on 18/08/2016.
 */
public class ServiceTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
//            .withFixture("CREATE (p:Person {name: 'Nicole'})")
//            .withFixture("CREATE (p:Person {name: 'Mark'})")
            .withExtension("/kingdsp", "com.king.dsp.neo4j");


    @Test
    public void shouldReturnOnline()
    {
        URI serverURI = neo4j.httpsURI();
        HTTP.Response res = HTTP.GET(serverURI.resolve("/kingdsp/service/sayhello").toString());
        System.out.println(res.toString());

    }

    @Test
    public void shouldCreateNodesFromRfbJson()
    {
        URI serverURI = neo4j.httpsURI();
        HTTP.Response res = HTTP.POST(serverURI.resolve("/kingdsp/service/write/rfblogs").toString());
        System.out.println(res.toString());
    }

//    @Test
//    public void testRfbs()
//    {
//        String rfbInput = "{\"app\":{\"bundle\":\"com.zynga.wwf2.free\",\"cat\":[\"IAB1\",\"IAB9\",\"IAB9-30\",\"entertainment\",\"games\"],\"id\":\"1cb88d26e4c74248bf957e3bb9d0884d\",\"name\":\"Words With Friends 2 Android\",\"publisher\":{\"id\":\"eb99c87f16d44af380cca10c8691f91a\",\"name\":\"Zynga\"},\"ver\":\"3.852\"},\"at\":2,\"badv\":[\"apprope.com\",\"badoo.com\",\"classesusa.com\",\"cm.best-thing.eu\",\"dicewithbuddies.com\",\"etermax.com\",\"fanatee.com\",\"fogs.com\",\"ifunny\",\"ifunny.co\",\"maginteractive.com\",\"pg.com\",\"puzzlesocial.com\",\"scopely.com\",\"withbuddies\",\"withbuddies.com\",\"wordswithfriends\",\"yahtzeewithbuddies.com\",\"zynga\"],\"bcat\":[\"IAB25\",\"IAB25-2\",\"IAB26\",\"IAB7-39\",\"IAB9-9\"],\"device\":{\"connectiontype\":2,\"dnt\":0,\"geo\":{\"city\":\"Prestons\",\"country\":\"AUS\",\"region\":\"02\",\"zip\":\"2170\"},\"h\":1024,\"ifa\":\"769e3449-f174-4315-83aa-ba66933905f9\",\"ip\":\"121.217.126.194\",\"js\":1,\"language\":\"en\",\"make\":\"samsung\",\"model\":\"SM-T550\",\"os\":\"Android\",\"osv\":\"6.0.1\",\"ua\":\"Mozilla/5.0 (Linux; Android 6.0.1; SM-T550 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36\",\"w\":768},\"id\":\"8455da99-6cff-4ae1-9e84-e4ce9d00eeb6\",\"imp\":[{\"banner\":{\"api\":[3,5],\"battr\":[1,2,3,8,9,10,13,14,6],\"btype\":[4],\"h\":90,\"pos\":1,\"w\":728},\"bidfloor\":1.310,\"displaymanager\":\"mopub\",\"displaymanagerver\":\"4.5.1\",\"ext\":{\"brsrclk\":1,\"dlp\":1},\"id\":\"1\",\"instl\":0,\"secure\":0,\"tagid\":\"79394cf5180940d0a1828c2618e74ca5\"}],\"user\":{\"keywords\":\"z_sdkversion:4.7.1,z_impression_id:bab6d91b-188d-45bf-b55f-f72f35b12456,z_slot_name:MOB_WWF2_BAN,z_nexage:true,z_min_version:4.2.1\"}}";
//
//        JSONObject rfbJsonObject = new JSONObject(rfbInput);
//
//        int deviceId = rfbJsonObject.getInt("id");
//
//        JSONObject app = (JSONObject) rfbJsonObject.get("app");
//        JSONObject bcat = (JSONObject) rfbJsonObject.get("bcat");
//        JSONObject device = (JSONObject) rfbJsonObject.get("device");
//        JSONObject imp = (JSONObject) rfbJsonObject.get("imp");
//
////        handleDeviceJSON(gra);
//    }



}
