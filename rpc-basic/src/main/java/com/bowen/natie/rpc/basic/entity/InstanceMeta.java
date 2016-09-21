package com.bowen.natie.rpc.basic.entity;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Created by mylonelyplanet on 16/9/9.
 * In a real application, the Service payload will most likely
 * be more detailed than this. But, this gives a good example.
 */

@JsonRootName("meta")
public class InstanceMeta
{
    private String        description;

    public InstanceMeta()
    {
        this("");
    }

    public InstanceMeta(String description)
    {
        this.description = description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }


}
