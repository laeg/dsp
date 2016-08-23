package com.king.dsp.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by laeg on 19/08/2016.
 */
public enum Relationship implements RelationshipType {
    HAS_USED_CONNECTION,
    IP_FROM_ZIP_CODE,
    ZIP_IN_CITY,
    CITY_IN_REGION,
    REGION_IN_COUNTY,
    OWNS_APPLICATION,
    APP_IN_CATEGORY
}