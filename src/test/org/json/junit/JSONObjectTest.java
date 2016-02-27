package org.json.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import com.jayway.jsonpath.*;

/**
 * JSONObject, along with JSONArray, are the central classes of the reference app.
 * All of the other classes interact with them, and JSON functionality would
 * otherwise be impossible.
 */
public class JSONObjectTest {

    /**
     * JSONObject built from a bean, but only using a null value.
     * Nothing good is expected to happen.
     * Expects NullPointerException
     */
    @Test(expected=NullPointerException.class)
    public void jsonObjectByNullBean() {
        MyBean myBean = null;
        new JSONObject(myBean);
    }

    /**
     * A JSONObject can be created with no content
     */
    @Test
    public void emptyJsonObject() {
        JSONObject jsonObject = new JSONObject();
        assertTrue("jsonObject should be empty", jsonObject.length() == 0);
    }

    /**
     * A JSONObject can be created from another JSONObject plus a list of names.
     * In this test, some of the starting JSONObject keys are not in the 
     * names list.
     */
    @Test
    public void jsonObjectByNames() {
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"nullKey\":null,"+
                "\"stringKey\":\"hello world!\","+
                "\"escapeStringKey\":\"h\be\tllo w\u1234orld!\","+
                "\"intKey\":42,"+
                "\"doubleKey\":-23.45e67"+
            "}";
        String[] keys = {"falseKey", "stringKey", "nullKey", "doubleKey"};
        JSONObject jsonObject = new JSONObject(str);

        // validate JSON
        JSONObject jsonObjectByName = new JSONObject(jsonObject, keys);
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObjectByName.toString());
        assertTrue("expected 4 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 4);
        assertTrue("expected \"falseKey\":false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected \"nullKey\":null", null == JsonPath.read(doc, "$.nullKey"));
        assertTrue("expected \"stringKey\":\"hello world!\"", "hello world!".equals(JsonPath.read(doc, "$.stringKey")));
        assertTrue("expected \"doubleKey\":-23.45e67", Double.valueOf("-23.45e67").equals(JsonPath.read(doc, "$.doubleKey")));
    }

    /**
     * JSONObjects can be built from a Map<String, Object>. 
     * In this test the map is null.
     * the JSONObject(JsonTokener) ctor is not tested directly since it already
     * has full coverage from other tests.
     */
    @Test
    public void jsonObjectByNullMap() {
        Map<String, Object> map = null;
        JSONObject jsonObject = new JSONObject(map);
        assertTrue("jsonObject should be empty", jsonObject.length() == 0);
    }

