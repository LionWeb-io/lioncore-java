package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.model.Node;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Specific tests of JsonSerialization for the serialization and unserialization of primitive
 * values.
 */
public class SerializationOfPrimitiveValuesTest extends SerializationTest {

  @Test
  public void serializeBoolean() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP1(true);

    JsonObject expected =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": \"true\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(node).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, serialized);
  }

  @Test
  public void unserializeBoolean() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP1(true);

    JsonObject serialized =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": \"true\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(MyNodeWithProperties.LANGUAGE);
    jsonSerialization
        .getInstantiator()
        .registerCustomUnserializer(
            MyNodeWithProperties.CONCEPT.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValue) ->
                new MyNodeWithProperties(serializedNode.getID()));
    List<Node> unserialized = jsonSerialization.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(node), unserialized);
  }

  @Test
  public void serializeString() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP3("qwerty");

    JsonObject expected =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": \"qwerty\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(node).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, serialized);
  }

  @Test
  public void unserializeString() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP3("qwerty");

    JsonObject serialized =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": \"qwerty\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(MyNodeWithProperties.LANGUAGE);
    jsonSerialization
        .getInstantiator()
        .registerCustomUnserializer(
            MyNodeWithProperties.CONCEPT.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValue) ->
                new MyNodeWithProperties(serializedNode.getID()));
    List<Node> unserialized = jsonSerialization.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(node), unserialized);
  }

  @Test
  public void serializeInteger() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP2(2904);

    JsonObject expected =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": \"2904\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(node).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, serialized);
  }

  @Test
  public void unserializeInteger() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    node.setP2(2904);

    JsonObject serialized =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": \"2904\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(MyNodeWithProperties.LANGUAGE);
    jsonSerialization
        .getInstantiator()
        .registerCustomUnserializer(
            MyNodeWithProperties.CONCEPT.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValue) ->
                new MyNodeWithProperties(serializedNode.getID()));
    List<Node> unserialized = jsonSerialization.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(node), unserialized);
  }

  @Test
  public void serializeJSON() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    JsonArray ja = new JsonArray();
    ja.add(1);
    ja.add("foo");
    node.setP4(ja);

    JsonObject expected =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": \"[1,\\\"foo\\\"]\"\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(node).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, serialized);
  }

  @Test
  public void unserializeJSON() {
    MyNodeWithProperties node = new MyNodeWithProperties("n1");
    JsonArray ja = new JsonArray();
    ja.add(1);
    ja.add("foo");
    node.setP4(ja);

    JsonObject serialized =
        JsonParser.parseString(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2023.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"n1\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"mylanguage\",\n"
                    + "        \"version\": \"1\",\n"
                    + "        \"key\": \"concept-MyNodeWithProperties\"\n"
                    + "      },\n"
                    + "      \"properties\": [\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p1\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p2\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p3\"\n"
                    + "          },\n"
                    + "          \"value\": null\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"property\": {\n"
                    + "            \"language\": \"mylanguage\",\n"
                    + "            \"version\": \"1\",\n"
                    + "            \"key\": \"p4\"\n"
                    + "          },\n"
                    + "          \"value\": \"[1,\\\"foo\\\"]\"\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"children\": [],\n"
                    + "      \"references\": [],\n"
                    + "      \"annotations\": [],\n"
                    + "      \"parent\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(MyNodeWithProperties.LANGUAGE);
    jsonSerialization
        .getInstantiator()
        .registerCustomUnserializer(
            MyNodeWithProperties.CONCEPT.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValue) ->
                new MyNodeWithProperties(serializedNode.getID()));
    List<Node> unserialized = jsonSerialization.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(node), unserialized);
  }
}
