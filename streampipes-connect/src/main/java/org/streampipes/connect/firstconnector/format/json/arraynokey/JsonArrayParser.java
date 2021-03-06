package org.streampipes.connect.firstconnector.format.json.arraynokey;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streampipes.commons.exceptions.SpRuntimeException;
import org.streampipes.connect.EmitBinaryEvent;
import org.streampipes.connect.firstconnector.format.Parser;
import org.streampipes.connect.firstconnector.sdk.ParameterExtractor;
import org.streampipes.dataformat.json.JsonDataFormatDefinition;
import org.streampipes.model.modelconnect.FormatDescription;
import org.streampipes.model.schema.*;
import org.streampipes.vocabulary.SO;
import org.streampipes.vocabulary.XSD;

import javax.json.Json;
import javax.json.stream.JsonParserFactory;
import java.io.InputStream;
import java.util.*;

public class JsonArrayParser extends Parser {

    Logger logger = LoggerFactory.getLogger(JsonArrayParser.class);


    @Override
    public Parser getInstance(FormatDescription formatDescription) {
        ParameterExtractor extractor = new ParameterExtractor(formatDescription.getConfig());

        return new JsonArrayParser();
    }

    public JsonArrayParser() {
    }

    @Override
    public void parse(InputStream data, EmitBinaryEvent emitBinaryEvent) {
        JsonParserFactory factory = Json.createParserFactory(null);
        String s = data.toString();
        javax.json.stream.JsonParser jsonParser = factory.createParser(data);

        // Find first event in array
        boolean foundBeginning = false;
        while (jsonParser.hasNext() && !foundBeginning ) {
            javax.json.stream.JsonParser.Event event = jsonParser.next();
            if (event.equals(javax.json.stream.JsonParser.Event.START_ARRAY)) {
                foundBeginning = true;
            }
        }

        // Parse all events
        JsonDataFormatDefinition jsonDefinition = new JsonDataFormatDefinition();
        boolean isEvent = true;
        boolean result = true;
        int objectCount = 0;
        while (jsonParser.hasNext() && isEvent && result) {
            Map<String, Object> objectMap = parseObject(jsonParser, true, 1);
            if (objectMap != null) {
                byte[] tmp = new byte[0];
                try {
                    tmp = jsonDefinition.fromMap(objectMap);
                } catch (SpRuntimeException e) {
                    e.printStackTrace();
                }
//                    handleEvent(new EventObjectEndEvent(parseObject(tmp)));
                // TODO decide what happens id emit returns false
                result = emitBinaryEvent.emit(tmp);
            } else {
                isEvent = false;
            }

        }
    }

    @Override
    public EventSchema getEventSchema(byte[] oneEvent) {
        EventSchema resultSchema = new EventSchema();

        JsonDataFormatDefinition jsonDefinition = new JsonDataFormatDefinition();

        Map<String, Object> exampleEvent = null;

        try {
            exampleEvent = jsonDefinition.toMap(oneEvent);
        } catch (SpRuntimeException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Object> entry : exampleEvent.entrySet())
        {
//            System.out.println(entry.getKey() + "/" + entry.getValue());
            EventProperty p = getEventProperty(entry.getKey(), entry.getValue());

            resultSchema.addEventProperty(p);

        }

        return resultSchema;
    }

    private EventProperty getEventProperty(String key, Object o) {
        EventProperty resultProperty = null;

        System.out.println("Key: " + key);
        System.out.println("Class: " + o.getClass());
        System.out.println("Primitive: " + o.getClass().isPrimitive());
        System.out.println("Array: " + o.getClass().isArray());
        System.out.println("TypeName: " + o.getClass().getTypeName());


        System.out.println("=======================");

        if (o.getClass().equals(Boolean.class)) {
            resultProperty = new EventPropertyPrimitive();
            resultProperty.setRuntimeName(key);
            ((EventPropertyPrimitive) resultProperty).setRuntimeType(XSD._boolean.toString());
        }
        else if (o.getClass().equals(String.class)) {
            resultProperty = new EventPropertyPrimitive();
            resultProperty.setRuntimeName(key);
            ((EventPropertyPrimitive) resultProperty).setRuntimeType(XSD._string.toString());
        }
        else if (o.getClass().equals(Integer.class) || o.getClass().equals(Double.class)|| o.getClass().equals(Long.class)) {
            resultProperty = new EventPropertyPrimitive();
            resultProperty.setRuntimeName(key);
            ((EventPropertyPrimitive) resultProperty).setRuntimeType(SO.Number.toString());
        }
        else if (o.getClass().equals(LinkedHashMap.class)) {
            resultProperty = new EventPropertyNested();
            resultProperty.setRuntimeName(key);
            List<EventProperty> all = new ArrayList<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) o).entrySet()) {
               all.add(getEventProperty(entry.getKey(), entry.getValue()));
            }

            ((EventPropertyNested) resultProperty).setEventProperties(all);

        } else if (o.getClass().equals(ArrayList.class)) {
           resultProperty = new EventPropertyList();
           resultProperty.setRuntimeName(key);
        }

        if (resultProperty == null) {
            logger.error("Property Type was not detected in JsonParser for the schema detection. This should never happen!");
        }

        return resultProperty;
    }

    public Map<String, Object> parseObject(javax.json.stream.JsonParser jsonParser, boolean root, int start) {
        // this variable is needed to skip the first object start
        String mapKey = "";
        Map<String, Object> result = new HashMap<>();
        List<Object> arr = null;

        while (jsonParser.hasNext()) {
            javax.json.stream.JsonParser.Event event = jsonParser.next();
            switch (event) {
                case KEY_NAME:
                    mapKey = jsonParser.getString();
                    logger.debug("key: " + mapKey );
                    break;
                case START_OBJECT:
                    if (start == 0) {
                        Map<String, Object> ob = parseObject(jsonParser, false, 0);
                        if (arr == null) {
                            result.put(mapKey, ob);
                        } else {
                            arr.add(ob);
                        }
                    } else {
                        start--;
                    }
                    logger.debug("start object");
                    break;
                case END_OBJECT:

                    logger.debug("end object");
                    return result;
                case START_ARRAY:
                    arr = new ArrayList<>();
                    logger.debug("start array");
                    break;
                case END_ARRAY:
                    // Check if just the end of array is entered
                    if (result.keySet().size() == 0 && mapKey.equals("")) {
                        return null;
                    }
                    result.put(mapKey, arr);
                    arr = null;
                    logger.debug("end array");
                    break;
                case VALUE_TRUE:
                    if (arr == null) {
                        result.put(mapKey, true);
                    } else {
                        arr.add(true);
                    }
                    logger.debug("value: true");
                    break;
                case VALUE_FALSE:
                    if (arr == null) {
                        result.put(mapKey, false);
                    } else {
                        arr.add(false);
                    }
                    logger.debug("value: false");
                    break;
                case VALUE_STRING:
                    if (arr == null) {
                        result.put(mapKey, jsonParser.getString());
                    } else {
                        arr.add(jsonParser.getString());
                    }
                    logger.debug("value string: " + jsonParser.getString());
                    break;
                case VALUE_NUMBER:
                    if (arr == null) {
                        result.put(mapKey, jsonParser.getBigDecimal());
                    } else {
                        arr.add(jsonParser.getBigDecimal());
                    }
                    logger.debug("value number: " + jsonParser.getBigDecimal());
                    break;
                case VALUE_NULL:
                    logger.debug("value null");
                    break;
                default:
                    logger.error("Error: " + event + " event is not handled in the JSON parser");
                    break;
            }
        }

        return result;
    }
}