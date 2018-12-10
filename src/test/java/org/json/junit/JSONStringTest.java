package org.json.junit;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.*;

import org.json.*;
import org.junit.Test;

/**
 * Tests for JSONString implementations, and the difference between
 * {@link JSONObject#valueToString} and {@link JSONObject#writeValue}.
 */
public class JSONStringTest {

    /**
     * This tests the JSONObject.writeValue() method. We can't test directly
     * due to it being a package-protected method. Instead, we can call
     * JSONArray.write(), which delegates the writing of each entry to
     * writeValue().
     */
    @Test
    public void writeValues() throws Exception {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put((Object)null);

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[null]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put(JSONObject.NULL);
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[null]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put(new JSONObject());
        }
        
        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[{}]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put(new JSONArray());
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[[]]".equals(output));
    
            jsonArray = new JSONArray();
            Map<?,?> singleMap = Collections.singletonMap("key1", "value1");
            jsonArray.put((Object)singleMap);
        }
        
        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[{\"key1\":\"value1\"}]".equals(output));
    
            jsonArray = new JSONArray();
            List<?> singleList = Collections.singletonList("entry1");
            jsonArray.put((Object)singleList);
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[[\"entry1\"]]".equals(output));
    
            jsonArray = new JSONArray();
            int[] intArray = new int[] { 1, 2, 3 };
            jsonArray.put(intArray);
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[[1,2,3]]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put(24);
        }
        
        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[24]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put("string value");
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[\"string value\"]".equals(output));
    
            jsonArray = new JSONArray();
            jsonArray.put(true);
        }

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[true]".equals(output));
        }

    }

    /**
     * This tests the JSONObject.valueToString() method. These should be
     * identical to the values above, except for the enclosing [ and ].
     */
    @SuppressWarnings("boxing")
    @Test
    public void valuesToString() throws Exception {

        String output = JSONObject.valueToString(null);
        assertTrue("String values should be equal", "null".equals(output));

        output = JSONObject.valueToString(JSONObject.NULL);
        assertTrue("String values should be equal", "null".equals(output));

        output = JSONObject.valueToString(new JSONObject());
        assertTrue("String values should be equal", "{}".equals(output));

        output = JSONObject.valueToString(new JSONArray());
        assertTrue("String values should be equal", "[]".equals(output));

        Map<?,?> singleMap = Collections.singletonMap("key1", "value1");
        output = JSONObject.valueToString(singleMap);
        assertTrue("String values should be equal", "{\"key1\":\"value1\"}".equals(output));

        List<?> singleList = Collections.singletonList("entry1");
        output = JSONObject.valueToString(singleList);
        assertTrue("String values should be equal", "[\"entry1\"]".equals(output));

        int[] intArray = new int[] { 1, 2, 3 };
        output = JSONObject.valueToString(intArray);
        assertTrue("String values should be equal", "[1,2,3]".equals(output));

        output = JSONObject.valueToString(24);
        assertTrue("String values should be equal", "24".equals(output));

        output = JSONObject.valueToString("string value");
        assertTrue("String values should be equal", "\"string value\"".equals(output));

        output = JSONObject.valueToString(true);
        assertTrue("String values should be equal", "true".equals(output));

    }

    /**
     * Test what happens when toJSONString() returns a well-formed JSON value.
     * This is the usual case.
     */
    @Test
    public void testJSONStringValue() throws Exception {
        JSONStringValue jsonString = new JSONStringValue();
        JSONArray jsonArray = new JSONArray();

        jsonArray.put(jsonString);

        
        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[\"the JSON string value\"]".equals(output));
    
            output = JSONObject.valueToString(jsonString);
            assertTrue("String values should be equal", "\"the JSON string value\"".equals(output));
        }
    }

    /**
     * Test what happens when toJSONString() returns null. In one case,
     * use the object's toString() method. In the other, throw a JSONException.
     */
    @Test
    public void testJSONNullStringValue() throws Exception {
        JSONNullStringValue jsonString = new JSONNullStringValue();
        JSONArray jsonArray = new JSONArray();

        jsonArray.put(jsonString);

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[\"the toString value\"]".equals(output));
    
            // The only different between writeValue() and valueToString():
            // in this case, valueToString throws a JSONException
            try {
                output = JSONObject.valueToString(jsonString);
                fail("Expected an exception, got a String value");
            } catch (Exception e) {
                assertTrue("Expected JSONException", e instanceof JSONException);
                assertTrue("Exception message does not match", "Bad value from toJSONString: null".equals(e.getMessage()));
            }
        }
    }

    /**
     * Test what happens when toJSONString() returns an exception. In both
     * cases, a JSONException is thrown, with the cause and message set from
     * the original exception.
     */
    @Test
    public void testJSONStringExceptionValue() {
        JSONStringExceptionValue jsonString = new JSONStringExceptionValue();
        JSONArray jsonArray = new JSONArray();

        jsonArray.put(jsonString);

        try (StringWriter writer = new StringWriter();) {
            jsonArray.write(writer).toString();
            fail("Expected an exception, got a String value");
        } catch (JSONException e) {
            assertEquals("Unable to write JSONArray value at index: 0", e.getMessage());
        } catch(Exception e) {
            fail("Expected JSONException");
        }

        try {
            JSONObject.valueToString(jsonString);
            fail("Expected an exception, got a String value");
        } catch (JSONException e) {
            assertTrue("Exception message does not match", "the exception value".equals(e.getMessage()));
        } catch(Exception e) {
            fail("Expected JSONException");
        }
    }

    /**
     * Test what happens when a Java object's toString() returns a String value.
     * This is the usual case.
     */
    @Test
    public void testStringValue() throws Exception {
        StringValue nonJsonString = new StringValue();
        JSONArray jsonArray = new JSONArray();

        jsonArray.put(nonJsonString);

        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[\"the toString value for StringValue\"]".equals(output));
    
            output = JSONObject.valueToString(nonJsonString);
            assertTrue("String values should be equal", "\"the toString value for StringValue\"".equals(output));
        }
    }

    /**
     * Test what happens when a Java object's toString() returns null.
     * Defaults to empty string.
     */
    @Test
    public void testNullStringValue() throws Exception {
        NullStringValue nonJsonString = new NullStringValue();
        JSONArray jsonArray = new JSONArray();

        jsonArray.put(nonJsonString);

        
        try (StringWriter writer = new StringWriter();) {
            String output = jsonArray.write(writer).toString();
            assertTrue("String values should be equal", "[\"\"]".equals(output));
    
            output = JSONObject.valueToString(nonJsonString);
            assertTrue("String values should be equal", "\"\"".equals(output));
        }
    }

    /**
     * A JSONString that returns a valid JSON string value.
     */
    private static final class JSONStringValue implements JSONString {

        @Override
        public String toJSONString() {
            return "\"the JSON string value\"";
        }

        @Override
        public String toString() {
            return "the toString value for JSONStringValue";
        }
    }

    /**
     * A JSONString that returns null when calling toJSONString().
     */
    private static final class JSONNullStringValue implements JSONString {

        @Override
        public String toJSONString() {
            return null;
        }

        @Override
        public String toString() {
            return "the toString value";
        }
    }

    /**
     * A JSONString that throw an exception when calling toJSONString().
     */
    private static final class JSONStringExceptionValue implements JSONString {

        @Override
        public String toJSONString() {
            throw new IllegalStateException("the exception value");
        }

        @Override
        public String toString() {
            return "the toString value for JSONStringExceptionValue";
        }
    }

    public static final class StringValue {

        @Override
        public String toString() {
            return "the toString value for StringValue";
        }
    }

    public static final class NullStringValue {

        @Override
        public String toString() {
            return null;
        }
    }
}
