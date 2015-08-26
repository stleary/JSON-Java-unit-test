package org.json.junit;

import static org.junit.Assert.*;

import java.io.*;

import org.json.*;
import org.junit.*;
import org.junit.rules.*;


/**
 * Tests for JSON-Java XML.java
 * Note: noSpace() will be tested by JSONMLTest
 */
public class XMLTest {
    /**
     * JUnit supports temporary files and folders that are cleaned up after the test.
     * https://garygregory.wordpress.com/2010/01/20/junit-tip-use-rules-to-manage-temporary-files-and-folders/
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * JSONObject from a null XML string.
     * Expects a NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void shouldHandleNullXML() {
        compareStringToJSONObject(null, "");
    }

    /**
     * Empty JSONObject from an empty XML string.
     */
    @Test
    public void shouldHandleEmptyXML() {

        compareStringToJSONObject("", "");
    }

    /**
     * Empty JSONObject from a non-XML string.
     */
    @Test
    public void shouldHandleNonXML() {
        String xmlStr = "{ \"this is\": \"not xml\"}";
        compareJSONObjectToString(xmlStr, "");
    }

    /**
     * Invalid XML string (tag contains a frontslash).
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidSlashInTag() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/x>\n" +
                        "       <street>abc street</street>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misshaped tag at 176 [character 14 line 5]".
                            equals(e.getMessage()));
        }
    }


    /**
     * Invalid XML read from a StringReader object (tag contains a frontslash).
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidSlashInTagWithReader() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/x>\n" +
                        "       <street>abc street</street>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(new StringReader(xmlStr));
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misshaped tag at 176 [character 14 line 5]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string ('!' char in tag)
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidBangInTag() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <!>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misshaped meta tag at 215 [character 13 line 8]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string ('!' char in tag)
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidBangInTagWithReader() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <!>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misshaped meta tag at 215 [character 13 line 8]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string read from a StringReader ('!' char and no closing tag brace)
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidBangNoCloseInTagWithReader() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <!\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misshaped meta tag at 214 [character 13 line 8]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string (no end brace for tag)
     * Expects JSONException
     */
    @Test
    public void shouldHandleNoCloseStartTag() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <abc\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misplaced '<' at 193 [character 4 line 7]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string read from a StringReader (no end brace for tag)
     * Expects JSONException
     */
    @Test
    public void shouldHandleNoCloseStartTagWithReader() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <abc\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(new StringReader(xmlStr));
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Misplaced '<' at 193 [character 4 line 7]".
                            equals(e.getMessage()));
        }
    }


    /**
     * Invalid XML string (partial CDATA chars in tag name)
     * Expects JSONException
     */
    @Test
    public void shouldHandleInvalidCDATABangInTag() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name>Joe Tester</name>\n" +
                        "       <![[]>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Expected 'CDATA[' at 204 [character 11 line 6]".
                            equals(e.getMessage()));
        }
    }

    /**
     * Invalid XML string read from a StringReader (partial CDATA chars in tag name)
     * Expects JSONException
     */
    @Test
    public void shouldHandleInvalidCDATABangInTagWithReader() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name>Joe Tester</name>\n" +
                        "       <![[]>\n" +
                        "   </address>\n" +
                        "</addresses>";
        try {
            XML.toJSONObject(new StringReader(xmlStr));
            assertTrue("Expecting a JSONException", false);
        } catch (JSONException e) {
            assertTrue("Expecting an exception message",
                    "Expected 'CDATA[' at 204 [character 11 line 6]".
                            equals(e.getMessage()));
        }
    }


    /**
     * Null JSONObject in XML.toString()
     * Expects NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void shouldHandleNullJSONXML() {
        JSONObject jsonObject = null;
        XML.toString(jsonObject);
    }

    /**
     * Empty JSONObject in XML.toString()
     */
    @Test
    public void shouldHandleEmptyJSONXML() {
        JSONObject jsonObject = new JSONObject();
        String xmlStr = XML.toString(jsonObject);
        assertTrue("xml string should be empty", xmlStr.length() == 0);
    }

    /**
     * No SML start tag. The ending tag ends up being treated as content.
     */
    @Test
    public void shouldHandleNoStartTag() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "    <address>\n" +
                        "       <name/>\n" +
                        "       <nocontent/>>\n" +
                        "   </address>\n" +
                        "</addresses>";
        String expectedStr =
                "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\",\"" +
                        "content\":\">\"},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\"" +
                        "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }

    /**
     * Valid XML to JSONObject
     */
    @Test
    public void shouldHandleSimpleXML() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "   <address>\n" +
                        "       <name>Joe Tester</name>\n" +
                        "       <street>[CDATA[Baker street 5]</street>\n" +
                        "       <NothingHere/>\n" +
                        "       <TrueValue>true</TrueValue>\n" +
                        "       <FalseValue>false</FalseValue>\n" +
                        "       <NullValue>null</NullValue>\n" +
                        "       <PositiveValue>42</PositiveValue>\n" +
                        "       <NegativeValue>-23</NegativeValue>\n" +
                        "       <DoubleValue>-23.45</DoubleValue>\n" +
                        "       <Nan>-23x.45</Nan>\n" +
                        "       <ArrayOfNum>1, 2, 3, 4.1, 5.2</ArrayOfNum>\n" +
                        "   </address>\n" +
                        "</addresses>";

        String expectedStr =
                "{\"addresses\":{\"address\":{\"street\":\"[CDATA[Baker street 5]\"," +
                        "\"name\":\"Joe Tester\",\"NothingHere\":\"\",TrueValue:true,\n" +
                        "\"FalseValue\":false,\"NullValue\":null,\"PositiveValue\":42,\n" +
                        "\"NegativeValue\":-23,\"DoubleValue\":-23.45,\"Nan\":-23x.45,\n" +
                        "\"ArrayOfNum\":\"1, 2, 3, 4.1, 5.2\"\n" +
                        "},\"xsi:noNamespaceSchemaLocation\":" +
                        "\"test.xsd\",\"xmlns:xsi\":\"http://www.w3.org/2001/" +
                        "XMLSchema-instance\"}}";

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }

    /**
     * Valid XML with comments to JSONObject
     */
    @Test
    public void shouldHandleCommentsInXML() {

        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!-- this is a comment -->\n" +
                        "<addresses>\n" +
                        "   <address>\n" +
                        "       <![CDATA[ this is -- <another> comment ]]>\n" +
                        "       <name>Joe Tester</name>\n" +
                        "       <!-- this is a - multi line \n" +
                        "            comment -->\n" +
                        "       <street>Baker street 5</street>\n" +
                        "   </address>\n" +
                        "</addresses>";

        String expectedStr = "{\"addresses\":{\"address\":{\"street\":\"Baker " +
                "street 5\",\"name\":\"Joe Tester\",\"content\":\" this is -- " +
                "<another> comment \"}}}";

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }

    /**
     * Valid XML to XML.toString()
     */
    @Test
    public void shouldHandleToString() {
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "   xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "   <address>\n" +
                        "       <name>[CDATA[Joe &amp; T &gt; e &lt; s &quot; t &apos; er]]</name>\n" +
                        "       <street>Baker street 5</street>\n" +
                        "       <ArrayOfNum>1, 2, 3, 4.1, 5.2</ArrayOfNum>\n" +
                        "   </address>\n" +
                        "</addresses>";

        String expectedStr =
                "{\"addresses\":{\"address\":{\"street\":\"Baker street 5\"," +
                        "\"name\":\"[CDATA[Joe & T > e < s \\\" t \\\' er]]\"," +
                        "\"ArrayOfNum\":\"1, 2, 3, 4.1, 5.2\"\n" +
                        "},\"xsi:noNamespaceSchemaLocation\":" +
                        "\"test.xsd\",\"xmlns:xsi\":\"http://www.w3.org/2001/" +
                        "XMLSchema-instance\"}}";

        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        String xmlToStr = XML.toString(jsonObject);

        compareStringToJSONObject(xmlToStr, expectedStr);
        compareReaderToJSONObject(xmlToStr, expectedStr);
        compareFileToJSONObject(xmlToStr, expectedStr);

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }

    /**
     * Converting a JSON doc containing '>' content to JSONObject, then
     * XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleContentNoArraytoString() {
        String expectedStr =
                "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\",\"" +
                        "content\":\">\"},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\"" +
                        "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses><address><name/><nocontent/>&gt;" +
                "</address><xsi:noNamespaceSchemaLocation>test.xsd</xsi:noName" +
                "spaceSchemaLocation><xmlns:xsi>http://www.w3.org/2001/XMLSche" +
                "ma-instance</xmlns:xsi></addresses>";

        assertTrue("Should handle expectedFinal: [" + expectedStr + "] final: [" +
                finalStr + "]", expectedFinalStr.equals(finalStr));
    }

    /**
     * Converting a JSON doc containing a 'content' array to JSONObject, then
     * XML.toString() should result in valid XML.
     * TODO: This is probably an error in how the 'content' keyword is used.
     */
    @Test
    public void shouldHandleContentArraytoString() {
        String expectedStr =
                "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\",\"" +
                        "content\":[1, 2, 3]},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\"" +
                        "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses><address><name/><nocontent/>" +
                "1\n2\n3" +
                "</address><xsi:noNamespaceSchemaLocation>test.xsd</xsi:noName" +
                "spaceSchemaLocation><xmlns:xsi>http://www.w3.org/2001/XMLSche" +
                "ma-instance</xmlns:xsi></addresses>";

        assertTrue("Should handle expectedFinal: [" + expectedStr + "] final: [" +
                finalStr + "]", expectedFinalStr.equals(finalStr));
    }

    /**
     * Converting a JSON doc containing a named array to JSONObject, then
     * XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleArraytoString() {
        String expectedStr =
                "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\"," +
                        "\"something\":[1, 2, 3]},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\"" +
                        "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses><address><name/><nocontent/>" +
                "<something>1</something><something>2</something><something>3</something>" +
                "</address><xsi:noNamespaceSchemaLocation>test.xsd</xsi:noName" +
                "spaceSchemaLocation><xmlns:xsi>http://www.w3.org/2001/XMLSche" +
                "ma-instance</xmlns:xsi></addresses>";

        assertTrue("Should handle expectedFinal: [" + expectedStr + "] final: [" +
                finalStr + "]", expectedFinalStr.equals(finalStr));
    }

    /**
     * Converting a JSON doc containing a named array of nested arrays to
     * JSONObject, then XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleNestedArraytoString() {
        String xmlStr =
                "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\"," +
                        "\"outer\":[[1], [2], [3]]},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\"" +
                        "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";

        String expectedStr = "<addresses><address><name/><nocontent/>" +
                "<outer><array>1</array></outer><outer><array>2</array>" +
                "</outer><outer><array>3</array></outer>" +
                "</address><xsi:noNamespaceSchemaLocation>test.xsd</xsi:noName" +
                "spaceSchemaLocation><xmlns:xsi>http://www.w3.org/2001/XMLSche" +
                "ma-instance</xmlns:xsi></addresses>";

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }


    /**
     * Possible bug:
     * Illegal node-names must be converted to legal XML-node-names.
     * The given example shows 2 nodes which are valid for JSON, but not for XML.
     * Therefore illegal arguments should be converted to e.g. an underscore (_).
     */
    @Test
    public void shouldHandleIllegalJSONNodeNames() {
        JSONObject inputJSON = new JSONObject();
        inputJSON.append("123IllegalNode", "someValue1");
        inputJSON.append("Illegal@node", "someValue2");

        String result = XML.toString(inputJSON);

        /**
         * This is invalid XML. Names should not begin with digits or contain
         * certain values, including '@'. One possible solution is to replace
         * illegal chars with '_', in which case the expected output would be:
         * <___IllegalNode>someValue1</___IllegalNode><Illegal_node>someValue2</Illegal_node>
         */
        String expected = "<123IllegalNode>someValue1</123IllegalNode><Illegal@node>someValue2</Illegal@node>";

        assertEquals(expected, result);
    }

    /**
     * JSONObject with NULL value, to XML.toString()
     */
    @Test
    public void shouldHandleNullNodeValue() {
        JSONObject inputJSON = new JSONObject();
        inputJSON.put("nullValue", JSONObject.NULL);
        // This is a possible preferred result
        // String expectedXML = "<nullValue/>";
        /**
         * This is the current behavior. JSONObject.NULL is emitted as 
         * the string, "null".
         */
        String actualXML = "<nullValue>null</nullValue>";
        String resultXML = XML.toString(inputJSON);
        assertEquals(actualXML, resultXML);
    }

    /**
     * Investigate exactly how the "content" keyword works
     */
    @Test
    public void contentOperations() {
        /**
         * When a standalone <!CDATA[...]] structure is found while parsing XML into a
         * JSONObject, the contents are placed in a string value with key="content".
         */
        String xmlStr = "<tag1></tag1><![CDATA[if (a < b && a > 0) then return]]><tag2></tag2>";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("1. 3 items", 3 == jsonObject.length());
        assertTrue("1. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("1. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("1. content found", "if (a < b && a > 0) then return".equals(jsonObject.get("content")));

        // Same tests, but for XML.toJSONObject(Reader)
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("1. 3 items", 3 == jsonObject.length());
        assertTrue("1. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("1. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("1. content found", "if (a < b && a > 0) then return".equals(jsonObject.get("content")));

        // multiple consecutive standalone cdatas are accumulated into an array
        xmlStr = "<tag1></tag1><![CDATA[if (a < b && a > 0) then return]]><tag2></tag2><![CDATA[here is another cdata]]>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("2. 3 items", 3 == jsonObject.length());
        assertTrue("2. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("2. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("2. content array found", jsonObject.get("content") instanceof JSONArray);
        JSONArray jsonArray = jsonObject.getJSONArray("content");
        assertTrue("2. array size", jsonArray.length() == 2);
        assertTrue("2. content array entry 0", "if (a < b && a > 0) then return".equals(jsonArray.get(0)));
        assertTrue("2. content array entry 1", "here is another cdata".equals(jsonArray.get(1)));


        // Same thing, but for XML.toJSONObject(Reader)
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("2. 3 items", 3 == jsonObject.length());
        assertTrue("2. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("2. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("2. content array found", jsonObject.get("content") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("content");
        assertTrue("2. array size", jsonArray.length() == 2);
        assertTrue("2. content array entry 0", "if (a < b && a > 0) then return".equals(jsonArray.get(0)));
        assertTrue("2. content array entry 1", "here is another cdata".equals(jsonArray.get(1)));

        /**
         * text content is accumulated in a "content" inside a local JSONObject.
         * If there is only one instance, it is saved in the context (a different JSONObject 
         * from the calling code. and the content element is discarded. 
         */
        xmlStr = "<tag1>value 1</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("3. 2 items", 1 == jsonObject.length());
        assertTrue("3. value tag1", "value 1".equals(jsonObject.get("tag1")));

        // Same tests for XML.toJSONObject(Reader)
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("3. 2 items", 1 == jsonObject.length());
        assertTrue("3. value tag1", "value 1".equals(jsonObject.get("tag1")));

        /**
         * array-style text content (multiple tags with the same name) is 
         * accumulated in a local JSONObject with key="content" and value=JSONArray,
         * saved in the context, and then the local JSONObject is discarded.
         */
        xmlStr = "<tag1>value 1</tag1><tag1>2</tag1><tag1>true</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("4. 1 item", 1 == jsonObject.length());
        assertTrue("4. content array found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("4. array size", jsonArray.length() == 3);
        assertTrue("4. content array entry 0", "value 1".equals(jsonArray.get(0)));
        assertTrue("4. content array entry 1", jsonArray.getInt(1) == 2);
        assertTrue("4. content array entry 2", jsonArray.getBoolean(2) == true);

        // XML.toJSONObject(Reader) tests
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("4. 1 item", 1 == jsonObject.length());
        assertTrue("4. content array found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("4. array size", jsonArray.length() == 3);
        assertTrue("4. content array entry 0", "value 1".equals(jsonArray.get(0)));
        assertTrue("4. content array entry 1", jsonArray.getInt(1) == 2);
        assertTrue("4. content array entry 2", jsonArray.getBoolean(2) == true);
        /**
         * Complex content is accumulated in a "content" field. For example, an element
         * may contain a mix of child elements and text. Each text segment is 
         * accumulated to content. 
         */
        xmlStr = "<tag1>val1<tag2/>val2</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("5. 1 item", 1 == jsonObject.length());
        assertTrue("5. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("5. 2 contained items", 2 == jsonObject.length());
        assertTrue("5. contained tag", "".equals(jsonObject.get("tag2")));
        assertTrue("5. contained content jsonArray found", jsonObject.get("content") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("content");
        assertTrue("5. array size", jsonArray.length() == 2);
        assertTrue("5. content array entry 0", "val1".equals(jsonArray.get(0)));
        assertTrue("5. content array entry 1", "val2".equals(jsonArray.get(1)));

        // XML.toJSONObject(Reader) tests
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("5. 1 item", 1 == jsonObject.length());
        assertTrue("5. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("5. 2 contained items", 2 == jsonObject.length());
        assertTrue("5. contained tag", "".equals(jsonObject.get("tag2")));
        assertTrue("5. contained content jsonArray found", jsonObject.get("content") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("content");
        assertTrue("5. array size", jsonArray.length() == 2);
        assertTrue("5. content array entry 0", "val1".equals(jsonArray.get(0)));
        assertTrue("5. content array entry 1", "val2".equals(jsonArray.get(1)));

        /**
         * If there is only 1 complex text content, then it is accumulated in a 
         * "content" field as a string.
         */
        xmlStr = "<tag1>val1<tag2/></tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("6. 1 item", 1 == jsonObject.length());
        assertTrue("6. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("6. contained content found", "val1".equals(jsonObject.get("content")));
        assertTrue("6. contained tag2", "".equals(jsonObject.get("tag2")));

        // XML.toJSONObject(Reader) tests
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("6. 1 item", 1 == jsonObject.length());
        assertTrue("6. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("6. contained content found", "val1".equals(jsonObject.get("content")));
        assertTrue("6. contained tag2", "".equals(jsonObject.get("tag2")));

        /**
         * In this corner case, the content sibling happens to have key=content
         * We end up with an array within an array, and no content element.
         * This is probably a bug. 
         */
        xmlStr = "<tag1>val1<content/></tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("7. 1 item", 1 == jsonObject.length());
        assertTrue("7. jsonArray found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("array size 1", jsonArray.length() == 1);
        assertTrue("7. contained array found", jsonArray.get(0) instanceof JSONArray);
        jsonArray = jsonArray.getJSONArray(0);
        assertTrue("7. inner array size 2", jsonArray.length() == 2);
        assertTrue("7. inner array item 0", "val1".equals(jsonArray.get(0)));
        assertTrue("7. inner array item 1", "".equals(jsonArray.get(1)));


        // Same tests for XML.toJSONObject(Reader)
        jsonObject = XML.toJSONObject(new StringReader(xmlStr));
        assertTrue("7. 1 item", 1 == jsonObject.length());
        assertTrue("7. jsonArray found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("array size 1", jsonArray.length() == 1);
        assertTrue("7. contained array found", jsonArray.get(0) instanceof JSONArray);
        jsonArray = jsonArray.getJSONArray(0);
        assertTrue("7. inner array size 2", jsonArray.length() == 2);
        assertTrue("7. inner array item 0", "val1".equals(jsonArray.get(0)));
        assertTrue("7. inner array item 1", "".equals(jsonArray.get(1)));

        /**
         * Confirm behavior of original issue
         */
        String jsonStr =
                "{" +
                        "\"Profile\": {" +
                        "\"list\": {" +
                        "\"history\": {" +
                        "\"entries\": [" +
                        "{" +
                        "\"deviceId\": \"id\"," +
                        "\"content\": {" +
                        "\"material\": [" +
                        "{" +
                        "\"stuff\": false" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}" +
                        "}";
        jsonObject = new JSONObject(jsonStr);
        xmlStr = XML.toString(jsonObject);
        /**
         * This is the created XML. Looks like content was mistaken for
         * complex (child node + text) XML. 
         *  <Profile>
         *      <list>
         *          <history>
         *              <entries>
         *                  <deviceId>id</deviceId>
         *                  {&quot;material&quot;:[{&quot;stuff&quot;:false}]}
         *              </entries>
         *          </history>
         *      </list>
         *  </Profile>
         */
        assertTrue("nothing to test here, see comment on created XML, above", true);
    }

    /**
     * Convenience method, given an input string and expected result,
     * convert to JSONObject and compare actual to expected result.
     *
     * @param xmlStr      the string to parse
     * @param expectedStr the expected JSON string
     */
    private void compareStringToJSONObject(String xmlStr, String expectedStr) {
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
    }

    /**
     * Convenience method, given an input string and expected result,
     * convert to JSONObject via reader and compare actual to expected result.
     *
     * @param xmlStr      the string to parse
     * @param expectedStr the expected JSON string
     */
    private void compareReaderToJSONObject(String xmlStr, String expectedStr) {
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
    }

    /**
     * Convenience method, given an input string and expected result,
     * convert to JSONObject via file and compare actual to expected result.
     *
     * @param xmlStr      the string to parse
     * @param expectedStr the expected JSON string
     * @throws IOException
     */
    private void compareFileToJSONObject(String xmlStr, String expectedStr) {
        try {
            JSONObject expectedJsonObject = new JSONObject(expectedStr);
            File tempFile = testFolder.newFile("fileToJSONObject.xml");
            FileWriter fileWriter = new FileWriter(tempFile);
            fileWriter.write(xmlStr);
            fileWriter.close();
            Reader reader = new FileReader(tempFile);
            JSONObject jsonObject = XML.toJSONObject(reader);
            Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
        } catch (IOException e) {
            assertTrue("file writer error: " + e.getMessage(), false);
        }
    }
}
