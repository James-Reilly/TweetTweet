package me.jreilly.JamesTweet.Etc.twitterRelationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/7/15.
 */
public class TwitterRelationship {

    private Relationship relationship;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The relationship
     */
    public Relationship getRelationship() {
        return relationship;
    }

    /**
     * @param relationship The relationship
     */
    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}