package org.streampipes.connect.firstconnector.format;


import org.streampipes.connect.GetNEvents;
import org.streampipes.model.modelconnect.FormatDescription;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.connect.EmitBinaryEvent;

import java.io.InputStream;
import java.util.List;

public abstract class Parser {

    public abstract Parser getInstance(FormatDescription formatDescription);

    public abstract void parse(InputStream data, EmitBinaryEvent emitBinaryEvent);

    public List<byte[]> parseNEvents(InputStream data, int n) {
        GetNEvents gne = new GetNEvents(n);

        parse(data, gne);

        return gne.getEvents();
    }

    /**
     * Pass one event to Parser to get the event schema
     * @param oneEvent
     * @return
     */
    public abstract EventSchema getEventSchema(byte[] oneEvent);
}
