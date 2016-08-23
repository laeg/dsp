package com.king.dsp.neo4j;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by laeg on 18/08/2016.
 */
public class IpService extends AbstractScheduledService {

    private static final Logger logger = Logger.getLogger(IpService.class.getName());
    private GraphDatabaseService graphDb;
//    public LinkedBlockingQueue<>

    public IpService(GraphDatabaseService graphDb){
        this.graphDb = graphDb;
        if (!this.isRunning()) {
            this.startAsync();
            this.awaitRunning();
            logger.info("Started IpService");
        }
    }

    @Override
    protected void runOneIteration() throws Exception {

    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.SECONDS);
    }

}
