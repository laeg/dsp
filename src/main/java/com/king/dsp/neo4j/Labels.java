package com.king.dsp.neo4j;

import org.neo4j.graphdb.Label;

/**
 * Created by laeg on 17/08/2016.
 */
public enum Labels implements Label {
    UserDevice,
    Ip,
    Country,
    Region,
    City,
    Zip,
    App,
    Publisher,
    Category
}