    /**
     * JSONObjects can be built from a Map<String, Object>. 
     * In this test all of the map entries are valid JSON types.
     */
    @Test
    public void jsonObjectByMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("trueKey", new Boolean(true));
        map.put("falseKey", new Boolean(false));
        map.put("stringKey", "hello world!");
        map.put("escapeStringKey", "h\be\tllo w\u1234orld!");
        map.put("intKey", new Long(42));
        map.put("doubleKey", new Double(-23.45e67));
        JSONObject jsonObject = new JSONObject(map);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 6 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 6);
        assertTrue("expected \"trueKey\":true", Boolean.TRUE.equals(JsonPath.read(doc, "$.trueKey")));
        assertTrue("expected \"falseKey\":false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected \"stringKey\":\"hello world!\"", "hello world!".equals(JsonPath.read(doc, "$.stringKey")));
        assertTrue("expected \"escapeStringKey\":\"h\be\tllo w\u1234orld!\"", "h\be\tllo w\u1234orld!".equals(JsonPath.read(doc,"$.escapeStringKey")));
        assertTrue("expected \"doubleKey\":-23.45e67", Double.valueOf("-23.45e67").equals(JsonPath.read(doc, "$.doubleKey")));
    }
    
    /**
     * Verifies that the constructor has backwards compatability with RAW types pre-java5.
     */
    @Test
    public void verifyConstructor() {
        
        final JSONObject expected = new JSONObject("{\"myKey\":10}");
        
        @SuppressWarnings("rawtypes")
        Map myRawC = Collections.singletonMap("myKey", Integer.valueOf(10));
        JSONObject jaRaw = new JSONObject(myRawC);

        Map<String, Object> myCStrObj = Collections.singletonMap("myKey",
                (Object) Integer.valueOf(10));
        JSONObject jaStrObj = new JSONObject(myCStrObj);

        Map<String, Integer> myCStrInt = Collections.singletonMap("myKey",
                Integer.valueOf(10));
        JSONObject jaStrInt = new JSONObject(myCStrInt);

        Map<?, ?> myCObjObj = Collections.singletonMap((Object) "myKey",
                (Object) Integer.valueOf(10));
        JSONObject jaObjObj = new JSONObject(myCObjObj);

        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaRaw));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaStrObj));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaStrInt));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaObjObj));
    }
    
    /**
     * Verifies that the put Collection has backwards compatability with RAW types pre-java5.
     */
    @Test
    public void verifyPutCollection() {
        
        final JSONObject expected = new JSONObject("{\"myCollection\":[10]}");

        @SuppressWarnings("rawtypes")
        Collection myRawC = Collections.singleton(Integer.valueOf(10));
        JSONObject jaRaw = new JSONObject();
        jaRaw.put("myCollection", myRawC);

        Collection<Object> myCObj = Collections.singleton((Object) Integer
                .valueOf(10));
        JSONObject jaObj = new JSONObject();
        jaObj.put("myCollection", myCObj);

        Collection<Integer> myCInt = Collections.singleton(Integer
                .valueOf(10));
        JSONObject jaInt = new JSONObject();
        jaInt.put("myCollection", myCInt);

        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaRaw));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaObj));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaInt));
    }

    
    /**
     * Verifies that the put Map has backwards compatability with RAW types pre-java5.
     */
    @Test
    public void verifyPutMap() {
        
        final JSONObject expected = new JSONObject("{\"myMap\":{\"myKey\":10}}");

        @SuppressWarnings("rawtypes")
        Map myRawC = Collections.singletonMap("myKey", Integer.valueOf(10));
        JSONObject jaRaw = new JSONObject();
        jaRaw.put("myMap", myRawC);

        Map<String, Object> myCStrObj = Collections.singletonMap("myKey",
                (Object) Integer.valueOf(10));
        JSONObject jaStrObj = new JSONObject();
        jaStrObj.put("myMap", myCStrObj);

        Map<String, Integer> myCStrInt = Collections.singletonMap("myKey",
                Integer.valueOf(10));
        JSONObject jaStrInt = new JSONObject();
        jaStrInt.put("myMap", myCStrInt);

        Map<?, ?> myCObjObj = Collections.singletonMap((Object) "myKey",
                (Object) Integer.valueOf(10));
        JSONObject jaObjObj = new JSONObject();
        jaObjObj.put("myMap", myCObjObj);

        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaRaw));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaStrObj));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaStrInt));
        assertTrue(
                "The RAW Collection should give me the same as the Typed Collection",
                expected.similar(jaObjObj));
    }


    /**
     * JSONObjects can be built from a Map<String, Object>. 
     * In this test the map entries are not valid JSON types.
     * The actual conversion is kind of interesting.
     */
    @Test
    public void jsonObjectByMapWithUnsupportedValues() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        // Just insert some random objects
        jsonMap.put("key1", new CDL());
        jsonMap.put("key2", new Exception());

        JSONObject jsonObject = new JSONObject(jsonMap);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 2 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 2);
        assertTrue("expected \"key2\":java.lang.Exception","java.lang.Exception".equals(JsonPath.read(doc, "$.key2")));
        assertTrue("expected 0 key1 items", ((Map<?,?>)(JsonPath.read(doc, "$.key1"))).size() == 0);
    }

    /**
     * JSONObjects can be built from a Map<String, Object>. 
     * In this test one of the map values is null 
     */
    @Test
    public void jsonObjectByMapWithNullValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("trueKey", new Boolean(true));
        map.put("falseKey", new Boolean(false));
        map.put("stringKey", "hello world!");
        map.put("nullKey", null);
        map.put("escapeStringKey", "h\be\tllo w\u1234orld!");
        map.put("intKey", new Long(42));
        map.put("doubleKey", new Double(-23.45e67));
        JSONObject jsonObject = new JSONObject(map);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 6 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 6);
        assertTrue("expected \"trueKey\":true", Boolean.TRUE.equals(JsonPath.read(doc, "$.trueKey")));
        assertTrue("expected \"falseKey\":false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected \"stringKey\":\"hello world!\"", "hello world!".equals(JsonPath.read(doc, "$.stringKey")));
        assertTrue("expected \"escapeStringKey\":\"h\be\tllo w\u1234orld!\"", "h\be\tllo w\u1234orld!".equals(JsonPath.read(doc,"$.escapeStringKey")));
        assertTrue("expected \"intKey\":42", Integer.valueOf("42").equals(JsonPath.read(doc, "$.intKey")));
        assertTrue("expected \"doubleKey\":-23.45e67", Double.valueOf("-23.45e67").equals(JsonPath.read(doc, "$.doubleKey")));
    }

    /**
     * JSONObject built from a bean. In this case all but one of the 
     * bean getters return valid JSON types
     */
    @Test
    public void jsonObjectByBean() {
        /**
         * Default access classes have to be mocked since JSONObject, which is
         * not in the same package, cannot call MyBean methods by reflection.
         */
        MyBean myBean = mock(MyBean.class);
        when(myBean.getDoubleKey()).thenReturn(-23.45e7);
        when(myBean.getIntKey()).thenReturn(42);
        when(myBean.getStringKey()).thenReturn("hello world!");
        when(myBean.getEscapeStringKey()).thenReturn("h\be\tllo w\u1234orld!");
        when(myBean.isTrueKey()).thenReturn(true);
        when(myBean.isFalseKey()).thenReturn(false);
        when(myBean.getStringReaderKey()).thenReturn(
            new StringReader("") {
            });

        JSONObject jsonObject = new JSONObject(myBean);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 8 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 8);
        assertTrue("expected true", Boolean.TRUE.equals(JsonPath.read(doc, "$.trueKey")));
        assertTrue("expected false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected hello world!","hello world!".equals(JsonPath.read(doc, "$.stringKey")));
        assertTrue("expected h\be\tllo w\u1234orld!", "h\be\tllo w\u1234orld!".equals(JsonPath.read(doc,"$.escapeStringKey")));
        assertTrue("expected 42", Integer.valueOf("42").equals(JsonPath.read(doc, "$.intKey")));
        assertTrue("expected -23.45e7", Double.valueOf("-23.45e7").equals(JsonPath.read(doc, "$.doubleKey")));
        assertTrue("expected 0 items in stringReaderKey", ((Map<?, ?>) (JsonPath.read(doc, "$.stringReaderKey"))).size() == 0);
        // sorry, mockito artifact
        assertTrue("expected 2 callbacks items", ((List<?>)(JsonPath.read(doc, "$.callbacks"))).size() == 2);
        assertTrue("expected 0 handler items", ((Map<?,?>)(JsonPath.read(doc, "$.callbacks[0].handler"))).size() == 0);
        assertTrue("expected 0 callbacks[1] items", ((Map<?,?>)(JsonPath.read(doc, "$.callbacks[1]"))).size() == 0);
    }

    /**
     * A bean is also an object. But in order to test the JSONObject
     * ctor that takes an object and a list of names, 
     * this particular bean needs some public
     * data members, which have been added to the class.
     */
    @Test
    public void jsonObjectByObjectAndNames() {
        String[] keys = {"publicString", "publicInt"};
        // just need a class that has public data members
        MyPublicClass myPublicClass = new MyPublicClass();
        JSONObject jsonObject = new JSONObject(myPublicClass, keys);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 2 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 2);
        assertTrue("expected \"publicString\":\"abc\"", "abc".equals(JsonPath.read(doc, "$.publicString")));
        assertTrue("expected \"publicInt\":42", Integer.valueOf(42).equals(JsonPath.read(doc, "$.publicInt")));
    }

    /**
     * Exercise the JSONObject from resource bundle functionality.
     * The test resource bundle is uncomplicated, but provides adequate test coverage.
     */
    @Test
    public void jsonObjectByResourceBundle() {
        JSONObject jsonObject = new
                JSONObject("org.json.junit.StringsResourceBundle",
                        Locale.getDefault());

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 2 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 2);
        assertTrue("expected 2 greetings items", ((Map<?,?>)(JsonPath.read(doc, "$.greetings"))).size() == 2);
        assertTrue("expected \"hello\":\"Hello, \"", "Hello, ".equals(JsonPath.read(doc, "$.greetings.hello")));
        assertTrue("expected \"world\":\"World!\"", "World!".equals(JsonPath.read(doc, "$.greetings.world")));
        assertTrue("expected 2 farewells items", ((Map<?,?>)(JsonPath.read(doc, "$.farewells"))).size() == 2);
        assertTrue("expected \"later\":\"Later, \"", "Later, ".equals(JsonPath.read(doc, "$.farewells.later")));
        assertTrue("expected \"world\":\"World!\"", "Alligator!".equals(JsonPath.read(doc, "$.farewells.gator")));
    }

    /**
     * Exercise the JSONObject.accumulate() method
     */
    @Test
    public void jsonObjectAccumulate() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("myArray", true);
        jsonObject.accumulate("myArray", false);
        jsonObject.accumulate("myArray", "hello world!");
        jsonObject.accumulate("myArray", "h\be\tllo w\u1234orld!");
        jsonObject.accumulate("myArray", 42);
        jsonObject.accumulate("myArray", -23.45e7);
        // include an unsupported object for coverage
        try {
            jsonObject.accumulate("myArray", Double.NaN);
            assertTrue("Expected exception", false);
        } catch (JSONException ignored) {}

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 1 top level item", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 1);
        assertTrue("expected 6 myArray items", ((List<?>)(JsonPath.read(doc, "$.myArray"))).size() == 6);
        assertTrue("expected true", Boolean.TRUE.equals(JsonPath.read(doc, "$.myArray[0]")));
        assertTrue("expected false", Boolean.FALSE.equals(JsonPath.read(doc, "$.myArray[1]")));
        assertTrue("expected hello world!", "hello world!".equals(JsonPath.read(doc, "$.myArray[2]")));
        assertTrue("expected h\be\tllo w\u1234orld!", "h\be\tllo w\u1234orld!".equals(JsonPath.read(doc,"$.myArray[3]")));
        assertTrue("expected 42", Integer.valueOf(42).equals(JsonPath.read(doc, "$.myArray[4]")));
        assertTrue("expected -23.45e7", Double.valueOf(-23.45e7).equals(JsonPath.read(doc, "$.myArray[5]")));
    }

    /**
     * Exercise the JSONObject append() functionality
     */
    @Test
    public void jsonObjectAppend() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("myArray", true);
        jsonObject.append("myArray", false);
        jsonObject.append("myArray", "hello world!");
        jsonObject.append("myArray", "h\be\tllo w\u1234orld!");
        jsonObject.append("myArray", 42);
        jsonObject.append("myArray", -23.45e7);
        // include an unsupported object for coverage
        try {
            jsonObject.append("myArray", Double.NaN);
            assertTrue("Expected exception", false);
        } catch (JSONException ignored) {}

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 1 top level item", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 1);
        assertTrue("expected 6 myArray items", ((List<?>)(JsonPath.read(doc, "$.myArray"))).size() == 6);
        assertTrue("expected true", Boolean.TRUE.equals(JsonPath.read(doc, "$.myArray[0]")));
        assertTrue("expected false", Boolean.FALSE.equals(JsonPath.read(doc, "$.myArray[1]")));
        assertTrue("expected hello world!", "hello world!".equals(JsonPath.read(doc, "$.myArray[2]")));
        assertTrue("expected h\be\tllo w\u1234orld!", "h\be\tllo w\u1234orld!".equals(JsonPath.read(doc,"$.myArray[3]")));
        assertTrue("expected 42", Integer.valueOf(42).equals(JsonPath.read(doc, "$.myArray[4]")));
        assertTrue("expected -23.45e7", Double.valueOf(-23.45e7).equals(JsonPath.read(doc, "$.myArray[5]")));
    }

    /**
     * Exercise the JSONObject doubleToString() method
     */
    @Test
    public void jsonObjectDoubleToString() {
        String [] expectedStrs = {"1", "1", "-23.4", "-2.345E68", "null", "null" };
        Double [] doubles = { 1.0, 00001.00000, -23.4, -23.45e67, 
                Double.NaN, Double.NEGATIVE_INFINITY }; 
        for (int i = 0; i < expectedStrs.length; ++i) {
            String actualStr = JSONObject.doubleToString(doubles[i]);
            assertTrue("value expected ["+expectedStrs[i]+
                    "] found ["+actualStr+ "]",
                    expectedStrs[i].equals(actualStr));
        }
    }

    /**
     * Exercise some JSONObject get[type] and opt[type] methods
     */
    @Test
    public void jsonObjectValues() {
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"trueStrKey\":\"true\","+
                "\"falseStrKey\":\"false\","+
                "\"stringKey\":\"hello world!\","+
                "\"intKey\":42,"+
                "\"intStrKey\":\"43\","+
                "\"longKey\":1234567890123456789,"+
                "\"longStrKey\":\"987654321098765432\","+
                "\"doubleKey\":-23.45e7,"+
                "\"doubleStrKey\":\"00001.000\","+
                "\"arrayKey\":[0,1,2],"+
                "\"objectKey\":{\"myKey\":\"myVal\"}"+
            "}";
        JSONObject jsonObject = new JSONObject(str);
        assertTrue("trueKey should be true", jsonObject.getBoolean("trueKey"));
        assertTrue("opt trueKey should be true", jsonObject.optBoolean("trueKey"));
        assertTrue("falseKey should be false", !jsonObject.getBoolean("falseKey"));
        assertTrue("trueStrKey should be true", jsonObject.getBoolean("trueStrKey"));
        assertTrue("trueStrKey should be true", jsonObject.optBoolean("trueStrKey"));
        assertTrue("falseStrKey should be false", !jsonObject.getBoolean("falseStrKey"));
        assertTrue("stringKey should be string",
            jsonObject.getString("stringKey").equals("hello world!"));
        assertTrue("doubleKey should be double", 
                jsonObject.getDouble("doubleKey") == -23.45e7);
        assertTrue("doubleStrKey should be double", 
                jsonObject.getDouble("doubleStrKey") == 1);
        assertTrue("opt doubleKey should be double", 
                jsonObject.optDouble("doubleKey") == -23.45e7);
        assertTrue("opt doubleKey with Default should be double", 
                jsonObject.optDouble("doubleStrKey", Double.NaN) == 1);
        assertTrue("intKey should be int", 
                jsonObject.optInt("intKey") == 42);
        assertTrue("opt intKey should be int", 
                jsonObject.optInt("intKey", 0) == 42);
        assertTrue("opt intKey with default should be int", 
                jsonObject.getInt("intKey") == 42);
        assertTrue("intStrKey should be int", 
                jsonObject.getInt("intStrKey") == 43);
        assertTrue("longKey should be long", 
                jsonObject.getLong("longKey") == 1234567890123456789L);
        assertTrue("opt longKey should be long", 
                jsonObject.optLong("longKey") == 1234567890123456789L);
        assertTrue("opt longKey with default should be long", 
                jsonObject.optLong("longKey", 0) == 1234567890123456789L);
        assertTrue("longStrKey should be long", 
                jsonObject.getLong("longStrKey") == 987654321098765432L);
        assertTrue("xKey should not exist",
                jsonObject.isNull("xKey"));
        assertTrue("stringKey should exist",
                jsonObject.has("stringKey"));
        assertTrue("opt stringKey should string",
                jsonObject.optString("stringKey").equals("hello world!"));
        assertTrue("opt stringKey with default should string",
                jsonObject.optString("stringKey", "not found").equals("hello world!"));
        JSONArray jsonArray = jsonObject.getJSONArray("arrayKey");
        assertTrue("arrayKey should be JSONArray", 
                jsonArray.getInt(0) == 0 &&
                jsonArray.getInt(1) == 1 &&
                jsonArray.getInt(2) == 2);
        jsonArray = jsonObject.optJSONArray("arrayKey");
        assertTrue("opt arrayKey should be JSONArray", 
                jsonArray.getInt(0) == 0 &&
                jsonArray.getInt(1) == 1 &&
                jsonArray.getInt(2) == 2);
        JSONObject jsonObjectInner = jsonObject.getJSONObject("objectKey");
        assertTrue("objectKey should be JSONObject", 
                jsonObjectInner.get("myKey").equals("myVal"));
    }

    /**
     * Check whether JSONObject handles large or high precision numbers correctly
     */
    @Test
    public void stringToValueNumbersTest() {
        assertTrue("-0 Should be a Double!",JSONObject.stringToValue("-0")  instanceof Double);
        assertTrue("-0.0 Should be a Double!",JSONObject.stringToValue("-0.0") instanceof Double);
        assertTrue("'-' Should be a String!",JSONObject.stringToValue("-") instanceof String);
        assertTrue( "0.2 should be a Double!",
                JSONObject.stringToValue( "0.2" ) instanceof Double );
        assertTrue( "Doubles should be Doubles, even when incorrectly converting floats!",
                JSONObject.stringToValue( new Double( "0.2f" ).toString() ) instanceof Double );
        /**
         * This test documents a need for BigDecimal conversion.
         */
        Object obj = JSONObject.stringToValue( "299792.457999999984" );
        assertTrue( "evaluates to 299792.458 doubld instead of 299792.457999999984 BigDecimal!",
                 obj.equals(new Double(299792.458)) );
        assertTrue( "1 should be an Integer!",
                JSONObject.stringToValue( "1" ) instanceof Integer );
        assertTrue( "Integer.MAX_VALUE should still be an Integer!",
                JSONObject.stringToValue( new Integer( Integer.MAX_VALUE ).toString() ) instanceof Integer );
        assertTrue( "Large integers should be a Long!",
                JSONObject.stringToValue( new Long( Long.sum( Integer.MAX_VALUE, 1 ) ).toString() ) instanceof Long );
        assertTrue( "Long.MAX_VALUE should still be an Integer!",
                JSONObject.stringToValue( new Long( Long.MAX_VALUE ).toString() ) instanceof Long );

        String str = new BigInteger( new Long( Long.MAX_VALUE ).toString() ).add( BigInteger.ONE ).toString();
        assertTrue( "Really large integers currently evaluate to string",
                JSONObject.stringToValue(str).equals("9223372036854775808"));
    }

    /**
     * This test documents numeric values which could be numerically
     * handled as BigDecimal or BigInteger. It helps determine what outputs
     * will change if those types are supported.
     */
    @Test
    public void jsonValidNumberValuesNeitherLongNorIEEE754Compatible() {
        // Valid JSON Numbers, probably should return BigDecimal or BigInteger objects
        String str = 
            "{"+
                "\"numberWithDecimals\":299792.457999999984,"+
                "\"largeNumber\":12345678901234567890,"+
                "\"preciseNumber\":0.2000000000000000111,"+
                "\"largeExponent\":-23.45e2327"+
            "}";
        JSONObject jsonObject = new JSONObject(str);
        // Comes back as a double, but loses precision
        assertTrue( "numberWithDecimals currently evaluates to double 299792.458",
                jsonObject.get( "numberWithDecimals" ).equals( new Double( "299792.458" ) ) );
        Object obj = jsonObject.get( "largeNumber" );
        assertTrue("largeNumber currently evaluates to string",
                "12345678901234567890".equals(obj));
        // comes back as a double but loses precision
        assertTrue( "preciseNumber currently evaluates to double 0.2",
                jsonObject.get( "preciseNumber" ).equals(new Double(0.2)));
        obj = jsonObject.get( "largeExponent" );
        assertTrue("largeExponent should currently evaluates as a string",
                "-23.45e2327".equals(obj));
    }

    /**
     * This test documents how JSON-Java handles invalid numeric input.
     */
    @Test
    public void jsonInvalidNumberValues() {
            // Number-notations supported by Java and invalid as JSON
        String str = 
            "{"+
                "\"hexNumber\":-0x123,"+
                "\"tooManyZeros\":00,"+
                "\"negativeInfinite\":-Infinity,"+
                "\"negativeNaN\":-NaN,"+
                "\"negativeFraction\":-.01,"+
                "\"tooManyZerosFraction\":00.001,"+
                "\"negativeHexFloat\":-0x1.fffp1,"+
                "\"hexFloat\":0x1.0P-1074,"+
                "\"floatIdentifier\":0.1f,"+
                "\"doubleIdentifier\":0.1d"+
            "}";
        JSONObject jsonObject = new JSONObject(str);
        Object obj;
        obj = jsonObject.get( "hexNumber" );
        assertFalse( "hexNumber must not be a number (should throw exception!?)",
                 obj instanceof Number );
        assertTrue("hexNumber currently evaluates to string",
                obj.equals("-0x123"));
        assertTrue( "tooManyZeros currently evaluates to string",
                jsonObject.get( "tooManyZeros" ).equals("00"));
        obj = jsonObject.get("negativeInfinite");
        assertTrue( "negativeInfinite currently evaluates to string",
                obj.equals("-Infinity"));
        obj = jsonObject.get("negativeNaN");
        assertTrue( "negativeNaN currently evaluates to string",
                obj.equals("-NaN"));
        assertTrue( "negativeFraction currently evaluates to double -0.01",
                jsonObject.get( "negativeFraction" ).equals(new Double(-0.01)));
        assertTrue( "tooManyZerosFraction currently evaluates to double 0.001",
                jsonObject.get( "tooManyZerosFraction" ).equals(new Double(0.001)));
        assertTrue( "negativeHexFloat currently evaluates to double -3.99951171875",
                jsonObject.get( "negativeHexFloat" ).equals(new Double(-3.99951171875)));
        assertTrue("hexFloat currently evaluates to double 4.9E-324",
                jsonObject.get("hexFloat").equals(new Double(4.9E-324)));
        assertTrue("floatIdentifier currently evaluates to double 0.1",
                jsonObject.get("floatIdentifier").equals(new Double(0.1)));
        assertTrue("doubleIdentifier currently evaluates to double 0.1",
                jsonObject.get("doubleIdentifier").equals(new Double(0.1)));
    }

    /**
     * Tests how JSONObject get[type] handles incorrect types
     */
    @Test
    public void jsonObjectNonAndWrongValues() {
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"trueStrKey\":\"true\","+
                "\"falseStrKey\":\"false\","+
                "\"stringKey\":\"hello world!\","+
                "\"intKey\":42,"+
                "\"intStrKey\":\"43\","+
                "\"longKey\":1234567890123456789,"+
                "\"longStrKey\":\"987654321098765432\","+
                "\"doubleKey\":-23.45e7,"+
                "\"doubleStrKey\":\"00001.000\","+
                "\"arrayKey\":[0,1,2],"+
                "\"objectKey\":{\"myKey\":\"myVal\"}"+
            "}";
        JSONObject jsonObject = new JSONObject(str);
        try {
            jsonObject.getBoolean("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("expecting an exception message", 
                    "JSONObject[\"nonKey\"] not found.".equals(e.getMessage()));
        }
        try {
            jsonObject.getBoolean("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"stringKey\"] is not a Boolean.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getString("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getString("trueKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"trueKey\"] not a string.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getDouble("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getDouble("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message",
                    "JSONObject[\"stringKey\"] is not a number.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getInt("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message",
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getInt("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"stringKey\"] is not an int.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getLong("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getLong("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"stringKey\"] is not a long.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getJSONArray("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getJSONArray("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"stringKey\"] is not a JSONArray.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getJSONObject("nonKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"nonKey\"] not found.".
                    equals(e.getMessage()));
        }
        try {
            jsonObject.getJSONObject("stringKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "JSONObject[\"stringKey\"] is not a JSONObject.".
                    equals(e.getMessage()));
        }
    }

    /**
     * This test documents an unexpected numeric behavior.
     * A double that ends with .0 is parsed, serialized, then
     * parsed again. On the second parse, it has become an int.
     */
    @Test
    public void unexpectedDoubleToIntConversion() {
        String key30 = "key30";
        String key31 = "key31";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key30, new Double(3.0));
        jsonObject.put(key31, new Double(3.1));

        assertTrue("3.0 should remain a double",
                jsonObject.getDouble(key30) == 3); 
        assertTrue("3.1 should remain a double",
                jsonObject.getDouble(key31) == 3.1); 
 
        // turns 3.0 into 3.
        String serializedString = jsonObject.toString();
        JSONObject deserialized = new JSONObject(serializedString);
        assertTrue("3.0 is now an int", deserialized.get(key30) instanceof Integer);
        assertTrue("3.0 can still be interpreted as a double",
                deserialized.getDouble(key30) == 3.0);
        assertTrue("3.1 remains a double", deserialized.getDouble(key31) == 3.1);
    }

    /**
     * Document behaviors of big numbers. Includes both JSONObject
     * and JSONArray tests
     */
    @Test
    public void bigNumberOperations() {
        /**
         * JSONObject tries to parse BigInteger as a bean, but it only has
         * one getter, getLowestBitSet(). The value is lost and an unhelpful
         * value is stored. This should be fixed.
         */
        BigInteger bigInteger = new BigInteger("123456789012345678901234567890");
        JSONObject jsonObject = new JSONObject(bigInteger);
        Object obj = jsonObject.get("lowestSetBit");
        assertTrue("JSONObject only has 1 value", jsonObject.length() == 1);
        assertTrue("JSONObject parses BigInteger as the Integer lowestBitSet",
                obj instanceof Integer);
        assertTrue("this bigInteger lowestBitSet happens to be 1",
                obj.equals(1));

        /**
         * JSONObject tries to parse BigDecimal as a bean, but it has
         * no getters, The value is lost and no value is stored.
         * This should be fixed.
         */
        BigDecimal bigDecimal = new BigDecimal(
                "123456789012345678901234567890.12345678901234567890123456789");
        jsonObject = new JSONObject(bigDecimal);
        assertTrue("large bigDecimal is not stored", jsonObject.length() == 0);

        /**
         * JSONObject put(String, Object) method stores and serializes
         * bigInt and bigDec correctly. Nothing needs to change. 
         */
        jsonObject = new JSONObject();
        jsonObject.put("bigInt", bigInteger);
        assertTrue("jsonObject.put() handles bigInt correctly",
                jsonObject.get("bigInt").equals(bigInteger));
        assertTrue("jsonObject.getBigInteger() handles bigInt correctly",
                jsonObject.getBigInteger("bigInt").equals(bigInteger));
        assertTrue("jsonObject.optBigInteger() handles bigInt correctly",
                jsonObject.optBigInteger("bigInt", BigInteger.ONE).equals(bigInteger));
        assertTrue("jsonObject serializes bigInt correctly",
                jsonObject.toString().equals("{\"bigInt\":123456789012345678901234567890}"));
        jsonObject = new JSONObject();
        jsonObject.put("bigDec", bigDecimal);
        assertTrue("jsonObject.put() handles bigDec correctly",
                jsonObject.get("bigDec").equals(bigDecimal));
        assertTrue("jsonObject.getBigDecimal() handles bigDec correctly",
                jsonObject.getBigDecimal("bigDec").equals(bigDecimal));
        assertTrue("jsonObject.optBigDecimal() handles bigDec correctly",
                jsonObject.optBigDecimal("bigDec", BigDecimal.ONE).equals(bigDecimal));
        assertTrue("jsonObject serializes bigDec correctly",
                jsonObject.toString().equals(
                "{\"bigDec\":123456789012345678901234567890.12345678901234567890123456789}"));

        /**
         * exercise some exceptions
         */
        try {
            jsonObject.getBigDecimal("bigInt");
            assertTrue("expected an exeption", false);
        } catch (JSONException ignored) {}
        obj = jsonObject.optBigDecimal("bigInt", BigDecimal.ONE);
        assertTrue("expected BigDecimal", obj.equals(BigDecimal.ONE));
        try {
            jsonObject.getBigInteger("bigDec");
            assertTrue("expected an exeption", false);
        } catch (JSONException ignored) {}
        jsonObject.put("stringKey",  "abc");
        try {
            jsonObject.getBigDecimal("stringKey");
            assertTrue("expected an exeption", false);
        } catch (JSONException ignored) {}
        obj = jsonObject.optBigInteger("bigDec", BigInteger.ONE);
        assertTrue("expected BigInteger", obj.equals(BigInteger.ONE));

        /**
         * JSONObject.numberToString() works correctly, nothing to change.
         */
        String str = JSONObject.numberToString(bigInteger);
        assertTrue("numberToString() handles bigInteger correctly",
                str.equals("123456789012345678901234567890"));
        str = JSONObject.numberToString(bigDecimal);
        assertTrue("numberToString() handles bigDecimal correctly",
                str.equals("123456789012345678901234567890.12345678901234567890123456789"));

        /**
         * JSONObject.stringToValue() turns bigInt into an accurate string,
         * and rounds bigDec. This incorrect, but users may have come to 
         * expect this behavior. Change would be marginally better, but 
         * might inconvenience users.
         */
        obj = JSONObject.stringToValue(bigInteger.toString());
        assertTrue("stringToValue() turns bigInteger string into string",
                obj instanceof String);
        obj = JSONObject.stringToValue(bigDecimal.toString());
        assertTrue("stringToValue() changes bigDecimal string",
                !obj.toString().equals(bigDecimal.toString()));

        /**
         * wrap() vs put() big number behavior is now the same.
         */
        // bigInt map ctor 
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("bigInt", bigInteger);
        jsonObject = new JSONObject(map);
        String actualFromMapStr = jsonObject.toString();
        assertTrue("bigInt in map (or array or bean) is a string",
                actualFromMapStr.equals(
                "{\"bigInt\":123456789012345678901234567890}"));
        // bigInt put
        jsonObject = new JSONObject();
        jsonObject.put("bigInt", bigInteger);
        String actualFromPutStr = jsonObject.toString();
        assertTrue("bigInt from put is a number",
                actualFromPutStr.equals(
                "{\"bigInt\":123456789012345678901234567890}"));
        // bigDec map ctor
        map = new HashMap<String, Object>();
        map.put("bigDec", bigDecimal);
        jsonObject = new JSONObject(map);
        actualFromMapStr = jsonObject.toString();
        assertTrue("bigDec in map (or array or bean) is a bigDec",
                actualFromMapStr.equals(
                "{\"bigDec\":123456789012345678901234567890.12345678901234567890123456789}"));
        // bigDec put
        jsonObject = new JSONObject();
        jsonObject.put("bigDec", bigDecimal);
        actualFromPutStr = jsonObject.toString();
        assertTrue("bigDec from put is a number",
                actualFromPutStr.equals(
                "{\"bigDec\":123456789012345678901234567890.12345678901234567890123456789}"));
        // bigInt,bigDec put 
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(bigInteger);
        jsonArray.put(bigDecimal);
        actualFromPutStr = jsonArray.toString();
        assertTrue("bigInt, bigDec from put is a number",
                actualFromPutStr.equals(
                "[123456789012345678901234567890,123456789012345678901234567890.12345678901234567890123456789]"));
        assertTrue("getBigInt is bigInt", jsonArray.getBigInteger(0).equals(bigInteger));
        assertTrue("getBigDec is bigDec", jsonArray.getBigDecimal(1).equals(bigDecimal));
        assertTrue("optBigInt is bigInt", jsonArray.optBigInteger(0, BigInteger.ONE).equals(bigInteger));
        assertTrue("optBigDec is bigDec", jsonArray.optBigDecimal(1, BigDecimal.ONE).equals(bigDecimal));
        jsonArray.put(Boolean.TRUE);
        try {
            jsonArray.getBigInteger(2);
            assertTrue("should not be able to get big int", false);
        } catch (Exception ignored) {}
        try {
            jsonArray.getBigDecimal(2);
            assertTrue("should not be able to get big dec", false);
        } catch (Exception ignored) {}
        assertTrue("optBigInt is default", jsonArray.optBigInteger(2, BigInteger.ONE).equals(BigInteger.ONE));
        assertTrue("optBigDec is default", jsonArray.optBigDecimal(2, BigDecimal.ONE).equals(BigDecimal.ONE));

        // bigInt,bigDec list ctor
        List<Object> list = new ArrayList<Object>();
        list.add(bigInteger);
        list.add(bigDecimal);
        jsonArray = new JSONArray(list);
        String actualFromListStr = jsonArray.toString();
        assertTrue("bigInt, bigDec in list is a bigInt, bigDec",
                actualFromListStr.equals(
                "[123456789012345678901234567890,123456789012345678901234567890.12345678901234567890123456789]"));
        // bigInt bean ctor
        MyBigNumberBean myBigNumberBean = mock(MyBigNumberBean.class);
        when(myBigNumberBean.getBigInteger()).thenReturn(new BigInteger("123456789012345678901234567890"));
        jsonObject = new JSONObject(myBigNumberBean);
        String actualFromBeanStr = jsonObject.toString();
        // can't do a full string compare because mockery adds an extra key/value
        assertTrue("bigInt from bean ctor is a bigInt",
                actualFromBeanStr.contains("123456789012345678901234567890"));
        // bigDec bean ctor
        myBigNumberBean = mock(MyBigNumberBean.class);
        when(myBigNumberBean.getBigDecimal()).thenReturn(new BigDecimal("123456789012345678901234567890.12345678901234567890123456789"));
        jsonObject = new JSONObject(myBigNumberBean);
        actualFromBeanStr = jsonObject.toString();
        // can't do a full string compare because mockery adds an extra key/value
        assertTrue("bigDec from bean ctor is a bigDec",
                actualFromBeanStr.contains("123456789012345678901234567890.12345678901234567890123456789"));
        // bigInt,bigDec wrap()
        obj = JSONObject.wrap(bigInteger);
        assertTrue("wrap() returns big num",obj.equals(bigInteger));
        obj = JSONObject.wrap(bigDecimal);
        assertTrue("wrap() returns string",obj.equals(bigDecimal));

    }

    /**
     * The purpose for the static method getNames() methods are not clear.
     * This method is not called from within JSON-Java. Most likely
     * uses are to prep names arrays for:  
     * JSONObject(JSONObject jo, String[] names)
     * JSONObject(Object object, String names[]),
     */
    @Test
    public void jsonObjectNames() {
        JSONObject jsonObject;

        // getNames() from null JSONObject
        assertTrue("null names from null Object", 
                null == JSONObject.getNames((Object)null));

        // getNames() from object with no fields
        assertTrue("null names from Object with no fields", 
                null == JSONObject.getNames(new MyJsonString()));

        // getNames from new JSONOjbect
        jsonObject = new JSONObject();
        String [] names = JSONObject.getNames(jsonObject);
        assertTrue("names should be null", names == null);

        
        // getNames() from empty JSONObject
        String emptyStr = "{}";
        jsonObject = new JSONObject(emptyStr);
        assertTrue("empty JSONObject should have null names",
                null == JSONObject.getNames(jsonObject));

        // getNames() from JSONObject
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"stringKey\":\"hello world!\","+
            "}";
        jsonObject = new JSONObject(str);
        names = JSONObject.getNames(jsonObject);
        JSONArray jsonArray = new JSONArray(names);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider()
                .parse(jsonArray.toString());
        List<?> docList = JsonPath.read(doc, "$");
        assertTrue("expected 3 items", docList.size() == 3);
        assertTrue(
                "expected to find trueKey",
                ((List<?>) JsonPath.read(doc, "$[?(@=='trueKey')]")).size() == 1);
        assertTrue(
                "expected to find falseKey",
                ((List<?>) JsonPath.read(doc, "$[?(@=='falseKey')]")).size() == 1);
        assertTrue(
                "expected to find stringKey",
                ((List<?>) JsonPath.read(doc, "$[?(@=='stringKey')]")).size() == 1);

        /**
         * getNames() from an enum with properties has an interesting result.
         * It returns the enum values, not the selected enum properties
         */
        MyEnumField myEnumField = MyEnumField.VAL1;
        names = JSONObject.getNames(myEnumField);

        // validate JSON
        jsonArray = new JSONArray(names);
        doc = Configuration.defaultConfiguration().jsonProvider()
                .parse(jsonArray.toString());
        docList = JsonPath.read(doc, "$");
        assertTrue("expected 3 items", docList.size() == 3);
        assertTrue(
                "expected to find VAL1",
                ((List<?>) JsonPath.read(doc, "$[?(@=='VAL1')]")).size() == 1);
        assertTrue(
                "expected to find VAL2",
                ((List<?>) JsonPath.read(doc, "$[?(@=='VAL2')]")).size() == 1);
        assertTrue(
                "expected to find VAL3",
                ((List<?>) JsonPath.read(doc, "$[?(@=='VAL3')]")).size() == 1);

        /**
         * A bean is also an object. But in order to test the static
         * method getNames(), this particular bean needs some public
         * data members.
         */
        MyPublicClass myPublicClass = new MyPublicClass();
        names = JSONObject.getNames(myPublicClass);

        // validate JSON
        jsonArray = new JSONArray(names);
        doc = Configuration.defaultConfiguration().jsonProvider()
                .parse(jsonArray.toString());
        docList = JsonPath.read(doc, "$");
        assertTrue("expected 2 items", docList.size() == 2);
        assertTrue(
                "expected to find publicString",
                ((List<?>) JsonPath.read(doc, "$[?(@=='publicString')]")).size() == 1);
        assertTrue(
                "expected to find publicInt",
                ((List<?>) JsonPath.read(doc, "$[?(@=='publicInt')]")).size() == 1);
    }

    /**
     * Populate a JSONArray from an empty JSONObject names() method.
     * It should be empty.
     */
    @Test
    public void emptyJsonObjectNamesToJsonAray() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = jsonObject.names();
        assertTrue("jsonArray should be null", jsonArray == null);
    }

    /**
     * Populate a JSONArray from a JSONObject names() method.
     * Confirm that it contains the expected names.
     */
    @Test
    public void jsonObjectNamesToJsonAray() {
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"stringKey\":\"hello world!\","+
            "}";

        JSONObject jsonObject = new JSONObject(str);
        JSONArray jsonArray = jsonObject.names();

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonArray.toString());
        assertTrue("expected 3 top level items", ((List<?>)(JsonPath.read(doc, "$"))).size() == 3);
        assertTrue("expected to find trueKey", ((List<?>) JsonPath.read(doc, "$[?(@=='trueKey')]")).size() == 1);
        assertTrue("expected to find falseKey", ((List<?>) JsonPath.read(doc, "$[?(@=='falseKey')]")).size() == 1);
        assertTrue("expected to find stringKey", ((List<?>) JsonPath.read(doc, "$[?(@=='stringKey')]")).size() == 1);
    }

    /**
     * Exercise the JSONObject increment() method.
     */
    @Test
    public void jsonObjectIncrement() {
        String str = 
            "{"+
                "\"keyLong\":9999999991,"+
                "\"keyDouble\":1.1"+
             "}";
        JSONObject jsonObject = new JSONObject(str);
        jsonObject.increment("keyInt");
        jsonObject.increment("keyInt");
        jsonObject.increment("keyLong");
        jsonObject.increment("keyDouble");
        jsonObject.increment("keyInt");
        jsonObject.increment("keyLong");
        jsonObject.increment("keyDouble");
        /**
         * JSONObject constructor won't handle these types correctly, but
         * adding them via put works.
         */
        jsonObject.put("keyFloat", new Float(1.1));
        jsonObject.put("keyBigInt", new BigInteger("123456789123456789123456789123456780"));
        jsonObject.put("keyBigDec", new BigDecimal("123456789123456789123456789123456780.1"));
        jsonObject.increment("keyFloat");
        jsonObject.increment("keyFloat");
        jsonObject.increment("keyBigInt");
        jsonObject.increment("keyBigDec");

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 6 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 6);
        assertTrue("expected 3", Integer.valueOf(3).equals(JsonPath.read(doc, "$.keyInt")));
        assertTrue("expected 9999999993", Long.valueOf(9999999993L).equals(JsonPath.read(doc, "$.keyLong")));
        assertTrue("expected 3.1", Double.valueOf(3.1).equals(JsonPath.read(doc, "$.keyDouble")));
        assertTrue("expected 123456789123456789123456789123456781", new BigInteger("123456789123456789123456789123456781").equals(JsonPath.read(doc, "$.keyBigInt")));
        assertTrue("expected 123456789123456789123456789123456781.1", new BigDecimal("123456789123456789123456789123456781.1").equals(JsonPath.read(doc, "$.keyBigDec")));

        /**
         * Should work the same way on any platform! @see https://docs.oracle
         * .com/javase/specs/jls/se7/html/jls-4.html#jls-4.2.3 This is the
         * effect of a float to double conversion and is inherent to the
         * shortcomings of the IEEE 754 format, when converting 32-bit into
         * double-precision 64-bit. Java type-casts float to double. A 32 bit
         * float is type-casted to 64 bit double by simply appending zero-bits
         * to the mantissa (and extended the signed exponent by 3 bits.) and
         * there is no way to obtain more information than it is stored in the
         * 32-bits float.
         * 
         * Like 1/3 cannot be represented as base10 number because it is
         * periodically, 1/5 (for example) cannot be represented as base2 number
         * since it is periodically in base2 (take a look at
         * http://www.h-schmidt.net/FloatConverter/) The same happens to 3.1,
         * that decimal number (base10 representation) is periodic in base2
         * representation, therefore appending zero-bits is inaccurate. Only
         * repeating the periodically occuring bits (0110) would be a proper
         * conversion. However one cannot detect from a 32 bit IEE754
         * representation which bits would "repeat infinitely", since the
         * missing bits would not fit into the 32 bit float, i.e. the
         * information needed simply is not there!
         */
        assertTrue("expected 3.0999999046325684", Double.valueOf(3.0999999046325684).equals(JsonPath.read(doc, "$.keyFloat")));

        /**
         * float f = 3.1f; double df = (double) f; double d = 3.1d;
         * System.out.println
         * (Integer.toBinaryString(Float.floatToRawIntBits(f)));
         * System.out.println
         * (Long.toBinaryString(Double.doubleToRawLongBits(df)));
         * System.out.println
         * (Long.toBinaryString(Double.doubleToRawLongBits(d)));
         * 
         * - Float:
         * seeeeeeeemmmmmmmmmmmmmmmmmmmmmmm
         * 1000000010001100110011001100110
         * - Double
         * seeeeeeeeeeemmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
         * 10000000   10001100110011001100110
         * 100000000001000110011001100110011000000000000000000000000000000
         * 100000000001000110011001100110011001100110011001100110011001101
         */

        /**
        * Examples of well documented but probably unexpected behavior in 
        * java / with 32-bit float to 64-bit float conversion.
        */
        assertFalse("Document unexpected behaviour with explicit type-casting float as double!", (double)0.2f == 0.2d );
        assertFalse("Document unexpected behaviour with implicit type-cast!", 0.2f == 0.2d );
        Double d1 = new Double( 1.1f );
        Double d2 = new Double( "1.1f" );
        assertFalse( "Document implicit type cast from float to double before calling Double(double d) constructor", d1.equals( d2 ) );

        assertTrue( "Correctly converting float to double via base10 (string) representation!", new Double( 3.1d ).equals(  new Double( new Float( 3.1f ).toString() ) ) );

        // Pinpointing the not so obvious "buggy" conversion from float to double in JSONObject
        JSONObject jo = new JSONObject();
        jo.put( "bug", 3.1f ); // will call put( String key, double value ) with implicit and "buggy" type-cast from float to double
        assertFalse( "The java-compiler did add some zero bits for you to the mantissa (unexpected, but well documented)", jo.get( "bug" ).equals(  new Double( 3.1d ) ) );

        JSONObject inc = new JSONObject();
        inc.put( "bug", new Float( 3.1f ) ); // This will put in instance of Float into JSONObject, i.e. call put( String key, Object value )
        assertTrue( "Everything is ok here!", inc.get( "bug" ) instanceof Float );
        inc.increment( "bug" ); // after adding 1, increment will call put( String key, double value ) with implicit and "buggy" type-cast from float to double!
        // this.put(key, (Float) value + 1);
        // 1.        The (Object)value will be typecasted to (Float)value since it is an instanceof Float actually nothing is done. 
        // 2.        Float instance will be autoboxed into float because the + operator will work on primitives not Objects!
        // 3.        A float+float operation will be performed and results into a float primitive.
        // 4.        There is no method that matches the signature put( String key, float value), java-compiler will choose the method
        //                put( String key, double value) and does an implicit type-cast(!) by appending zero-bits to the mantissa
        assertTrue( "JSONObject increment converts Float to Double", jo.get( "bug" ) instanceof Double );
        // correct implementation (with change of behavior) would be:
        // this.put(key, new Float((Float) value + 1)); 
        // Probably it would be better to deprecate the method and remove some day, while convenient processing the "payload" is not
        // really in the the scope of a JSON-library (IMHO.)

    }

    /**
     * Exercise JSONObject numberToString() method
     */
    @Test
    public void jsonObjectNumberToString() {
        String str;
        Double dVal;
        Integer iVal = 1;
        str = JSONObject.numberToString(iVal);
        assertTrue("expected "+iVal+" actual "+str, iVal.toString().equals(str));
        dVal = 12.34;
        str = JSONObject.numberToString(dVal);
        assertTrue("expected "+dVal+" actual "+str, dVal.toString().equals(str));
        dVal = 12.34e27;
        str = JSONObject.numberToString(dVal);
        assertTrue("expected "+dVal+" actual "+str, dVal.toString().equals(str));
        // trailing .0 is truncated, so it doesn't quite match toString()
        dVal = 5000000.0000000;
        str = JSONObject.numberToString(dVal);
        assertTrue("expected 5000000 actual "+str, str.equals("5000000"));
    }

    /**
     * Exercise JSONObject put() and similar() methods
     */
    @Test
    public void jsonObjectPut() {
        String expectedStr = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"arrayKey\":[0,1,2],"+
                "\"objectKey\":{"+
                    "\"myKey1\":\"myVal1\","+
                    "\"myKey2\":\"myVal2\","+
                    "\"myKey3\":\"myVal3\","+
                    "\"myKey4\":\"myVal4\""+
                "}"+
            "}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trueKey", true);
        jsonObject.put("falseKey", false);
        Integer [] intArray = { 0, 1, 2 };
        jsonObject.put("arrayKey", Arrays.asList(intArray));
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("myKey1", "myVal1");
        myMap.put("myKey2", "myVal2");
        myMap.put("myKey3", "myVal3");
        myMap.put("myKey4", "myVal4");
        jsonObject.put("objectKey", myMap);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 4 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 4);
        assertTrue("expected true", Boolean.TRUE.equals(JsonPath.read(doc, "$.trueKey")));
        assertTrue("expected false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected 3 arrayKey items", ((List<?>)(JsonPath.read(doc, "$.arrayKey"))).size() == 3);
        assertTrue("expected 0", Integer.valueOf(0).equals(JsonPath.read(doc, "$.arrayKey[0]")));
        assertTrue("expected 1", Integer.valueOf(1).equals(JsonPath.read(doc, "$.arrayKey[1]")));
        assertTrue("expected 2", Integer.valueOf(2).equals(JsonPath.read(doc, "$.arrayKey[2]")));
        assertTrue("expected 4 objectKey items", ((Map<?,?>)(JsonPath.read(doc, "$.objectKey"))).size() == 4);
        assertTrue("expected myVal1", "myVal1".equals(JsonPath.read(doc, "$.objectKey.myKey1")));
        assertTrue("expected myVal2", "myVal2".equals(JsonPath.read(doc, "$.objectKey.myKey2")));
        assertTrue("expected myVal3", "myVal3".equals(JsonPath.read(doc, "$.objectKey.myKey3")));
        assertTrue("expected myVal4", "myVal4".equals(JsonPath.read(doc, "$.objectKey.myKey4")));

        jsonObject.remove("trueKey");
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        assertTrue("unequal jsonObjects should not be similar",
                !jsonObject.similar(expectedJsonObject));
        assertTrue("jsonObject should not be similar to jsonArray",
                !jsonObject.similar(new JSONArray()));

        String aCompareValueStr = "{\"a\":\"aval\",\"b\":true}";
        String bCompareValueStr = "{\"a\":\"notAval\",\"b\":true}";
        JSONObject aCompareValueJsonObject = new JSONObject(aCompareValueStr);
        JSONObject bCompareValueJsonObject = new JSONObject(bCompareValueStr);
        assertTrue("different values should not be similar",
                !aCompareValueJsonObject.similar(bCompareValueJsonObject));

        String aCompareObjectStr = "{\"a\":\"aval\",\"b\":{}}";
        String bCompareObjectStr = "{\"a\":\"aval\",\"b\":true}";
        JSONObject aCompareObjectJsonObject = new JSONObject(aCompareObjectStr);
        JSONObject bCompareObjectJsonObject = new JSONObject(bCompareObjectStr);
        assertTrue("different nested JSONObjects should not be similar",
                !aCompareObjectJsonObject.similar(bCompareObjectJsonObject));

        String aCompareArrayStr = "{\"a\":\"aval\",\"b\":[]}";
        String bCompareArrayStr = "{\"a\":\"aval\",\"b\":true}";
        JSONObject aCompareArrayJsonObject = new JSONObject(aCompareArrayStr);
        JSONObject bCompareArrayJsonObject = new JSONObject(bCompareArrayStr);
        assertTrue("different nested JSONArrays should not be similar",
                !aCompareArrayJsonObject.similar(bCompareArrayJsonObject));
    }

    /**
     * Exercise JSONObject toString() method
     */
    @Test
    public void jsonObjectToString() {
        String str = 
            "{"+
                "\"trueKey\":true,"+
                "\"falseKey\":false,"+
                "\"arrayKey\":[0,1,2],"+
                "\"objectKey\":{"+
                    "\"myKey1\":\"myVal1\","+
                    "\"myKey2\":\"myVal2\","+
                    "\"myKey3\":\"myVal3\","+
                    "\"myKey4\":\"myVal4\""+
                "}"+
            "}";
        JSONObject jsonObject = new JSONObject(str);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 4 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 4);
        assertTrue("expected true", Boolean.TRUE.equals(JsonPath.read(doc, "$.trueKey")));
        assertTrue("expected false", Boolean.FALSE.equals(JsonPath.read(doc, "$.falseKey")));
        assertTrue("expected 3 arrayKey items", ((List<?>)(JsonPath.read(doc, "$.arrayKey"))).size() == 3);
        assertTrue("expected 0", Integer.valueOf(0).equals(JsonPath.read(doc, "$.arrayKey[0]")));
        assertTrue("expected 1", Integer.valueOf(1).equals(JsonPath.read(doc, "$.arrayKey[1]")));
        assertTrue("expected 2", Integer.valueOf(2).equals(JsonPath.read(doc, "$.arrayKey[2]")));
        assertTrue("expected 4 objectKey items", ((Map<?,?>)(JsonPath.read(doc, "$.objectKey"))).size() == 4);
        assertTrue("expected myVal1", "myVal1".equals(JsonPath.read(doc, "$.objectKey.myKey1")));
        assertTrue("expected myVal2", "myVal2".equals(JsonPath.read(doc, "$.objectKey.myKey2")));
        assertTrue("expected myVal3", "myVal3".equals(JsonPath.read(doc, "$.objectKey.myKey3")));
        assertTrue("expected myVal4", "myVal4".equals(JsonPath.read(doc, "$.objectKey.myKey4")));
    }

    /**
     * Explores how JSONObject handles maps. Insert a string/string map
     * as a value in a JSONObject. It will remain a map. Convert the 
     * JSONObject to string, then create a new JSONObject from the string. 
     * In the new JSONObject, the value will be stored as a nested JSONObject.
     * Confirm that map and nested JSONObject have the same contents.
     */
    @Test
    public void jsonObjectToStringSuppressWarningOnCastToMap() {
        JSONObject jsonObject = new JSONObject();
        Map<String, String> map = new HashMap<>();
        map.put("abc", "def");
        jsonObject.put("key", map);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 1 top level item", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 1);
        assertTrue("expected 1 key item", ((Map<?,?>)(JsonPath.read(doc, "$.key"))).size() == 1);
        assertTrue("expected def", "def".equals(JsonPath.read(doc, "$.key.abc")));
    }

    /**
     * Explores how JSONObject handles collections. Insert a string collection
     * as a value in a JSONObject. It will remain a collection. Convert the 
     * JSONObject to string, then create a new JSONObject from the string. 
     * In the new JSONObject, the value will be stored as a nested JSONArray.
     * Confirm that collection and nested JSONArray have the same contents.
     */
    @Test
    public void jsonObjectToStringSuppressWarningOnCastToCollection() {
        JSONObject jsonObject = new JSONObject();
        Collection<String> collection = new ArrayList<String>();
        collection.add("abc");
        // ArrayList will be added as an object
        jsonObject.put("key", collection);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 1 top level item", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 1);
        assertTrue("expected 1 key item", ((List<?>)(JsonPath.read(doc, "$.key"))).size() == 1);
        assertTrue("expected abc", "abc".equals(JsonPath.read(doc, "$.key[0]")));
    }

    /**
     * Exercises the JSONObject.valueToString() method for various types
     */
    @Test
    public void valueToString() {
        
        assertTrue("null valueToString() incorrect",
                "null".equals(JSONObject.valueToString(null)));
        MyJsonString jsonString = new MyJsonString();
        assertTrue("jsonstring valueToString() incorrect",
                "my string".equals(JSONObject.valueToString(jsonString)));
        assertTrue("boolean valueToString() incorrect",
                "true".equals(JSONObject.valueToString(Boolean.TRUE)));
        assertTrue("non-numeric double",
                "null".equals(JSONObject.doubleToString(Double.POSITIVE_INFINITY)));
        String jsonObjectStr = 
            "{"+
                "\"key1\":\"val1\","+
                "\"key2\":\"val2\","+
                "\"key3\":\"val3\""+
             "}";
        JSONObject jsonObject = new JSONObject(jsonObjectStr);
        assertTrue("jsonObject valueToString() incorrect",
                JSONObject.valueToString(jsonObject).equals(jsonObject.toString()));
        String jsonArrayStr = 
            "[1,2,3]";
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        assertTrue("jsonArra valueToString() incorrect",
                JSONObject.valueToString(jsonArray).equals(jsonArray.toString()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");
        assertTrue("map valueToString() incorrect",
                jsonObject.toString().equals(JSONObject.valueToString(map))); 
        Collection<Integer> collection = new ArrayList<Integer>();
        collection.add(new Integer(1));
        collection.add(new Integer(2));
        collection.add(new Integer(3));
        assertTrue("collection valueToString() expected: "+
                jsonArray.toString()+ " actual: "+
                JSONObject.valueToString(collection),
                jsonArray.toString().equals(JSONObject.valueToString(collection))); 
        Integer[] array = { new Integer(1), new Integer(2), new Integer(3) };
        assertTrue("array valueToString() incorrect",
                jsonArray.toString().equals(JSONObject.valueToString(array))); 
    }

    /**
     * Confirm that https://github.com/douglascrockford/JSON-java/issues/167 is fixed.
     * The following code was throwing a ClassCastException in the 
     * JSONObject(Map<String, Object>) constructor
     */
    @Test
    public void valueToStringConfirmException() {
        Map<Integer, String> myMap = new HashMap<Integer, String>();
        myMap.put(1,  "myValue");
        // this is the test, it should not throw an exception
        String str = JSONObject.valueToString(myMap);
        // confirm result, just in case
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(str);
        assertTrue("expected 1 top level item", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 1);
        assertTrue("expected myValue", "myValue".equals(JsonPath.read(doc, "$.1")));
    }

    /**
     * Exercise the JSONObject wrap() method. Sometimes wrap() will change
     * the object being wrapped, other times not. The purpose of wrap() is
     * to ensure the value is packaged in a way that is compatible with how
     * a JSONObject value or JSONArray value is supposed to be stored.
     */
    @Test
    public void wrapObject() {
        // wrap(null) returns NULL
        assertTrue("null wrap() incorrect",
                JSONObject.NULL == JSONObject.wrap(null));

        // wrap(Integer) returns Integer
        Integer in = new Integer(1);
        assertTrue("Integer wrap() incorrect",
                in == JSONObject.wrap(in));

        /**
         * This test is to document the preferred behavior if BigDecimal is
         * supported. Previously  bd returned as a string, since it
         * is recognized as being a Java package class. Now with explicit
         * support for big numbers, it remains a BigDecimal 
         */
        Object bdWrap = JSONObject.wrap(BigDecimal.ONE);
        assertTrue("BigDecimal.ONE evaluates to ONE",
                bdWrap.equals(BigDecimal.ONE));

        // wrap JSONObject returns JSONObject
        String jsonObjectStr = 
                "{"+
                    "\"key1\":\"val1\","+
                    "\"key2\":\"val2\","+
                    "\"key3\":\"val3\""+
                 "}";
        JSONObject jsonObject = new JSONObject(jsonObjectStr);
        assertTrue("JSONObject wrap() incorrect",
                jsonObject == JSONObject.wrap(jsonObject));

        // wrap collection returns JSONArray
        Collection<Integer> collection = new ArrayList<Integer>();
        collection.add(new Integer(1));
        collection.add(new Integer(2));
        collection.add(new Integer(3));
        JSONArray jsonArray = (JSONArray) (JSONObject.wrap(collection));

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonArray.toString());
        assertTrue("expected 3 top level items", ((List<?>)(JsonPath.read(doc, "$"))).size() == 3);
        assertTrue("expected 1", Integer.valueOf(1).equals(JsonPath.read(doc, "$[0]")));
        assertTrue("expected 2", Integer.valueOf(2).equals(JsonPath.read(doc, "$[1]")));
        assertTrue("expected 3", Integer.valueOf(3).equals(JsonPath.read(doc, "$[2]")));

        // wrap Array returns JSONArray
        Integer[] array = { new Integer(1), new Integer(2), new Integer(3) };
        JSONArray integerArrayJsonArray = (JSONArray)(JSONObject.wrap(array));

        // validate JSON
        doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonArray.toString());
        assertTrue("expected 3 top level items", ((List<?>)(JsonPath.read(doc, "$"))).size() == 3);
        assertTrue("expected 1", Integer.valueOf(1).equals(JsonPath.read(doc, "$[0]")));
        assertTrue("expected 2", Integer.valueOf(2).equals(JsonPath.read(doc, "$[1]")));
        assertTrue("expected 3", Integer.valueOf(3).equals(JsonPath.read(doc, "$[2]")));

        // validate JSON
        doc = Configuration.defaultConfiguration().jsonProvider().parse(integerArrayJsonArray.toString());
        assertTrue("expected 3 top level items", ((List<?>)(JsonPath.read(doc, "$"))).size() == 3);
        assertTrue("expected 1", Integer.valueOf(1).equals(JsonPath.read(doc, "$[0]")));
        assertTrue("expected 2", Integer.valueOf(2).equals(JsonPath.read(doc, "$[1]")));
        assertTrue("expected 3", Integer.valueOf(3).equals(JsonPath.read(doc, "$[2]")));

        // wrap map returns JSONObject
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");
        JSONObject mapJsonObject = (JSONObject) (JSONObject.wrap(map));

        // validate JSON
        doc = Configuration.defaultConfiguration().jsonProvider().parse(mapJsonObject.toString());
        assertTrue("expected 3 top level items", ((Map<?,?>)(JsonPath.read(doc, "$"))).size() == 3);
        assertTrue("expected val1", "val1".equals(JsonPath.read(doc, "$.key1")));
        assertTrue("expected val2", "val2".equals(JsonPath.read(doc, "$.key2")));
        assertTrue("expected val3", "val3".equals(JsonPath.read(doc, "$.key3")));
    }

    /**
     * Explore how JSONObject handles parsing errors.
     */
    @Test
    public void jsonObjectParsingErrors() {
        try {
            // does not start with '{'
            String str = "abc";
            new JSONObject(str);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "A JSONObject text must begin with '{' at 1 [character 2 line 1]".
                    equals(e.getMessage()));
        }
        try {
            // does not end with '}'
            String str = "{";
            new JSONObject(str);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "A JSONObject text must end with '}' at 2 [character 3 line 1]".
                    equals(e.getMessage()));
        }
        try {
            // key with no ':'
            String str = "{\"myKey\" = true}";
            new JSONObject(str);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "Expected a ':' after a key at 10 [character 11 line 1]".
                    equals(e.getMessage()));
        }
        try {
            // entries with no ',' separator
            String str = "{\"myKey\":true \"myOtherKey\":false}";
            new JSONObject(str);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "Expected a ',' or '}' at 15 [character 16 line 1]".
                    equals(e.getMessage()));
        }
        try {
            // append to wrong key
            String str = "{\"myKey\":true, \"myOtherKey\":false}";
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.append("myKey", "hello");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message",
                    "JSONObject[myKey] is not a JSONArray.".
                    equals(e.getMessage()));
        }
        try {
            // increment wrong key
            String str = "{\"myKey\":true, \"myOtherKey\":false}";
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.increment("myKey");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message",
                    "Unable to increment [\"myKey\"].".
                    equals(e.getMessage()));
        }
        try {
            // invalid key
            String str = "{\"myKey\":true, \"myOtherKey\":false}";
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.get(null);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message",
                    "Null key.".
                    equals(e.getMessage()));
        }
        try {
            // invalid numberToString()
            JSONObject.numberToString((Number)null);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("Expecting an exception message", 
                    "Null pointer".
                    equals(e.getMessage()));
        }
        try {
            // null put key 
            JSONObject jsonObject = new JSONObject("{}");
            jsonObject.put(null, 0);
            assertTrue("Expected an exception", false);
        } catch (NullPointerException ignored) { 
        }
        try {
            // multiple putOnce key 
            JSONObject jsonObject = new JSONObject("{}");
            jsonObject.putOnce("hello", "world");
            jsonObject.putOnce("hello", "world!");
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("", true);
        }
        try {
            // test validity of invalid double 
            JSONObject.testValidity(Double.NaN);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("", true);
        }
        try {
            // test validity of invalid float 
            JSONObject.testValidity(Float.NEGATIVE_INFINITY);
            assertTrue("Expected an exception", false);
        } catch (JSONException e) { 
            assertTrue("", true);
        }
    }

    /**
     * Confirm behavior when putOnce() is called with null parameters
     */
    @Test
    public void jsonObjectPutOnceNull() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce(null, null);
        assertTrue("jsonObject should be empty", jsonObject.length() == 0);
    }

    /**
     * Exercise JSONObject opt(key, default) method
     */
    @Test
    public void jsonObjectOptDefault() {

        String str = "{\"myKey\": \"myval\"}";
        JSONObject jsonObject = new JSONObject(str);

        assertTrue("optBoolean() should return default boolean",
                Boolean.TRUE == jsonObject.optBoolean("myKey", Boolean.TRUE));
        assertTrue("optInt() should return default int",
                42 == jsonObject.optInt("myKey", 42));
        assertTrue("optInt() should return default int",
                42 == jsonObject.optInt("myKey", 42));
        assertTrue("optLong() should return default long",
                42 == jsonObject.optLong("myKey", 42));
        assertTrue("optDouble() should return default double",
                42.3 == jsonObject.optDouble("myKey", 42.3));
        assertTrue("optString() should return default string",
                "hi".equals(jsonObject.optString("hiKey", "hi")));
    }

    /**
     * Confirm behavior when JSONObject put(key, null object) is called
     */
    @Test
    public void jsonObjectputNull() {

        // put null should remove the item.
        String str = "{\"myKey\": \"myval\"}";
        JSONObject jsonObjectRemove = new JSONObject(str);
        jsonObjectRemove.remove("myKey");

        JSONObject jsonObjectPutNull = new JSONObject(str);
        jsonObjectPutNull.put("myKey", (Object) null);

        // validate JSON
        assertTrue("jsonObject should be empty", jsonObjectRemove.length() == 0
                && jsonObjectPutNull.length() == 0);
    }

    /**
     * Exercise JSONObject quote() method
     * This purpose of quote() is to ensure that for strings with embedded
     * quotes, the quotes are properly escaped.
     */
    @Test
    public void jsonObjectQuote() {
        String str;
        str = "";
        String quotedStr;
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped quotes, found "+quotedStr,
                "\"\"".equals(quotedStr));
        str = "\"\"";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped quotes, found "+quotedStr,
                "\"\\\"\\\"\"".equals(quotedStr));
        str = "</";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped frontslash, found "+quotedStr,
                "\"<\\/\"".equals(quotedStr));
        str = "AB\bC";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped backspace, found "+quotedStr,
                "\"AB\\bC\"".equals(quotedStr));
        str = "ABC\n";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped newline, found "+quotedStr,
                "\"ABC\\n\"".equals(quotedStr));
        str = "AB\fC";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped formfeed, found "+quotedStr,
                "\"AB\\fC\"".equals(quotedStr));
        str = "\r";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped return, found "+quotedStr,
                "\"\\r\"".equals(quotedStr));
        str = "\u1234\u0088";
        quotedStr = JSONObject.quote(str);
        assertTrue("quote() expected escaped unicode, found "+quotedStr,
                "\"\u1234\\u0088\"".equals(quotedStr));
    }

    /**
     * Confirm behavior when JSONObject stringToValue() is called for an
     * empty string
     */
    @Test
    public void stringToValue() {
        String str = "";
        String valueStr = (String)(JSONObject.stringToValue(str));
        assertTrue("stringToValue() expected empty String, found "+valueStr,
                "".equals(valueStr));
    }

    /**
     * Confirm behavior when toJSONArray is called with a null value
     */
    @Test
    public void toJSONArray() {
        assertTrue("toJSONArray() with null names should be null",
                null == new JSONObject().toJSONArray(null));
    }

    /**
     * Exercise the JSONObject write() method
     */
    @Test
    public void write() {
        String str = "{\"key\":\"value\"}";
        String expectedStr = str;
        JSONObject jsonObject = new JSONObject(str);
        StringWriter stringWriter = new StringWriter();
        Writer writer = jsonObject.write(stringWriter);
        String actualStr = writer.toString();
        assertTrue("write() expected " +expectedStr+
                "but found " +actualStr,
                expectedStr.equals(actualStr));
    }

    /**
     * Exercise the JSONObject equals() method
     */
    @Test
    public void equals() {
        String str = "{\"key\":\"value\"}";
        JSONObject aJsonObject = new JSONObject(str);
        assertTrue("Same JSONObject should be equal to itself",
                aJsonObject.equals(aJsonObject));

        assertFalse("JSONObject should not be equal to null",
                new JSONObject().equals(null));
        assertFalse("JSONObject should not be equal to a class not of type JSONObject",
                new JSONObject().equals("not a JSONObject"));
        assertTrue("Two empty JSONObjects should be equal",
                new JSONObject().equals(new JSONObject()));
        assertTrue("Two JSONObjects with the same content should be equal",
                new JSONObject(str).equals(new JSONObject(str)));
    }

    /**
     * JSON null is not the same as Java null. This test examines the differences
     * in how they are handled by JSON-java.
     */
    @Test
    public void jsonObjectNullOperations() {
        /**
         * The Javadoc for JSONObject.NULL states:
         *      "JSONObject.NULL is equivalent to the value that JavaScript calls null,
         *      whilst Java's null is equivalent to the value that JavaScript calls
         *      undefined."
         * 
         * Standard ECMA-262 6th Edition / June 2015 (included to help explain the javadoc):
         *      undefined value: primitive value used when a variable has not been assigned a value
         *      Undefined type:  type whose sole value is the undefined value
         *      null value:      primitive value that represents the intentional absence of any object value
         *      Null type:       type whose sole value is the null value
         * Java SE8 language spec (included to help explain the javadoc):
         *      The Kinds of Types and Values ...
         *      There is also a special null type, the type of the expression null, which has no name.
         *      Because the null type has no name, it is impossible to declare a variable of the null 
         *      type or to cast to the null type. The null reference is the only possible value of an 
         *      expression of null type. The null reference can always be assigned or cast to any reference type.
         *      In practice, the programmer can ignore the null type and just pretend that null is merely 
         *      a special literal that can be of any reference type.
         * Extensible Markup Language (XML) 1.0 Fifth Edition / 26 November 2008
         *      No mention of null
         * ECMA-404 1st Edition / October 2013:
         *      JSON Text  ...
         *      These are three literal name tokens: ...
         *      null 
         * 
         * There seems to be no best practice to follow, it's all about what we
         * want the code to do.
         */

        // add JSONObject.NULL then convert to string in the manner of XML.toString() 
        JSONObject jsonObjectJONull = new JSONObject();
        Object obj = JSONObject.NULL;
        jsonObjectJONull.put("key", obj);
        Object value = jsonObjectJONull.opt("key");
        assertTrue("opt() JSONObject.NULL should find JSONObject.NULL",
                obj.equals(value));
        value = jsonObjectJONull.get("key");
        assertTrue("get() JSONObject.NULL should find JSONObject.NULL",
                obj.equals(value));
        if (value == null) {
            value = "";
        }
        String string = value instanceof String ? (String)value : null;
        assertTrue("XML toString() should convert JSONObject.NULL to null",
                string == null);

        // now try it with null
        JSONObject jsonObjectNull = new JSONObject();
        obj = null;
        jsonObjectNull.put("key", obj);
        value = jsonObjectNull.opt("key");
        assertTrue("opt() null should find null", value == null);
        if (value == null) {
            value = "";
        }
        string = value instanceof String ? (String)value : null;
        assertTrue("should convert null to empty string", "".equals(string));
        try {
            value = jsonObjectNull.get("key");
            assertTrue("get() null should throw exception", false);
        } catch (Exception ignored) {}

        /**
         * XML.toString() then goes on to do something with the value
         * if the key val is "content", then value.toString() will be 
         * called. This will evaluate to "null" for JSONObject.NULL,
         * and the empty string for null.
         * But if the key is anything else, then JSONObject.NULL will be emitted
         * as <key>null</key> and null will be emitted as ""
         */
        String sJONull = XML.toString(jsonObjectJONull);
        assertTrue("JSONObject.NULL should emit a null value", 
                "<key>null</key>".equals(sJONull));
        String sNull = XML.toString(jsonObjectNull);
        assertTrue("null should emit an empty string", "".equals(sNull));
    }
}
